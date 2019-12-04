package com.gitlab.incognitojam.ext2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static final SimpleDateFormat CURRENT_YEAR = new SimpleDateFormat("MMM dd HH:mm");
    private static final SimpleDateFormat ALTERNATE_YEAR = new SimpleDateFormat("MMM dd YYYY");

    /**
     * TODO(docs): write javadoc
     */
    public static String formatDirectoryListingDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final int year = calendar.get(Calendar.YEAR);
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        return (year == currentYear ? CURRENT_YEAR : ALTERNATE_YEAR).format(date);
    }
}
