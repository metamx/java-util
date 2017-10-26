package com.metamx.emitter.core.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.ParametrizedUriEmitter;
import com.metamx.emitter.core.ParametrizedUriEmitterConfig;
import org.asynchttpclient.AsyncHttpClient;

public class ParametrizedUriEmitterFactory extends ParametrizedUriEmitterConfig implements EmitterFactory
{

  @Override
  public Emitter makeEmitter(ObjectMapper objectMapper, AsyncHttpClient httpClient, Lifecycle lifecycle)
  {
    final Emitter retVal = new ParametrizedUriEmitter(this, httpClient, objectMapper);
    lifecycle.addManagedInstance(retVal);
    return retVal;
  }
}
