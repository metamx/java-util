/*
 * Copyright 2012 Metamarkets Group Inc.
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

import javax.validation.constraints.NotNull;

/**
 */
public class HttpEmitterConfig extends BaseHttpEmittingConfig
{
  @NotNull
  @JsonProperty
  String recipientBaseUrl = null;

  /**
   * For JSON deserialization only. In other cases use {@link Builder}
   */
  public HttpEmitterConfig() {}

  public HttpEmitterConfig(BaseHttpEmittingConfig base, String recipientBaseUrl)
  {
    this.flushMillis = base.flushMillis;
    this.flushCount = base.flushCount;
    this.flushTimeOut = base.flushTimeOut;
    this.recipientBaseUrl = recipientBaseUrl;
    this.basicAuthentication = base.basicAuthentication;
    this.batchingStrategy = base.batchingStrategy;
    this.maxBatchSize = base.maxBatchSize;
    this.maxBufferSize = base.maxBufferSize;
    this.contentEncoding = base.contentEncoding;
  }

  public String getRecipientBaseUrl()
  {
    return recipientBaseUrl;
  }

  @Override
  public String toString()
  {
    return "HttpEmitterConfig{" +
           toStringBase() +
           ", recipientBaseUrl=\'" + recipientBaseUrl + '\'' +
           '}';
  }

  public static class Builder extends HttpEmitterConfig
  {
    public Builder(String recipientBaseUrl)
    {
      this.recipientBaseUrl = recipientBaseUrl;
    }

    public Builder setFlushMillis(long flushMillis)
    {
      this.flushMillis = flushMillis;
      return this;
    }

    public Builder setFlushCount(int flushCount)
    {
      this.flushCount = flushCount;
      return this;
    }

    public Builder setFlushTimeOut(long flushTimeOut)
    {
      this.flushTimeOut = flushTimeOut;
      return this;
    }

    public Builder setBasicAuthentication(String basicAuthentication)
    {
      this.basicAuthentication = basicAuthentication;
      return this;
    }

    public Builder setBatchingStrategy(BatchingStrategy batchingStrategy)
    {
      this.batchingStrategy = batchingStrategy;
      return this;
    }

    public Builder setMaxBatchSize(int maxBatchSize)
    {
      this.maxBatchSize = maxBatchSize;
      return this;
    }

    public Builder setMaxBufferSize(long maxBufferSize)
    {
      this.maxBufferSize = maxBufferSize;
      return this;
    }

    public Builder setContentEncoding(ContentEncoding contentEncoding)
    {
      this.contentEncoding = contentEncoding;
      return this;
    }

    public HttpEmitterConfig build()
    {
      return new HttpEmitterConfig(this, recipientBaseUrl);
    }
  }
}
