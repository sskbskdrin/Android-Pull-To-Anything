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

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class ListFragment extends IFragment {

	private IBaseAdapter<String> mAdapter;
	private List<String> list = new ArrayList<>();

	@Override
	protected int getLayoutId() {
		return R.layout.list_layout;
	}

	@Override
	protected void initView() {
		ListView listView = $(R.id.list_content);
		list = new ArrayList<>();
		for (int i = 'A'; i < 'Z'; i++) {
			list.add((char) i + "");
		}
		mAdapter = new Adapter(getContext(), list);
		listView.setAdapter(mAdapter);
		final PullLayout layout = $(R.id.list_pull);
		final PullRefreshHolder holder = layout.getPullRefreshHolder();
		holder.addPullRefreshCallback(PullLayout.Direction.TOP, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				layout.postDelayed(new Runnable() {
					@Override
					public void run() {
						list.add(0, "refresh header");
						mAdapter.updateList(list);
						holder.refreshComplete(PullLayout.Direction.TOP);
					}
				}, 2000);
			}
		});

		holder.addPullRefreshCallback(PullLayout.Direction.BOTTOM, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				layout.postDelayed(new Runnable() {
					@Override
					public void run() {
						list.add("refresh footer");
						mAdapter.updateList(list);
						holder.refreshComplete(PullLayout.Direction.BOTTOM);
					}
				}, 2000);
			}
		});
	}

	@Override
	protected void initData() {

	}
}
