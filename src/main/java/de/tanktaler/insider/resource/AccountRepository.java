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

package de.tanktaler.insider.resource;

import de.tanktaler.insider.model.account.Account;
import de.tanktaler.insider.model.account.AccountRole;
import de.tanktaler.insider.model.user.User;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import java.util.function.Supplier;

public final class AccountRepository extends ResourceRepositoryBase<Account, ObjectId> {
  private final Datastore datastore;
  private final Supplier<User> currentUser;

  public AccountRepository(final Datastore datastore, final Supplier<User> currentUser) {
    super(Account.class);
    this.datastore = datastore;
    this.currentUser = currentUser;
  }

  @Override
  public <S extends Account> S save(final S account) {
    this.datastore.save(account);
    return account;
  }

  @Override
  public ResourceList<Account> findAll(final QuerySpec querySpec) {
    return querySpec.apply(
      this.datastore.createQuery(Account.class)
        .field("users.id").equal(this.currentUser.get().getId())
        .field("users.role").equal(AccountRole.ACCOUNT_OWNER).fetch()
    );
  }

  @Override
	public <S extends Account> S create(final S account) {
		throw new UnsupportedOperationException();
	}
}
