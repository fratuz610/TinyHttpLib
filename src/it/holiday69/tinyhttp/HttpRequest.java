package it.holiday69.tinyhttp;

import it.holiday69.tinyhttp.method.Get;
import it.holiday69.tinyhttp.method.Method;
import it.holiday69.tinyhttp.request.FileUploadParam;
import it.holiday69.tinyhttp.request.HttpParam;
import it.holiday69.tinyhttp.request.KeyValueParam;
import it.holiday69.tinyhttp.request.RawDataParam;
import it.holiday69.tinyhttp.shared.HttpHeader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class HttpRequest
{
  private Class<? extends Method> method = Get.class;
  private String url;
  private int timeout = 0;
  private ThreadFactory timeoutTf = null;
  private boolean followRedirects = false;
  private final List<HttpHeader> headerList = new LinkedList();
  private final List<HttpParam> paramList = new LinkedList();

  public HttpRequest withMethod(Class<? extends Method> method) { this.method = method; return this; } 
  public HttpRequest withURL(String url) {
    this.url = url; return this;
  }
  public HttpRequest withTimeoutThreadFactory(ThreadFactory timeoutTf) { this.timeoutTf = timeoutTf; return this; } 
  public HttpRequest withTimeout(int timeout) {
    this.timeout = timeout; return this;
  }
  public HttpRequest withFollowRedirects(boolean followRedirects) { this.followRedirects = followRedirects; return this; } 
  public HttpRequest withHeader(String headerString) {
    headerList.add(new HttpHeader(headerString)); return this;
  }
  public HttpRequest withHeader(String key, String value) { headerList.add(new HttpHeader(key, value)); return this; } 
  public HttpRequest withParam(FileUploadParam fileUploadParam) {
    paramList.add(fileUploadParam); return this;
  }
  public HttpRequest withParam(String key, String value) { paramList.add(new KeyValueParam(key, value)); return this; } 
  public HttpRequest withParam(String rawData) {
    paramList.add(new RawDataParam(rawData)); return this;
  }
  public Class<? extends Method> getMethod() {
    return method; } 
  public String getURL() { return url; } 
  public int getTimeout() { return timeout; } 
  public ThreadFactory getTimeoutThreadFactory() { return timeoutTf; } 
  public boolean getFollowRedirects() { return followRedirects; } 
  public List<HttpHeader> getHeaderList() { return headerList; } 
  public List<HttpParam> getParamList() { return paramList; }

}