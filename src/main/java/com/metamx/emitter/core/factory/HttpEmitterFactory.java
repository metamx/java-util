package com.metamx.emitter.core.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.HttpEmitterConfig;
import com.metamx.emitter.core.HttpPostEmitter;
import com.metamx.http.client.HttpClient;

public class HttpEmitterFactory extends HttpEmitterConfig implements EmitterFactory
{

  @Override
  public Emitter makeEmitter(ObjectMapper objectMapper, HttpClient httpClient, Lifecycle lifecycle)
  {
    Emitter retVal = new HttpPostEmitter(this, httpClient, objectMapper);
    lifecycle.addManagedInstance(retVal);
    return retVal;
  }
}
