/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.v2;

import it.holiday69.tinyhttp.HttpCall;
import it.holiday69.tinyhttp.HttpRequest;
import it.holiday69.tinyhttp.internal.utils.ExceptionUtils;
import it.holiday69.tinyhttp.internal.utils.IOHelper;
import it.holiday69.tinyhttp.method.Post;
import it.holiday69.tinyhttp.request.FileUploadParam;
import it.holiday69.tinyhttp.response.ByteArrayResponse;
import it.holiday69.tinyhttp.response.TextResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author fratuz610
 */
public class HttpCallTest {
  
  public HttpCallTest() {
  }

  /**
   * Test of call method, of class HttpCall.
   */
  @Test
  public void testGetCall() throws Exception {
    System.out.println("GET call");
    
    HttpRequest req = new HttpRequest().withURL("http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html");
    
    HttpCall<TextResponse> call = new HttpCall<TextResponse>(req, TextResponse.class);
    
    TextResponse resp = call.call();
    
    assertNotNull(resp.getText());
    assertTrue(resp.getText().length() > 0);
    
  }
  
  /**
   * Test of call method, of class HttpCall.
   */
  @Test
  public void testSimplePostCall() throws Exception {
    
    System.out.println("POST call");
    
    HttpRequest req = new HttpRequest()
            .withMethod(Post.class)
            .withURL("http://www.holiday69.it/aka.php")
            .withParam("Hello", "world")
            .withParam("AnotherOne", "world");
    
    HttpCall<TextResponse> call = new HttpCall<TextResponse>(req, TextResponse.class);
    
    TextResponse resp = call.call();
    
    assertNotNull(resp.getText());
    assertTrue(resp.getText().length() > 0);
    
    System.out.println("Response body: " + resp.getText());
    
  }
  
  /**
   * Test of call method, of class HttpCall.
   */
  @Test
  public void testFileUploadPostCall() throws Exception {
    
    System.out.println("File Upload POST call");
    
    HttpRequest req = new HttpRequest()
            .withMethod(Post.class)
            .withURL("http://www.holiday69.it/aka.php")
            .withParam(new FileUploadParam("A text file".getBytes(), "textFile.txt"));
    
    HttpCall<TextResponse> call = new HttpCall<TextResponse>(req, TextResponse.class);
    
    TextResponse resp = null;
    try {
      resp = call.call();
    } catch(IOException ex) {
      System.out.println("IOException: " + ExceptionUtils.getFullExceptionInfo(ex));
      return;
    }
    
    assertNotNull(resp.getText());
    assertTrue(resp.getText().length() > 0);
    
    System.out.println("Response body: " + resp.getText());
    
  }

  /**
   * Test of call method, of class HttpCall.
   */
  @Test
  public void testMultipartFileUploadPostCall() throws Exception {
    
    System.out.println("File Multipart File Upload POST call");
    
    HttpRequest req = new HttpRequest()
            .withMethod(Post.class)
            .withURL("http://www.holiday69.it/aka.php")
            .withParam(new FileUploadParam("A text file".getBytes(), "textFile.txt"))
            .withParam("SimpleKey", "SimpleValue");
    
    
    HttpCall<TextResponse> call = new HttpCall<TextResponse>(req, TextResponse.class);
    
    TextResponse resp = null;
    try {
      resp = call.call();
    } catch(IOException ex) {
      System.out.println("IOException: " + ExceptionUtils.getFullExceptionInfo(ex));
      return;
    }
    
    assertNotNull(resp.getText());
    assertTrue(resp.getText().length() > 0);
    
    System.out.println("Response body: " + resp.getText());
    
  }
  
  /**
   * Test of call method, of class HttpCall.
   */
  @Test
  public void testDownloadFile() throws Exception {

    System.out.println("GET Download File");

    HttpRequest req = new HttpRequest()
            .withURL("http://upload.wikimedia.org/wikipedia/commons/2/2b/Crystal_Clear_app_download_manager.png");

    HttpCall<ByteArrayResponse> call = new HttpCall<ByteArrayResponse>(req, ByteArrayResponse.class);

    ByteArrayResponse resp = null;
    try {
      resp = call.call();
    } catch (IOException ex) {
      System.out.println("IOException: " + ExceptionUtils.getFullExceptionInfo(ex));
      return;
    }
    
    System.out.println("Call log: " + call.getCallLog());
    
    assertNotNull(resp.getByteArray());
    assertTrue(resp.getByteArray().length > 0);
    
    File tempFile = File.createTempFile("it.holiday69", "");
    
    new IOHelper().copy(resp.getByteArrayInputStream(), new FileOutputStream(tempFile));
    
    System.out.println("Temp file: " + tempFile);
  }

}