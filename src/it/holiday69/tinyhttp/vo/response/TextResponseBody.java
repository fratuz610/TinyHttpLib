/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.vo.response;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 *
 * @author fratuz610
 */
public class TextResponseBody implements ResponseBody {
  
  private final ByteArrayOutputStream _out = new ByteArrayOutputStream();
  
  public TextResponseBody() { }
  
  public OutputStream getOutputStream() { return _out; }
  
  public String getText() { return _out.toString(); }
  
}
