package org.dyndns.pawitp.salayatrammap.map;

import android.view.animation.AnimationUtils;

// Inspired by Scroller
public class Zoomer {
	
	private static final int DURATION = 250;
	private static final float DURATION_RECIPROCAL = (float) 1/DURATION;
	
	private boolean mFinished = true;
	private long mStartTime;
	private float mPivotX;
	private float mPivotY;
	
	private float mCurrScale;
	private float mStartScale;
	private float mDestScale;
	private float mDeltaScale;
	
	boolean compute() {
		if (mFinished == true) {
			return false;
		}
		
		int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
		if (timePassed < DURATION) {
			float x = timePassed * DURATION_RECIPROCAL;
			
			mCurrScale = mStartScale + x * mDeltaScale;
		}
		else {
			mCurrScale = mDestScale;
			mFinished = true;
		}
		
		return true;
	}
	
	float getPivotX() {
		return mPivotX;
	}
	
	float getPivotY() {
		return mPivotY;
	}
	
	float getCurrScale() {
		return mCurrScale;
	}
	
	void zoomTo(float startScale, float destScale, float pivotX, float pivotY) {
		mFinished = false;
		mStartTime = AnimationUtils.currentAnimationTimeMillis();
		mStartScale = startScale;
		mDestScale = destScale;
		mDeltaScale = destScale - startScale;
		mPivotX = pivotX;
		mPivotY = pivotY;
	}
}
