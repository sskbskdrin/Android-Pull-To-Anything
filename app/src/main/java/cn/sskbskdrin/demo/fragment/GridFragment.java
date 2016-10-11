package cn.sskbskdrin.demo.fragment;

import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.base.IFragment;
import cn.sskbskdrin.base.ViewHolder;
import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;

/**
 * Created by ayke on 2016/9/26 0026.
 */
public class GridFragment extends IFragment {

	private IBaseAdapter<String> mAdapter;
	private List<String> list = new ArrayList<>();

	@Override
	protected int getLayoutId() {
		return R.layout.grid_layout;
	}

	@Override
	protected void initView() {
		GridView gridView = $(R.id.grid_content);
		list = new ArrayList<>();
		for (int i = 'A'; i < 'Z'; i++) {
			list.add((char) i + "");
		}
		mAdapter = new IBaseAdapter<String>(getContext(), list, R.layout.item_home) {
			@Override
			public void bindViewHolder(ViewHolder holder, String item) {
				holder.setText(R.id.id_num, item);
			}
		};
		gridView.setAdapter(mAdapter);
		final PullLayout layout = $(R.id.grid_pull);
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
