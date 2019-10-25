package com.metamx.emitter.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metamx.emitter.core.filter.BWListEventFilterConfig;

import javax.validation.constraints.NotNull;

public class ParametrizedUriEmitterConfig
{
  private static final BaseHttpEmittingConfig DEFAULT_HTTP_EMITTING_CONFIG = new BaseHttpEmittingConfig();

  private static final BWListEventFilterConfig DEFAULT_BW_LIST_EVENT_FILTER_CONFIG = new BWListEventFilterConfig();

  @NotNull
  @JsonProperty
  private String recipientBaseUrlPattern;

  @JsonProperty("httpEmitting")
  private BaseHttpEmittingConfig httpEmittingConfig = DEFAULT_HTTP_EMITTING_CONFIG;

  @JsonProperty("bwFilter")
  private BWListEventFilterConfig bwListEventFilterConfig = DEFAULT_BW_LIST_EVENT_FILTER_CONFIG;

  public String getRecipientBaseUrlPattern()
  {
    return recipientBaseUrlPattern;
  }

  public BWListEventFilterConfig getBwListEventFilterConfig()
  {
    return bwListEventFilterConfig;
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
           ", httpEmittingConfig=" + httpEmittingConfig + '\'' +
           ", bwListEventFilterConfig='" + bwListEventFilterConfig +
           '}';
  }
}
