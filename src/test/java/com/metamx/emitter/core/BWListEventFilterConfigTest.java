package com.metamx.emitter.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class BWListEventFilterConfigTest
{
  @Test
  public void testDefaults()
  {
    final Properties props = new Properties();

    final ObjectMapper objectMapper = new ObjectMapper();
    final ParametrizedUriEmitterConfig paramConfig = objectMapper.convertValue(Emitters.makeCustomFactoryMap(props), ParametrizedUriEmitterConfig.class);

    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getWhiteList().isEmpty());
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getBlackList().isEmpty());
  }

  @Test
  public void testSettingEverything()
  {
    final Properties props = new Properties();
    props.setProperty("com.metamx.emitter.bwFilter.whiteList.A", "AW1, AW2");
    props.setProperty("com.metamx.emitter.bwFilter.blackList.A", "AB1, AB2");
    props.setProperty("com.metamx.emitter.bwFilter.blackList.B", "BB1, BB2");

    final ObjectMapper objectMapper = new ObjectMapper();
    final ParametrizedUriEmitterConfig paramConfig = objectMapper.convertValue(Emitters.makeCustomFactoryMap(props), ParametrizedUriEmitterConfig.class);

    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getWhiteList().containsKey("A"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getWhiteList().get("A").contains("AW1"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getWhiteList().get("A").contains("AW2"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getBlackList().containsKey("A"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getBlackList().get("A").contains("AB1"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getBlackList().get("A").contains("AB2"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getBlackList().containsKey("B"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getBlackList().get("B").contains("BB1"));
    Assert.assertTrue(paramConfig.getBwListEventFilterConfig().getBlackList().get("B").contains("BB2"));
  }
}
