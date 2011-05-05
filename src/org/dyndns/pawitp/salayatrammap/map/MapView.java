package org.dyndns.pawitp.salayatrammap.map;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Scroller;

public class MapView extends ImageView implements OnClickListener {
	
	private static final String TAG = "MapView";
	
	private static final float DEFAULT_ZOOM = 0.8F;
	private static final float MAX_ZOOM = 1.2F;
	private static final float TRACKBALL_FACTOR = 10F;
	private static final int SEARCH_LIMIT = 50; // Limit for searching nearest stop, see TramDbHelper for more info
	
	private static final String KEY_MATRIX = "matrix";
	
	private boolean mRestored = false;
	private float[] mTmpValues = new float[9];
	private Matrix mMatrix = new Matrix(); // for using in manipulate, don't create a new matrix everytime to reduce GC
	private Scroller mScroller = new Scroller(getContext());
	private Zoomer mZoomer = new Zoomer();
	private ScaleGestureDetector mScaleGestureDetector; // Cannot be instantiated here in order to support pre-froyo
	private GestureDetector mGestureDetector; // Instantiated later because pre-froyo needs a different constructor
	private TramDbHelper mDbHelper = new TramDbHelper(getContext());
	
	public MapView(Context context) {
		super(context);
		init();
	}

	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void init() {
		setFocusable(true);
		requestFocus();
		setClickable(true);
		setOnClickListener(this);
		
		mDbHelper.open();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mGestureDetector = new GestureDetector(getContext(), mGestureDetectorListener, null, true);
			
			mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

				@Override
				public boolean onScale(ScaleGestureDetector detector) {
					mMatrix.set(getImageMatrix());
					
					mMatrix.getValues(mTmpValues);
					if ((mTmpValues[Matrix.MSCALE_X] == MAX_ZOOM && detector.getScaleFactor() > 1) ||
						(mTmpValues[Matrix.MSCALE_X] == findFullscreenScale() && detector.getScaleFactor() < 1) ) {
						return true;
					}
					
					mMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
					checkZoom(mMatrix);
					checkEdges(mMatrix);
					setImageMatrix(mMatrix);
					return true;
				}
				
			});
		}
		else {
			mGestureDetector = new GestureDetector(getContext(), mGestureDetectorListener);
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE); // stupid check
		
		Bundle bundle = (Bundle) state;
		mTmpValues = bundle.getFloatArray(KEY_MATRIX);
		
		// Check zoom, edges later when widths and heights are initialized (onLayout)
		
		mRestored = true;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		super.onSaveInstanceState(); // stupid check
		
		Bundle state = new Bundle();
		getImageMatrix().getValues(mTmpValues);
		state.putFloatArray(KEY_MATRIX, mTmpValues);
		return state;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		// Zoom the map out by default and center it
		// Cannot be done in the constructor because the size of the view is not known yet
		if (!mRestored) {
			mMatrix.set(getImageMatrix());
			float scale = findFullscreenScale();
			mMatrix.setScale(scale, scale);
			
			float width = -(getDrawable().getIntrinsicWidth() * scale - getWidth()) / 2;
			float height = -(getDrawable().getIntrinsicHeight() * scale - getHeight()) / 2;
			mMatrix.postTranslate(width, height);
			
			setImageMatrix(mMatrix);
		}
		else {
			mMatrix.set(getImageMatrix());
			mMatrix.setValues(mTmpValues);
			checkZoom(mMatrix);
			checkEdges(mMatrix);
			setImageMatrix(mMatrix);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		ret = mGestureDetector.onTouchEvent(event); // feed it multi-touch event as well so it knows what to ignore
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			ret |= mScaleGestureDetector.onTouchEvent(event);
		}
		return ret;
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getX() != 0 || event.getY() != 0) {
			getImageMatrix().getValues(mTmpValues);
			mScroller.startScroll((int) mTmpValues[Matrix.MTRANS_X], (int) mTmpValues[Matrix.MTRANS_Y],
								  (int) (-event.getX() * TRACKBALL_FACTOR), (int) (-event.getY() * TRACKBALL_FACTOR));
			invalidate();
			return true;
		}
		else {
			return super.onTrackballEvent(event);
		}
		
	}
	
	@Override
	public void onClick(View v) {
		mMatrix.set(getImageMatrix());
		
		mMatrix.getValues(mTmpValues);
		
		float scale;
		if (mTmpValues[Matrix.MSCALE_X] < DEFAULT_ZOOM - 0.01F /* floating point inaccuracy */) { // scale x == scale y
			scale = DEFAULT_ZOOM;
		}
		else {
			scale = findFullscreenScale();
		}
		
		mZoomer.zoomTo(mTmpValues[Matrix.MSCALE_X], scale, getWidth() / 2, getHeight() / 2);
		
		invalidate();	
	}

	@Override
	public void computeScroll() {		
		if (mScroller.computeScrollOffset()) {
			mMatrix.set(getImageMatrix());
			
			mMatrix.getValues(mTmpValues);
			
			int currX = mScroller.getCurrX();
			int currY = mScroller.getCurrY();
			if ((currX == mScroller.getFinalX() && currX != mScroller.getStartX()) || // Prevent awkward scrolling along the edge
				(currY == mScroller.getFinalY() && currY != mScroller.getStartY())) {
				mScroller.abortAnimation();
			}
			else {
				mMatrix.postTranslate(-mTmpValues[Matrix.MTRANS_X] + currX,
									 -mTmpValues[Matrix.MTRANS_Y] + currY);
				
				checkEdges(mMatrix);
				setImageMatrix(mMatrix);
			}
			invalidate();
		}
		
		if (mZoomer.compute()) {
			mMatrix.set(getImageMatrix());
			
			mMatrix.getValues(mTmpValues);
			
			float scale = mZoomer.getCurrScale() / mTmpValues[Matrix.MSCALE_X];
			mMatrix.postScale(scale, scale, mZoomer.getPivotX(), mZoomer.getPivotY());
			
			checkEdges(mMatrix);
			setImageMatrix(mMatrix);
			
			invalidate();
		}
	}
	
	GestureDetector.SimpleOnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Matrix matrix = new Matrix(getImageMatrix());
			
			matrix.getValues(mTmpValues);
			
			float scale;
			if (mTmpValues[Matrix.MSCALE_X] < DEFAULT_ZOOM - 0.01F /* floating point inaccuracy */) { // scale x == scale y
				scale = DEFAULT_ZOOM;
			}
			else {
				scale = findFullscreenScale();
			}
			
			mZoomer.zoomTo(mTmpValues[Matrix.MSCALE_X], scale, e.getX(), e.getY());
			
			invalidate();
			
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			Matrix matrix = new Matrix(getImageMatrix());
			matrix.postTranslate(-distanceX, -distanceY);
			
			checkEdges(matrix);
			
			setImageMatrix(matrix);
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			getImageMatrix().getValues(mTmpValues);
			
			int minWidth = (int) (-getDrawable().getIntrinsicWidth() * mTmpValues[Matrix.MSCALE_X]) + getWidth();
			int minHeight = (int) (-getDrawable().getIntrinsicHeight() * mTmpValues[Matrix.MSCALE_Y]) + getHeight();
			
			mScroller.fling((int) mTmpValues[Matrix.MTRANS_X], (int) mTmpValues[Matrix.MTRANS_Y],
							(int) velocityX, (int) velocityY, minWidth, 0, minHeight, 0);
			
			invalidate();
			
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			if (!mScroller.isFinished()) { // Abort fling on user touch
				mScroller.abortAnimation();
			}
			return true;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			onSingleTapConfirmed(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			getImageMatrix().getValues(mTmpValues);
			int imageX = (int)((e.getX() - mTmpValues[Matrix.MTRANS_X]) / mTmpValues[Matrix.MSCALE_X]);
			int imageY = (int)((e.getY() - mTmpValues[Matrix.MTRANS_Y]) / mTmpValues[Matrix.MSCALE_Y]);
			
			Log.v(TAG, "Tap/Long press: x: " + imageX + " y: " + imageY);
			
			Cursor cursor = mDbHelper.findNearestStop(imageX, imageY, (int) (SEARCH_LIMIT / mTmpValues[Matrix.MSCALE_X]));
			try {
				if (cursor.getCount() > 0) {
					Log.v(TAG, "Stop: " + cursor.getInt(cursor.getColumnIndex(TramDbHelper.KEY_ROWID)) + " d: " + cursor.getInt(3));
					return true;
				}
				else {
					return false;
				}
			}
			finally {
				cursor.close();
			}
		}
		
	};
	
	private float findFullscreenScale() {
		// Find a scale such that the image fills the view
		float scaleHeight = getHeight() / (float) getDrawable().getIntrinsicHeight();
		float scaleWidth = getWidth() / (float) getDrawable().getIntrinsicWidth();
		return Math.max(scaleHeight, scaleWidth);
	}
	
	private void checkEdges(Matrix matrix) {
		matrix.getValues(mTmpValues);
		
		if (mTmpValues[Matrix.MTRANS_X] > 0) {
			matrix.postTranslate(-mTmpValues[Matrix.MTRANS_X], 0);
		}
		
		float maxWidth = -getDrawable().getIntrinsicWidth() * mTmpValues[Matrix.MSCALE_X] + getWidth();
		if (mTmpValues[Matrix.MTRANS_X] < maxWidth) {
			matrix.postTranslate(maxWidth - mTmpValues[Matrix.MTRANS_X], 0);
		}
		
		if (mTmpValues[Matrix.MTRANS_Y] > 0) {
			matrix.postTranslate(0, -mTmpValues[Matrix.MTRANS_Y]);
		}
		
		float maxHeight = -getDrawable().getIntrinsicHeight() * mTmpValues[Matrix.MSCALE_X] + getHeight();
		if (mTmpValues[Matrix.MTRANS_Y] < maxHeight) {
			matrix.postTranslate(0, maxHeight - mTmpValues[Matrix.MTRANS_Y]);
		}
	}
	
	private void checkZoom(Matrix matrix) {
		matrix.getValues(mTmpValues);
		
		float minScale = findFullscreenScale();
		
		if (mTmpValues[Matrix.MSCALE_X] > MAX_ZOOM) {
			float scale = MAX_ZOOM / mTmpValues[Matrix.MSCALE_X];
			matrix.postScale(scale, scale);
		}
		else if (mTmpValues[Matrix.MSCALE_X] < minScale) {
			float scale = minScale / mTmpValues[Matrix.MSCALE_X];
			matrix.postScale(scale, scale);
		}
	}

}
