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
public class HttpHeaderTest {
  
  public HttpHeaderTest() {
  }

  @Test
  public void testHeaderFromString() {
    
    HttpHeader header = new HttpHeader("hello:world");
    
    assertEquals(header.key, "hello");
    assertEquals(header.value, "world");
    
  }
  
  @Test
  public void testHeaderFromStringSpaces() {
    
    HttpHeader header = new HttpHeader("hello : world");
    
    assertEquals(header.key, "hello");
    assertEquals(header.value, "world");
    
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testWrongString() {
    
    HttpHeader header = new HttpHeader("hello-world");
    
    
  }
}
