package org.dyndns.pawitp.salayatrammap;

import java.util.Calendar;

import android.content.Context;
import android.text.format.DateFormat;

public class Utils {
	
	public static String formatTime(Context context, Calendar cal) {
		return DateFormat.getTimeFormat(context).format(cal.getTime());
	}
}
