/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.v2.request;

import it.holiday69.tinyhttp.v2.request.HttpParam;
import it.holiday69.tinyhttp.v2.request.HttpParam;
import it.holiday69.tinyhttp.v2.shared.KeyValuePair;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class KeyValueParam extends KeyValuePair implements HttpParam {
  
  public KeyValueParam() {
    super();
  }
  
  public KeyValueParam(String key, String value) {
    super(key, value);
  }
}
