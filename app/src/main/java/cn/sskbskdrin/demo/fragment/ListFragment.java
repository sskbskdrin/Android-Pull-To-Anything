package cn.sskbskdrin.demo.fragment;

import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.base.IFragment;
import cn.sskbskdrin.base.ViewHolder;
import cn.sskbskdrin.demo.Adapter;
import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;
import cn.sskbskdrin.pull.refresh.MaterialHeader;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class ListFragment extends BaseFragment {

	private IBaseAdapter<String> mAdapter;
	private List<String> list = new ArrayList<>();

	@Override
	protected int getLayoutId() {
		return R.layout.list_layout;
	}

	@Override
	protected void initData() {
		ListView listView = $(R.id.list_content);
		list = new ArrayList<>();
		for (int i = 'A'; i < 'Z'; i++) {
			list.add((char) i + "");
		}
		mAdapter = new Adapter(getContext(), list);
		listView.setAdapter(mAdapter);
		MaterialHeader footer = new MaterialHeader(getContext());
		footer.setPadding(0, 20, 0, 20);
		listView.addFooterView(footer);
		mPullRefreshHolder.addUIHandler(PullLayout.Direction.BOTTOM, footer);
		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.TOP, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						list.add(0, "refresh header");
						mAdapter.updateList(list);
						mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
					}
				}, 2000);
			}
		});

		mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.BOTTOM, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				mRootView.postDelayed(new Runnable() {
					@Override
					public void run() {
						list.add("refresh footer");
						mAdapter.updateList(list);
						mPullRefreshHolder.refreshComplete(PullLayout.Direction.BOTTOM);
					}
				}, 2000);
			}
		});
	}
}
