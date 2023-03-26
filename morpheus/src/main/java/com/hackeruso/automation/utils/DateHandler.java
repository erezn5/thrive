package com.hackeruso.automation.utils;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateHandler {

    private static SimpleDateFormat sdf;
    private static DateTimeFormatter dtf;

    static {
        setFormat();
    }

    static void setFormat() {
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    }

    /**
     * @return The class Date represents a specific instant in time, with millisecond precision.
     */
    public static Date setTodayDate() throws ParseException {
        String completeDate = setTodayCompleteDateAsString();
        return sdf.parse(completeDate);
    }

    private static String setTodayCompleteDateAsString() {
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static String returnCompleteDateAsString(String dateFormat) {
        dtf = DateTimeFormatter.ofPattern(dateFormat);
        return setTodayCompleteDateAsString();
    }

    public static boolean isDateInDescOrder(Date firstDate, Date secondDate) {
        return firstDate.after(secondDate);
    }

    public static Date getStringAsDate(String datePattern, String targetDate) throws ParseException {
        return new SimpleDateFormat(datePattern).parse(targetDate);
    }

    public static String convertLongDateFormatByPattern(String datePattern, Long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat(datePattern);
        return format.format(date);
    }

    public static String updateDateBy(String simpleDateFormat, String dateToUpdate, int calendarInstance, int byValue) {
        DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(dateFormat.parse(dateToUpdate));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        calendar.add(calendarInstance, byValue);
        return dateFormat.format(calendar.getTime());
    }

    public static String getDateByFormat(String dateFormatPattern) {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat(dateFormatPattern).format(calendar.getTime());
    }

    public static boolean isDateInCorrectDateFormat(String dateToValidate, String datePattern) {
        try {
            getStringAsDate(datePattern, dateToValidate);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
