package cn.sskbskdrin.demo.fragment;

import android.widget.TextView;

import cn.sskbskdrin.demo.R;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class PinFragment extends BaseFragment {
	TextView content;

	@Override
	protected int getLayoutId() {
		return R.layout.pager_layout;
	}

	@Override
	protected void initData() {
		content = $(R.id.id_num);
		content.setText("pin content");
		mPullLayout.setPinContent(true);
	}

	@Override
	protected void refreshTop() {
		content.setText("I already refresh");
	}
}
