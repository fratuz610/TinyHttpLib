/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.shared;

import it.holiday69.tinyhttp.internal.utils.StringUtils;
import java.io.Serializable;

/**
 *
 * @author Stefano Fratini <stefano@holiday69.it>
 */
public class ProxyObject implements Serializable {

  private String host;
  private Integer port;
  private String username;
  private String password;
    
  public ProxyObject(String proxyInfo) {
    
    if(proxyInfo.indexOf(":") == -1)
      throw new IllegalArgumentException("The proxyInfo string can be either: 'username:password@host:port' or 'host:port'");
    
    if(proxyInfo.indexOf(":") == proxyInfo.lastIndexOf(":")) {
      
      username = null;
      password = null;
      host = proxyInfo.substring(0, proxyInfo.indexOf(":"));
      port = Integer.valueOf(proxyInfo.substring(proxyInfo.indexOf(":") +1));
      
    } else {
      
      //username:password@address:port
      
      if(proxyInfo.indexOf("@") == -1)
        throw new IllegalArgumentException("The proxyInfo string can be either: 'username:password@host:port' or 'host:port'");
      
      username = proxyInfo.substring(0, proxyInfo.indexOf(":"));
      password = proxyInfo.substring(proxyInfo.indexOf(":") +1, proxyInfo.indexOf("@"));
      host = proxyInfo.substring(proxyInfo.indexOf("@") + 1, proxyInfo.lastIndexOf(":"));
      port = Integer.valueOf(proxyInfo.substring(proxyInfo.lastIndexOf(":") +1));
      
      if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        throw new IllegalArgumentException("The proxyInfo string can be either: 'username:password@host:port' or 'host:port'");
    }
    
    if(StringUtils.isEmpty(host))
      throw new IllegalArgumentException("No valid host name retrievable from '" +proxyInfo +"'");
      
    if(port == null || port <= 0)
      throw new IllegalArgumentException("No valid port number retrievable from '" +proxyInfo +"'");
  }
  
  public String getHost() { return host; }
  public int getPort() { return port; }
  public String getUsername() { return username; }
  public String getPassword() { return password; }
  
  @Override
  public String toString() { 
    if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
      return host + ":" + port;
    else
      return username + ":" + password + "@" + host + ":" + port;
  }
  
}