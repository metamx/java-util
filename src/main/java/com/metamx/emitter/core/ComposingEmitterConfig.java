package com.metamx.emitter.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ComposingEmitterConfig
{
  @JsonProperty("composite")
  private Map<String, Map<String, Object>> emitterConfigs = ImmutableMap.of();

  public Map<String, Map<String, Object>> getEmitterConfigs()
  {
    return emitterConfigs;
  }
}
