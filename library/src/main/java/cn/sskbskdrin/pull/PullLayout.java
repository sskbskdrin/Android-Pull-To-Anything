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
			mOrientation = arr.getInt(R.styleable.PullLayout_pull_orientation, VERTICAL);
			mPullIndicator.setResistance(arr.getFloat(R.styleable.PullLayout_pull_resistance, mPullIndicator.getResistance()));
			mPullIndicator.setIncreaseResistance(arr.getBoolean(R.styleable.PullLayout_pull_resistance_increase, mPullIndicator.isIncreaseResistance()));
			mPullRangePercent = arr.getFraction(R.styleable.PullLayout_pull_max_range, 1, 1, mPullRangePercent);
			isPinContent = arr.getBoolean(R.styleable.PullLayout_pull_pin_content, isPinContent);
			mCloseBackTime = arr.getInteger(R.styleable.PullLayout_pull_close_back_time, mCloseBackTime);
			arr.recycle();
		}
	}

	@Override
	protected void onFinishInflate() {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = getChildAt(i);
			LayoutParams lp = (LayoutParams) view.getLayoutParams();
			if (lp.isContent) {
				mContentView = view;
			} else if (lp.direction != PullRefreshHolder.Direction.NONE) {
				views[lp.direction.getValue()] = view;
				if (view instanceof PullUIHandler) {
					getPullRefreshHolder().addUIHandler(lp.direction, (PullUIHandler) view);
				}
			}
		}
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
			((LayoutParams) mContentView.getLayoutParams()).gravity = Gravity.CENTER;
		}
		((LayoutParams) mContentView.getLayoutParams()).isContent = true;
		super.onFinishInflate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int range = isVertical() ? getMeasuredHeight() : getMeasuredWidth();
		mPullIndicator.setPullMaxRange((int) (mPullRangePercent * range));
		final int count = getChildCount();

		int maxHeight = 0;
		int maxWidth = 0;

		// Find rightmost and bottommost child
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
				maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
				maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
			}
		}

		// Account for padding too
		maxWidth += getPaddingLeft() + getPaddingRight();
		maxHeight += getPaddingTop() + getPaddingBottom();

		// Check against our minimum height and width
		maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
		maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

		setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
				resolveSize(maxHeight, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int parentLeft = getPaddingLeft();
		final int parentRight = right - left - getPaddingRight();
		final int parentTop = getPaddingTop();
		final int parentBottom = bottom - top - getPaddingBottom();

		int offsetX = mPullIndicator.getCurrentX();
		int offsetY = mPullIndicator.getCurrentY();
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			LayoutParams lp = (LayoutParams) view.getLayoutParams();
			if (view instanceof PullUIHandler) {
				getPullRefreshHolder().addUIHandler(lp.direction, (PullUIHandler) view);
			} else if (view instanceof PullPositionChangeListener) {
				addPullPositionChangeListener((PullPositionChangeListener) view);
			}
			if (lp.isContent) {
				if (isPinContent) {
					offsetX = 0;
					offsetY = 0;
				}
				int l = parentLeft + lp.leftMargin + offsetX;
				int t = parentTop + lp.topMargin + offsetY;
				int r = parentRight - lp.rightMargin + offsetX;
				int b = parentBottom - lp.bottomMargin + offsetY;
				view.layout(l, t, r, b);
				mContentView = view;
			} else if (lp.direction != PullRefreshHolder.Direction.NONE) {
				int width = view.getMeasuredWidth();
				int height = view.getMeasuredHeight();
				int l = parentLeft + lp.leftMargin;
				int t = parentTop + lp.topMargin;
				int r = parentRight - lp.rightMargin;
				int b = parentBottom - lp.bottomMargin;
				if (lp.direction == PullRefreshHolder.Direction.LEFT) {
					l = l - width + offsetX - lp.rightMargin - lp.leftMargin;
					view.layout(l, t, l + width, t + height);
				}
				if (lp.direction == PullRefreshHolder.Direction.TOP) {
					t = t - height + offsetY - lp.topMargin - lp.bottomMargin;
					view.layout(l, t, l + width, t + height);
				}
				if (lp.direction == PullRefreshHolder.Direction.RIGHT) {
					r = r + offsetX + width + lp.rightMargin + lp.leftMargin;
					view.layout(r - width, t, r, t + height);
				}
				if (lp.direction == PullRefreshHolder.Direction.BOTTOM) {
					b = b + offsetY + height + lp.topMargin + lp.bottomMargin;
					view.layout(l, b - height, l + width, b);
				}
				views[lp.direction.getValue()] = view;
				getPullRefreshHolder().setRefreshThreshold(lp.direction, Math.min(view.getMeasuredHeight(), view.getMeasuredWidth()));
				view.bringToFront();
			} else {
				layoutChildren(view, parentLeft, parentTop, parentRight, parentBottom);
			}
		}
	}

	private void layoutChildren(View child, int parentLeft, int parentTop, int parentRight, int parentBottom) {
		final LayoutParams lp = (LayoutParams) child.getLayoutParams();
		final int width = child.getMeasuredWidth();
		final int height = child.getMeasuredHeight();

		int childLeft = parentLeft;
		int childTop = parentTop;

		final int gravity = lp.gravity;

		if (gravity != -1) {
			final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
			final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

			switch (horizontalGravity) {
				case Gravity.LEFT:
					childLeft = parentLeft + lp.leftMargin;
					break;
				case Gravity.CENTER_HORIZONTAL:
					childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
							lp.leftMargin - lp.rightMargin;
					break;
				case Gravity.RIGHT:
					childLeft = parentRight - width - lp.rightMargin;
					break;
				default:
					childLeft = parentLeft + lp.leftMargin;
			}

			switch (verticalGravity) {
				case Gravity.TOP:
					childTop = parentTop + lp.topMargin;
					break;
				case Gravity.CENTER_VERTICAL:
					childTop = parentTop + (parentBottom - parentTop - height) / 2 +
							lp.topMargin - lp.bottomMargin;
					break;
				case Gravity.BOTTOM:
					childTop = parentBottom - height - lp.bottomMargin;
					break;
				default:
					childTop = parentTop + lp.topMargin;
			}
		}
		child.layout(childLeft, childTop, childLeft + width, childTop + height);
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
		updatePosition(deltaX, deltaY, mPullIndicator.getCurrentX(), mPullIndicator.getCurrentY(), isUnderTouch);
		invalidate();
	}

	PullRefreshHolder.Direction mCurrentDirection = PullRefreshHolder.Direction.NONE;
	View[] views = new View[4];

	private void updatePosition(int dx, int dy, int offsetX, int offsetY, boolean isUnderTouch) {
		PullRefreshHolder.Direction old = mCurrentDirection;
		if (offsetX > 0) {
			mCurrentDirection = PullRefreshHolder.Direction.LEFT;
		} else if (offsetX < 0) {
			mCurrentDirection = PullRefreshHolder.Direction.RIGHT;
		} else if (offsetY > 0) {
			mCurrentDirection = PullRefreshHolder.Direction.TOP;
		} else if (offsetY < 0) {
			mCurrentDirection = PullRefreshHolder.Direction.BOTTOM;
		} else {
			mCurrentDirection = PullRefreshHolder.Direction.NONE;
		}
		PullRefreshHolder.Direction used = mCurrentDirection == PullRefreshHolder.Direction.NONE ? old : mCurrentDirection;
		if (used != PullRefreshHolder.Direction.NONE && views[used.getValue()] != null) {
			views[used.getValue()].offsetLeftAndRight(dx);
			views[used.getValue()].offsetTopAndBottom(dy);
			Log.d(TAG, "movePos: content off=" + offsetY + " left=" + views[used.getValue()].getLeft() + " top=" + views[used.getValue()].getTop());
		}
		for (PullPositionChangeListener listener : mListeners) {
			listener.onUIPositionChange(dx, dy, offsetX, offsetY, isUnderTouch ? 1 : 0);
		}
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
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p != null && p instanceof LayoutParams;
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
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
				mScroller.abortAnimation();
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

	public static class LayoutParams extends MarginLayoutParams {

		public PullRefreshHolder.Direction direction = PullRefreshHolder.Direction.NONE;
		public boolean isContent;

		public int gravity = -1;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			TypedArray arr = c.obtainStyledAttributes(attrs, R.styleable.PullLayout_Layout, 0, 0);
			if (arr != null) {
				isContent = arr.getBoolean(R.styleable.PullLayout_Layout_pull_isContentView, false);
				boolean isLeft = arr.getBoolean(R.styleable.PullLayout_Layout_pull_inParentLeft, false);
				boolean isTop = arr.getBoolean(R.styleable.PullLayout_Layout_pull_inParentTop, false);
				boolean isRight = arr.getBoolean(R.styleable.PullLayout_Layout_pull_inParentRight, false);
				boolean isBottom = arr.getBoolean(R.styleable.PullLayout_Layout_pull_inParentBottom, false);
				if (isLeft) {
					direction = PullRefreshHolder.Direction.LEFT;
				}
				if (isTop) {
					direction = PullRefreshHolder.Direction.TOP;
				}
				if (isRight) {
					direction = PullRefreshHolder.Direction.RIGHT;
				}
				if (isBottom) {
					direction = PullRefreshHolder.Direction.BOTTOM;
				}
				gravity = arr.getInt(R.styleable.PullLayout_Layout_android_layout_gravity, -1);
				arr.recycle();
			}
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

}
