package it.holiday69.tinyhttp.request;

public class RawDataParam
  implements HttpParam
{
  private String _rawData;

  public RawDataParam(String rawData)
  {
    _rawData = rawData;
  }
  public String getRawData() {
    return _rawData;
  }
}