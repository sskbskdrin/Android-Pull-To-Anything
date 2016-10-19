package cn.sskbskdrin.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.BaseFragmentActivity;
import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;
import cn.sskbskdrin.pull.PullUIHandler;
import cn.sskbskdrin.pull.PullUIHandlerHook;
import cn.sskbskdrin.pull.refresh.MaterialHeader;
import cn.sskbskdrin.utils.ToastUtil;

public class MainActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener {
	public static final String TAG = "MainActivity";
	private PullLayout mPullLayout;
	private IBaseAdapter<String> mBaseAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		showTitle(R.string.app_name);

		ListView listView = $(R.id.main_list);
		final List<String> list = new ArrayList<>();
		list.add("list");
		list.add("grid");
		for (int i = 0; i < 20; i++) {
			list.add("" + i);
		}
		mBaseAdapter = new Adapter(this, list);
		listView.setAdapter(mBaseAdapter);
		listView.setOnItemClickListener(this);

		mPullLayout = $(R.id.main_pull);
		final PullRefreshHolder holder = mPullLayout.getPullRefreshHolder();
		MaterialHeader header = $(R.id.main_bottom);
		header.setRefreshHandler(holder);
		holder.addPullRefreshCallback(PullLayout.Direction.TOP, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				mPullLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						holder.refreshComplete(PullLayout.Direction.TOP);
						ToastUtil.show(getBaseContext(), "刷新完成");
					}
				}, 2000);
			}
		});

		holder.addPullRefreshCallback(PullLayout.Direction.BOTTOM, new PullRefreshCallback() {
			@Override
			public void onUIRefreshBegin() {
				mPullLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						holder.refreshComplete(PullLayout.Direction.BOTTOM);
						ToastUtil.show(getBaseContext(), "刷新完成");
					}
				}, 2000);
			}
		});
		holder.setPullUIHandlerHook(new PullUIHandlerHook(PullLayout.Direction.TOP) {
			@Override
			public void run() {
				ToastUtil.show(getBaseContext(), "这是一个hook");
				resume();
			}
		});

		holder.addUIHandler(PullLayout.Direction.TOP, new PullUIHandler() {
			@Override
			public void onUIReset() {

			}

			@Override
			public void onUIRefreshPull() {

			}

			@Override
			public void onUIRefreshPrepare() {

			}

			@Override
			public void onUIRefreshComplete() {

			}

			@Override
			public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {

			}

			@Override
			public void onUIRefreshBegin() {

			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, ContentActivity.class);
		intent.putExtra("fragment", mBaseAdapter.getItem(position));
//		startActivity(intent);
	}
}
