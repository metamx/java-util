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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metamx.common.ISE;
import com.metamx.http.client.response.HttpResponseHandler;
import org.joda.time.Duration;

import java.util.concurrent.Future;

/**
 */
public class GoHandlers
{
  public static GoHandler failingHandler()
  {
    return new GoHandler()
    {
      @Override
      public <Intermediate, Final> ListenableFuture<Final> go(
          Request request,
          HttpResponseHandler<Intermediate, Final> handler,
          Duration requestReadTimeout
      ) throws Exception
      {
        throw new ISE("Shouldn't be called");
      }
    };
  }

  public static GoHandler passingHandler(final Object retVal)
  {
    return new GoHandler()
    {
      @SuppressWarnings("unchecked")
      @Override
      public <Intermediate, Final> ListenableFuture<Final> go(
          Request request,
          HttpResponseHandler<Intermediate, Final> handler,
          Duration requestReadTimeout
      ) throws Exception
      {
        return Futures.immediateFuture((Final) retVal);
      }
    };
  }
}
