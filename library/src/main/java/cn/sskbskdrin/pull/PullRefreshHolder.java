package cn.sskbskdrin.pull;

import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sskbskdrin.pull.PullLayout.Direction;

/**
 * Created by sskbskdrin on 2016/九月/17 下午6:02.
 */
public class PullRefreshHolder implements PullPositionChangeListener {
    private static final String TAG = "PullRefreshHolder";
    public final static byte STATUS_NONE = 0;
    public final static byte STATUS_PULL = 1;
    public final static byte STATUS_PREPARE = 2;
    public final static byte STATUS_LOADING = 3;
    public final static byte STATUS_COMPLETE = 4;

    private final static long MIN_REFRESH_TIME = 500;

    private Map<PullLayout.Direction, UIViewHolder> map = new HashMap<>(4);
    private Direction mCurrentDirection;
    private PullLayout mPullLayout;
    private boolean isTouch;

    PullRefreshHolder(PullLayout layout) {
        mPullLayout = layout;
    }

    /**
     * 设置刷新门槛
     *
     * @param direction 方位
     * @param threshold 门槛值，设置一次有效
     */
    public void setRefreshThreshold(Direction direction, int threshold) {
        if (map.containsKey(direction)) {
            map.get(direction).setRefreshThreshold(threshold);
        } else {
            UIViewHolder holder = new UIViewHolder(direction);
            holder.setRefreshThreshold(threshold);
            map.put(direction, holder);
        }
    }

    /**
     * 添加刷新UI回调
     *
     * @param direction 方位
     * @param handler   回调handler
     */
    public void addUIHandler(Direction direction, PullUIHandler handler) {
        if (map.containsKey(direction)) {
            map.get(direction).addUIHandler(handler);
        } else {
            UIViewHolder holder = new UIViewHolder(direction);
            holder.addUIHandler(handler);
            map.put(direction, holder);
        }
    }

    /**
     * 添加开始刷新回调
     *
     * @param direction 方位
     * @param callback  回调callback
     */
    public void addPullRefreshCallback(Direction direction, PullRefreshCallback callback) {
        if (map.containsKey(direction)) {
            map.get(direction).addCallback(callback);
        }
    }

