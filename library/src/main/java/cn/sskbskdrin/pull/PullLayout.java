package cn.sskbskdrin.pull;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by sskbskdrin on 2016/九月/17 上午9:14.
 */
public class PullLayout extends ViewGroup {
    private static final String TAG = "PullLayout";
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private View mContentView;
    private Map<Direction, View> views = new HashMap<>(4);
    private Map<Direction, Boolean> mEnables = new HashMap<>(4);
    private Map<Direction, Boolean> mPinContents = new HashMap<>(4);

    private boolean isPinContent = false;
    private float mPullRangePercent = 0.3f;

    private int mCloseBackTime = 500;
    private int mOrientation = VERTICAL;

    private ScrollChecker mScrollChecker;
    private MotionEvent mLastMoveEvent;

    private PullRefreshHolder mPullRefreshHolder;
    private PullIndicator mPullIndicator;

    private List<PullPositionChangeListener> mListeners;

    private boolean mHasSendCancelEvent = false;

    private PullCheckHandler mPullCheckHandler;

    private Stack<Integer> mPointIndex = new Stack<>();

    private Direction mCurrentDirection;

    public enum Direction {
        LEFT, TOP, RIGHT, BOTTOM, NONE
    }

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
            mPullIndicator.setResistance(arr.getFloat(R.styleable.PullLayout_pull_resistance, mPullIndicator
                .getResistance()));
            mPullIndicator.setIncreaseResistance(arr.getBoolean(R.styleable.PullLayout_pull_resistance_increase,
                mPullIndicator.isIncreaseResistance()));
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
            } else if (lp.direction != Direction.NONE) {
                views.put(lp.direction, view);
                if (view instanceof PullUIHandler) {
                    getPullRefreshHolder().addUIHandler(lp.direction, (PullUIHandler) view);
                }
            }
        }
        if (mContentView == null) {
            if (childCount > 0) mContentView = getChildAt(0);
        }
        if (mContentView == null) {
            TextView errorView = new TextView(getContext());
            errorView.setClickable(true);
            errorView.setTextColor(0xffff6600);
            errorView.setGravity(Gravity.CENTER);
            errorView.setTextSize(20);
            errorView.setText("content view is empty!!!");
            mContentView = errorView;
            addView(mContentView);
            ((LayoutParams) mContentView.getLayoutParams()).gravity = Gravity.CENTER;
        }
        ((LayoutParams) mContentView.getLayoutParams()).isContent = true;
        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mPullIndicator.isInStartPosition()) {
            reset(0);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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

        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec));

        int range = isVertical() ? getMeasuredHeight() : getMeasuredWidth();
        mPullIndicator.setPullMaxRange((int) (mPullRangePercent * range));
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
                int tempX = offsetX;
                int tempY = offsetY;
                if (getPinContent(getCurrentDirection())) {
                    tempX = 0;
                    tempY = 0;
                }
                int l = parentLeft + lp.leftMargin + tempX;
                int t = parentTop + lp.topMargin + tempY;
                int r = parentRight - lp.rightMargin + tempX;
                int b = parentBottom - lp.bottomMargin + tempY;
                view.layout(l, t, r, b);
                mContentView = view;
            } else if (lp.direction != Direction.NONE) {
                int width = view.getMeasuredWidth();
                int height = view.getMeasuredHeight();
                int l = parentLeft + lp.leftMargin;
                int t = parentTop + lp.topMargin;
                int r = parentRight - lp.rightMargin;
                int b = parentBottom - lp.bottomMargin;
                if (lp.direction == Direction.LEFT) {
                    l = l - width + offsetX - lp.rightMargin - lp.leftMargin;
                    if (getCurrentDirection() != Direction.LEFT) l -= offsetX;
                    view.layout(l, t, l + width, t + height);
                }
                if (lp.direction == Direction.TOP) {
                    t = t - height + offsetY - lp.topMargin - lp.bottomMargin;
                    if (getCurrentDirection() != Direction.TOP) t -= offsetY;
                    view.layout(l, t, l + width, t + height);
                }
                if (lp.direction == Direction.RIGHT) {
                    r = r + offsetX + width + lp.rightMargin + lp.leftMargin;
                    if (getCurrentDirection() != Direction.RIGHT) r -= offsetX;
                    view.layout(r - width, t, r, t + height);
                }
                if (lp.direction == Direction.BOTTOM) {
                    b = b + offsetY + height + lp.topMargin + lp.bottomMargin;
                    if (getCurrentDirection() != Direction.BOTTOM) b -= offsetY;
                    view.layout(l, b - height, l + width, b);
                }
                views.put(lp.direction, view);
                getPullRefreshHolder().setRefreshThreshold(lp.direction, Math.min(view.getMeasuredHeight(), view
                    .getMeasuredWidth()));
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
                    childLeft = parentLeft + (parentRight - parentLeft - width) / 2 + lp.leftMargin - lp.rightMargin;
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
                    childTop = parentTop + (parentBottom - parentTop - height) / 2 + lp.topMargin - lp.bottomMargin;
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
        reset(offset, mCloseBackTime);
    }

    void reset(int offset, int duration) {
        if (isVertical()) {
            mScrollChecker.tryToScrollTo(0, offset - mPullIndicator.getCurrentY(), duration);
        } else {
            mScrollChecker.tryToScrollTo(offset - mPullIndicator.getCurrentX(), 0, duration);
        }
    }

    private boolean checkCanDoPull(boolean moveRight, boolean moveDown, boolean isVertical) {
        if (mPullCheckHandler != null && mPullIndicator.isInStartPosition()) {
            if (isVertical) {
                if (moveDown) {
                    return mPullCheckHandler.checkCanDoPullTop(this, mContentView) && getEnable(Direction.TOP);
                } else {
                    return mPullCheckHandler.checkCanDoPullBottom(this, mContentView) && getEnable(Direction.BOTTOM);
                }
            } else {
                if (moveRight) {
                    return mPullCheckHandler.checkCanDoPullLeft(this, mContentView) && getEnable(Direction.LEFT);
                } else {
                    return mPullCheckHandler.checkCanDoPullRight(this, mContentView) && getEnable(Direction.LEFT);
                }
            }
        }
        return true;
    }

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
                if (mPullRefreshHolder != null) {
                    mPullRefreshHolder.onDown();
                }
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
                    int offset = Math.abs(offsetX) > Math.abs(offsetY) ? Math.abs(offsetX) : Math.abs(offsetY);
                    if (mPullIndicator.isInStartPosition() && offset < 10) {
                        break;
                    }
                    move(offsetX, offsetY, true);
                    return true;
                } else {
                    if (mPullIndicator.isInStartPosition() && mHasSendCancelEvent) sendDownEvent();
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
                    mPullRefreshHolder.onRelease(getCurrentDirection());
                    reset(mPullRefreshHolder.getResetExtent(getCurrentDirection()));
                } else {
                    reset(0);
                }
                break;
            default:
        }
        return superDispatchTouchEvent(ev);
    }

    private boolean superDispatchTouchEvent(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    private void move(int deltaX, int deltaY, boolean isUnderTouch) {
        if (deltaX == 0 && deltaY == 0) {
            return;
        }

        if (!mHasSendCancelEvent) {
            sendCancelEvent();
        }

        mPullIndicator.offsetPosition(deltaX, deltaY);
        if (!getPinContent(getCurrentDirection())) {
            mContentView.offsetLeftAndRight(deltaX);
            mContentView.offsetTopAndBottom(deltaY);
        }
        updatePosition(deltaX, deltaY, mPullIndicator.getCurrentX(), mPullIndicator.getCurrentY(), isUnderTouch);
        invalidate();
    }

    private void updatePosition(int dx, int dy, int offsetX, int offsetY, boolean isUnderTouch) {
        Direction old = mCurrentDirection;
        if (offsetX > 0) {
            mCurrentDirection = Direction.LEFT;
        } else if (offsetX < 0) {
            mCurrentDirection = Direction.RIGHT;
        } else if (offsetY > 0) {
            mCurrentDirection = Direction.TOP;
        } else if (offsetY < 0) {
            mCurrentDirection = Direction.BOTTOM;
        } else {
            mCurrentDirection = Direction.NONE;
        }
        Direction used = mCurrentDirection == Direction.NONE ? old : mCurrentDirection;
        if (views.containsKey(used)) {
            views.get(used).offsetLeftAndRight(dx);
            views.get(used).offsetTopAndBottom(dy);
        }
        for (PullPositionChangeListener listener : mListeners) {
            listener.onUIPositionChange(dx, dy, offsetX, offsetY, isUnderTouch ? 1 : 0);
        }
    }

    public void setEnable(Direction direction, boolean enable) {
        mEnables.put(direction, enable);
    }

    public boolean getEnable(Direction direction) {
        if (mEnables.containsKey(direction)) {
            return mEnables.get(direction);
        } else {
            mEnables.put(direction, true);
            return true;
        }
    }

    Direction getCurrentDirection() {
        return mCurrentDirection;
    }

    private void sendCancelEvent() {
        if (mLastMoveEvent == null) {
            return;
        }
        mHasSendCancelEvent = true;
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getEventTime(), last.getEventTime() + ViewConfiguration
            .getPressedStateDuration(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        superDispatchTouchEvent(e);
    }

    private void sendDownEvent() {
        mHasSendCancelEvent = false;
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getEventTime() - ViewConfiguration.getPressedStateDuration(), last
            .getEventTime() - ViewConfiguration.getPressedStateDuration(), MotionEvent.ACTION_DOWN, last.getX(), last
            .getY(), last.getMetaState());
        superDispatchTouchEvent(e);
    }

    /**
     * 设置可拉动相对于父view的宽高百分比
     *
     * @param rangePercent 百分比的值
     */
    public void setPullMaxRangePercent(float rangePercent) {
        mPullRangePercent = rangePercent;
        requestLayout();
    }

    /**
     * 设置主要控件是否固定
     *
     * @param pin true则固定
     */
    public void setPinContent(boolean pin) {
        isPinContent = pin;
        mPinContents.clear();
    }

    public void setPinContent(Direction direction, boolean pin) {
        mPinContents.put(direction, pin);
    }

    private boolean getPinContent(Direction direction) {
        if (mPinContents.containsKey(direction)) {
            return mPinContents.get(direction);
        } else {
            return isPinContent;
        }
    }

    /**
     * 添加位置偏移回调
     *
     * @param listener 回调监听
     */
    public void addPullPositionChangeListener(PullPositionChangeListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * 设置可滑动的方向
     *
     * @param orientation VERTICAL与HORIZONTAL
     */
    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public int getCloseBackTime() {
        return mCloseBackTime;
    }

    public void setCloseBackTime(int closeBackTime) {
        this.mCloseBackTime = closeBackTime;
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
        private Handler mHandler;

        public ScrollChecker() {
            mScroller = new Scroller(getContext());
            mHandler = new Handler();
        }

        public void run() {
            if (mScroller.computeScrollOffset()) {
                int deltaX = mScroller.getCurrX() - mLastFlingX;
                int deltaY = mScroller.getCurrY() - mLastFlingY;
                mLastFlingX = mScroller.getCurrX();
                mLastFlingY = mScroller.getCurrY();

                move(deltaX, deltaY, false);
                mHandler.post(this);
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
            if (dx == 0 && dy == 0) return;
            destroy();
            mScroller.startScroll(0, 0, dx, dy, duration);
            mHandler.post(this);
        }
    }

    public static class LayoutParams extends MarginLayoutParams {

        /**
         * 相对父view的位置，为NONE则根据gravity确定位置；否则会在父view的上下左右的位置，且会在父view拖动的时候跟随移动
         */
        public Direction direction = Direction.NONE;
        /**
         * 是否是PullLayout中控制的view。
         */
        public boolean isContent;

        public int gravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray arr = c.obtainStyledAttributes(attrs, R.styleable.PullLayout_Layout, 0, 0);
            if (arr != null) {
                isContent = arr.getBoolean(R.styleable.PullLayout_Layout_pull_isContentView, false);
                int position = arr.getInt(R.styleable.PullLayout_Layout_pull_inParentPosition, 0);
                if (position == 1) {
                    direction = Direction.LEFT;
                } else if (position == 2) {
                    direction = Direction.TOP;
                } else if (position == 3) {
                    direction = Direction.RIGHT;
                } else if (position == 4) {
                    direction = Direction.BOTTOM;
                } else {
                    direction = Direction.NONE;
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
