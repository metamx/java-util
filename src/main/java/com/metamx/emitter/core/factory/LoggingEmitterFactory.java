package com.metamx.emitter.core.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.LoggingEmitter;
import com.metamx.emitter.core.LoggingEmitterConfig;
import com.metamx.http.client.HttpClient;

public class LoggingEmitterFactory extends LoggingEmitterConfig implements EmitterFactory
{
  public LoggingEmitterFactory() {}

  @Override
  public Emitter makeEmitter(ObjectMapper objectMapper, HttpClient httpClient, Lifecycle lifecycle)
  {
    return makeEmitter(objectMapper, lifecycle);
  }

  public Emitter makeEmitter(ObjectMapper objectMapper, Lifecycle lifecycle)
  {
    Emitter retVal = new LoggingEmitter(this, objectMapper);
    lifecycle.addManagedInstance(retVal);
    return retVal;
  }
}
