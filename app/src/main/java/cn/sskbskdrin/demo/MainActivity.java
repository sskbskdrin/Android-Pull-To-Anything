package cn.sskbskdrin.demo;

import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.BaseFragmentActivity;
import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.base.ViewHolder;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;
import cn.sskbskdrin.pull.refresh.MaterialHeader;

public class MainActivity extends BaseFragmentActivity {

	private BaseAdapter mListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		showTitle("Pull-To-Anything");
		mTitleView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

		final ListView listView = $(R.id.main_list);
		final List list = new ArrayList();
		for (int i = 'A'; i < 'N'; i++) {
			list.add((char) i + "");
		}
		mListAdapter = new IBaseAdapter<String>(this, list, android.R.layout.simple_list_item_1) {
			@Override
			public void bindViewHolder(ViewHolder holder, String item) {
				holder.setText(android.R.id.text1, item);
			}
		};
		listView.setAdapter(mListAdapter);
		final PullLayout layout = $(R.id.main_pull);
		final PullRefreshHolder holder = ((PullLayout) $(R.id.main_pull)).getPullRefreshHolder();
		holder.addPullRefreshCallback(PullRefreshHolder.Direction.TOP, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				layout.postDelayed(new Runnable() {
					@Override
					public void run() {
						list.add(0, "Refresh");
						mListAdapter.notifyDataSetChanged();
						holder.refreshComplete(PullRefreshHolder.Direction.TOP);
					}
				}, 2000);
			}
		});
		MaterialHeader header = $(R.id.main_top);
		header.setRefreshHolder(holder, PullRefreshHolder.Direction.TOP);
		holder.addPullRefreshCallback(PullRefreshHolder.Direction.BOTTOM, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				layout.postDelayed(new Runnable() {
					@Override
					public void run() {
						list.add("Load");
						mListAdapter.notifyDataSetChanged();
						holder.refreshComplete(PullRefreshHolder.Direction.BOTTOM);
					}
				}, 2000);
			}
		});
	}
}
