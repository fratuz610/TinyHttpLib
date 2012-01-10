/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo;

import it.holiday69.tinyhttp.vo.response.ResponseBody;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class HttpResponse {
  
  public int responseCode;
  
  public String responseType;
  public long responseLength;
  
  public final List<KeyValuePair> responseHeaderList = new LinkedList<KeyValuePair>();
  
  public ResponseBody responseBody;
  
}
