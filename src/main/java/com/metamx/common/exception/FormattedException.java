package com.metamx.common.exception;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 */
public class FormattedException extends RuntimeException
{
  public static enum ErrorCode
  {
    FILE_NOT_FOUND,
    BAD_HEADER,
    UNPARSABLE_ROW,
    MISSING_TIMESTAMP,
    UNPARSABLE_TIMESTAMP,
    MISSING_METRIC,
    UNPARSABLE_METRIC,
    SERVER_ERROR
  }

  public static class Builder
  {
    private ErrorCode errorCode;
    private Map<String, Object> details;
    private String message;

    public Builder() {}

    public Builder withErrorCode(ErrorCode errorCode)
    {
      this.errorCode = errorCode;
      return this;
    }

    public Builder withDetails(Map<String, Object> details)
    {
      this.details = details;
      return this;
    }

    public Builder withMessage(String message)
    {
      this.message = message;
      return this;
    }

    public FormattedException build()
    {
      return new FormattedException(errorCode, details, message);
    }
  }

  private final ErrorCode errorCode;
  private final Map<String, Object> details;
  private final String message;

  private FormattedException(ErrorCode errorCode, Map<String, Object> details, String message)
  {
    this.errorCode = errorCode;
    this.details = details;
    this.message = message;
  }

  @JsonProperty
  public ErrorCode getErrorCode()
  {
    return errorCode;
  }

  @JsonProperty
  public Map<String, Object> getDetails()
  {
    return details;
  }

  @JsonProperty
  public String getMessage()
  {
    return message;
  }
}
