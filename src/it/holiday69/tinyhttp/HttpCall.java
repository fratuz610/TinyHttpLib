/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp;

import it.holiday69.tinyhttp.internal.utils.StringUtils;
import it.holiday69.tinyhttp.internal.utils.IOHelper;
import it.holiday69.tinyhttp.internal.utils.ExceptionUtils;
import it.holiday69.tinyhttp.internal.utils.DebugHelper;
import it.holiday69.tinyhttp.internal.utils.Base64;
import it.holiday69.tinyhttp.internal.utils.RandomHelper;
import it.holiday69.tinyhttp.shared.ProxyObject;
import it.holiday69.tinyhttp.shared.HttpHeader;
import it.holiday69.tinyhttp.response.HttpResponse;
import it.holiday69.tinyhttp.request.FileUploadParam;
import it.holiday69.tinyhttp.request.HttpParam;
import it.holiday69.tinyhttp.request.KeyValueParam;
import it.holiday69.tinyhttp.request.RawDataParam;
import it.holiday69.tinyhttp.method.Delete;
import it.holiday69.tinyhttp.method.Put;
import it.holiday69.tinyhttp.method.Post;
import it.holiday69.tinyhttp.method.Get;
import it.holiday69.tinyhttp.task.HttpTimeoutTask;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author fratuz610
 */
public class HttpCall<T extends HttpResponse> implements Callable<T> {
  
  private HttpRequest httpRequest;
  private Class<T> classOfT;
  private ProxyObject proxyObj;
  
  private final String CR_LF = "\r\n";
  
  /**
   * The static constant corresponding to the Content-Type http header
   */
  protected final static String CONTENT_TYPE_HEADER_KEY = "Content-Type";
  
  /**
   * The static constant corresponding to the Content-Length http header
   */
  protected final static String CONTENT_LENGTH_HEADER_KEY = "Content-Length";
  
  /**
   * The number of bytes uploaded so far. Thread-safe.
   */
  public final AtomicLong bytesUploadCount = new AtomicLong(0);
  
  /**
   * The total number of bytes to be uploaded. Thread-safe.
   */
  public final AtomicLong bytesUploadTotal = new AtomicLong(0);
  
  /**
   * The current average upload speed in bytes/second. Thread-safe.
   */
  public final AtomicLong bytesUploadSpeed = new AtomicLong(0);
  
  /**
   * The number of bytes downloaded so far. Thread-safe.
   */
  public final AtomicLong bytesDownloadCount = new AtomicLong(0);
  
  /**
   * The total number of bytes to be downloaded. If the server uses <a href="http://en.wikipedia.org/wiki/Chunked_transfer_encoding">Chunked transfer encoding</a>
   * this value will remain 0 until the download is completed< Thread-safe.
   */
  public final AtomicLong bytesDownloadTotal = new AtomicLong(0);
  
  /**
   * The current average download speed in bytes/second. Thread-safe.
   */
  public final AtomicLong bytesDownloadSpeed = new AtomicLong(0);
  
  private final DebugHelper debug = new DebugHelper();
  
  
  public HttpCall(HttpRequest httpRequest, Class<T> classOfT) {
    this.httpRequest = httpRequest;
    this.classOfT = classOfT;
  }
  
  public HttpCall withProxy(String proxyData) { 
    if(proxyData != null)
      proxyObj = new ProxyObject(proxyData); 
    return this; 
  }
  
  @Override
  public T call() throws IOException {
    
    // we instanciate the response
    T httpResp = null;
    try {
      httpResp = classOfT.newInstance();
    } catch(Throwable th) {
      throw new RuntimeException("Unable to instanciate " + classOfT.getName() + " because: " + ExceptionUtils.getDisplableExceptionInfo(th));
    } 
    
    // clears the log
    debug.clear();

    AtomicBoolean interruptFlag = new AtomicBoolean(false);

    Thread timeoutThread = null;

    try {

      // creates a connection with the final URL
      URL url = new URL(getFinalURL(httpRequest));

      debug.log("Attempting a " + httpRequest.getMethod().getSimpleName() + " call to " + url.toString());

      HttpURLConnection urlConn;

      if(proxyObj == null) {
        urlConn = (HttpURLConnection) url.openConnection();
        debug.log("No proxy is used");
      } else {
        urlConn = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyObj.getHost(), proxyObj.getPort())));
        
