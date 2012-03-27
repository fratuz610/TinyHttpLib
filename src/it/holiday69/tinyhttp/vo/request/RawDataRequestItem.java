/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo.request;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class RawDataRequestItem implements RequestItem {
  
  private String _rawData;
  
  public RawDataRequestItem(String rawData) {
    _rawData = rawData;
  }
  
  public String getRawData() { return _rawData; }
  
}
