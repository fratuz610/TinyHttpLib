package it.holiday69.tinyhttp.shared;

public class HttpHeader extends KeyValuePair
{
  public HttpHeader()
  {
  }

  public HttpHeader(String key, String value)
  {
    super(key, value);
  }

  public HttpHeader(String headerString)
  {
    if (headerString.indexOf(":") == -1) {
      throw new IllegalArgumentException("Invalid headerString: '" + headerString + "' no ':' found");
    }
    key = headerString.substring(0, headerString.indexOf(":")).trim();
    value = headerString.substring(headerString.indexOf(":") + 1).trim();
  }
}