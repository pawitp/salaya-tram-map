package org.dyndns.pawitp.salayatrammap;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Time;

public class Utils {
	
	private static final String DATE_24HR_FORMAT = "%H:%M";
	private static final String DATE_12HR_FORMAT = "%I:%M %p";
	
	public static String formatTime(Context context, Time time) {
		if (DateFormat.is24HourFormat(context)) {
			return time.format(DATE_24HR_FORMAT);
		}
		else {
			return time.format(DATE_12HR_FORMAT).toUpperCase();
		}
	}
}
