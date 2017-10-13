package com.metamx.emitter.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ParametrizedUriEmitterConfig
{
  private static final BaseHttpEmittingConfig DEFAULT_HTTP_EMITTING_CONFIG = new BaseHttpEmittingConfig();

  @NotNull
  @JsonProperty
  private String recipientBaseUrlPattern;

  @JsonProperty("httpEmitting")
  private BaseHttpEmittingConfig httpEmittingConfig = DEFAULT_HTTP_EMITTING_CONFIG;

  public String getRecipientBaseUrlPattern()
  {
    return recipientBaseUrlPattern;
  }

  public HttpEmitterConfig buildHttpEmitterConfig(String baseUri)
  {
    return new HttpEmitterConfig(httpEmittingConfig, baseUri);
  }

  @Override
  public String toString()
  {
    return "ParametrizedUriEmitterConfig{" +
           "recipientBaseUrlPattern='" + recipientBaseUrlPattern + '\'' +
           ", httpEmittingConfig=" + httpEmittingConfig +
           '}';
  }
}
