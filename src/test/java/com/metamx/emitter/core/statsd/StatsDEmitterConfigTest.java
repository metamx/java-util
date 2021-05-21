package com.metamx.emitter.core.statsd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamx.common.lifecycle.Lifecycle;
import com.metamx.emitter.core.Emitter;
import com.metamx.emitter.core.Emitters;
import com.metamx.emitter.core.factory.EmitterFactory;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StatsDEmitterConfigTest
{

  @Test
  public void testInstantiatingConfigFromProperties()
  {
    final Properties props = new Properties();
    props.setProperty("hostname", "localhost");
    props.setProperty("port", "9999");
    props.setProperty("dogstatsd", "true");
    props.setProperty("dogstatsdServiceAsTag", "true");
    props.setProperty("dogstatsdConstantTags", "app:application,component:ingress,environment:prod");
    final StatsDEmitterConfig statsDEmitterConfig = new ObjectMapper().convertValue(props, StatsDEmitterConfig.class);
    assertEquals(statsDEmitterConfig.getHostname(), "localhost");
    assertEquals(statsDEmitterConfig.getPort(), 9999);
    assertEquals(statsDEmitterConfig.isDogstatsd(), true);
    assertEquals(statsDEmitterConfig.isDogstatsdServiceAsTag(), true);
    assertArrayEquals(statsDEmitterConfig.getDogstatsdConstantTags().toArray(), new String[] {"app:application","component:ingress","environment:prod"});
  }

  @Test
  public void testInstantiatingFactoryFromProperties()
  {
    final Properties props = new Properties();
    props.setProperty("type", "statsd");
    props.setProperty("hostname", "localhost");
    props.setProperty("port", "9999");
    props.setProperty("dogstatsd", "true");
    props.setProperty("dogstatsdServiceAsTag", "true");
    props.setProperty("dogstatsdConstantTags", "app:application,component:ingress,environment:prod");
    final StatsDEmitterFactory emitterFactory = (StatsDEmitterFactory) new ObjectMapper().convertValue(props, EmitterFactory.class);
    assertEquals(emitterFactory.getHostname(), "localhost");
    assertEquals(emitterFactory.getPort(), 9999);
    assertEquals(emitterFactory.isDogstatsd(), true);
    assertEquals(emitterFactory.isDogstatsdServiceAsTag(), true);
    assertArrayEquals(emitterFactory.getDogstatsdConstantTags().toArray(), new String[] {"app:application","component:ingress","environment:prod"});
  }

  @Test
  public void testInstantiatingEmitterFromProperties()
  {
    final Properties props = new Properties();
    props.setProperty("com.metamx.emitter.type", "statsd");
    props.setProperty("com.metamx.emitter.hostname", "localhost");
    props.setProperty("com.metamx.emitter.port", "9999");
    props.setProperty("com.metamx.emitter.dogstatsd", "true");
    props.setProperty("com.metamx.emitter.dogstatsdServiceAsTag", "true");
    props.setProperty("com.metamx.emitter.dogstatsdConstantTags", "app:application,component:ingress,environment:prod");
    final Emitter stasdEmitter = Emitters.create(props, null, new ObjectMapper(), new Lifecycle());
    assertNotNull(stasdEmitter);
  }
}
