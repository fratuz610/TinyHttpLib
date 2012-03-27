/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo;

import it.holiday69.tinyhttp.utils.StringUtils;
import it.holiday69.tinyhttp.vo.request.RequestItem;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class HttpRequest {
  
  public HttpRequest() { 
    
  }
  
  public HttpRequest(HttpMethod method, String url) { 
    this.method = method;
    this.url = url; 
  }
  
  public HttpMethod method;
  public String url;
  public int timeout = 30000;
  public boolean followRedirects = true;
  
  public final List<KeyValuePair> requestHeaderList = new LinkedList<KeyValuePair>();
  
  public final List<RequestItem> requestParamList = new LinkedList<RequestItem>();
  
  public void validate() throws Exception {
    if(method == null)
      throw new Exception("The http method must be specified");
    
    if(StringUtils.isEmpty(url))
      throw new Exception("The call URL must be specified");
    
    if(timeout < 0)
      throw new Exception("The timeout value cannot be negative");
  }
}
