/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.vo.response;

import java.io.OutputStream;
import java.io.Serializable;

/**
 *
 * @author fratuz610
 */
public interface ResponseBody extends Serializable {
  
  public OutputStream getOutputStream();
}
