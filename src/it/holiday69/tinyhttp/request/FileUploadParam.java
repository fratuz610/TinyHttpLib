/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.request;

import java.io.*;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class FileUploadParam implements HttpParam {
  
  private String _name;
  private long _size;
  private InputStream _inputStream;
  
  public FileUploadParam(File file) throws IOException {
    _name = file.getName();
    _size = file.length();
    _inputStream = new FileInputStream(file);
  }
  
  public FileUploadParam(byte[] srcByteArray, String name) throws IOException {
    _name = "" + name;
    _size = srcByteArray.length;
    _inputStream = new ByteArrayInputStream(srcByteArray);
  }
  
  public FileUploadParam() {
    
  }
  
  public String getName() { return _name; }
  public long getSize() { return _size; }
  public InputStream getInputStream() { return _inputStream; }
}
