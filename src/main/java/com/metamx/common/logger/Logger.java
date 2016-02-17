/*
 * Copyright 2011,2012 Metamarkets Group Inc.
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

package com.metamx.common.logger;

import com.metamx.common.StringUtils;
import org.slf4j.LoggerFactory;

/**
 */
public class Logger
{
  private final org.slf4j.Logger log;

  public Logger(String name)
  {
    log = LoggerFactory.getLogger(name);
  }

  public Logger(Class clazz)
  {
    log = LoggerFactory.getLogger(clazz);
  }

  public void trace(String message, Object... formatArgs)
  {
    if (log.isTraceEnabled()) {
      log.trace(StringUtils.safeFormat(message, formatArgs));
    }
  }

  public void trace(Throwable t, String message, Object... formatArgs)
  {
    if (log.isTraceEnabled()) {
      log.trace(StringUtils.safeFormat(message, formatArgs), t);
    }
  }

  public void debug(String message, Object... formatArgs)
  {
    if (log.isDebugEnabled()) {
      log.debug(StringUtils.safeFormat(message, formatArgs));
    }
  }

  public void debug(Throwable t, String message, Object... formatArgs)
  {
    if (log.isDebugEnabled()) {
      log.debug(StringUtils.safeFormat(message, formatArgs), t);
    }
  }

  public void info(String message, Object... formatArgs)
  {
    if (log.isInfoEnabled()) {
      log.info(StringUtils.safeFormat(message, formatArgs));
    }
  }

  public void info(Throwable t, String message, Object... formatArgs)
  {
    if (log.isInfoEnabled()) {
      log.info(StringUtils.safeFormat(message, formatArgs), t);
    }
  }

  public void warn(String message, Object... formatArgs)
  {
    log.warn(StringUtils.safeFormat(message, formatArgs));
  }

  public void warn(Throwable t, String message, Object... formatArgs)
  {
    log.warn(StringUtils.safeFormat(message, formatArgs), t);
  }

  public void error(String message, Object... formatArgs)
  {
    log.error(StringUtils.safeFormat(message, formatArgs));
  }

  public void error(Throwable t, String message, Object... formatArgs)
  {
    log.error(StringUtils.safeFormat(message, formatArgs), t);
  }

  public void wtf(String message, Object... formatArgs)
  {
    log.error(StringUtils.safeFormat("WTF?!: " + message, formatArgs), new Exception());
  }

  public void wtf(Throwable t, String message, Object... formatArgs)
  {
    log.error(StringUtils.safeFormat("WTF?!: " + message, formatArgs), t);
  }

  public boolean isTraceEnabled()
  {
    return log.isTraceEnabled();
  }

  public boolean isDebugEnabled()
  {
    return log.isDebugEnabled();
  }

  public boolean isInfoEnabled()
  {
    return log.isInfoEnabled();
  }
}
