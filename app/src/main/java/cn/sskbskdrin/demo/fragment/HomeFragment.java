package cn.sskbskdrin.demo.fragment;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.base.IFragment;
import cn.sskbskdrin.base.ViewHolder;
import cn.sskbskdrin.demo.MainActivity;
import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;
import cn.sskbskdrin.recycler.BaseRecyclerView;
import cn.sskbskdrin.utils.ToastUtil;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class HomeFragment extends IFragment {

	private List<String> list = new ArrayList<>();
	private BaseRecyclerView mRecyclerView;
	private PullLayout mPullLayout;
	private IBaseAdapter<String> mBaseAdapter;

	@Override
	protected int getLayoutId() {
		return R.layout.home_layout;
	}

	@Override
	protected void initView() {
		showTitle("SSK-PTA");
		list = new ArrayList<>();
		for (int i = 'A'; i <= 'Z'; i++) {
			list.add("" + (char) i);
		}
		mRecyclerView = $(R.id.home_list);
		mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
		mRecyclerView.setHasFixedSize(true);
		mBaseAdapter = new IBaseAdapter<String>(getContext(), list, R.layout.item_home) {
			@Override
			public void bindViewHolder(ViewHolder holder, String item) {
				holder.setText(R.id.id_num, item);
			}
		};
		mRecyclerView.setBaseAdapter(mBaseAdapter);
		// 设置item动画
		mRecyclerView.setOnItemClickListener(new BaseRecyclerView.OnItemClickListener() {
			@Override
			public void onItemClick(RecyclerView parent, View view, int position) {
				ToastUtil.show(getContext(), "onClick " + position);
			}
		});
		mRecyclerView.setOnItemLongClickListener(new BaseRecyclerView.OnItemLongClickListener() {
			@Override
			public void onItemLongClick(RecyclerView parent, View view, int position) {
				ToastUtil.show(getContext(), "onLongClick " + position);
			}
		});
	}

	@Override
	protected void initData() {

	}
}
