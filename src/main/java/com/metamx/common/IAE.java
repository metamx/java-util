package com.metamx.common;

/**
 */
public class IAE extends IllegalArgumentException
{
  public IAE(String formatText, Object... arguments)
  {
    super(String.format(formatText, arguments));
  }

  public IAE(Throwable cause, String formatText, Object... arguments)
  {
    super(String.format(formatText, arguments), cause);
  }
}