        if(StringUtils.hasContent(proxyObj.getUsername()) && StringUtils.hasContent(proxyObj.getPassword())) {
          debug.log("Using basic auth for user: " + proxyObj.getUsername());
          String encodedAuth = new String(Base64.encodeBytes((proxyObj.getUsername() + ":" + proxyObj.getPassword()).getBytes()));
          urlConn.setRequestProperty("Proxy-Authorization", "Basic " + encodedAuth);
        }
         
        debug.log("Using proxy: " + proxyObj.getHost() + ":" + proxyObj.getPort());
      }

      // setting GET/POST/PUT/DELETE method
      urlConn.setRequestMethod(httpRequest.getMethod().getSimpleName().toUpperCase());

      // Sets the connection timeout
      if(httpRequest.getTimeout() > 0) {
        urlConn.setConnectTimeout(httpRequest.getTimeout() * 1000);
        debug.log("Using timeout: " + httpRequest.getTimeout() + " sec");
      }

      // sets the follow redirects policy
      urlConn.setInstanceFollowRedirects(httpRequest.getFollowRedirects());
      debug.log("Using followRedirects: " + httpRequest.getFollowRedirects());

      // no caching by default
      urlConn.setUseCaches (false);

      // setting custom request headers
      if(httpRequest.getHeaderList() != null && !httpRequest.getHeaderList().isEmpty()) {
        for(HttpHeader httpHeader : httpRequest.getHeaderList()) {
          urlConn.setRequestProperty(httpHeader.key, httpHeader.value);
          debug.log("Sending request header: " + httpHeader.key + " => " + httpHeader.value);
        }
      }

      // starts the timeout thread if necessary
      if(httpRequest.getTimeout() > 0) {
        debug.log("Timeout set to " + httpRequest.getTimeout() + " millisec, starting timeout thread");
        
        HttpTimeoutTask timeoutTask = new HttpTimeoutTask(urlConn, httpRequest.getTimeout()*1000, interruptFlag);
        
        if(httpRequest.getTimeoutThreadFactory() != null)
          timeoutThread = httpRequest.getTimeoutThreadFactory().newThread(timeoutTask);
        else
          timeoutThread = new Thread(timeoutTask);
        
        timeoutThread.start();
        
      } else {
        debug.log("No timeout in use");
      }

      // for sure we always need to get some data back
      if(httpRequest.getTimeout() > 0)
        urlConn.setReadTimeout(httpRequest.getTimeout()*1000);
      
      urlConn.setDoInput(true);

      // we send the data out if necessary (upload and/or parameters)
      sendDataOut(urlConn, httpRequest);

      // we read the response code
      httpResp.withResponseCode(urlConn.getResponseCode());

      debug.log("Retrieving response code: " + httpResp.getResponseCode());

      // we read the response headers
      readResponseHeaders(urlConn, httpResp);
      
      //All 1xx (informational), 204 (no content), and 304 (not modified) responses MUST NOT include a message-body. All other responses do include a message-body, although it MAY be of zero length. 
      
      // we read the response body if any
      if((httpResp.getResponseCode() >= 100 && httpResp.getResponseCode() <= 199) ||
              httpResp.getResponseCode() == 204 ||
              httpResp.getResponseCode() == 304 ||
              httpResp.getResponseCode() >= 500 ) {
        debug.log("Skipping response body because of httpResponse code: " + httpResp.getResponseCode());
        
      } else {
        
        // read the data into a file or into a variable
        readResponseBody(urlConn, httpResp);
        
        // get the response code
        if(interruptFlag.get())
          httpResp.withResponseCode(408);
      }

      // checks if it's a redirect
      if(httpResp.getResponseCode() == 302 && httpRequest.getFollowRedirects()) {

        // retrieve the location response header
        String locationValue = null;
        for(HttpHeader httpHeader : httpResp.getHttpHeaderList()) {
          debug.log(httpHeader.key + " => " + httpHeader.value);
          if("location".equalsIgnoreCase(httpHeader.key))
            locationValue = httpHeader.value; 
        }

        if(locationValue != null) {
          
          HttpRequest redirectReq = new HttpRequest()
                  .withMethod(Get.class)
                  .withURL(locationValue);
          
          HttpCall<T> redirectCall = new HttpCall<T>(redirectReq, classOfT);
          
          if(proxyObj != null)
            redirectCall.withProxy(proxyObj.toString());
          
          return redirectCall.call();
        }

      }

    } catch (MalformedURLException e) {
      debug.log("MalformedURLException: " + ExceptionUtils.getFullExceptionInfo(e));
      throw new IOException("MalformedURLException: " + e.getMessage());
    } catch (IOException e) {

      if(interruptFlag.get())
        throw new IOException("The http call timed out: " + httpRequest.getTimeout() + " sec");

      debug.log("IOException: " + ExceptionUtils.getFullExceptionInfo(e));
      throw new IOException("IOException: " + e.getMessage());
    } catch (Throwable e) {
      debug.log("Generic Exception: " + ExceptionUtils.getFullExceptionInfo(e));
      throw new IOException("Exception: " + e.getMessage());
    } finally {

      // stops the timeout thread
      try { 
        if(timeoutThread != null)
          timeoutThread.interrupt(); 
      } catch(Throwable th) { }

    }
    
    return httpResp;
  }
  
  private String getFinalURL(HttpRequest httpRequest) throws Exception {
      
      String finalURL = httpRequest.getURL();
      
      if(httpRequest.getMethod() == Get.class || httpRequest.getMethod() == Delete.class) {

        if(httpRequest.getParamList() != null && !httpRequest.getParamList().isEmpty()) {
          // compose the URL for the get method
          List<String> getParamList = new LinkedList<String>();
          for(HttpParam getParam: httpRequest.getParamList()) {
            
            if(!(getParam instanceof KeyValueParam))
              throw new Exception("Unable to send non key/value pairs in a GET or DELETE call");
            
            KeyValueParam keyValueParam = (KeyValueParam) getParam;
            
            getParamList.add(URLEncoder.encode(keyValueParam.key, "UTF-8")+"="+URLEncoder.encode(keyValueParam.value, "UTF-8"));
          }

          finalURL += "?" + StringUtils.join("&", getParamList.toArray(new String[0]));
        }
      }
      
      return finalURL;
    }
    
    private void sendDataOut(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
      if(httpRequest.getMethod() == Post.class || httpRequest.getMethod() == Put.class) {
        
        // we write the post data
        urlConn.setDoOutput(true);
        writePostData(urlConn, httpRequest);
      } else {

        // we send get data only
        urlConn.setDoOutput(false);
        debug.log("No output data to send");
      }
    }
    
    private void writePostData(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
      
      // we send the POST data here
      urlConn.setDoOutput(true);
      
      boolean rawDataRequest = false;
      
      for(HttpParam httpParam : httpRequest.getParamList()) {
        if(httpParam instanceof RawDataParam)
          rawDataRequest = true;
      }
      
      if(rawDataRequest) {
        sendRawDataRequest(urlConn, httpRequest);
        return;
      } 
      
      // we determine if the request has only Key/Value parameters
      
      boolean pureKeyValueRequest = true;
      
      for(HttpParam httpParam : httpRequest.getParamList()) {
        if(httpParam instanceof FileUploadParam)
          pureKeyValueRequest = false;
      }
      
      if(pureKeyValueRequest)
        sendFormUrlEncodedRequest(urlConn, httpRequest);
      else
        sendMultipartFormDataRequest(urlConn, httpRequest);
      
    }
    
    private void sendRawDataRequest(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
      
      String rawData = null;
      
      for(HttpParam httpParam : httpRequest.getParamList()) {
        if(httpParam instanceof RawDataParam) {
          rawData = ((RawDataParam) httpParam).getRawData();
          break;
        }
      }
      
      if(rawData == null)
        throw new Exception("Internal error: sendRawDataRequest didn't find any RawDataParam items");
      
      debug.log("Sending body with raw data: " + rawData.length() + " bytes");
      
      // updates for monitoring
      bytesUploadTotal.set(rawData.length());
      
      DataOutputStream output = null;
      try {
        // Send POST data
        output = new DataOutputStream ( urlConn.getOutputStream ());
        output.writeBytes (rawData);
        output.flush ();
      
      } finally {
        if(output != null) output.close();
      }
      
      // updates for monitoring
      bytesUploadTotal.set(rawData.length());
      
      debug.log("Raw data sent");
    }
    
    private void sendFormUrlEncodedRequest(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
      
      String postData = "";

      if(httpRequest.getParamList() != null && !httpRequest.getParamList().isEmpty()) {
        // compose the URL for the get method
        List<String> postParamList = new LinkedList<String>();
        for(HttpParam httpParam: httpRequest.getParamList()) {
          
          if(!(httpParam instanceof KeyValueParam))
            throw new Exception("Internal Error, unable to process a non Key/Value request param in a www-form-urlencoded request");
          
          KeyValueParam postParam = (KeyValueParam) httpParam;
          
          if(postParam.value.length() > 100)
            debug.log("Sending body parameter: " + postParam.key + " => " + postParam.value.substring(0, 100));
          else
            debug.log("Sending body parameter: " + postParam.key + " => " + postParam.value);
          
          postParamList.add(URLEncoder.encode(postParam.key, "UTF-8")+"="+URLEncoder.encode(postParam.value, "UTF-8"));
        }

        postData = StringUtils.join("&", postParamList.toArray(new String[0]));
      }

      // updates for monitoring
      bytesUploadTotal.set(postData.length());

      DataOutputStream output = null;
      try {
        // Send POST data
        output = new DataOutputStream ( urlConn.getOutputStream ());
        output.writeBytes (postData);
        output.flush ();
      
      } finally {
        if(output != null) output.close();
      }
      
      debug.log("Body parameters sent (if any)");
      
      // updates for monitoring
      bytesUploadCount.set(postData.length());
    }
    
    private void sendMultipartFormDataRequest(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
            
      String randomDelimiter = new RandomHelper().getRandomDelimiter();
      
      urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + randomDelimiter); 

      // let's compute content length
      
      List<HttpCall.HttpUploadObject> httpUploadList = new LinkedList<HttpCall.HttpUploadObject>();
      
      long aggregateContentLength = 0;
      
      for(HttpParam httpParam : httpRequest.getParamList()) {
        
        HttpCall.HttpUploadObject obj = new HttpCall.HttpUploadObject();
        
        if(httpParam instanceof KeyValueParam) {
          
          KeyValueParam keyValueReqItem = (KeyValueParam) httpParam;
          
          obj.itemToUpload = keyValueReqItem;
          
          debug.log("Processing form data request item: " + keyValueReqItem.key + " => " + keyValueReqItem.value);
          
          // creates the message header
          obj.headerMessage = ""; 
          obj.headerMessage += "--" + randomDelimiter + CR_LF; 
          obj.headerMessage += "Content-Disposition: form-data; name=\""+keyValueReqItem.key+"\""+ CR_LF;
          obj.headerMessage += CR_LF;
                    
          aggregateContentLength += obj.headerMessage.length() + keyValueReqItem.value.getBytes().length + CR_LF.length();
          
        } else if(httpParam instanceof FileUploadParam) {
          
          FileUploadParam uploadParam = (FileUploadParam) httpParam;
          
          obj.itemToUpload = uploadParam;
          
          debug.log("Processing FileUpload request item: " + uploadParam.getName());
          
          FileNameMap fileNameMap = URLConnection.getFileNameMap();
          String mimeType = fileNameMap.getContentTypeFor(uploadParam.getName());

          if(mimeType == null) mimeType = "application/octet-stream";
          
          // creates the message header
          obj.headerMessage = ""; 
          obj.headerMessage += "--" + randomDelimiter + CR_LF; 
          obj.headerMessage += "Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""+uploadParam.getName()+"\"" + CR_LF;
          obj.headerMessage += "Content-Type: " + mimeType + CR_LF; 
          obj.headerMessage += "Content-Transfer-Encoding: binary"+ CR_LF; 
          obj.headerMessage += CR_LF;

          aggregateContentLength += obj.headerMessage.length() + uploadParam.getSize() + CR_LF.length();
        }
        
        httpUploadList.add(obj);
        
        debug.log("In total we have " + httpUploadList.size() + " mime parts to send");
      }
      
      String footerMessage = "--" + randomDelimiter + "--";
      
      aggregateContentLength += footerMessage.length();
      
      // might not need to specify the content-length when sending chunked data. 
      urlConn.setRequestProperty("Content-Length", "" + aggregateContentLength); 
      
      urlConn.setFixedLengthStreamingMode((int)aggregateContentLength);
      
      debug.log("Sending upload contentLength " + aggregateContentLength + " bytes");
      
      // updates for speed monitoring
      bytesUploadTotal.set(aggregateContentLength);
            
      OutputStream output = null;
      
      try {
      
        for(HttpCall.HttpUploadObject uploadObject : httpUploadList) {
          
          // writes them all out
          output = urlConn.getOutputStream(); 
            
          if(uploadObject.itemToUpload instanceof FileUploadParam) {
            
            FileUploadParam fileUploadParam = (FileUploadParam) uploadObject.itemToUpload;
            
            debug.log("Uploading file item " + fileUploadParam.getName());
            
            // let's write the header
            output.write(uploadObject.headerMessage.getBytes()); 

            output.flush(); 
            
            bytesUploadCount.addAndGet(uploadObject.headerMessage.length());
            
            // let's write the body
            
            new IOHelper().copy(fileUploadParam.getInputStream(), output, bytesUploadCount, bytesUploadSpeed);
            
            output.write(CR_LF.getBytes());
            
            output.flush(); 
            
            debug.log("File data for "+ fileUploadParam.getName() +" uploaded");
            
          } else if(uploadObject.itemToUpload instanceof KeyValueParam) {
            
            KeyValueParam keyValueParam = (KeyValueParam) uploadObject.itemToUpload;
            
            debug.log("Uploading form data item " + keyValueParam.key);
            
            // let's write the header
            output.write(uploadObject.headerMessage.getBytes()); 

            output.flush(); 
            
            bytesUploadCount.addAndGet(uploadObject.headerMessage.length());
            
            // let's write the text value
            output.write((keyValueParam.value + CR_LF).getBytes()); 
            
            bytesUploadCount.addAndGet((keyValueParam.value + CR_LF).length());
            
            output.flush(); 
            
            debug.log("Form data item "+ keyValueParam.key +" uploaded");
          }
          
        }
        
        output.write(footerMessage.getBytes());
        
        // updates for monitoring
        bytesUploadCount.addAndGet(footerMessage.getBytes().length);

        debug.log("Multipart upload complete");
        
      } finally {
        
        if(output != null) output.close();
      }
    }
    
    private static class HttpUploadObject {
      private String headerMessage;
      private HttpParam itemToUpload;
    }
    
    private void readResponseHeaders(HttpURLConnection urlConn, T httpResponse) {
      
      Map<String, List<String>> headerMap = urlConn.getHeaderFields();

      if(headerMap.size() > 0) {
        for(String key : headerMap.keySet()) {
          
          HttpHeader header = new HttpHeader(key, headerMap.get(key).get(0));
          httpResponse.getHttpHeaderList().add(header);
          
          debug.log("Response header: " + header.key + " => " + header.value);
          
          if(CONTENT_TYPE_HEADER_KEY.equalsIgnoreCase(key)) 
            httpResponse.withResponseType(header.value);

          if(CONTENT_LENGTH_HEADER_KEY.equalsIgnoreCase(key)) 
            httpResponse.withResponseLength(Long.parseLong(header.value));
        }
      }
      
    }
    
    private void readResponseBody(HttpURLConnection urlConn, T httpResponse) throws Exception {
      
      debug.log("Reading response body");
      
      // sets for monitoring
      bytesDownloadTotal.set(httpResponse.getResponseLength());
      
      try {
        new IOHelper().copy(urlConn.getInputStream(), httpResponse.getOutputStream(), bytesDownloadCount, bytesDownloadSpeed);
      } finally {
        httpResponse.getOutputStream().close();
      }
      
      debug.log("Response Body read : " + bytesDownloadCount.get() + " bytes");
    }
    
    public String getCallLog() {
      return StringUtils.join("\n", debug.getLogList().toArray(new String[0]));
    }
    
}
