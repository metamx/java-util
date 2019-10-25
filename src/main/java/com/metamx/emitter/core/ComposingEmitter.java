/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.metamx.emitter.core;

import com.google.common.base.Preconditions;
import com.metamx.common.lifecycle.LifecycleStart;
import com.metamx.common.lifecycle.LifecycleStop;
import com.metamx.common.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComposingEmitter implements Emitter
{
  private static Logger log = new Logger(ComposingEmitter.class);

  private final Map<String, Emitter> emitters;

  public ComposingEmitter(Map<String, Emitter> emitters)
  {
    this.emitters = Preconditions.checkNotNull(emitters, "null emitters");
  }

  public ComposingEmitter(List<Emitter> emitters)
  {
    this.emitters = emitters.stream().collect(
        Collectors.toMap(
            // Default toString implementation to make the key be unique
            e -> e.getClass().getName() + "@" + Integer.toHexString(e.hashCode()),
            e -> e
        )
    );
  }

  @Override
  @LifecycleStart
  public void start()
  {
    log.info("Starting Composing Emitter.");

    for (Map.Entry<String, Emitter> e : emitters.entrySet()) {
      log.info("Starting emitter [%s].", e.getKey());
      e.getValue().start();
    }
  }

  @Override
  public void emit(Event event)
  {
    for (Emitter e : emitters.values()) {
      e.emit(event);
    }
  }

  @Override
  public void flush() throws IOException
  {
    boolean fail = false;
    log.info("Flushing Composing Emitter.");

    for (Map.Entry<String, Emitter> e : emitters.entrySet()) {
      try {
        log.info("Flushing emitter [%s].", e.getKey());
        e.getValue().flush();
      }
      catch (IOException ex) {
        log.error(ex, "Failed to flush emitter [%s]", e.getKey());
        fail = true;
      }
    }

    if (fail) {
      throw new IOException("failed to flush one or more emitters");
    }
  }

  @Override
  @LifecycleStop
  public void close() throws IOException
  {
    boolean fail = false;
    log.info("Closing Composing Emitter.");

    for (Map.Entry<String, Emitter> e : emitters.entrySet()) {
      try {
        log.info("Closing emitter [%s].", e.getKey());
        e.getValue().close();
      }
      catch (IOException ex) {
        log.error(ex, "Failed to close emitter [%s]", e.getKey());
        fail = true;
      }
    }

    if (fail) {
      throw new IOException("failed to close one or more emitters");
    }
  }

  @Override
  public String toString()
  {
    return "ComposingEmitter{" +
           "emitters=" + emitters +
           '}';
  }
}
