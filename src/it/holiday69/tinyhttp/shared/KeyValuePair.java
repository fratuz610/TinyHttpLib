package it.holiday69.tinyhttp.shared;

import java.io.Serializable;

public class KeyValuePair
  implements Serializable
{
  public String key;
  public String value;

  public KeyValuePair()
  {
  }

  public KeyValuePair(String key, String value)
  {
    this.key = key;
    this.value = value;
  }
  public KeyValuePair withKey(String key) {
    this.key = key; return this; } 
  public KeyValuePair withValue(String value) { this.value = value; return this;
  }
}