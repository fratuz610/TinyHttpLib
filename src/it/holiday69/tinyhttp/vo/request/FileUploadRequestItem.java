/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.vo.request;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class FileUploadRequestItem implements RequestItem {
  
  private String _name;
  private long _size;
  private InputStream _inputStream;
  
  public FileUploadRequestItem(File file) throws IOException {
    _name = file.getName();
    _size = file.length();
    _inputStream = new FileInputStream(file);
  }
  
  public FileUploadRequestItem(byte[] srcByteArray, String name) throws IOException {
    _name = "" + name;
    _size = srcByteArray.length;
    _inputStream = new ByteArrayInputStream(srcByteArray);
  }
  
  public String getName() { return _name; }
  public long getSize() { return _size; }
  public InputStream getInputStream() { return _inputStream; }
}
