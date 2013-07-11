package it.holiday69.tinyhttp.response;

import it.holiday69.tinyhttp.shared.HttpHeader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public abstract class HttpResponse
{
  private int responseCode;
  private String responseType;
  private long responseLength;
  private final List<HttpHeader> responseHeaderList = new LinkedList();

  public HttpResponse withResponseCode(int responseCode) { this.responseCode = responseCode; return this; } 
  public HttpResponse withResponseType(String responseType) { this.responseType = responseType; return this; } 
  public HttpResponse withResponseLength(long responseLength) { this.responseLength = responseLength; return this; } 
  public HttpResponse withHttpHeader(HttpHeader header) {
    responseHeaderList.add(header); return this;
  }
  public int getResponseCode() { return responseCode; } 
  public String getResponseType() { return responseType; } 
  public long getResponseLength() { return responseLength; } 
  public List<HttpHeader> getHttpHeaderList() { return responseHeaderList; }


  public abstract OutputStream getOutputStream();
}