/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.v2.response;

import it.holiday69.tinyhttp.vo.response.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 *
 * @author fratuz610
 */
public class ByteArrayResponse extends HttpResponse {
  
  private final ByteArrayOutputStream _out = new ByteArrayOutputStream();
  
  public ByteArrayResponse() { }
  
  @Override
  public OutputStream getOutputStream() { return _out; }
  
  public byte[] getByteArray() { return _out.toByteArray(); }
  public ByteArrayInputStream getByteArrayInputStream() { return new ByteArrayInputStream(_out.toByteArray()); }
  
}
