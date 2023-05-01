package org.orm.util;

public class StringUtil {

    public static String removeLastCharacter(String str){
        if (str == null) {
            return null;
        }
        return str.substring(0,str.length()-1);
    }
}
