package cn.sskbskdrin.demo.fragment;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class FrameFragment extends BaseFragment {
	TextView content;
	boolean isEnable;

	@Override
	protected int getLayoutId() {
		return R.layout.frame_layout;
	}

	@Override
	protected void initData() {
		content = $(R.id.text_content);
		mPullRefreshHolder.setRefreshShowView(PullLayout.Direction.TOP, false);
		content.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: ");
				mPullLayout.setEnable(PullLayout.Direction.BOTTOM, isEnable = !isEnable);
			}
		});

	}

	@Override
	protected void refreshTop() {
		content.setText("I already refresh");
		mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
	}
}
