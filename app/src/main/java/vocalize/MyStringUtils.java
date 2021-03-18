package vocalize;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by AMAGNONI on 22/03/2018.
 */

public class MyStringUtils extends StringUtils {

    public static String rtrim(String s) {
        int i = s.length()-1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }
        return s.substring(0,i+1);
    }

    public static String ltrim(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return s.substring(i);
    }

    public static String ltrim(String s, char token) {
        int i = 0;
        while (i < s.length() && (s.charAt(i)==token)) {
            i++;
        }
        return s.substring(i);
    }

    public static boolean isNullOrEmpty(String sdato){
        if(sdato==null){
            return true;
        } else if(sdato.isEmpty()){
            return true;
        } else if(sdato.equals("")){
            return true;
        }

        return false;
    }

    public static boolean isNumeric(String sdato){
        if(isNullOrEmpty(sdato)) return false;
        return sdato.matches("\\d*");
    }

    public static boolean isBTAddress(String sdato){
        if(isNullOrEmpty(sdato)) return false;
        return sdato.matches("([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])$");
    }
}
