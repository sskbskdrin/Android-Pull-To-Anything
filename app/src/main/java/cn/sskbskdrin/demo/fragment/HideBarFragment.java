package cn.sskbskdrin.demo.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class HideBarFragment extends BaseFragment {
	WebView content;
	private Dialog dialog;

	@Override
	protected int getLayoutId() {
		return R.layout.web_layout;
	}

	@Override
	protected void initView() {
		super.initView();
		content = $(R.id.web_content);
	}

	@Override
	protected void initData() {
		content.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
				if (dialog != null) dialog.dismiss();
			}
		});

		mPullRefreshHolder.setRefreshShowView(PullLayout.Direction.TOP, false);
	}

	@Override
	protected void refreshTop() {
		content.loadUrl("https://www.baidu.com/");
		dialog = ProgressDialog.show(getContext(), "", "加载中");
	}
}
