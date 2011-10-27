/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.task;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author fratuz610
 */
public class HttpTimeoutTask implements Runnable {
    
  private HttpURLConnection _con;
  private int _timeout;
  private AtomicBoolean _interruptFlag;

  public HttpTimeoutTask(HttpURLConnection con, int timeout, AtomicBoolean interruptFlag) {
      _con = con;
      _timeout = timeout;
      _interruptFlag = interruptFlag;
  }

  @Override
  public void run() {
      try {
        Thread.sleep(_timeout);
        _interruptFlag.set(true);
        _con.disconnect();
      } catch (Throwable e) {  }
  }
}
