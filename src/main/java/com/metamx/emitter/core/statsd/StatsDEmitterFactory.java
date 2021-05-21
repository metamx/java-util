package com.metamx.emitter.core.statsd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.JacksonUtil;
import com.metamx.emitter.core.factory.EmitterFactory;
import org.asynchttpclient.AsyncHttpClient;

import javax.annotation.Nullable;
import java.util.List;

public class StatsDEmitterFactory extends StatsDEmitterConfig implements EmitterFactory
{
  @JsonCreator
  public StatsDEmitterFactory(
      @JsonProperty("hostname") String hostname,
      @JsonProperty("port") Integer port,
      @JsonProperty("prefix") @Nullable String prefix,
      @JsonProperty("separator") @Nullable String separator,
      @JsonProperty("includeHost") @Nullable Boolean includeHost,
      @JsonProperty("dimensionMapPath") @Nullable String dimensionMapPath,
      @JsonProperty("blankHolder") @Nullable String blankHolder,
      @JsonProperty("dogstatsd") @Nullable Boolean dogstatsd,
      @JsonProperty("dogstatsdConstantTags")
      @JsonDeserialize(using = JacksonUtil.CommaDelimitedListDeserializer.class)
      @Nullable List<String> dogstatsdConstantTags,
      @JsonProperty("dogstatsdServiceAsTag") @Nullable Boolean dogstatsdServiceAsTag,
      @JsonProperty("dogstatsdEvents") @Nullable Boolean dogstatsdEvents
  )
  {
    super(
        hostname,
        port,
        prefix,
        separator,
        includeHost,
        dimensionMapPath,
        blankHolder,
        dogstatsd,
        dogstatsdConstantTags,
        dogstatsdServiceAsTag,
        dogstatsdEvents
    );
  }

  @Override
  public Emitter makeEmitter(ObjectMapper objectMapper, AsyncHttpClient httpClient, Lifecycle lifecycle)
  {
    Emitter retVal = StatsDEmitter.of(this, objectMapper);
    lifecycle.addManagedInstance(retVal);
    return retVal;
  }
}
