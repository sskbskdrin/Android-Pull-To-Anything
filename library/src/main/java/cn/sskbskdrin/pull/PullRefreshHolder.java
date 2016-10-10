package cn.sskbskdrin.pull;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private Map<Direction, UIViewHolder> map = new HashMap<>(4);

	private Direction mCurrentDirection;

	private PullLayout mPullLayout;

	PullRefreshHolder(PullLayout layout) {
		mPullLayout = layout;
	}

	public enum Direction {
		LEFT(0), TOP(1), RIGHT(2), BOTTOM(3), NONE(-1);
		int mValue;

		Direction(int value) {
			mValue = value;
		}

		public int getValue() {
			return mValue;
		}
	}

	public void setRefreshThreshold(Direction direction, int threshold) {
		if (map.containsKey(direction)) {
			map.get(direction).setRefreshThreshold(threshold);
		} else {
			map.put(direction, new UIViewHolder(direction, threshold));
		}
	}

	public void addUIHandler(Direction direction, PullUIHandler handler) {
		if (map.containsKey(direction)) {
			map.get(direction).addUIHandler(handler);
		} else {
			map.put(direction, new UIViewHolder(direction, handler));
		}
	}

	public void addPullRefreshCallback(Direction direction, PullRefreshCallback callback) {
		if (map.containsKey(direction)) {
			map.get(direction).addCallback(callback);
		}
	}

	public void refreshComplete(Direction direction) {
		if (map.containsKey(direction)) {
			Log.d(TAG, "reset: direction=" + direction);
			UIViewHolder holder = map.get(direction);
			holder.refreshComplete();
			if (holder.getHandlerHook() != null) {
				holder.getHandlerHook().takeOver();
				return;
			}
		}
		if (mCurrentDirection == direction)
			mPullLayout.reset(getResetExtent());
	}

	public void setPullUIHandlerHook(PullUIHandlerHook hook) {
		hook.setResumeAction(new PullUIHandlerHook(hook.getDirection()) {
			@Override
			public void run() {
				if (mCurrentDirection == getDirection())
					mPullLayout.reset(getResetExtent());
			}
		});
		if (map.containsKey(hook.getDirection())) {
			map.get(hook.getDirection()).setPullUIHook(hook);
		}
	}

	protected int getResetExtent() {
		int result = 0;
		if (map.containsKey(mCurrentDirection)) {
			UIViewHolder handler = map.get(mCurrentDirection);
			result = handler.getResetExtent();
		}
		return result;
	}

	protected void onRelease() {
		if (map.containsKey(mCurrentDirection)) {
			map.get(mCurrentDirection).onTouchUp();
		}
	}

	@Override
	public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {
//		Log.d(TAG, "onUIPositionChange: offsetX=" + offsetX + " offsetY=" + offsetY);
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
		if (map.containsKey(used)) {
			map.get(used).updatePosition(dx, dy, offsetX, offsetY);
		}
	}

	private static class UIViewHolder {
		private int mStatus = STATUS_NONE;
		private int mOffsetX = 0;
		private int mOffsetY = 0;
		private List<PullUIHandler> mUIHandlers;
		private List<PullRefreshCallback> mCallbacks;
		private PullUIHandlerHook mHandlerHook;
		private int mThreshold = 0;

		private Direction mDirection;

		public UIViewHolder(Direction direction, PullUIHandler handler) {
			mDirection = direction;
			mCallbacks = new ArrayList<>();
			mUIHandlers = new ArrayList<>();
			addUIHandler(handler);
		}

		public UIViewHolder(Direction direction, int threshold) {
			mDirection = direction;
			mUIHandlers = new ArrayList<>();
			mCallbacks = new ArrayList<>();
			mThreshold = threshold;
		}

		public void onTouchUp() {
			if (mStatus == STATUS_PREPARE) {
				for (PullUIHandler handler : mUIHandlers) {
					updateStatus(handler, STATUS_LOADING);
				}
				if (mCallbacks != null) {
					for (PullRefreshCallback callback : mCallbacks) {
						callback.onUIRefreshBegin();
					}
				}
			}
		}

		public void refreshComplete() {
			if (mStatus == STATUS_LOADING) {
				for (PullUIHandler handler : mUIHandlers) {
					updateStatus(handler, STATUS_COMPLETE);
				}
			}
		}

		protected PullUIHandlerHook getHandlerHook() {
			return mHandlerHook;
		}

		public void setPullUIHook(PullUIHandlerHook hook) {
			mHandlerHook = hook;
		}

		public void addUIHandler(PullUIHandler handler) {
			if (!mUIHandlers.contains(handler))
				mUIHandlers.add(handler);
		}

		public void addCallback(PullRefreshCallback callback) {
			if (mCallbacks == null)
				mCallbacks = new ArrayList<>();
			if (!mCallbacks.contains(callback))
				mCallbacks.add(callback);
		}

		public void updatePosition(int dx, int dy, int offsetX, int offsetY) {
			mOffsetX = offsetX;
			mOffsetY = offsetY;
			checkStatus(offsetX, offsetY);
			for (PullUIHandler handler : mUIHandlers) {
				handler.onUIPositionChange(dx, dy, offsetX, offsetY, mStatus);
			}
		}

		private int getResetExtent() {
			int result = 0;
			int threshold = getRefreshThreshold();
			if (mStatus == STATUS_PREPARE)
				result = threshold;
			if (mStatus == STATUS_LOADING) {
				int offsetX = Math.abs(mOffsetX);
				int offsetY = Math.abs(mOffsetY);
				if (isVertical()) {
					result = Math.min(offsetY, threshold);
				} else {
					result = Math.min(offsetX, threshold);
				}
			}
			if (mDirection == Direction.RIGHT || mDirection == Direction.BOTTOM)
				result = -result;
			return result;
		}

		private boolean isVertical() {
			return mDirection == Direction.TOP || mDirection == Direction.BOTTOM;
		}

		public int getRefreshThreshold() {
			return mThreshold;
		}

		public void setRefreshThreshold(int threshold) {
			if (mThreshold == 0)
				this.mThreshold = threshold;
		}

		private void checkStatus(int offsetX, int offsetY) {
			if (STATUS_LOADING != mStatus) {
				int status = mStatus;
				int extent = getRefreshThreshold();
				if (offsetX == 0 && offsetY == 0) {
					mStatus = STATUS_NONE;
				} else if (Math.abs(offsetX) > extent || Math.abs(offsetY) > extent) {
					mStatus = STATUS_PREPARE;
				} else if (mStatus == STATUS_COMPLETE) {
					mStatus = STATUS_COMPLETE;
				} else {
					mStatus = STATUS_PULL;
				}
				if (status != mStatus) {
					for (PullUIHandler handler : mUIHandlers) {
						updateStatus(handler, mStatus);
					}
					if (mStatus == STATUS_LOADING) {
						if (mCallbacks != null) {
							for (PullRefreshCallback callback : mCallbacks) {
								callback.onUIRefreshBegin();
							}
						}
					}
				}
			}
		}

		public void updateStatus(PullUIHandler handler, int status) {
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
					handler.onUIRefreshBegin();
					break;
				case STATUS_COMPLETE:
					handler.onUIRefreshComplete();
					break;
			}
		}
	}
}
