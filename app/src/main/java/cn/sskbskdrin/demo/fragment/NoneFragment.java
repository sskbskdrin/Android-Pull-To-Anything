package cn.sskbskdrin.demo.fragment;

import android.view.View;
import android.view.ViewGroup;

import cn.sskbskdrin.demo.R;

/**
 * Created by ex-keayuan001 on 2017/9/25.
 */

public class NoneFragment extends BaseFragment {
	@Override
	protected int getLayoutId() {
		return R.layout.text_layout;
	}

	@Override
	protected void initData() {
		View view = $(R.id.list_top);
		((ViewGroup) view.getParent()).removeView(view);
	}
}
