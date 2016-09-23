package cn.sskbskdrin.pull;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by sskbskdrin on 2016/九月/17 上午9:14.
 */
public class PullLayout extends ViewGroup {
	private static final String TAG = "PullLayout";
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	private int mLeftViewId = 0;
	private int mTopViewId = 0;
	private int mRightViewId = 0;
	private int mBottomViewId = 0;
	private int mContentViewId = 0;
	private View mContentView;

	private boolean isPinContent = false;
	private float mPullRangePercent = 0.6f;
	private int mCloseBackTime = 500;
	private int mOrientation = VERTICAL;

	private ScrollChecker mScrollChecker;
	private MotionEvent mLastMoveEvent;

	private PullRefreshHolder mPullRefreshHolder;
	private PullIndicator mPullIndicator;

	private List<PullPositionChangeListener> mListeners;

	private boolean mHasSendCancelEvent = false;

	private PullCheckHandler mPullCheckHandler;

	public PullLayout(Context context) {
		this(context, null);
	}

	public PullLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PullLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mScrollChecker = new ScrollChecker();
		mPullIndicator = new PullIndicator();
		mPullCheckHandler = new PullDefaultCheckHandler();
		mListeners = new ArrayList<>();
		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PullLayout, 0, 0);
		if (arr != null) {
			mContentViewId = arr.getResourceId(R.styleable.PullLayout_pull_content_id, mContentViewId);
			mLeftViewId = arr.getResourceId(R.styleable.PullLayout_pull_left_id, mLeftViewId);
			mTopViewId = arr.getResourceId(R.styleable.PullLayout_pull_top_id, mTopViewId);
			mRightViewId = arr.getResourceId(R.styleable.PullLayout_pull_right_id, mRightViewId);
			mBottomViewId = arr.getResourceId(R.styleable.PullLayout_pull_bottom_id, mBottomViewId);

			mOrientation = arr.getInt(R.styleable.PullLayout_pull_orientation, VERTICAL);
			mPullIndicator.setResistance(arr.getFloat(R.styleable.PullLayout_pull_resistance, mPullIndicator.getResistance()));
			mPullRangePercent = arr.getFraction(R.styleable.PullLayout_pull_max_range, 1, 1, mPullRangePercent);
			isPinContent = arr.getBoolean(R.styleable.PullLayout_pull_pin_content, isPinContent);
			mCloseBackTime = arr.getInteger(R.styleable.PullLayout_pull_close_back_time, mCloseBackTime);
			arr.recycle();
		}
	}

	@Override
	protected void onFinishInflate() {
		int childCount = getChildCount();
		if (mContentViewId != 0)
			mContentView = findViewById(mContentViewId);
		if (mContentView == null) {
			if (childCount > 0)
				mContentView = getChildAt(0);
		}
		if (mContentView == null) {
			TextView errorView = new TextView(getContext());
			errorView.setClickable(true);
			errorView.setTextColor(0xffff6600);
			errorView.setGravity(Gravity.CENTER);
			errorView.setTextSize(20);
			errorView.setText("The content view in PullLayout is empty. Do you forget to specify its id in xml layout file?");
			mContentView = errorView;
			addView(mContentView);
		}
		if (mLeftViewId != 0) {
			getPullRefreshHolder().setRefreshView(PullRefreshHolder.Direction.LEFT, findViewById(mLeftViewId));
		}
		if (mTopViewId != 0) {
			getPullRefreshHolder().setRefreshView(PullRefreshHolder.Direction.TOP, findViewById(mTopViewId));
		}
		if (mRightViewId != 0) {
			getPullRefreshHolder().setRefreshView(PullRefreshHolder.Direction.RIGHT, findViewById(mRightViewId));
		}
		if (mBottomViewId != 0) {
			getPullRefreshHolder().setRefreshView(PullRefreshHolder.Direction.BOTTOM, findViewById(mBottomViewId));
		}
		super.onFinishInflate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int range = isVertical() ? getMeasuredHeight() : getMeasuredWidth();
		mPullIndicator.setPullMaxRange((int) (mPullRangePercent * range));
		for (int i = 0; i < getChildCount(); i++) {
			measureChildWithMargins(getChildAt(i), widthMeasureSpec, 0, heightMeasureSpec, 0);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (mContentView != null) {
			int offsetX = mPullIndicator.getCurrentX();
			int offsetY = mPullIndicator.getCurrentY();
			if (isPinContent) {
				offsetX = 0;
				offsetY = 0;
			}
			MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
			int l = getPaddingLeft() + lp.leftMargin + offsetX;
			int t = getPaddingTop() + lp.topMargin + offsetY;
			int r = right - left - getPaddingRight() - lp.rightMargin + offsetX;
			int b = bottom - top - getPaddingBottom() - lp.bottomMargin + offsetY;
			mContentView.layout(l, t, r, b);
		}
		if (mPullRefreshHolder != null) {
			mPullRefreshHolder.layout(getPaddingLeft(), getPaddingTop(), right - getPaddingRight(), bottom - getPaddingBottom());
		}
		layoutChildren(left, top, right, bottom);
	}

	private void layoutChildren(int left, int top, int right, int bottom) {
		int paddingTop = getPaddingTop();
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (checkRefreshView(child) || child.equals(mContentView))
				continue;
			MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
			int l = paddingLeft + lp.leftMargin;
			int t = paddingTop + lp.topMargin;
			int r = right - left - paddingRight - lp.rightMargin;
			int b = bottom - top - paddingBottom - lp.bottomMargin;
			child.layout(l, t, r, b);
		}
	}

	private boolean checkRefreshView(View view) {
		return mPullRefreshHolder != null && (view.equals(mPullRefreshHolder.getRefreshView(PullRefreshHolder.Direction.LEFT))
				|| view.equals(mPullRefreshHolder.getRefreshView(PullRefreshHolder.Direction.TOP))
				|| view.equals(mPullRefreshHolder.getRefreshView(PullRefreshHolder.Direction.RIGHT))
				|| view.equals(mPullRefreshHolder.getRefreshView(PullRefreshHolder.Direction.BOTTOM)));
	}

	void reset(int offset) {
		if (isVertical()) {
			mScrollChecker.tryToScrollTo(0, offset - mPullIndicator.getCurrentY(), mCloseBackTime);
		} else {
			mScrollChecker.tryToScrollTo(offset - mPullIndicator.getCurrentX(), 0, mCloseBackTime);
		}
	}

	private boolean checkCanDoPull(boolean moveRight, boolean moveDown, boolean isVertical) {
		if (mPullCheckHandler != null && mPullIndicator.isInStartPosition()) {
			if (isVertical) {
				if (moveDown) {
					return mPullCheckHandler.checkCanDoPullTop(this, mContentView);
				} else {
					return mPullCheckHandler.checkCanDoPullBottom(this, mContentView);
				}
			} else {
				if (moveRight) {
					return mPullCheckHandler.checkCanDoPullLeft(this, mContentView);
				} else {
					return mPullCheckHandler.checkCanDoPullRight(this, mContentView);
				}
			}
		}
		return true;
	}

	private Stack<Integer> mPointIndex = new Stack<>();

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isEnabled() || mContentView == null) {
			return superDispatchTouchEvent(ev);
		}
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				mPointIndex.push(ev.getActionIndex());
				mScrollChecker.destroy();
				mHasSendCancelEvent = false;
				mPullIndicator.onPressDown(ev.getX(ev.getActionIndex()), ev.getY(ev.getActionIndex()));
				superDispatchTouchEvent(ev);
				return true;
			case MotionEvent.ACTION_MOVE:
				mLastMoveEvent = ev;
				mPullIndicator.onMove(ev.getX(mPointIndex.peek()), ev.getY(mPointIndex.peek()));
				int offsetX = mPullIndicator.getOffsetX();
				int offsetY = mPullIndicator.getOffsetY();
				boolean isVertical = isVertical();
				if (isVertical) {
					offsetX = 0;
				} else {
					offsetY = 0;
				}

				if (checkCanDoPull(offsetX > 0, offsetY > 0, isVertical)) {
					movePos(offsetX, offsetY, true);
					return true;
				} else {
					if (mPullIndicator.isInStartPosition() && mHasSendCancelEvent)
						sendDownEvent();
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mPointIndex.pop();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mPointIndex.clear();
				mHasSendCancelEvent = true;
				mPullIndicator.onRelease();
				if (mPullRefreshHolder != null) {
					mPullRefreshHolder.onRelease();
					reset(mPullRefreshHolder.getResetExtent());
				} else {
					reset(0);
				}
				break;
		}
		return superDispatchTouchEvent(ev);
	}

	private boolean superDispatchTouchEvent(MotionEvent e) {
		return super.dispatchTouchEvent(e);
	}

	private void movePos(int deltaX, int deltaY, boolean isUnderTouch) {
		if (deltaX == 0 && deltaY == 0) {
			return;
		}

		if (!mHasSendCancelEvent) {
			sendCancelEvent();
		}

		mPullIndicator.offsetPosition(deltaX, deltaY);
		if (!isPinContent) {
			mContentView.offsetLeftAndRight(deltaX);
			mContentView.offsetTopAndBottom(deltaY);
		}
		for (PullPositionChangeListener listener : mListeners) {
			listener.onUIPositionChange(deltaX, deltaY, mPullIndicator.getCurrentX(), mPullIndicator.getCurrentY(), 0);
		}
//		Log.d(TAG, "movePos: content left=" + mContentView.getLeft() + " top=" + mContentView.getTop());
		invalidate();
	}

	private void sendCancelEvent() {
		if (mLastMoveEvent == null) {
			return;
		}
		mHasSendCancelEvent = true;
		MotionEvent last = mLastMoveEvent;
		MotionEvent e = MotionEvent.obtain(last.getEventTime(), last.getEventTime() + ViewConfiguration.getPressedStateDuration(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
		Log.d(TAG, "sendCancelEvent time=" + e.getEventTime());
		superDispatchTouchEvent(e);
	}

	private void sendDownEvent() {
		mHasSendCancelEvent = false;
		final MotionEvent last = mLastMoveEvent;
		MotionEvent e = MotionEvent.obtain(last.getEventTime() - ViewConfiguration.getPressedStateDuration(), last.getEventTime() - ViewConfiguration.getPressedStateDuration(), MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
		Log.d(TAG, "sendDownEvent  time=" + e.getEventTime());
		superDispatchTouchEvent(e);
	}

	public void setPullMaxRangePercent(float rangePercent) {
		mPullRangePercent = rangePercent;
		requestLayout();
	}

	public void setPinContent(boolean pin) {
		isPinContent = pin;
	}

	public void addPullPositionChangeListener(PullPositionChangeListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}

	public PullRefreshHolder getPullRefreshHolder() {
		if (mPullRefreshHolder == null) {
			mPullRefreshHolder = new PullRefreshHolder(this);
			addPullPositionChangeListener(mPullRefreshHolder);
		}
		return mPullRefreshHolder;
	}

	private boolean isVertical() {
		return mOrientation == VERTICAL;
	}

	@Override
	protected boolean checkLayoutParams(LayoutParams p) {
		return p != null && p instanceof MarginLayoutParams;
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	@Override
	protected LayoutParams generateLayoutParams(LayoutParams p) {
		return new MarginLayoutParams(p);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mScrollChecker != null) {
			mScrollChecker.destroy();
		}
	}

	private class ScrollChecker implements Runnable {

		private int mLastFlingX;
		private int mLastFlingY;
		private Scroller mScroller;

		public ScrollChecker() {
			mScroller = new Scroller(getContext());
		}

		public void run() {
			if (mScroller.computeScrollOffset()) {
				int deltaX = mScroller.getCurrX() - mLastFlingX;
				int deltaY = mScroller.getCurrY() - mLastFlingY;
				mLastFlingX = mScroller.getCurrX();
				mLastFlingY = mScroller.getCurrY();

				movePos(deltaX, deltaY, false);
				post(this);
			} else {
				finish();
			}
		}

		private void finish() {
			reset();
		}

		private void reset() {
			mLastFlingX = 0;
			mLastFlingY = 0;
			removeCallbacks(this);
		}

		private void destroy() {
			reset();
			if (!mScroller.isFinished()) {
				mScroller.forceFinished(true);
			}
		}

		public void tryToScrollTo(int dx, int dy, int duration) {
			Log.d(TAG, "current x=" + mPullIndicator.getCurrentX() + " y=" + mPullIndicator.getCurrentY() + "\ntryToScrollTo: dx=" + dx + " dy=" + dy);
			if (dx == 0 && dy == 0)
				return;
			destroy();
			mScroller.startScroll(0, 0, dx, dy, duration);
			post(this);
		}
	}
}
