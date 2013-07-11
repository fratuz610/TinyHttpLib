package it.holiday69.tinyhttp;

import it.holiday69.tinyhttp.internal.utils.Base64;
import it.holiday69.tinyhttp.internal.utils.DebugHelper;
import it.holiday69.tinyhttp.internal.utils.ExceptionUtils;
import it.holiday69.tinyhttp.internal.utils.IOHelper;
import it.holiday69.tinyhttp.internal.utils.RandomHelper;
import it.holiday69.tinyhttp.internal.utils.StringUtils;
import it.holiday69.tinyhttp.method.Delete;
import it.holiday69.tinyhttp.method.Get;
import it.holiday69.tinyhttp.method.Post;
import it.holiday69.tinyhttp.method.Put;
import it.holiday69.tinyhttp.request.FileUploadParam;
import it.holiday69.tinyhttp.request.HttpParam;
import it.holiday69.tinyhttp.request.KeyValueParam;
import it.holiday69.tinyhttp.request.RawDataParam;
import it.holiday69.tinyhttp.response.HttpResponse;
import it.holiday69.tinyhttp.shared.HttpHeader;
import it.holiday69.tinyhttp.shared.ProxyObject;
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

public class HttpCall<T extends HttpResponse> implements Callable<T>
{
  private HttpRequest httpRequest;
  private Class<T> classOfT;
  private ProxyObject proxyObj;
  private final String CR_LF = "\r\n";
  protected static final String CONTENT_TYPE_HEADER_KEY = "Content-Type";
  protected static final String CONTENT_LENGTH_HEADER_KEY = "Content-Length";
  public final AtomicLong bytesUploadCount = new AtomicLong(0L);

  public final AtomicLong bytesUploadTotal = new AtomicLong(0L);

  public final AtomicLong bytesUploadSpeed = new AtomicLong(0L);

  public final AtomicLong bytesDownloadCount = new AtomicLong(0L);

  public final AtomicLong bytesDownloadTotal = new AtomicLong(0L);

  public final AtomicLong bytesDownloadSpeed = new AtomicLong(0L);

  private final DebugHelper debug = new DebugHelper();

  public HttpCall(HttpRequest httpRequest, Class<T> classOfT)
  {
    this.httpRequest = httpRequest;
    this.classOfT = classOfT;
  }

  public HttpCall withProxy(String proxyData) {
    if (proxyData != null)
      proxyObj = new ProxyObject(proxyData);
    return this;
  }

  @Override
  public T call()
    throws IOException
  {
    T httpResp = null;
    try {
      httpResp = classOfT.newInstance();
    } catch (Throwable th) {
      throw new RuntimeException("Unable to instanciate " + classOfT.getName() + " because: " + ExceptionUtils.getDisplableExceptionInfo(th));
    }

    debug.clear();

    AtomicBoolean interruptFlag = new AtomicBoolean(false);

    Thread timeoutThread = null;
    try
    {
      URL url = new URL(getFinalURL(httpRequest));

      debug.log("Attempting a " + httpRequest.getMethod().getSimpleName() + " call to " + url.toString());
      HttpURLConnection urlConn;
      if (proxyObj == null) {
        urlConn = (HttpURLConnection)url.openConnection();
        debug.log("No proxy is used");
      } else {
        urlConn = (HttpURLConnection)url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyObj.getHost(), proxyObj.getPort())));

        if ((StringUtils.hasContent(proxyObj.getUsername())) && (StringUtils.hasContent(proxyObj.getPassword()))) {
          debug.log("Using basic auth for user: " + proxyObj.getUsername());
          String encodedAuth = new String(Base64.encodeBytes((proxyObj.getUsername() + ":" + proxyObj.getPassword()).getBytes()));
          urlConn.setRequestProperty("Proxy-Authorization", "Basic " + encodedAuth);
        }

