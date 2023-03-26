package com.hackeruso.automation.utils;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

public class StringUtils {
    private final static Logger Log = Logger.getLogger(StringUtils.class.getName());

    private StringUtils() {
    }

    /**
     * get the substring of text from start/end
     *
     * @param text  = the target text
     * @param start = chars from left side of text (beginning)
     * @param end   = chars from the right side of text (ending)
     * @return = the string that left from the text after substring
     */
    public static String getSubString(String text, int start, int end) {
        Log.info(String.format("substring chars from text: " + text + " from start: %d , from end: %d", start, end));
        return text.trim().substring(start, end);
    }

    /**
     * get value of double as string
     *
     * @param d = double number
     * @return = double number as string
     */
    public static String getValueOfDoubleAsString(double d) {
        return String.valueOf(d);
    }

    /**
     * Generate only alphabetic characters with/without combination of letter /numbers
     *
     * @param stringLength = the length  of the string we want
     * @param userLetters  = boolean
     * @param useNumbers   = boolean
     * @return = the string we wanted with the parameters
     */
    public static String getRandomString(int stringLength, boolean userLetters, boolean useNumbers) {
        String generatedString = RandomStringUtils.random(stringLength, userLetters, useNumbers);
        Log.info(String.format("Generate random string of length =[%d] using letters =[%s] and using numbers = [%s]"
                , stringLength, userLetters, useNumbers));
        return generatedString;
    }

    /**
     * Replace some part of string with desire string
     *
     * @param str                   = String to target
     * @param sourceCharToReplace   = the String we want to replace
     * @param destCharToReplaceWith = the string we want to use instead
     * @return = String with the replacement
     */
    public static String replaceChar(String str, String sourceCharToReplace, String destCharToReplaceWith) {
        return str.replace(sourceCharToReplace, destCharToReplaceWith);
    }
}
