package com.metamx.http.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.metamx.http.client.response.HttpResponseHandler;

public abstract class AbstractHttpClient implements HttpClient
{
  @Override
  public <Intermediate, Final> ListenableFuture<Final> go(
      final Request request,
      final HttpResponseHandler<Intermediate, Final> handler
  )
  {
    return go(request, handler, null);
  }
}