        debug.log("Using proxy: " + proxyObj.getHost() + ":" + proxyObj.getPort());
      }

      urlConn.setRequestMethod(httpRequest.getMethod().getSimpleName().toUpperCase());

      if (httpRequest.getTimeout() > 0) {
        urlConn.setConnectTimeout(httpRequest.getTimeout() * 1000);
        urlConn.setReadTimeout(httpRequest.getTimeout() * 1000);
        debug.log("Using timeout: " + httpRequest.getTimeout() + " sec");
      }

      urlConn.setInstanceFollowRedirects(httpRequest.getFollowRedirects());
      debug.log("Using followRedirects: " + httpRequest.getFollowRedirects());

      urlConn.setUseCaches(false);

      if ((httpRequest.getHeaderList() != null) && (!httpRequest.getHeaderList().isEmpty())) {
        for (HttpHeader httpHeader : httpRequest.getHeaderList()) {
          urlConn.setRequestProperty(httpHeader.key, httpHeader.value);
          debug.log("Sending request header: " + httpHeader.key + " => " + httpHeader.value);
        }

      }

      if (httpRequest.getTimeout() > 0) {
        debug.log("Timeout set to " + httpRequest.getTimeout() + " millisec, starting timeout thread");

        HttpTimeoutTask timeoutTask = new HttpTimeoutTask(urlConn, httpRequest.getTimeout() * 1000, interruptFlag);

        if (httpRequest.getTimeoutThreadFactory() != null)
          timeoutThread = httpRequest.getTimeoutThreadFactory().newThread(timeoutTask);
        else {
          timeoutThread = new Thread(timeoutTask);
        }
        timeoutThread.start();
      }
      else {
        debug.log("No timeout in use");
      }

      if (httpRequest.getTimeout() > 0) {
        urlConn.setReadTimeout(httpRequest.getTimeout() * 1000);
      }
      urlConn.setDoInput(true);

      sendDataOut(urlConn, httpRequest);

      httpResp.withResponseCode(urlConn.getResponseCode());

      debug.log("Retrieving response code: " + httpResp.getResponseCode());

      readResponseHeaders(urlConn, httpResp);

      if (((httpResp.getResponseCode() >= 100) && (httpResp.getResponseCode() <= 199)) || (httpResp.getResponseCode() == 204) || (httpResp.getResponseCode() == 304) || (httpResp.getResponseCode() >= 500))
      {
        debug.log("Skipping response body because of httpResponse code: " + httpResp.getResponseCode());
      }
      else
      {
        readResponseBody(urlConn, httpResp);

        if (interruptFlag.get()) {
          httpResp.withResponseCode(408);
        }
      }

      if ((httpResp.getResponseCode() == 302) && (httpRequest.getFollowRedirects()))
      {
        String locationValue = null;
        for (HttpHeader httpHeader : httpResp.getHttpHeaderList()) {
          debug.log(httpHeader.key + " => " + httpHeader.value);
          if ("location".equalsIgnoreCase(httpHeader.key)) {
            locationValue = httpHeader.value;
          }
        }
        if (locationValue != null)
        {
          HttpRequest redirectReq = new HttpRequest()
                  .withMethod(Get.class)
                  .withURL(locationValue);

          HttpCall<T> redirectCall = new HttpCall(redirectReq, classOfT);

          if (proxyObj != null) {
            redirectCall.withProxy(proxyObj.toString());
          }
          return redirectCall.call();
        }
      }
    }
    catch (MalformedURLException e)
    {
      debug.log("MalformedURLException: " + ExceptionUtils.getFullExceptionInfo(e));
      throw new IOException("MalformedURLException: " + e.getMessage());
    }
    catch (IOException e) {
      if (interruptFlag.get()) {
        throw new IOException("The http call timed out: " + httpRequest.getTimeout() + " sec");
      }
      debug.log("IOException: " + ExceptionUtils.getFullExceptionInfo(e));
      throw new IOException("IOException: " + e.getMessage());
    } catch (Throwable e) {
      debug.log("Generic Exception: " + ExceptionUtils.getFullExceptionInfo(e));
      throw new IOException("Exception: " + e.getMessage());
    }
    finally
    {
      try {
        if (timeoutThread != null)
          timeoutThread.interrupt();
      }
      catch (Throwable th) {
      }
    }
    return httpResp;
  }

  private String getFinalURL(HttpRequest httpRequest) throws Exception
  {
    String finalURL = httpRequest.getURL();

    if ((httpRequest.getMethod() == Get.class) || (httpRequest.getMethod() == Delete.class))
    {
      if ((httpRequest.getParamList() != null) && (!httpRequest.getParamList().isEmpty()))
      {
        List getParamList = new LinkedList();
        for (HttpParam getParam : httpRequest.getParamList())
        {
          if (!(getParam instanceof KeyValueParam)) {
            throw new Exception("Unable to send non key/value pairs in a GET or DELETE call");
          }
          KeyValueParam keyValueParam = (KeyValueParam)getParam;

          getParamList.add(URLEncoder.encode(keyValueParam.key, "UTF-8") + "=" + URLEncoder.encode(keyValueParam.value, "UTF-8"));
        }

        finalURL = finalURL + "?" + StringUtils.join("&", (String[])getParamList.toArray(new String[0]));
      }
    }

    return finalURL;
  }

  private void sendDataOut(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception {
    if ((httpRequest.getMethod() == Post.class) || (httpRequest.getMethod() == Put.class))
    {
      urlConn.setDoOutput(true);
      writePostData(urlConn, httpRequest);
    }
    else
    {
      urlConn.setDoOutput(false);
      debug.log("No output data to send");
    }
  }

  private void writePostData(HttpURLConnection urlConn, HttpRequest httpRequest)
    throws Exception
  {
    urlConn.setDoOutput(true);

    boolean rawDataRequest = false;

    for (HttpParam httpParam : httpRequest.getParamList()) {
      if ((httpParam instanceof RawDataParam)) {
        rawDataRequest = true;
      }
    }
    if (rawDataRequest) {
      sendRawDataRequest(urlConn, httpRequest);
      return;
    }

    boolean pureKeyValueRequest = true;

    for (HttpParam httpParam : httpRequest.getParamList()) {
      if ((httpParam instanceof FileUploadParam)) {
        pureKeyValueRequest = false;
      }
    }
    if (pureKeyValueRequest)
      sendFormUrlEncodedRequest(urlConn, httpRequest);
    else
      sendMultipartFormDataRequest(urlConn, httpRequest);
  }

  private void sendRawDataRequest(HttpURLConnection urlConn, HttpRequest httpRequest)
    throws Exception
  {
    String rawData = null;

    for (HttpParam httpParam : httpRequest.getParamList()) {
      if ((httpParam instanceof RawDataParam)) {
        rawData = ((RawDataParam)httpParam).getRawData();
        break;
      }
    }

    if (rawData == null) {
      throw new Exception("Internal error: sendRawDataRequest didn't find any RawDataParam items");
    }
    debug.log("Sending body with raw data: " + rawData.length() + " bytes");

    bytesUploadTotal.set(rawData.length());

    DataOutputStream output = null;
    try
    {
      output = new DataOutputStream(urlConn.getOutputStream());
      output.writeBytes(rawData);
      output.flush();
    }
    finally {
      if (output != null) output.close();

    }

    bytesUploadTotal.set(rawData.length());

    debug.log("Raw data sent");
  }

  private void sendFormUrlEncodedRequest(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception
  {
    String postData = "";

    if ((httpRequest.getParamList() != null) && (!httpRequest.getParamList().isEmpty()))
    {
      List postParamList = new LinkedList();
      for (HttpParam httpParam : httpRequest.getParamList())
      {
        if (!(httpParam instanceof KeyValueParam)) {
          throw new Exception("Internal Error, unable to process a non Key/Value request param in a www-form-urlencoded request");
        }
        KeyValueParam postParam = (KeyValueParam)httpParam;

        if (postParam.value.length() > 100)
          debug.log("Sending body parameter: " + postParam.key + " => " + postParam.value.substring(0, 100));
        else {
          debug.log("Sending body parameter: " + postParam.key + " => " + postParam.value);
        }
        postParamList.add(URLEncoder.encode(postParam.key, "UTF-8") + "=" + URLEncoder.encode(postParam.value, "UTF-8"));
      }

      postData = StringUtils.join("&", (String[])postParamList.toArray(new String[0]));
    }

    bytesUploadTotal.set(postData.length());

    DataOutputStream output = null;
    try
    {
      output = new DataOutputStream(urlConn.getOutputStream());
      output.writeBytes(postData);
      output.flush();
    }
    finally {
      if (output != null) output.close();
    }

    debug.log("Body parameters sent (if any)");

    bytesUploadCount.set(postData.length());
  }

  private void sendMultipartFormDataRequest(HttpURLConnection urlConn, HttpRequest httpRequest) throws Exception
  {
    String randomDelimiter = new RandomHelper().getRandomDelimiter();

    urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + randomDelimiter);

    List<HttpUploadObject> httpUploadList = new LinkedList<HttpUploadObject>();

    long aggregateContentLength = 0L;

    for (HttpParam httpParam : httpRequest.getParamList())
    {
      HttpUploadObject obj = new HttpUploadObject();

      if ((httpParam instanceof KeyValueParam))
      {
        KeyValueParam keyValueReqItem = (KeyValueParam)httpParam;

        obj.itemToUpload = keyValueReqItem;

        debug.log("Processing form data request item: " + keyValueReqItem.key + " => " + keyValueReqItem.value);

        // creates the message header
        obj.headerMessage = ""; 
        obj.headerMessage += "--" + randomDelimiter + CR_LF; 
        obj.headerMessage += "Content-Disposition: form-data; name=\""+keyValueReqItem.key+"\""+ CR_LF;
        obj.headerMessage += CR_LF;

        aggregateContentLength += obj.headerMessage.length() + keyValueReqItem.value.getBytes().length + "\r\n".length();
      }
      else if ((httpParam instanceof FileUploadParam))
      {
        FileUploadParam uploadParam = (FileUploadParam)httpParam;

        obj.itemToUpload = uploadParam;

        debug.log("Processing FileUpload request item: " + uploadParam.getName());

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(uploadParam.getName());

        if (mimeType == null) mimeType = "application/octet-stream";

        // creates the message header
        obj.headerMessage = ""; 
        obj.headerMessage += "--" + randomDelimiter + CR_LF; 
        obj.headerMessage += "Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""+uploadParam.getName()+"\"" + CR_LF;
        obj.headerMessage += "Content-Type: " + mimeType + CR_LF; 
        obj.headerMessage += "Content-Transfer-Encoding: binary"+ CR_LF; 
        obj.headerMessage += CR_LF;

        aggregateContentLength += obj.headerMessage.length() + uploadParam.getSize() + "\r\n".length();
      }

      httpUploadList.add(obj);

      debug.log("In total we have " + httpUploadList.size() + " mime parts to send");
    }

    String footerMessage = "--" + randomDelimiter + "--";

    aggregateContentLength += footerMessage.length();

    urlConn.setRequestProperty("Content-Length", "" + aggregateContentLength);

    urlConn.setFixedLengthStreamingMode((int)aggregateContentLength);

    debug.log("Sending upload contentLength " + aggregateContentLength + " bytes");

    bytesUploadTotal.set(aggregateContentLength);

    OutputStream output = null;
    try
    {
      for (HttpUploadObject uploadObject : httpUploadList)
      {
        output = urlConn.getOutputStream();

        if ((uploadObject.itemToUpload instanceof FileUploadParam))
        {
          FileUploadParam fileUploadParam = (FileUploadParam)uploadObject.itemToUpload;

          debug.log("Uploading file item " + fileUploadParam.getName());

          output.write(uploadObject.headerMessage.getBytes());

          output.flush();

          bytesUploadCount.addAndGet(uploadObject.headerMessage.length());

          new IOHelper().copy(fileUploadParam.getInputStream(), output, bytesUploadCount, bytesUploadSpeed);

          output.write("\r\n".getBytes());

          output.flush();

          debug.log("File data for " + fileUploadParam.getName() + " uploaded");
        }
        else if ((uploadObject.itemToUpload instanceof KeyValueParam))
        {
          KeyValueParam keyValueParam = (KeyValueParam)uploadObject.itemToUpload;

          debug.log("Uploading form data item " + keyValueParam.key);

          output.write(uploadObject.headerMessage.getBytes());

          output.flush();

          bytesUploadCount.addAndGet(uploadObject.headerMessage.length());

          output.write((keyValueParam.value + "\r\n").getBytes());

          bytesUploadCount.addAndGet((keyValueParam.value + "\r\n").length());

          output.flush();

          debug.log("Form data item " + keyValueParam.key + " uploaded");
        }

      }

      output.write(footerMessage.getBytes());

      bytesUploadCount.addAndGet(footerMessage.getBytes().length);

      debug.log("Multipart upload complete");
    }
    finally
    {
      if (output != null) output.close();
    }
  }

  private void readResponseHeaders(HttpURLConnection urlConn, HttpResponse httpResponse)
  {
    Map<String, List<String>> headerMap = urlConn.getHeaderFields();

    if (headerMap.size() > 0)
      for (String key : headerMap.keySet())
      {
        HttpHeader header = new HttpHeader(key, (String)((List)headerMap.get(key)).get(0));
        httpResponse.getHttpHeaderList().add(header);

        debug.log("Response header: " + header.key + " => " + header.value);

        if ("Content-Type".equalsIgnoreCase(key)) {
          httpResponse.withResponseType(header.value);
        }
        if ("Content-Length".equalsIgnoreCase(key))
          httpResponse.withResponseLength(Long.parseLong(header.value));
      }
  }

  private void readResponseBody(HttpURLConnection urlConn, HttpResponse httpResponse)
    throws Exception
  {
    debug.log("Reading response body");

    bytesDownloadTotal.set(httpResponse.getResponseLength());
    try
    {
      new IOHelper().copy(urlConn.getInputStream(), httpResponse.getOutputStream(), bytesDownloadCount, bytesDownloadSpeed);
    } finally {
      httpResponse.getOutputStream().close();
    }

    debug.log("Response Body read : " + bytesDownloadCount.get() + " bytes");
  }

  public String getCallLog() {
    return StringUtils.join("\n", (String[])debug.getLogList().toArray(new String[0]));
  }

  private static class HttpUploadObject
  {
    private String headerMessage;
    private HttpParam itemToUpload;
  }
}