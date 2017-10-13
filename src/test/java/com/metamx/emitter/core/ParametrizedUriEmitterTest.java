package com.metamx.emitter.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.service.UnitEvent;
import com.metamx.http.client.GoHandler;
import com.metamx.http.client.GoHandlers;
import com.metamx.http.client.MockHttpClient;
import com.metamx.http.client.Request;
import com.metamx.http.client.response.HttpResponseHandler;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static com.metamx.emitter.core.EmitterTest.okResponse;
import static org.junit.Assert.assertEquals;

public class ParametrizedUriEmitterTest
{
  private static final ObjectMapper jsonMapper = new ObjectMapper();

  private MockHttpClient httpClient;
  private Lifecycle lifecycle;

  @Before
  public void setUp() throws Exception
  {
    httpClient = new MockHttpClient();
  }

  @After
  public void tearDown() throws Exception
  {
    if (lifecycle != null) {
      lifecycle.stop();
    }
  }

  private Emitter parametrizedEmmiter(String uriPattern) throws Exception
  {
    final Properties props = new Properties();
    props.setProperty("com.metamx.emitter.type", "parametrized");
    props.setProperty("com.metamx.emitter.recipientBaseUrlPattern", uriPattern);
    lifecycle = new Lifecycle();
    Emitter emitter = Emitters.create(props, httpClient, lifecycle);
    assertEquals(ParametrizedUriEmitter.class, emitter.getClass());
    lifecycle.start();
    return emitter;
  }

  @Test
  public void testParametrizedEmitterCreated() throws Exception
  {
    parametrizedEmmiter("http://example.com/");
  }

  @Test
  public void testEmitterWithFeedUriExtractor() throws Exception
  {
    Emitter emitter = parametrizedEmmiter("http://example.com/{feed}");
    final List<UnitEvent> events = Arrays.asList(
        new UnitEvent("test", 1),
        new UnitEvent("test", 2)
    );

    httpClient.setGoHandler(
        new GoHandler()
        {
          @Override
          public <Intermediate, Final> ListenableFuture<Final> go(
              Request request,
              HttpResponseHandler<Intermediate, Final> handler,
              Duration requestReadTimeout
          ) throws Exception
          {
            Assert.assertEquals(new URL("http://example.com/test"), request.getUrl());
            Assert.assertEquals(
                String.format(
                    "[%s,%s]\n",
                    jsonMapper.writeValueAsString(events.get(0)),
                    jsonMapper.writeValueAsString(events.get(1))
                ),
                request.getContent().toString(Charsets.UTF_8)
            );

            return Futures.immediateFuture((Final) okResponse());
          }
        }.times(1)
    );

    for (UnitEvent event : events) {
      emitter.emit(event);
    }
    emitter.flush();
    Assert.assertTrue(httpClient.succeeded());
  }

  @Test
  public void testEmitterWithMultipleFeeds() throws Exception
  {
    Emitter emitter = parametrizedEmmiter("http://example.com/{feed}");
    final List<UnitEvent> events = Arrays.asList(
        new UnitEvent("test1", 1),
        new UnitEvent("test2", 2)
    );

    final Map<String, String> results = new HashMap<>();

    httpClient.setGoHandler(
        new GoHandler()
        {
          @Override
          public <Intermediate, Final> ListenableFuture<Final> go(
              Request request,
              HttpResponseHandler<Intermediate, Final> handler,
              Duration requestReadTimeout
          ) throws Exception
          {
            results.put(request.getUrl().toString(), request.getContent().toString(Charsets.UTF_8));
            return Futures.immediateFuture((Final) okResponse());
          }
        }.times(2)
    );

    for (UnitEvent event : events) {
      emitter.emit(event);
    }
    emitter.flush();
    Assert.assertTrue(httpClient.succeeded());
    Map<String, String> expected = ImmutableMap.of(
        "http://example.com/test1", String.format("[%s]\n", jsonMapper.writeValueAsString(events.get(0))),
        "http://example.com/test2", String.format("[%s]\n", jsonMapper.writeValueAsString(events.get(1))));
    Assert.assertEquals(expected, results);
  }

  @Test
  public void testEmitterWithParametrizedUriExtractor() throws Exception
  {
    Emitter emitter = parametrizedEmmiter("http://example.com/{key1}/{key2}");
    final List<UnitEvent> events = Arrays.asList(
        new UnitEvent("test", 1, ImmutableMap.of("key1", "val1", "key2", "val2")),
        new UnitEvent("test", 2, ImmutableMap.of("key1", "val1", "key2", "val2"))
    );

    httpClient.setGoHandler(
        new GoHandler()
        {
          @Override
          public <Intermediate, Final> ListenableFuture<Final> go(
              Request request,
              HttpResponseHandler<Intermediate, Final> handler,
              Duration requestReadTimeout
          ) throws Exception
          {
            Assert.assertEquals(new URL("http://example.com/val1/val2"), request.getUrl());
            Assert.assertEquals(
                String.format(
                    "[%s,%s]\n",
                    jsonMapper.writeValueAsString(events.get(0)),
                    jsonMapper.writeValueAsString(events.get(1))
                ),
                request.getContent().toString(Charsets.UTF_8)
            );

            return Futures.immediateFuture((Final) okResponse());
          }
        }.times(1)
    );

    for (UnitEvent event : events) {
      emitter.emit(event);
    }
    emitter.flush();
    Assert.assertTrue(httpClient.succeeded());
  }

  @Test
  public void failEmitMalformedEvent() throws Exception
  {
    Emitter emitter = parametrizedEmmiter("http://example.com/{keyNotSetInEvents}");
    Event event = new UnitEvent("test", 1);

    httpClient.setGoHandler(GoHandlers.failingHandler());

    try {
      emitter.emit(event);
      emitter.flush();
    }
    catch (IllegalArgumentException e) {
      Assert.assertEquals(
          e.getMessage(),
          String.format(
              "ParametrizedUriExtractor with pattern http://example.com/{keyNotSetInEvents} requires keyNotSetInEvents to be set in event, but found %s", event.toMap())
      );
    }
  }
}
