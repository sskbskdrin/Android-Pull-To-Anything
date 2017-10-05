package cn.sskbskdrin.demo.fragment;

import android.support.annotation.CallSuper;
import android.view.ViewGroup;

import cn.sskbskdrin.base.IFragment;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;

/**
 * Created by ayke on 2016/10/19 0019.
 */

public abstract class BaseFragment extends IFragment {

	protected PullLayout mPullLayout;
	protected PullRefreshHolder mPullRefreshHolder;

	@CallSuper
	@Override
	protected void initView() {
		if (mRootView instanceof PullLayout) {
			mPullLayout = (PullLayout) mRootView;
		} else {
			for (int i = 0; i < ((ViewGroup) mRootView).getChildCount(); i++) {
				if (((ViewGroup) mRootView).getChildAt(i) instanceof PullLayout) {
					mPullLayout = (PullLayout) ((ViewGroup) mRootView).getChildAt(i);
					break;
				}
			}
		}
		if (mPullLayout == null) return;
		mPullRefreshHolder = mPullLayout.getPullRefreshHolder();
		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.LEFT, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin(PullLayout.Direction direction) {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshLeft();
						mPullRefreshHolder.refreshComplete(PullLayout.Direction.LEFT);
					}
				}, 1500);
			}
		});
		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.TOP, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin(PullLayout.Direction direction) {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshTop();
						mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
					}
				}, 1500);
			}
		});
		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.RIGHT, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin(PullLayout.Direction direction) {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshRight();
						mPullRefreshHolder.refreshComplete(PullLayout.Direction.RIGHT);
					}
				}, 1500);
			}
		});
		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.BOTTOM, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin(PullLayout.Direction direction) {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshBottom();
						mPullRefreshHolder.refreshComplete(PullLayout.Direction.BOTTOM);
					}
				}, 1500);
			}
		});
	}

	protected void refreshLeft() {
	}

	protected void refreshTop() {
	}

	protected void refreshRight() {
	}

	protected void refreshBottom() {
	}
}
