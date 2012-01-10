/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.vo.response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author fratuz610
 */
public class FileResponseBody implements ResponseBody {
  
  private File _file;
  private FileOutputStream _out;
  
  public FileResponseBody(File file) throws IOException { 
    _out = new FileOutputStream(file); 
    _file = file;
  }
  
  @Override
  public OutputStream getOutputStream() { return _out; }
  
  public File getFile() { return _file; }
  
}
