/*
 * Copyright 2017 Metamarkets Group Inc.
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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;

public class BaseHttpEmittingConfig
{
  public static final long DEFAULT_FLUSH_MILLIS = 60 * 1000;
  public static final int DEFAULT_FLUSH_COUNTS = 500;
  public static final int DEFAULT_MAX_BATCH_SIZE = 5 * 1024 * 1024;
  public static final long DEFAULT_MAX_BUFFER_SIZE = 250 * 1024 * 1024;
  /** Do not time out in case flushTimeOut is not set */
  public static final long DEFAULT_FLUSH_TIME_OUT = Long.MAX_VALUE;
  public static final String DEFAULT_BASIC_AUTHENTICATION = null;
  public static final BatchingStrategy DEFAULT_BATCHING_STRATEGY = BatchingStrategy.ARRAY;
  public static final ContentEncoding DEFAULT_CONTENT_ENCODING = null;

  @Min(1)
  @JsonProperty
  long flushMillis = DEFAULT_FLUSH_MILLIS;

  @Min(0)
  @JsonProperty
  int flushCount = DEFAULT_FLUSH_COUNTS;

  @Min(0)
  @JsonProperty
  long flushTimeOut = DEFAULT_FLUSH_TIME_OUT;

  @JsonProperty
  String basicAuthentication = DEFAULT_BASIC_AUTHENTICATION;

  @JsonProperty
  BatchingStrategy batchingStrategy = DEFAULT_BATCHING_STRATEGY;

  @Min(0)
  @JsonProperty
  int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;

  @Min(0)
  @JsonProperty
  long maxBufferSize = DEFAULT_MAX_BUFFER_SIZE;

  @JsonProperty
  ContentEncoding contentEncoding = DEFAULT_CONTENT_ENCODING;

  public long getFlushMillis()
  {
    return flushMillis;
  }

  public int getFlushCount()
  {
    return flushCount;
  }

  public long getFlushTimeOut() {
    return flushTimeOut;
  }

  public String getBasicAuthentication()
  {
    return basicAuthentication;
  }

  public BatchingStrategy getBatchingStrategy()
  {
    return batchingStrategy;
  }

  public int getMaxBatchSize()
  {
    return maxBatchSize;
  }

  public long getMaxBufferSize()
  {
    return maxBufferSize;
  }

  public ContentEncoding getContentEncoding() {
    return contentEncoding;
  }

  @Override
  public String toString()
  {
    return "BaseHttpEmittingConfig{" + toStringBase() + '}';
  }

  protected String toStringBase()
  {
    return
        "flushMillis=" + flushMillis +
        ", flushCount=" + flushCount +
        ", flushTimeOut=" + flushTimeOut +
        ", basicAuthentication='" + basicAuthentication + '\'' +
        ", batchingStrategy=" + batchingStrategy +
        ", maxBatchSize=" + maxBatchSize +
        ", maxBufferSize=" + maxBufferSize +
        ", contentEncoding=" + contentEncoding;
  }
}
