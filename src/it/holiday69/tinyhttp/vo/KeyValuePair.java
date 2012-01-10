/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class KeyValuePair {

  public String key;
  public String value;
  
  public KeyValuePair() {
    
  }
  
  public KeyValuePair(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
  public KeyValuePair withKey(String key) { this.key = key; return this; }
  public KeyValuePair withValue(String value) { this.value = value; return this; }
  

}
