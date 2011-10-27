/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class HttpParam {

  public HttpParam() {
    
  }
  public HttpParam(String key, String value) {
    this.key = key;
    this.value = value;
  }
  public String key;
  public String value;

}
