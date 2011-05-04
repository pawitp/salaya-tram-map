package org.dyndns.pawitp.salayatrammap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
	
	private static final int FILE_BUFFER_SIZE = 1024;
	
	public static void writeInputStreamToFile(InputStream is, File file) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
		
		try {
	    	byte[] buffer = new byte[FILE_BUFFER_SIZE];
	    	int length;
	    	while ((length = is.read(buffer)) >0){
	    		bos.write(buffer, 0, length);
	    	}
		}
		finally {
			bos.flush();
			bos.close();
			is.close();
		}
    	
    	
	}
}
