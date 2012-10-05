/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.v2.request;


/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class RawDataParam implements HttpParam {
  
  private String _rawData;
  
  public RawDataParam(String rawData) {
    _rawData = rawData;
  }
  
  public String getRawData() { return _rawData; }
  
}
