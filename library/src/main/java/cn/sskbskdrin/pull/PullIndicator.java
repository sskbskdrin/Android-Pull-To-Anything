package cn.sskbskdrin.pull;

import android.graphics.Point;
import android.graphics.PointF;

public class PullIndicator {
	private static final String TAG = "PullIndicator";

	private final static int POS_START = 0;
	private PointF mPtLastMove = new PointF();
	private Point mCurrentPosition = new Point(0, 0);
	private int mOffsetX;
	private int mOffsetY;
	private Point mLastPosition = new Point();
	private int mMaxRange = Integer.MAX_VALUE;

	private float mResistance = 1.5f;
	private boolean isIncreaseResistance = true;

	public float getResistance() {
		int offset = Math.abs(mCurrentPosition.x);
		if (offset == 0)
			offset = Math.abs(mCurrentPosition.y);
		if (offset > mMaxRange)
			offset = mMaxRange;
		float scale = (mMaxRange - offset) * 1f / mMaxRange;
		if (!isIncreaseResistance && scale != 0)
			scale = 1;
		return mResistance / scale;
	}

	public void setResistance(float resistance) {
		if (resistance == 0)
			throw new IllegalArgumentException("resistance Cannot be zero");
		mResistance = resistance;
	}

	public void setPullMaxRange(int range) {
		mMaxRange = range;
	}

	public void onRelease() {
	}

	protected void processOnMove(float offsetX, float offsetY) {
		float resistance = getResistance();
		setOffset(offsetX / resistance, offsetY / resistance);
	}

	public void onPressDown(float x, float y) {
		mPtLastMove.set(x, y);
	}

	public final void onMove(float x, float y) {
		float offsetX = x - mPtLastMove.x;
		float offsetY = y - mPtLastMove.y;
		processOnMove(offsetX, offsetY);
		mPtLastMove.set(x, y);
	}

	private void setOffset(float x, float y) {
		float offX = mCurrentPosition.x + x;
		float offY = mCurrentPosition.y + y;
		if (offX * mCurrentPosition.x < 0)
			x = -mCurrentPosition.x;
		if (offY * mCurrentPosition.y < 0)
			y = -mCurrentPosition.y;
		mOffsetX = (int) x;
		mOffsetY = (int) y;
	}

	public int getOffsetX() {
		return mOffsetX;
	}

	public int getOffsetY() {
		return mOffsetY;
	}

	public int getCurrentX() {
		return mCurrentPosition.x;
	}

	public int getCurrentY() {
		return mCurrentPosition.y;
	}

	/**
	 * Update current position before update the UI
	 */
	public final void offsetPosition(int dx, int dy) {
		mLastPosition.set(mCurrentPosition.x, mCurrentPosition.y);
		mCurrentPosition.offset(dx, dy);
//		Log.d(TAG, "offsetPosition: dx=" + dx + " dy=" + dy + " pos=" + mCurrentPosition);
	}

	public boolean isInStartPosition() {
		return mCurrentPosition.x == POS_START && mCurrentPosition.y == POS_START;
	}

	public void setIncreaseResistance(boolean increaseResistance) {
		isIncreaseResistance = increaseResistance;
	}

	public boolean isIncreaseResistance() {
		return isIncreaseResistance;
	}
}
