package it.holiday69.tinyhttp.response;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class TextResponse extends HttpResponse
{
  private final ByteArrayOutputStream _out = new ByteArrayOutputStream();

  public int getSize() { return _out.size(); } 
  public String getText() { return _out.toString(); }

  public OutputStream getOutputStream() {
    return _out;
  }
}