    /**
     * 刷新完成
     *
     * @param direction 方位
     */
    public void refreshComplete(final Direction direction) {
        UIViewHolder holder;
        if (map.containsKey(direction)) {
            holder = map.get(direction);
        } else {
            return;
        }
        long time = System.currentTimeMillis() - holder.mStartTime;
        if (time < MIN_REFRESH_TIME) {
            mPullLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshComplete(direction);
                }
            }, MIN_REFRESH_TIME - time);
            return;
        }
        holder.refreshComplete();
        if (holder.getHandlerHook() != null) {
            holder.getHandlerHook().takeOver();
            return;
        }
        if (mPullLayout.getCurrentDirection() == direction) {
            if (!isTouch) mPullLayout.reset(getResetExtent(direction));
        } else {
            holder.checkStatus(0, 0, false);
        }
    }

    public void autoRefresh(final Direction direction, long delay) {
        mPullLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (map.containsKey(direction)) {
                    int threshold = map.get(direction).mThreshold + 1;
                    if (direction == Direction.BOTTOM || direction == Direction.RIGHT) threshold = -threshold;
                    if (mPullLayout != null) {
                        mPullLayout.reset(threshold);
                        mPullLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onRelease(direction);
                            }
                        }, mPullLayout.getCloseBackTime());
                    }
                }
            }
        }, delay);
    }

    public void autoRefresh(final Direction direction) {
        autoRefresh(direction, 10);
    }

    /**
     * 设置刷新完成时可执行的{@link PullUIHandlerHook}
     *
     * @param hook PullUIHandlerHook
     */
    public void setPullUIHandlerHook(PullUIHandlerHook hook) {
        hook.setResumeAction(new PullUIHandlerHook(hook.getDirection()) {
            @Override
            public void run() {
                if (mPullLayout.getCurrentDirection() == getDirection()) {
                    if (!isTouch) mPullLayout.reset(getResetExtent(getDirection()));
                } else {
                    if (map.containsKey(getDirection())) {
                        map.get(getDirection()).checkStatus(0, 0, false);
                    }
                }
            }
        });
        if (map.containsKey(hook.getDirection())) {
            map.get(hook.getDirection()).setPullUIHook(hook);
        }
    }

    /**
     * 设置刷新模式
     *
     * @param direction      方位
     * @param releaseRefresh true则释放刷新，反之则到达门槛刷新
     */
    public void setReleaseRefresh(Direction direction, boolean releaseRefresh) {
        if (map.containsKey(direction)) {
            map.get(direction).setReleaseRefresh(releaseRefresh);
        } else {
            UIViewHolder holder = new UIViewHolder(direction);
            holder.setReleaseRefresh(releaseRefresh);
            map.put(direction, holder);
        }
    }

    /**
     * 设置刷新时，是否显示view
     *
     * @param direction 方位
     * @param show      true则刷新时显示，反之则不显示
     */
    public void setRefreshShowView(Direction direction, boolean show) {
        if (map.containsKey(direction)) {
            map.get(direction).setRefreshShowView(show);
        } else {
            UIViewHolder holder = new UIViewHolder(direction);
            holder.setRefreshShowView(show);
            map.put(direction, holder);
        }
    }

    int getResetExtent(Direction direction) {
        int result = 0;
        if (map.containsKey(direction)) {
            UIViewHolder handler = map.get(direction);
            result = handler.getResetExtent();
        }
        return result;
    }

    void onDown() {
        isTouch = true;
    }

    void onRelease(Direction direction) {
        isTouch = false;
        if (map.containsKey(direction)) {
            map.get(direction).onTouchUp();
        }
    }

    @Override
    public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {
        //		Log.d(TAG, "onUIPositionChange: offsetX=" + offsetX + " offsetY=" + offsetY);
        Direction current = mPullLayout.getCurrentDirection();
        Direction used = current == Direction.NONE ? mCurrentDirection : current;
        if (map.containsKey(used)) {
            map.get(used).updatePosition(dx, dy, offsetX, offsetY, isTouch);
        }
        mCurrentDirection = current;
    }

    private static class UIViewHolder {
        private long mStartTime;
        private int mStatus = STATUS_NONE;
        private int mOffsetX = 0;
        private int mOffsetY = 0;
        private List<PullUIHandler> mUIHandlers;
        private List<PullRefreshCallback> mCallbacks;
        private PullUIHandlerHook mHandlerHook;
        private int mThreshold = 0;
        private boolean isReleaseRefresh = true;
        private boolean isRefreshShowView = true;

        private Direction mDirection;

        UIViewHolder(Direction direction) {
            mDirection = direction;
        }

        void onTouchUp() {
            if (mStatus == STATUS_PREPARE) {
                mStartTime = System.currentTimeMillis();
                if (mUIHandlers != null) {
                    for (PullUIHandler handler : mUIHandlers) {
                        updateStatus(handler, STATUS_LOADING);
                    }
                }
                if (mCallbacks != null) {
                    for (PullRefreshCallback callback : mCallbacks) {
                        callback.onUIRefreshBegin(mDirection);
                    }
                }
            }
        }

        void refreshComplete() {
            if (mStatus == STATUS_LOADING) {
                if (mUIHandlers != null) {
                    for (PullUIHandler handler : mUIHandlers) {
                        updateStatus(handler, STATUS_COMPLETE);
                    }
                }
            }
        }

        PullUIHandlerHook getHandlerHook() {
            return mHandlerHook;
        }

        void setReleaseRefresh(boolean releaseRefresh) {
            isReleaseRefresh = releaseRefresh;
        }

        void setRefreshShowView(boolean show) {
            isRefreshShowView = show;
        }

        /**
         * 设置刷新距离门槛 只能设置一次，多次设置无效
         *
         * @param threshold 门槛值
         */
        void setRefreshThreshold(int threshold) {
            if (mThreshold == 0) this.mThreshold = threshold;
        }

        void setPullUIHook(PullUIHandlerHook hook) {
            mHandlerHook = hook;
        }

        void addUIHandler(PullUIHandler handler) {
            if (mUIHandlers == null) mUIHandlers = new ArrayList<>();
            if (!mUIHandlers.contains(handler)) mUIHandlers.add(handler);
        }

        void addCallback(PullRefreshCallback callback) {
            if (mCallbacks == null) mCallbacks = new ArrayList<>();
            if (!mCallbacks.contains(callback)) mCallbacks.add(callback);
        }

        void updatePosition(int dx, int dy, int offsetX, int offsetY, boolean touch) {
            mOffsetX = offsetX;
            mOffsetY = offsetY;
            checkStatus(offsetX, offsetY, touch);
            if (mUIHandlers != null) {
                for (PullUIHandler handler : mUIHandlers) {
                    handler.onUIPositionChange(dx, dy, offsetX, offsetY, mStatus);
                }
            }
        }

        private int getResetExtent() {
            int result = 0;
            if (isRefreshShowView) {
                if (mStatus == STATUS_PREPARE) result = mThreshold;
                if (mStatus == STATUS_LOADING) {
                    int offsetX = Math.abs(mOffsetX);
                    int offsetY = Math.abs(mOffsetY);
                    if (isVertical()) {
                        result = Math.min(offsetY, mThreshold);
                    } else {
                        result = Math.min(offsetX, mThreshold);
                    }
                }
                if (mDirection == Direction.RIGHT || mDirection == Direction.BOTTOM) result = -result;
            }
            return result;
        }

        private boolean isVertical() {
            return mDirection == Direction.TOP || mDirection == Direction.BOTTOM;
        }

        private void checkStatus(int offsetX, int offsetY, boolean touch) {
            if (STATUS_LOADING != mStatus) {
                int status = mStatus;
                if (offsetX == 0 && offsetY == 0) {
                    mStatus = STATUS_NONE;
                } else if (mStatus == STATUS_COMPLETE) {
                    mStatus = STATUS_COMPLETE;
                } else if (Math.abs(offsetX) >= mThreshold || Math.abs(offsetY) >= mThreshold) {
                    if (isReleaseRefresh) mStatus = STATUS_PREPARE;
                    else mStatus = STATUS_LOADING;
                } else {
                    mStatus = STATUS_PULL;
                }
                if (status != mStatus) {
                    if (mUIHandlers != null) {
                        for (PullUIHandler handler : mUIHandlers) {
                            updateStatus(handler, mStatus);
                        }
                    }
                    if (mStatus == STATUS_LOADING) {
                        mStartTime = System.currentTimeMillis();
                        if (mCallbacks != null) {
                            for (PullRefreshCallback callback : mCallbacks) {
                                callback.onUIRefreshBegin(mDirection);
                            }
                        }
                    }
                }
            }
        }

        void updateStatus(PullUIHandler handler, int status) {
            mStatus = status;
            switch (mStatus) {
                case STATUS_NONE:
                    handler.onUIReset();
                    break;
                case STATUS_PULL:
                    handler.onUIRefreshPull();
                    break;
                case STATUS_PREPARE:
                    handler.onUIRefreshPrepare();
                    break;
                case STATUS_LOADING:
                    handler.onUIRefreshBegin(mDirection);
                    break;
                case STATUS_COMPLETE:
                    handler.onUIRefreshComplete();
                    break;
            }
            if (handler instanceof View) {
                ((View) handler).requestLayout();
            }
        }
    }
}
