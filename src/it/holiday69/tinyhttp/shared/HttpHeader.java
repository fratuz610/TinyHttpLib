/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.shared;

import it.holiday69.tinyhttp.request.KeyValueParam;


/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class HttpHeader extends KeyValuePair {

  public HttpHeader() {
    super();
  }
  
  public HttpHeader(String key, String value) {
    super(key, value);
  }
}
