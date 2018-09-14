/**
 * Copyright 2018 ThinxNet GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package one.ryd.insider.resources;

import com.mongodb.BasicDBList;
import one.ryd.insider.core.auth.InsiderAuthPrincipal;
import one.ryd.insider.core.response.InsiderEnvelop;
import one.ryd.insider.models.session.SessionSummary;
import one.ryd.insider.models.session.aggregation.ActivityDto;
import one.ryd.insider.models.thing.Thing;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.caching.CacheControl;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.aggregation.Accumulator;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.query.Sort;

@Path("/statistics")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class StatisticsResource {
  // @todo! move it to validators
  private final Datastore dsInsider;
  private final Datastore dsSession;

  @Inject
  private Morphia morphia;

  public StatisticsResource(final Datastore dsInsider, final Datastore dsSession) {
    this.dsInsider = dsInsider;
    this.dsSession = dsSession;
  }

  @GET
  @Path("/{id}/activity")
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.HOURS)
  public Response fetchOne(
    @Auth final InsiderAuthPrincipal user,
    @PathParam("id") final ObjectId id
  ) {
    final Thing thing = this.dsInsider.get(Thing.class, id);
    if (Objects.isNull(thing)) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final BasicDBList result = new BasicDBList();
    final Instant timestamp = Instant.now().minusSeconds(2419200); // 28 days

    this.dsSession.createAggregation(SessionSummary.class)
      .match(
        this.dsInsider.createQuery(SessionSummary.class)
          .field("device").equal(thing.getDevice())
          .field("incomplete").equal(false)
          .field("timestamp").greaterThanOrEq(timestamp)
      )
      .sort(Sort.descending("end"))
      .group(
        Group.grouping("_id", Accumulator.accumulator("$dayOfYear", "end")),
        Group.grouping("count", Accumulator.accumulator("$sum", 1)),
        Group.grouping("geoDistanceM", Group.sum("statistics.geoDistanceM")),
        Group.grouping("geoDriveDurationS", Group.sum("statistics.geoDriveDurationS")),
        Group.grouping("geoStayDurationS", Group.sum("statistics.geoStayDurationS")),
        Group.grouping("gpsDistanceM", Group.sum("statistics.gpsDistanceM")),
        Group.grouping("gpsDriveDurationS", Group.sum("statistics.gpsDriveDurationS")),
        Group.grouping("gpsStayDurationS", Group.sum("statistics.gpsStayDurationS")),
        Group.grouping("obdDistanceM", Group.sum("statistics.obdDistanceM")),
        Group.grouping("obdDriveDurationS", Group.sum("statistics.obdDriveDurationS")),
        Group.grouping("obdStayDurationS", Group.sum("statistics.obdStayDurationS"))
      )
      .sort(Sort.ascending("_id"))
      .aggregate(ActivityDto.class)
      .forEachRemaining(result::add);

    return Response.ok(new InsiderEnvelop(result)).build();
  }
}