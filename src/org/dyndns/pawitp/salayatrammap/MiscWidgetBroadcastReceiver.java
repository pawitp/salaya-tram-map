package org.dyndns.pawitp.salayatrammap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

// Disableable broadcast receiver to receive broadcasts which only applies while widget is running
public class MiscWidgetBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "MiscWidgetBroadcastReceiver";
	
	public static final String UPDATE_WIDGET_IF_ENABLED_INTENT = "org.dyndns.pawitp.salayatrammap.UPDATE_WIDGET_IF_ENABLED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		Log.v(TAG, action);
		
		if (UPDATE_WIDGET_IF_ENABLED_INTENT.equals(action) || 
			Intent.ACTION_TIME_CHANGED.equals(action)) {
			Intent i = new Intent(TramScheduleWidgetProvider.UPDATE_INTENT);
			context.sendBroadcast(i);
		}
	}

	public static void setEnabled(Context context, boolean enabled) {	
		ComponentName receiver = new ComponentName(context, MiscWidgetBroadcastReceiver.class);
		int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		context.getPackageManager().setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP);
	}
	
}
