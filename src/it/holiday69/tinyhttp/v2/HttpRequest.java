/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.v2;

import it.holiday69.tinyhttp.v2.method.GET;
import it.holiday69.tinyhttp.v2.method.Method;
import it.holiday69.tinyhttp.v2.request.FileUploadParam;
import it.holiday69.tinyhttp.v2.request.HttpParam;
import it.holiday69.tinyhttp.v2.request.KeyValueParam;
import it.holiday69.tinyhttp.v2.request.RawDataParam;
import it.holiday69.tinyhttp.v2.shared.HttpHeader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author fratuz610
 */
public class HttpRequest {
  
  private Class<? extends Method> method = GET.class;
  private String url;
  private int timeout = 0;
  private boolean followRedirects = false;
  private final List<HttpHeader> headerList = new LinkedList<HttpHeader>();
  private final List<HttpParam> paramList = new LinkedList<HttpParam>();
  
  public HttpRequest withMethod(Class<? extends Method> method) { this.method = method; return this; }
  
  public HttpRequest withURL(String url) { this.url = url; return this; }
  
  public HttpRequest withTimeout(int timeout) { this.timeout = timeout; return this; }
  
  public HttpRequest withFollowRedirects(boolean followRedirects) { this.followRedirects = followRedirects; return this; }
  
  public HttpRequest withHeader(String key, String value) { this.headerList.add(new HttpHeader(key, value)); return this; }
  
  public HttpRequest withParam(FileUploadParam fileUploadParam) { this.paramList.add(fileUploadParam); return this; }
  
  public HttpRequest withParam(String key, String value) { this.paramList.add(new KeyValueParam(key, value)); return this; }
  
  public HttpRequest withParam(String rawData) { this.paramList.add(new RawDataParam(rawData)); return this; }
  
  // Getters
  public Class<? extends Method> getMethod() { return this.method; }
  public String getURL() { return url; }
  public int getTimeout() { return timeout; }
  public boolean getFollowRedirects() { return followRedirects; }
  public List<HttpHeader> getHeaderList() { return headerList; }
  public List<HttpParam> getParamList() { return paramList; }
  
}
