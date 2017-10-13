package com.metamx.emitter.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.factory.EmitterFactory;
import com.metamx.http.client.HttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class CustomEmitterFactoryTest
{
  @JsonTypeName("test")
  public static class TestEmitterConfig implements EmitterFactory
  {
    @JsonProperty
    private String stringProperty;
    @JsonProperty
    private int intProperty;

    @Override
    public Emitter makeEmitter(ObjectMapper objectMapper, HttpClient httpClient, Lifecycle lifecycle)
    {
      return new StubEmitter(stringProperty, intProperty);
    }
  }

  public static class StubEmitter implements Emitter
  {
    private String stringProperty;
    private int intProperty;

    public StubEmitter(String stringProperty, int intProperty)
    {
      this.stringProperty = stringProperty;
      this.intProperty = intProperty;
    }

    public String getStringProperty()
    {
      return stringProperty;
    }

    public int getIntProperty()
    {
      return intProperty;
    }

    @Override
    public void start() {}

    @Override
    public void emit(Event event) {}

    @Override
    public void flush() throws IOException {}

    @Override
    public void close() throws IOException {}
  }

  @Test
  public void testCustomEmitter()
  {
    final Properties props = new Properties();
    props.put("com.metamx.emitter.stringProperty", "http://example.com/");
    props.put("com.metamx.emitter.intProperty", "1");
    props.put("com.metamx.emitter.type", "test");

    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerSubtypes(TestEmitterConfig.class);
    final Lifecycle lifecycle = new Lifecycle();
    final Emitter emitter = Emitters.create(props, null, objectMapper, lifecycle);

    Assert.assertTrue("created emitter should be of class StubEmitter", emitter instanceof StubEmitter);
    StubEmitter stubEmitter = (StubEmitter) emitter;
    Assert.assertEquals("http://example.com/", stubEmitter.getStringProperty());
    Assert.assertEquals(1, stubEmitter.getIntProperty());
  }
}
