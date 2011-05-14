package org.dyndns.pawitp.salayatrammap;

import org.dyndns.pawitp.salayatrammap.schedule.NoMoreTramException;
import org.dyndns.pawitp.salayatrammap.schedule.TramCarSchedule;
import org.dyndns.pawitp.salayatrammap.schedule.TramException;
import org.dyndns.pawitp.salayatrammap.schedule.TramsSchedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class TramScheduleWidgetProvider extends AppWidgetProvider {
	
	private static final String TAG = "TramScheduleWidgetProvider";
	
	public static final String UPDATE_INTENT = "org.dyndns.pawitp.salayatrammap.UPDATE_WIDGET";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.v(TAG, "onUpdate");
		
		updateWidgets(context, appWidgetManager, appWidgetIds); // Ids passed here can be a single id if another widget is added
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive");
		
		if (UPDATE_INTENT.equals(intent.getAction())) {
			Log.v(TAG, "onReceive: UPDATE_INTENT");
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName componentName = new ComponentName(context.getPackageName(), TramScheduleWidgetProvider.class.getName());
			updateWidgets(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName));
		}
		else {		
			super.onReceive(context, intent);
		}
	}
	
	@Override
	public void onEnabled(Context context) {
		MiscWidgetBroadcastReceiver.setEnabled(context, true);
	}

	@Override
	public void onDisabled(Context context) {
		Log.v(TAG, "onDisabled");
		
		MiscWidgetBroadcastReceiver.setEnabled(context, false);
		
		Intent i = new Intent(UPDATE_INTENT);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pi);
	}
	
	private static void updateWidgets(Context context, AppWidgetManager appWidgetManager,
			int[] updateAppWidgetIds) {
		// For launching application
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		views.setOnClickPendingIntent(R.id.widgetLayout, pendingIntent);
		
		// Update
		TramsSchedule tramsSchedule = new TramsSchedule(context);
		tramsSchedule.updateSchedules();
		
		updateTramTime(context, tramsSchedule, views, TramsSchedule.TRAM_GREEN, R.id.txtTramLeftGreen, R.id.txtTramNextGreen);
		updateTramTime(context, tramsSchedule, views, TramsSchedule.TRAM_BLUE, R.id.txtTramLeftBlue, R.id.txtTramNextBlue);
		updateTramTime(context, tramsSchedule, views, TramsSchedule.TRAM_RED, R.id.txtTramLeftRed, R.id.txtTramNextRed);
		
		// Not sure why anyone would want multiple widgets, but..
		final int len = updateAppWidgetIds.length;
		for (int i = 0; i < len; i++) {
			appWidgetManager.updateAppWidget(updateAppWidgetIds[i], views);
		}
		
		// Set alarm
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		long time = System.currentTimeMillis() + tramsSchedule.getNextUpdateTime();
		
		Intent i = new Intent(UPDATE_INTENT);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		
		am.set(AlarmManager.RTC, time, pi);
	}
	
	private static void updateTramTime(Context context, TramsSchedule tramsSchedule,
			RemoteViews views, int tramId, int idLeft, int idNext) {
		TramCarSchedule schedule = tramsSchedule.getSchedule(tramId);
		
		try {
			views.setTextViewText(idNext, Utils.formatTime(context, schedule.getNextTram()));
		} catch (NoMoreTramException e) {
			views.setTextViewText(idNext, context.getString(R.string.no_tram));
		}
		
		try {
			views.setTextViewText(idLeft, Utils.formatTime(context, schedule.getLastTram()));
		} catch (TramException e) {
			views.setTextViewText(idLeft, context.getString(R.string.no_tram));
		}
	}
}
