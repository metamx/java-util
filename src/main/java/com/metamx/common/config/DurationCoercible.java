package com.metamx.common.config;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.skife.config.Coercer;
import org.skife.config.Coercible;

/**
*/
public class DurationCoercible implements Coercible<Duration>
{
  @Override
  public Coercer<Duration> accept(Class<?> clazz)
  {
    if (Duration.class != clazz) {
      return null;
    }

    return new Coercer<Duration>()
    {
      @Override
      public Duration coerce(String value)
      {
        return new Period(value).toStandardDuration();
      }
    };
  }
}
