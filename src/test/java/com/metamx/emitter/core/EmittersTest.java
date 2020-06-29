package com.metamx.emitter.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

public class EmittersTest
{
  @Test
  public void makeCustomFactoryMapTest()
  {
    final Properties properties = new Properties();
    properties.setProperty("com.metamx.emitter.property.A", "valueA");
    properties.setProperty("com.metamx.emitter.property.B", "valueB");
    final Map<String, Object> map = Emitters.makeCustomFactoryMap(properties);
    Assert.assertEquals("valueA", ((Map<?, ?>) map.get("property")).get("A"));
    Assert.assertEquals("valueB", ((Map<?, ?>) map.get("property")).get("B"));
  }
}
