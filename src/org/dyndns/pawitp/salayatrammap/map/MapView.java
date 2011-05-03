package org.dyndns.pawitp.salayatrammap.map;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.Scroller;

public class MapView extends ImageView {
	
	private static final float DEFAULT_ZOOM = 0.8F;
	private static final float MAX_ZOOM = 1.2F;
	
	private Scroller mScroller = new Scroller(getContext());
	private Zoomer mZoomer = new Zoomer();
	private ScaleGestureDetector mScaleGestureDetector; // Cannot be instantiated here in order to support pre-froyo
	
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

				@Override
				public boolean onScale(ScaleGestureDetector detector) {
					Matrix matrix = new Matrix(getImageMatrix());
					matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
					checkZoom(matrix);
					checkEdges(matrix);
					setImageMatrix(matrix);
					return true;
				}
				
			});
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getPointerCount() == 1) {
			return mGestureDetector.onTouchEvent(event);
		}
		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			return mScaleGestureDetector.onTouchEvent(event);
		}
		else {
			return false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		// Zoom the map out by default and center it
		// Cannot be done in the constructor because the size of the view is not known yet
		Matrix matrix = new Matrix();
		float scale = findFullscreenScale();
		matrix.setScale(scale, scale);
		
		float width = -(getDrawable().getIntrinsicWidth() * scale - getWidth()) / 2;
		float height = -(getDrawable().getIntrinsicHeight() * scale - getHeight()) / 2;
		matrix.postTranslate(width, height);
		
		setImageMatrix(matrix);
	}

	@Override
	public void computeScroll() {		
		if (mScroller.computeScrollOffset()) {
			Matrix matrix = new Matrix(getImageMatrix());
			
			float[] values = new float[9];
			matrix.getValues(values);
			
			matrix.postTranslate(-values[Matrix.MTRANS_X] + mScroller.getCurrX(),
								 -values[Matrix.MTRANS_Y] + mScroller.getCurrY());
			
			setImageMatrix(matrix);
			
			invalidate();
		}
		
		if (mZoomer.compute()) {
			Matrix matrix = new Matrix(getImageMatrix());
			
			float[] values = new float[9];
			matrix.getValues(values);
			
			float scale = mZoomer.getCurrScale() / values[Matrix.MSCALE_X];
			matrix.postScale(scale, scale, mZoomer.getPivotX(), mZoomer.getPivotY());
			
			checkEdges(matrix);
			setImageMatrix(matrix);
			
			invalidate();
		}
	}
	
	// TODO: Restore state on orientation change
	
	GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Matrix matrix = new Matrix(getImageMatrix());
			
			float[] values = new float[9];
			matrix.getValues(values);
			
			float scale;
			if (values[Matrix.MSCALE_X] < DEFAULT_ZOOM - 0.01F /* floating point inaccuracy */) { // scale x == scale y
				scale = DEFAULT_ZOOM;
			}
			else {
				scale = findFullscreenScale();
			}
			
			mZoomer.zoomTo(values[Matrix.MSCALE_X], scale, e.getX(), e.getY());
			
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
			float[] values = new float[9];
			getImageMatrix().getValues(values);
			
			int minWidth = (int) (-getDrawable().getIntrinsicWidth() * values[Matrix.MSCALE_X]) + getWidth();
			int minHeight = (int) (-getDrawable().getIntrinsicHeight() * values[Matrix.MSCALE_Y]) + getHeight();
			
			mScroller.fling((int) values[Matrix.MTRANS_X], (int) values[Matrix.MTRANS_Y],
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
		
	});
	
	private float findFullscreenScale() {
		// Find a scale such that the image fills the view
		float scaleHeight = getHeight() / (float) getDrawable().getIntrinsicHeight();
		float scaleWidth = getWidth() / (float) getDrawable().getIntrinsicWidth();
		return Math.max(scaleHeight, scaleWidth);
	}
	
	private void checkEdges(Matrix matrix) {
		float[] values = new float[9];
		matrix.getValues(values);
		
		if (values[Matrix.MTRANS_X] > 0) {
			matrix.postTranslate(-values[Matrix.MTRANS_X], 0);
		}
		
		float maxWidth = -getDrawable().getIntrinsicWidth() * values[Matrix.MSCALE_X] + getWidth();
		if (values[Matrix.MTRANS_X] < maxWidth) {
			matrix.postTranslate(maxWidth - values[Matrix.MTRANS_X], 0);
		}
		
		if (values[Matrix.MTRANS_Y] > 0) {
			matrix.postTranslate(0, -values[Matrix.MTRANS_Y]);
		}
		
		float maxHeight = -getDrawable().getIntrinsicHeight() * values[Matrix.MSCALE_X] + getHeight();
		if (values[Matrix.MTRANS_Y] < maxHeight) {
			matrix.postTranslate(0, maxHeight - values[Matrix.MTRANS_Y]);
		}
	}
	
	private void checkZoom(Matrix matrix) {
		float[] values = new float[9];
		matrix.getValues(values);
		
		float minScale = findFullscreenScale();
		
		if (values[Matrix.MSCALE_X] > MAX_ZOOM) {
			float scale = MAX_ZOOM / values[Matrix.MSCALE_X];
			matrix.postScale(scale, scale);
		}
		else if (values[Matrix.MSCALE_X] < minScale) {
			float scale = minScale / values[Matrix.MSCALE_X];
			matrix.postScale(scale, scale);
		}
	}

}
