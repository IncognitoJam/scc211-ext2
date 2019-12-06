package com.gitlab.incognitojam.ext2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility methods relating to dates.
 */
public class DateUtils {
    private static final SimpleDateFormat CURRENT_YEAR = new SimpleDateFormat("MMM dd HH:mm");
    private static final SimpleDateFormat ALTERNATE_YEAR = new SimpleDateFormat("MMM dd YYYY");

    /**
     * Format a given Date object in the UNIX directory listing format.
     * <p>
     * Dates in the current year will be output with the time information,
     * while dates outside the current year will be output with the year
     * number.
     *
     * @param date The date object to format.
     * @return Returns the formatted date string.
     */
    public static String formatDirectoryListingDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final int year = calendar.get(Calendar.YEAR);
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        return (year == currentYear ? CURRENT_YEAR : ALTERNATE_YEAR).format(date);
    }
}
