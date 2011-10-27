/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp;

import it.holiday69.tinyhttp.enume.ResponseType;
import it.holiday69.tinyhttp.vo.HttpRequest;
import it.holiday69.tinyhttp.vo.HttpResponse;
import it.holiday69.tinyhttp.vo.ProxyObject;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/** The abstract class that represents a generic http call
 * 
 * At the moment only one known concrete implementation 
 * is available: {@link <it.holiday69.tinyhttp.task.HttpCall> HttpCall}
 * 
 * This class implements the {@link <java.util.concurrent.Callable> Callable} 
 * interface for an easy use  * with the Executors framework
 * 
 * @author Stefano Fratini
 */
public abstract class AbstractHttpCall implements Callable<Void> {
  
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
  
  /**
   * The type of response to expect from the server. See {@link <it.holiday69.tinyhttp.enume.ResponseType> ResponseType} 
   * It defaults to ResponseType.TEXT
   */
  public ResponseType responseType = ResponseType.TEXT;
  
  /**
   * A flag indicating whether the http call has completed (with or without errors) or not. Thread-safe.
   */
  public boolean callCompleted = false;
  
  /**
   * The HttpRequest object originating this call
   */
  protected volatile HttpRequest httpRequest = null;
  
  /**
   * The HttpResponse object returned by this call
   */
  public volatile HttpResponse httpResponse = null;
  
   /**
   * The ProxyObject to be used for this call. Set to <code>null</code> for no proxy.
   */
  protected ProxyObject _proxyObj;
  
  
  public AbstractHttpCall(HttpRequest httpReq) throws Exception {
    this(httpReq, null);
  }
  
  public AbstractHttpCall(HttpRequest httpReq, ProxyObject proxyObj) throws Exception {
    httpReq.validate();
    httpRequest = httpReq;
    _proxyObj = proxyObj;
  }
  
  @Override
  public abstract Void call() throws Exception;
    
}
