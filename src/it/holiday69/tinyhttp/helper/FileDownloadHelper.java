/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.helper;

import it.holiday69.tinyhttp.HttpCall;
import it.holiday69.tinyhttp.HttpRequest;
import it.holiday69.tinyhttp.method.Get;
import it.holiday69.tinyhttp.response.ByteArrayResponse;
import it.holiday69.tinyhttp.response.TempFileResponse;
import it.holiday69.tinyhttp.response.TextResponse;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author fratuz610
 */
public class FileDownloadHelper {
  
  private int _timeout = 0;
  
  public FileDownloadHelper() { }
  
  public FileDownloadHelper(int timeout) {
    
    if(_timeout < 0)
      throw new IllegalArgumentException("Timeout value cannot be negative");
    
    _timeout = timeout;
  }
  
  public String getTextFile(String url) throws IOException {
    
    TextResponse resp = new HttpCall<TextResponse>(new HttpRequest()
              .withURL(url)
              .withFollowRedirects(true)
              .withTimeout(_timeout)
              .withMethod(Get.class), TextResponse.class).call();
      
      if(resp.getResponseCode() > 299)
        throw new IOException("Unable to download file from url: " + url + " http error code: " + resp.getResponseCode());
      
      return resp.getText();
  }
  
  public byte[] getBinaryFile(String url) throws IOException {
    
    ByteArrayResponse resp = new HttpCall<ByteArrayResponse>(new HttpRequest()
              .withURL(url)
              .withFollowRedirects(true)
              .withTimeout(_timeout)
              .withMethod(Get.class), ByteArrayResponse.class).call();
      
      if(resp.getResponseCode() > 299)
        throw new IOException("Unable to download file from url: " + url + " http error code: " + resp.getResponseCode());
      
      return resp.getByteArray();
  }
  
  public File getTempFile(String url) throws IOException {
    return getTempFile(url, 0);
  }
  
  public File getTempFile(String url, int timeout) throws IOException {
    
    TempFileResponse resp = new HttpCall<TempFileResponse>(new HttpRequest()
              .withURL(url)
              .withFollowRedirects(true)
              .withTimeout(_timeout)
              .withMethod(Get.class), TempFileResponse.class).call();
      
      if(resp.getResponseCode() > 299)
        throw new IOException("Unable to download file from url: " + url + " http error code: " + resp.getResponseCode());
      
      return resp.getFile();
  }
  
}
