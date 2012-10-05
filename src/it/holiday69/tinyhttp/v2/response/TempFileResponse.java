/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.v2.response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author fratuz610
 */
public class TempFileResponse extends HttpResponse {
  
  private File _file;
  private FileOutputStream _out;
  
  public TempFileResponse(File file) throws IOException { 
    _out = new FileOutputStream(file); 
    _file = file;
  }
  
  public TempFileResponse(String prefix, String postfix) throws IOException {
    this(File.createTempFile(prefix, postfix));
  }
  
  public TempFileResponse() throws IOException {
    this(File.createTempFile("", ""));
  }
    
  @Override
  public OutputStream getOutputStream() { return _out; }
  
  public File getFile() { return _file; }
  
}
