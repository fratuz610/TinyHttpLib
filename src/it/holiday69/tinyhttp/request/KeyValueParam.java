package it.holiday69.tinyhttp.request;

import it.holiday69.tinyhttp.shared.KeyValuePair;

public class KeyValueParam extends KeyValuePair
  implements HttpParam
{
  public KeyValueParam()
  {
  }

  public KeyValueParam(String key, String value)
  {
    super(key, value);
  }
}