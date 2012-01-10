
import it.holiday69.tinyhttp.enume.ResponseType;
import it.holiday69.tinyhttp.task.HttpCall;
import it.holiday69.tinyhttp.vo.HttpMethod;
import it.holiday69.tinyhttp.vo.HttpRequest;
import it.holiday69.tinyhttp.vo.response.FileResponseBody;
import java.io.File;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author fratuz610
 */
public class TestLibrary {
  
  public static void main(String args[]) throws Exception {
    
    HttpRequest httpReq = new HttpRequest(HttpMethod.GET, "http://trac.red5.org/downloads/0_9/red5-0.9.RC1.zip");
    
    HttpCall.setTempFilePrefix("ywc");
    
    HttpCall httpCall = null;
    try {
      httpCall = new HttpCall(httpReq);
      httpCall.responseType = ResponseType.FILE;
      httpReq.timeout = 120000;
      httpCall.call();
    } catch(Exception ex) {
      System.out.println("Call failed because: " + ex.getMessage());
    }
    
    FileResponseBody fileRespBody = (FileResponseBody) httpCall.httpResponse.responseBody;
    
    if(fileRespBody == null)
      throw new Exception("Null file resp body");
    
    if(fileRespBody.getFile() == null)
      throw new Exception("Null file resp body dot file");
    
    System.out.println("Call response code: " +httpCall.httpResponse.responseCode);
    System.out.println("Call log: " +httpCall.getCallLog());
    
    fileRespBody.getFile().renameTo(new File("temp.zip"));
  }
}
