package cn.sskbskdrin.demo.fragment;

import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.base.ViewHolder;
import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;

/**
 * Created by ayke on 2016/9/26 0026.
 */
public class GridFragment extends BaseFragment {

	private IBaseAdapter<String> mAdapter;
	private List<String> list = new ArrayList<>();

	@Override
	protected int getLayoutId() {
		return R.layout.grid_layout;
	}

	@Override
	protected void initData() {
		GridView gridView = $(R.id.grid_content);
		list = new ArrayList<>();
		for (int i = 'A'; i < 'Z'; i++) {
			list.add((char) i + "");
		}
		for (int i = 0; i < 10; i++) {
			list.add("" + i);
		}
		mAdapter = new IBaseAdapter<String>(getContext(), list, R.layout.item_home) {
			@Override
			public void bindViewHolder(ViewHolder holder, String item) {
				holder.setText(R.id.id_num, item);
			}
		};
		gridView.setAdapter(mAdapter);
	}

	@Override
	protected void refreshTop() {
		list.add(0, "refresh header");
		mAdapter.updateList(list);
		mPullRefreshHolder.refreshComplete(PullLayout.Direction.TOP);
	}

	@Override
	protected void refreshBottom() {
		list.add("refresh footer");
		mAdapter.updateList(list);
		mPullRefreshHolder.refreshComplete(PullLayout.Direction.BOTTOM);
	}
}
