package com.walterade.callcrusher.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Walter on 11/26/16.
 */

public class DateUtils {

    public static final String FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss a";

    public static String format(long date) {
        Date dt = new Date(date);
        SimpleDateFormat df2 = new SimpleDateFormat(FORMAT_DEFAULT, Locale.US);
        String dateText = df2.format(dt);
        return dateText;
    }

    public static String format(String format, long date) {
        Date dt = new Date(date);
        SimpleDateFormat df2 = new SimpleDateFormat(format, Locale.US);
        String dateText = df2.format(dt);
        return dateText;
    }

    public static String getDuration(String date) {
        return getDuration(FORMAT_DEFAULT, date);
    }

    public static String getDuration(String format, String date) {
        // ex. format: yyyy-MM-dd HH:mm:ss
        SimpleDateFormat f = new SimpleDateFormat(format, Locale.US);
        try {
            Date now = new Date();
            Date dt = f.parse(date);

            long dif = now.getTime() - dt.getTime();
            long s = dif /= 1000;
            long m = dif /= 60;
            long h = dif /= 60;
            long d = dif /= 24;

            if (d > 0) return HelperUtils.plural("# day", (int) d) + " ago";
            else {
                if (h > 0) return HelperUtils.plural("# hour", (int) h) + " ago";
                else if (m > 0) return HelperUtils.plural("# min", (int) m) + " ago";
                else if (s > 0) return HelperUtils.plural("# sec", (int) s) + " ago";
                else return "just now";
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
