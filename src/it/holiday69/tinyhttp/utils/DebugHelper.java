/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.utils;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author fratuz610
 */
public class DebugHelper {

  private final static List<String> _debugList = Collections.synchronizedList(new LinkedList<String>());
	
	public void log(String logString) {
    
    GregorianCalendar cal = new GregorianCalendar();
    
    String stringDay = autopad(cal.get(Calendar.DATE), 2);
    String stringMonth = autopad(cal.get(Calendar.MONTH) + 1, 2);
    String stringYeah = "" + cal.get(Calendar.YEAR);
    String stringHour = autopad(cal.get(Calendar.HOUR_OF_DAY), 2);
    String stringMinutes = autopad(cal.get(Calendar.MINUTE), 2);
    String stringSeconds = autopad(cal.get(Calendar.SECOND), 2);
        
    _debugList.add(stringDay + "-" + stringMonth + "-" + stringYeah + " " + stringHour + ":" + stringMinutes + ":" + stringSeconds + " " + logString);
  }
  
  public void clear() {
    _debugList.clear();
  }
  
  public List<String> getLogList() {
    return new LinkedList<String>(_debugList);
  }
  
  private String autopad(int value, int numDigits) {
    
    String strVal = "" + value;
    while(strVal.length() < numDigits)
      strVal = "0" + strVal;
    
    return strVal;
  }
}
