package cn.sskbskdrin.demo.fragment;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class WebFragment extends BaseFragment {
	WebView content;

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
		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.TOP, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						content.loadUrl("https://www.baidu.com/");
					}
				}, 2000);
			}
		});
		content.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
			}
		});

		mPullRefreshHolder.autoRefresh(PullLayout.Direction.TOP);
	}
}
