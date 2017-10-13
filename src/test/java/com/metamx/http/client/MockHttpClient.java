/*
 * Copyright 2011 - 2015 Metamarkets Group Inc.
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

package com.metamx.http.client;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import com.metamx.http.client.pool.ResourceFactory;
import com.metamx.http.client.pool.ResourcePool;
import com.metamx.http.client.pool.ResourcePoolConfig;
import com.metamx.http.client.response.HttpResponseHandler;
import org.jboss.netty.channel.ChannelFuture;
import org.joda.time.Duration;

/**
 */
public class MockHttpClient extends NettyHttpClient
{
  private volatile GoHandler goHandler;

  public MockHttpClient()
  {
    super(
        new ResourcePool<String, ChannelFuture>(
            new ResourceFactory<String, ChannelFuture>()
            {
              @Override
              public ChannelFuture generate(String key)
              {
                return null;
              }

              @Override
              public boolean isGood(ChannelFuture resource)
              {
                return false;
              }

              @Override
              public void close(ChannelFuture resource)
              {

              }
            },
            new ResourcePoolConfig(1)
        )
    );
  }

  public GoHandler getGoHandler()
  {
    return goHandler;
  }

  public void setGoHandler(GoHandler goHandler)
  {
    this.goHandler = goHandler;
  }

  public boolean succeeded()
  {
    return goHandler.succeeded();
  }

  @Override
  public <Intermediate, Final> ListenableFuture<Final> go(
      Request request,
      HttpResponseHandler<Intermediate, Final> handler,
      Duration requestReadTimeout
  )
  {
    try {
      return goHandler.run(request, handler, requestReadTimeout);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
