/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo.request;

import it.holiday69.tinyhttp.vo.KeyValuePair;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class KeyValueRequestItem extends KeyValuePair implements RequestItem {
  
  public KeyValueRequestItem() {
    super();
  }
  
  public KeyValueRequestItem(String key, String value) {
    super(key, value);
  }
}
