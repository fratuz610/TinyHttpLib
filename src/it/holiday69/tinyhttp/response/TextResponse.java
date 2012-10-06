/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.response;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 *
 * @author fratuz610
 */
public class TextResponse extends HttpResponse {
  
  private final ByteArrayOutputStream _out = new ByteArrayOutputStream();
  
  public int getSize() { return _out.size(); }
  public String getText() { return _out.toString(); }
  
  @Override
  public OutputStream getOutputStream() { return _out; }

}
