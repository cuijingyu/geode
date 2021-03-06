/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.redis.internal.executor.key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import org.apache.geode.test.awaitility.GeodeAwaitility;
import org.apache.geode.test.dunit.rules.RedisPortSupplier;

public abstract class AbstractPTTLIntegrationTest implements RedisPortSupplier {

  private Jedis jedis;
  private static final int REDIS_CLIENT_TIMEOUT =
      Math.toIntExact(GeodeAwaitility.getTimeout().toMillis());

  @Before
  public void setUp() {
    jedis = new Jedis("localhost", getPort(), REDIS_CLIENT_TIMEOUT);
  }

  @After
  public void tearDown() {
    jedis.flushAll();
    jedis.close();
  }

  @Test
  public void givenKeyNotProvided_returnsWrongNumberOfArgumentsError() {
    assertThatThrownBy(() -> jedis.sendCommand(Protocol.Command.PTTL))
        .hasMessageContaining("ERR wrong number of arguments for 'pttl' command");
  }

  @Test
  public void givenMoreThanTwoArguments_returnsWrongNumberOfArgumentsError() {
    assertThatThrownBy(() -> jedis.sendCommand(Protocol.Command.PTTL, "key", "extraArg"))
        .hasMessageContaining("ERR wrong number of arguments for 'pttl' command");
  }

  @Test
  public void shouldReturnNegativeTwo_givenKeyDoesNotExist() {
    assertThat(jedis.pttl("doesNotExist")).isEqualTo(-2);
  }

  @Test
  public void shouldReturnNegativeOne_givenKeyDoesNotHaveExpirationSet() {
    jedis.set("orange", "crush");

    assertThat(jedis.pttl("orange")).isEqualTo(-1);
  }

  @Test
  public void shouldReturnCorrectExpiration_givenKeyHasExpirationSet() {
    jedis.set("orange", "crush");
    jedis.expire("orange", 20);

    assertThat(jedis.pttl("orange")).isGreaterThan(1500);
  }
}
