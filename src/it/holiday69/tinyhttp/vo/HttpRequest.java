/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo;

import it.holiday69.tinyhttp.utils.StringUtils;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class HttpRequest {
  
  public HttpRequest(HttpMethod method, String url) { 
    this.method = method;
    this.url = url; 
  }
  
  public HttpMethod method;
  public String url;
  public int timeout = 30000;
  public boolean followRedirects = true;
  public final List<HttpParam> requestParamList = new LinkedList<HttpParam>();
  public final List<HttpParam> requestHeaderList = new LinkedList<HttpParam>();;
  
  public final List<File> uploadFileList = new LinkedList<File>();
  
  public void validate() throws Exception {
    if(method == null)
      throw new Exception("The http method must be specified");
    
    if(StringUtils.isEmpty(url))
      throw new Exception("The call URL must be specified");
  }
}
