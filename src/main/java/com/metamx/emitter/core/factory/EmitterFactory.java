package com.metamx.emitter.core.factory;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.http.client.HttpClient;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(value = {
    @JsonSubTypes.Type(name = "http", value = HttpEmitterFactory.class),
    @JsonSubTypes.Type(name = "logging", value = LoggingEmitterFactory.class),
    @JsonSubTypes.Type(name = "parametrized", value = ParametrizedUriEmitterFactory.class),
    @JsonSubTypes.Type(name = "noop", value = NoopEmiterFactory.class),
})
public interface EmitterFactory
{
  Emitter makeEmitter(ObjectMapper objectMapper, HttpClient httpClient, Lifecycle lifecycle);
}
