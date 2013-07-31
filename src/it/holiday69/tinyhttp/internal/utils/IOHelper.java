/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.internal.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author fratuz610
 */
public class IOHelper {

	public final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
	public long copy(InputStream input, OutputStream output) throws IOException {
		return copy(input, output, null, null);
	}
  
  public long copy(InputStream input, OutputStream output, AtomicLong bytesRead, AtomicLong bytesPerSec) throws IOException {
    
    long copyStartTime = System.nanoTime();
    
    // resets counter
    if(bytesRead!=null) bytesRead.set(0);
    
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
        output.write(buffer, 0, n);
        output.flush();
        count += n;
        if(bytesRead!=null) bytesRead.set(count);
              
        long copyTime = System.nanoTime() - copyStartTime;
        long countNormalized = (long) (count * 1e9);
              
        if(bytesPerSec != null) bytesPerSec.set(countNormalized / copyTime);
    }
    return count;
  }
  
  public String readAsString(InputStream input) throws IOException {
    return new String(readAsByteArray(input));
	}
  
  public String readAsString(InputStream input, AtomicLong bytesRead, AtomicLong bytesPerSec) throws IOException {
    return new String(readAsByteArray(input, bytesRead, bytesPerSec));
	}
  
  public byte[] readAsByteArray(InputStream input) throws IOException {
    return readAsByteArray(input, null, null);
	}
  
  public byte[] readAsByteArray(InputStream input, AtomicLong bytesRead, AtomicLong bytesPerSec) throws IOException {
    
    long copyStartTime = System.nanoTime();
    
    if(bytesRead!=null) bytesRead.set(0);
    
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
        output.write(buffer, 0, n);
        count += n;
        if(bytesRead!=null) bytesRead.set(count);
        
        long copyTime = System.nanoTime() - copyStartTime;
        long countNormalized = (long) (count * 1e9);
              
        if(bytesPerSec != null) bytesPerSec.set(countNormalized / copyTime);
    }
    
    return output.toByteArray();
  }
}
