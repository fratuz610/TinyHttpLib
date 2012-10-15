/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.shared;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fratuz610
 */
public class ProxyObjectTest {
  
  public ProxyObjectTest() {
  }

  /**
   * Test of getHost method, of class ProxyObject.
   */
  @Test
  public void testNoUsernameAndPassword() {
    System.out.println("testNoUsernameAndPassword");
    
    ProxyObject proxyObj = new ProxyObject("127.0.0.1:3128");
    
    assertEquals(proxyObj.getHost(), "127.0.0.1");
    assertEquals(proxyObj.getPort(), 3128);
    
  }

  /**
   * Test of getPort method, of class ProxyObject.
   */
  @Test
  public void testUsernameAndPassword() {
    System.out.println("testUsernameAndPassword");
    
    ProxyObject proxyObj = new ProxyObject("user:pass@127.0.0.1:3128");
    
    assertEquals(proxyObj.getHost(), "127.0.0.1");
    assertEquals(proxyObj.getPort(), 3128);
    assertEquals(proxyObj.getUsername(), "user");
    assertEquals(proxyObj.getPassword(), "pass");
    
  }

  @Test()
  public void testToString() {
    System.out.println("testToString");
    
    ProxyObject proxyObj = new ProxyObject("user:pass@127.0.0.1:3128");
    assertEquals(proxyObj.toString(), "user:pass@127.0.0.1:3128");
    
    proxyObj = new ProxyObject("127.0.0.1:3128");
    assertEquals(proxyObj.toString(), "127.0.0.1:3128");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalString1() {
    System.out.println("testIllegalString1");
    
    new ProxyObject("127.0.0.1");
    
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalString2() {
    System.out.println("testIllegalString2");
    
    new ProxyObject("127.0.0.1:aaa");
    
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalString3() {
    System.out.println("testIllegalString3");
    
    new ProxyObject("AAA:@127.0.0.1:3128");
    
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testIllegalString4() {
    System.out.println("testIllegalString4");
    
    new ProxyObject(":aaa@127.0.0.1:3128");
    
  }
  
  
  
  
}
