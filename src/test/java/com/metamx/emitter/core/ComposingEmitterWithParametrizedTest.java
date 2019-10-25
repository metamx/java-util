package com.metamx.emitter.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.service.UnitEvent;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.metamx.emitter.core.EmitterTest.okResponse;
import static org.junit.Assert.assertEquals;

public class ComposingEmitterWithParametrizedTest
{
  private static final ObjectMapper jsonMapper = new ObjectMapper();

  private MockHttpClient httpClient;
  private Lifecycle lifecycle;

  @Before
  public void setUp()
  {
    httpClient = new MockHttpClient();
  }

  @After
  public void tearDown()
  {
    if (lifecycle != null) {
      lifecycle.stop();
    }
  }

  private Emitter composingEmitter() throws Exception
  {
    return composingEmitter(new Properties());
  }

  private Emitter composingEmitter(Properties overrideProps) throws Exception
  {
    final Properties props = new Properties();
    props.setProperty("com.metamx.emitter.type", "composing");
    props.setProperty("com.metamx.emitter.composite.noop.type", "noop");
    props.setProperty("com.metamx.emitter.composite.log.type", "logging");
    props.setProperty("com.metamx.emitter.composite.http1.type", "parametrized");
    props.setProperty("com.metamx.emitter.composite.http1.recipientBaseUrlPattern", "http://example.com/{feed}");
    props.setProperty("com.metamx.emitter.composite.http2.type", "parametrized");
    props.setProperty("com.metamx.emitter.composite.http2.recipientBaseUrlPattern", "http://example.com/{feed}");
    props.putAll(overrideProps);
    lifecycle = new Lifecycle();
    Emitter emitter = Emitters.create(props, httpClient, lifecycle);
    assertEquals(ComposingEmitter.class, emitter.getClass());
    lifecycle.start();
    return emitter;
  }

  @Test
  public void testComposingEmitterCreated() throws Exception
  {
    composingEmitter();
  }


  @Test
  public void testComposingEmitterWithTwoParametrized() throws Exception
  {
    final Properties props = new Properties();
    Emitter emitter = composingEmitter(props);
    final List<UnitEvent> events = Arrays.asList(
        new UnitEvent("test1", 1),
        new UnitEvent("test2", 2)
    );

    httpClient.setGoHandler(
        new GoHandler()
        {
          @Override
          public ListenableFuture<Response> go(Request request) throws JsonProcessingException
          {
            switch (request.getUrl()) {
              case "http://example.com/test1" :
                Assert.assertEquals(
                    String.format("[%s]\n",jsonMapper.writeValueAsString(events.get(0))),
                    Charsets.UTF_8.decode(request.getByteBufferData().slice()).toString()
                );
                break;
              case "http://example.com/test2" :
                Assert.assertEquals(
                    String.format("[%s]\n",jsonMapper.writeValueAsString(events.get(1))),
                    Charsets.UTF_8.decode(request.getByteBufferData().slice()).toString()
                );
                break;
            }
            return GoHandlers.immediateFuture(okResponse());
          }
        }.times(4)
    );

    for (Event event : events) {
      emitter.emit(event);
    }
    emitter.flush();
    Assert.assertTrue(httpClient.succeeded());
  }

}
