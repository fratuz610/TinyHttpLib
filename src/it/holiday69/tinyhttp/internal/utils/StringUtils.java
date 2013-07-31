/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.holiday69.tinyhttp.internal.utils;

/**
 *
 * @author Stefano Fratini <stefano.fratini@yeahpoint.com>
 */
public class StringUtils {

  public static String byteArrayToHexString(byte[] byteArray) {
    String hexString = "";
    for  (int i = 0; i < byteArray.length; i++) {
     hexString += String.format("%02X%s", byteArray[i], "");
    }
    return hexString;
  }

  public static int stringToInt(String s) {

    String goodStr = "";

    for(int i = 0; i < s.length(); i++) {
      if(s.charAt(i) >= 48 && s.charAt(i) <= 57)
        goodStr += s.charAt(i);
    }

    Integer intVal = new Integer(goodStr);
    return intVal.intValue();

  }

  public static String trimAll(String src) {

    String ret = src.trim();

    while(ret.endsWith("\n"))
      ret = ret.substring(0, src.length()-1);

    while(ret.endsWith("\r"))
      ret = ret.substring(0, src.length()-1);

    return ret;
  }

  public static String join(String glue, String[] s)
  {
    int k=s.length;
    if (k==0)
      return null;
    StringBuilder out=new StringBuilder();
    out.append(s[0]);
    for (int x=1;x<k;++x)
      out.append(glue).append(s[x]);
    return out.toString();
  }
  
  public static boolean isEmpty(String val) {
    if(val == null)
      return true;
    
    if(val.trim().equals(""))
      return true;
    
    return false;
  }
  
  public static boolean hasContent(String val) {
    return !isEmpty(val);
  }
  
  public static String head(String src, String dividerRegex) {
    String[] splitList = src.split(dividerRegex);
    if(splitList.length == 0)
      return src;
    else
      return splitList[0];
  }
  
  public static String tail(String src, String dividerRegex) {
    String[] splitList = src.split(dividerRegex);
    if(splitList.length == 0)
      return src;
    else
      return splitList[splitList.length-1];
  }
  
  public static String humanReadableByteCount(long bytes) {
    return humanReadableByteCount(bytes, false);
  }
  
  public static String humanReadableByteCount(long bytes, boolean si) {
    int unit = si ? 1000 : 1024;
    if (bytes < unit) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }


}
