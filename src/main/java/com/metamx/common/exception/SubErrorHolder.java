package com.metamx.common.exception;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 */
public class SubErrorHolder
{
  private final FormattedException.SubErrorCode subErrorCode;
  private final Object invalid_key;
  private final Object invalid_value;

  public SubErrorHolder(FormattedException.SubErrorCode subErrorCode, Object invalid_key, Object invalid_value)
  {
    this.subErrorCode = subErrorCode;
    this.invalid_key = invalid_key;
    this.invalid_value = invalid_value;
  }

  public Map<String, Object> get()
  {
    return ImmutableMap.<String, Object>of(
        "subErrorCode", subErrorCode,
        "invalid_key", invalid_key,
        "invalid_value", invalid_value
    );
  }
}
