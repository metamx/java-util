package com.metamx.emitter.core.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.NoopEmitter;
import com.metamx.http.client.HttpClient;

public class NoopEmiterFactory implements EmitterFactory
{
  @Override
  public Emitter makeEmitter(ObjectMapper objectMapper, HttpClient httpClient, Lifecycle lifecycle)
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
