/*
 * Copyright 2012 - 2015 Metamarkets Group Inc.
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

package com.metamx.emitter.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class LoggingEmitterConfigTest
{
  @Test
  public void testDefaults()
  {
    final Properties props = new Properties();
    final ObjectMapper objectMapper = new ObjectMapper();
    final LoggingEmitterConfig config = objectMapper.convertValue(
        Emitters.makeCustomFactoryMap(props),
        LoggingEmitterConfig.class
    );
    Assert.assertEquals("getLoggerClass", LoggingEmitter.class.getName(), config.getLoggerClass());
    Assert.assertEquals("getLogLevel", "info", config.getLogLevel());
  }

  @Test
  public void testDefaultsLegacy()
  {
    final Properties props = new Properties();
    final ObjectMapper objectMapper = new ObjectMapper();
    final LoggingEmitterConfig config = objectMapper.convertValue(
        Emitters.makeLoggingMap(props),
        LoggingEmitterConfig.class
    );

    Assert.assertEquals("getLoggerClass", LoggingEmitter.class.getName(), config.getLoggerClass());
    Assert.assertEquals("getLogLevel", "debug", config.getLogLevel());
  }

  @Test
  public void testSettingEverything()
  {
    final Properties props = new Properties();
    props.setProperty("com.metamx.emitter.loggerClass", "Foo");
    props.setProperty("com.metamx.emitter.logLevel", "INFO");

    final ObjectMapper objectMapper = new ObjectMapper();
    final LoggingEmitterConfig config = objectMapper.convertValue(
        Emitters.makeCustomFactoryMap(props),
        LoggingEmitterConfig.class
    );

    Assert.assertEquals("getLoggerClass", "Foo", config.getLoggerClass());
    Assert.assertEquals("getLogLevel", "INFO", config.getLogLevel());
  }

  @Test
  public void testSettingEverythingLegacy()
  {
    final Properties props = new Properties();
    props.setProperty("com.metamx.emitter.logging.class", "Foo");
    props.setProperty("com.metamx.emitter.logging.level", "INFO");

    final ObjectMapper objectMapper = new ObjectMapper();
    final LoggingEmitterConfig config = objectMapper.convertValue(
        Emitters.makeLoggingMap(props),
        LoggingEmitterConfig.class
    );

    Assert.assertEquals("getLoggerClass", "Foo", config.getLoggerClass());
    Assert.assertEquals("getLogLevel", "INFO", config.getLogLevel());
  }
}
