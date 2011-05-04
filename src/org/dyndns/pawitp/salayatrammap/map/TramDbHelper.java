package org.dyndns.pawitp.salayatrammap.map;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.dyndns.pawitp.salayatrammap.R;
import org.dyndns.pawitp.salayatrammap.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TramDbHelper {
	
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME_TH = "name_th";
    public static final String KEY_NAME_EN = "name_en";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_TRAM_GREEN = "tram_green";
    public static final String KEY_TRAM_BLUE = "tram_blue";
    public static final String KEY_TRAM_RED = "tram_red";
    
    private static final String DATABASE_NAME = "tram.db";
    private static final int DATABASE_VERSION = 1;
    
    private SQLiteDatabase mDb;
    private boolean mUpgrading = false;
    
    private final Context mContext;

	public TramDbHelper(Context context) {
		mContext = context;
	}
	
	public void open() {
		if (mDb != null && mDb.isOpen()) {
			return;
		}
		
		mDb = mContext.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
		
		if (mDb.getVersion() != DATABASE_VERSION) {
			if (mUpgrading) {
				// Recursive upgrade!?
				throw new IllegalStateException("Recursive upgrade");
			}
			
			try {
				// Replace database with one from the apk
				mUpgrading = true;
				close();
				
				InputStream is = mContext.getResources().openRawResource(R.raw.database);
				Utils.writeInputStreamToFile(is, mContext.getDatabasePath(DATABASE_NAME));
				
				open();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void close() {
		mDb.close();
		mDb = null;
	}
	
	// limit is dx + dy NOT pythagorus
	public Cursor findNearestStop(int x, int y, int limit) {
		String query = String.format("SELECT _id, name_th, name_en, abs(%d - x) + abs(%d - y) AS d FROM stops WHERE d < %d ORDER BY d ASC LIMIT 1", x, y, limit);
		Cursor cursor = mDb.rawQuery(query, null);
		
		cursor.moveToFirst();
		
		return cursor;
	}
}
