package cn.sskbskdrin.demo.fragment;

import android.widget.TextView;

import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class ScrollFragment extends BaseFragment {
	TextView content;

	@Override
	protected int getLayoutId() {
		return R.layout.scroll_layout;
	}

	@Override
	protected void initData() {
		content = $(R.id.scroll_text);
		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.TOP, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						content.setText("I already refresh");
						mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
					}
				}, 2000);
			}
		});
	}
}
