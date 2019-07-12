package util;
/**
 * Since Java has no utility (that I could find) for converting an integer to a String and 
 * padding it with the appropriate number of 0's, I wrote this class that takes care of that
 * for me.
 */
public class PadString{
  // Pads a number with 0's up to the requested length.
  static public String padNumber(int number, int length){
    return padString("" + number, length, '0');
  }
  // Converts the number to hex, then pads it with 0's up to the requested length.
  static public String padHex(int number, int length){
    return padString(Integer.toHexString(number), length, '0');
  }
  // Pads the requested string to the requested length with the specified character.
  static public String padString(String str, int length, char c){
    while(str.length() < length)
      str = c + str;
    return str;
  }
}
