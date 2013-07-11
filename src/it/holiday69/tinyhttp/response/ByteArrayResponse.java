package it.holiday69.tinyhttp.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class ByteArrayResponse extends HttpResponse
{
  private final ByteArrayOutputStream _out = new ByteArrayOutputStream();

  public OutputStream getOutputStream()
  {
    return _out;
  }
  public byte[] getByteArray() { return _out.toByteArray(); } 
  public ByteArrayInputStream getByteArrayInputStream() { return new ByteArrayInputStream(_out.toByteArray()); }

}