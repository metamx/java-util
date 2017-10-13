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
import com.metamx.common.ISE;
import com.metamx.http.client.response.HttpResponseHandler;
import org.joda.time.Duration;

import java.util.concurrent.atomic.AtomicInteger;

/**
*/
public abstract class GoHandler
{
  /******* Abstract Methods *********/
  protected abstract <Intermediate, Final> ListenableFuture<Final> go(
      Request request,
      HttpResponseHandler<Intermediate, Final> handler,
      Duration requestReadTimeout
  ) throws Exception;

  /******* Non Abstract Methods ********/
  private volatile boolean succeeded = false;

  public boolean succeeded()
  {
    return succeeded;
  }

  public <Intermediate, Final> ListenableFuture<Final> run(
      Request request,
      HttpResponseHandler<Intermediate, Final> handler
  ) throws Exception
  {
    return run(request, handler, null);
  }

  public <Intermediate, Final> ListenableFuture<Final> run(
      Request request,
      HttpResponseHandler<Intermediate, Final> handler,
      Duration requestReadTimeout
  ) throws Exception
  {
    try {
      final ListenableFuture<Final> retVal = go(request, handler, requestReadTimeout);
      succeeded = true;
      return retVal;
    }
    catch (Throwable e) {
      succeeded = false;
      Throwables.propagateIfPossible(e, Exception.class);
      throw Throwables.propagate(e);
    }
  }

  public GoHandler times(final int n)
  {
    final GoHandler myself = this;

    return new GoHandler()
    {
      AtomicInteger counter = new AtomicInteger(0);

      @Override
      public <Intermediate, Final> ListenableFuture<Final> go(
          final Request request,
          final HttpResponseHandler<Intermediate, Final> handler,
          final Duration requestReadTimeout
      ) throws Exception
      {
        if (counter.getAndIncrement() < n) {
          return myself.go(request, handler, requestReadTimeout);
        }
        succeeded = false;
        throw new ISE("Called more than %d times", n);
      }
    };
  }
}
