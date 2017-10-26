package com.metamx.emitter.core.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.NoopEmitter;
import org.asynchttpclient.AsyncHttpClient;

public class NoopEmiterFactory implements EmitterFactory
{
  @Override
  public Emitter makeEmitter(ObjectMapper objectMapper, AsyncHttpClient httpClient, Lifecycle lifecycle)
  {
    return makeEmitter(lifecycle);
  }

  public Emitter makeEmitter(Lifecycle lifecycle)
  {
    Emitter retVal = new NoopEmitter();
    lifecycle.addManagedInstance(retVal);
    return retVal;
  }
}
