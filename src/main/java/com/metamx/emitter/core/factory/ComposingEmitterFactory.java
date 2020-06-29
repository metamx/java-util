package com.metamx.emitter.core.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.ComposingEmitter;
import com.metamx.emitter.core.ComposingEmitterConfig;
import com.metamx.emitter.core.Emitter;
import org.asynchttpclient.AsyncHttpClient;

import java.util.Map;
import java.util.stream.Collectors;

public class ComposingEmitterFactory extends ComposingEmitterConfig implements EmitterFactory
{
  @Override
  public Emitter makeEmitter(
      ObjectMapper objectMapper,
      AsyncHttpClient httpClient,
      Lifecycle lifecycle
  )
  {
    final Map<String, Emitter> emitters = getEmitterConfigs()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> objectMapper.convertValue(e.getValue(), EmitterFactory.class)
                             .makeEmitter(objectMapper, httpClient, lifecycle)
        ));
    return new ComposingEmitter(emitters);
  }
}
