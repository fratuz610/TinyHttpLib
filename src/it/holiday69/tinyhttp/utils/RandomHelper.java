/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.holiday69.tinyhttp.utils;

/**
 *
 * @author fratuz610
 */
public class RandomHelper {

	private final String RANDOM_DELIMETER_CHAR_LIST = "0123456789";
  private final int RANDOM_DELIMETER_LENGTH = 13;

  public String getRandomDelimiter() {

    StringBuilder delimeter = new StringBuilder();
    while(delimeter.length() < RANDOM_DELIMETER_LENGTH) {
      int randPos = (int) Math.floor(Math.random() * RANDOM_DELIMETER_CHAR_LIST.length());
      delimeter.append(RANDOM_DELIMETER_CHAR_LIST.charAt(randPos));
    }
    return delimeter.toString();
  }
}
