package cn.sskbskdrin.pull;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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
		LEFT, TOP, RIGHT, BOTTOM, NONE
	}

	public void setRefreshView(Direction direction, View view) {
		Log.d(TAG, "setRefreshView: direction=" + direction);
		if (map.containsKey(direction)) {
			map.get(direction).setView(view);
		} else {
			map.put(direction, new UIViewHolder(direction, view));
		}
	}

	public void setEnable(Direction direction, boolean enable) {
		if (map.containsKey(direction)) {
			map.get(direction).setEnable(enable);
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

	protected void layout(int l, int t, int r, int b) {
		Log.d(TAG, "layout: l=" + l + " t=" + t + " r=" + r + " b=" + b);
		for (Map.Entry<Direction, UIViewHolder> entry : map.entrySet()) {
			UIViewHolder handler = entry.getValue();
			Direction direction = entry.getKey();
			View view = handler.getView();
			if (view != null) {
				int width = view.getMeasuredWidth();
				int height = view.getMeasuredHeight();
				ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
				int left = l + mlp.leftMargin;
				int top = t + mlp.topMargin;
				int right = r - mlp.rightMargin;
				int bottom = b - mlp.bottomMargin;
				if (direction == Direction.LEFT) {
					left = left - width + handler.mOffsetX - mlp.rightMargin - mlp.leftMargin;
					view.layout(left, top, left + width, top + height);
				}
				if (direction == Direction.TOP) {
					top = top - height + handler.mOffsetY - mlp.topMargin - mlp.bottomMargin;
					view.layout(left, top, left + width, top + height);
				}
				if (direction == Direction.RIGHT) {
					right = right + handler.mOffsetX + width + mlp.rightMargin + mlp.leftMargin;
					view.layout(right - width, top, right, top + height);
				}
				if (direction == Direction.BOTTOM) {
					bottom = bottom + handler.mOffsetY + height + mlp.topMargin + mlp.bottomMargin;
					view.layout(left, bottom - height, left + width, bottom);
				}
				view.bringToFront();
				Log.d(TAG, "layout: direction=" + direction + " l=" + left + " t=" + top + " r=" + right + " b=" + bottom);
				Log.d(TAG, "layout: direction=" + direction + " l=" + view.getLeft() + " t=" + view.getTop() + " r=" + view.getRight() + " b=" + view.getBottom());
			}
		}
	}

	protected View getRefreshView(Direction direction) {
		if (map.containsKey(direction)) {
			return map.get(direction).getView();
		}
		return null;
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
		private int mOffsetX = 0;
		private int mOffsetY = 0;
		private boolean isEnable = true;
		private View mView;
		private UIHandlerHolder mHolder;
		private List<UIHandlerHolder> mHandlerHolders;
		private PullUIHandlerHook mHandlerHook;

		private Direction mDirection;

		public UIViewHolder(Direction direction, PullUIHandler handler) {
			mDirection = direction;
			mHandlerHolders = new ArrayList<>();
			addUIHandler(handler);
		}

		public UIViewHolder(Direction direction, View view) {
			mDirection = direction;
			mHandlerHolders = new ArrayList<>();
			setView(view);
		}

		public void onTouchUp() {
			for (UIHandlerHolder holder : mHandlerHolders) {
				if (holder.mStatus == STATUS_PREPARE)
					holder.updateStatus(STATUS_LOADING);
			}
		}

		public void refreshComplete() {
			for (UIHandlerHolder holder : mHandlerHolders) {
				if (holder.mStatus == STATUS_LOADING)
					holder.updateStatus(STATUS_COMPLETE);
			}
		}

		protected PullUIHandlerHook getHandlerHook() {
			return mHandlerHook;
		}

		public void setPullUIHook(PullUIHandlerHook hook) {
			mHandlerHook = hook;
		}

		public void addUIHandler(PullUIHandler handler) {
			UIHandlerHolder holder = new UIHandlerHolder(handler);
			if (!mHandlerHolders.contains(holder)) {
				mHandlerHolders.add(holder);
				if (mHolder == null)
					mHolder = holder;
			}
		}

		public void addCallback(PullRefreshCallback callback) {
			if (mHolder != null)
				mHolder.addCallback(callback);
		}

		public void setEnable(boolean enable) {
			isEnable = enable;
		}

		public void setView(View view) {
			mView = view;
			if (view instanceof PullUIHandler) {
				addUIHandler((PullUIHandler) view);
				mHolder = mHandlerHolders.get(0);
			}
		}

		public View getView() {
			return mView;
		}

		public void updatePosition(int dx, int dy, int offsetX, int offsetY) {
			if (!isEnable)
				return;
			mOffsetX = offsetX;
			mOffsetY = offsetY;
			if (mView != null) {
				mView.offsetLeftAndRight(dx);
				mView.offsetTopAndBottom(dy);
				Log.d(TAG,"update;top="+mView.getTop());
			}
			for (UIHandlerHolder holder : mHandlerHolders) {
				holder.updatePosition(dx, dy, offsetX, offsetY);
			}
		}

		private int getResetExtent() {
			int result = 0;
			int extent = getRefreshExtent();
			if (mHolder != null) {
				if (mHolder.mStatus == STATUS_PREPARE)
					result = extent;
				if (mHolder.mStatus == STATUS_LOADING) {
					if (isVertical()) {
						result = Math.abs(mOffsetY) < extent ? mOffsetY : extent;
					} else {
						result = Math.abs(mOffsetX) < extent ? mOffsetX : extent;
					}
				}
				if (mHolder.mStatus == STATUS_COMPLETE)
					result = Math.abs(result);
				if (mDirection == Direction.RIGHT || mDirection == Direction.BOTTOM)
					result = -result;
			}
			return result;
		}

		private int getRefreshExtent() {
			int extent = 0;
			if (mHolder != null) {
				extent = mHolder.mHandler.getRefreshExtent();
			} else {
				if (mView != null) {
					ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
					if (isVertical()) {
						extent = mView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
					} else {
						extent = mView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
					}
				}
			}
			return extent;
		}

		private boolean isVertical() {
			return mDirection == Direction.TOP || mDirection == Direction.BOTTOM;
		}
	}

	private static class UIHandlerHolder {
		private int mStatus = STATUS_NONE;
		private PullUIHandler mHandler;
		private List<PullRefreshCallback> mCallbacks;

		public UIHandlerHolder(PullUIHandler handler) {
			mHandler = handler;
		}

		public void addCallback(PullRefreshCallback callback) {
			if (mCallbacks == null)
				mCallbacks = new ArrayList<>();
			if (!mCallbacks.contains(callback))
				mCallbacks.add(callback);
		}

		public void updatePosition(int dx, int dy, int offsetX, int offsetY) {
			checkStatus(offsetX, offsetY);
			mHandler.onUIPositionChange(dx, dy, offsetX, offsetY, mStatus);
		}

		private void checkStatus(int offsetX, int offsetY) {
			if (STATUS_LOADING != mStatus) {
				int status = mStatus;
				int extent = mHandler.getRefreshExtent();
				if (offsetX == 0 && offsetY == 0) {
					mStatus = STATUS_NONE;
				} else if (Math.abs(offsetX) > extent || Math.abs(offsetY) > extent) {
					mStatus = STATUS_PREPARE;
				} else if (mStatus != STATUS_COMPLETE) {
					mStatus = STATUS_PULL;
				}
				if (status != mStatus) {
					updateStatus(mStatus);
				}
			}
		}

		public void updateStatus(int status) {
//			Log.d(TAG, "updateStatus: status=" + status);
			mStatus = status;
			switch (mStatus) {
				case STATUS_NONE:
					mHandler.onUIReset();
					break;
				case STATUS_PULL:
					mHandler.onUIRefreshPull();
					break;
				case STATUS_PREPARE:
					mHandler.onUIRefreshPrepare();
					break;
				case STATUS_LOADING:
					mHandler.onUIRefreshBegin();
					if (mCallbacks != null) {
						for (PullRefreshCallback callback : mCallbacks) {
							callback.onUIRefreshBegin();
						}
					}
					break;
				case STATUS_COMPLETE:
					mHandler.onUIRefreshComplete();
					break;
			}
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof UIHandlerHolder) {
				return mHandler.equals(((UIHandlerHolder) o).mHandler);
			}
			return super.equals(o);
		}
	}


}
