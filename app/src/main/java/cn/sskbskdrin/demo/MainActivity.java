package cn.sskbskdrin.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.BaseFragmentActivity;
import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.base.ViewHolder;
import cn.sskbskdrin.demo.fragment.HomeFragment;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;
import cn.sskbskdrin.pull.PullUIHandler;
import cn.sskbskdrin.pull.refresh.StoreHouseHeader;
import cn.sskbskdrin.recycler.BaseRecyclerView;
import cn.sskbskdrin.utils.ToastUtil;

public class MainActivity extends BaseFragmentActivity {
	public static final String TAG = "MainActivity";
	private BaseRecyclerView mRecyclerView;
	private PullLayout mPullLayout;
	private IBaseAdapter<String> mBaseAdapter;
	private List<String> mDatas;

	private TextView tipView;
	private PullRefreshHolder holder;
	private PullUIHandler bottom;

	PullUIHandler uiHandler = new PullUIHandler() {
		@Override
		public int getRefreshExtent() {
			return bottom.getRefreshExtent();
		}

		@Override
		public void onUIReset() {
			tipView.setText("reset");
		}

		@Override
		public void onUIRefreshPull() {
			tipView.setText("pull");
		}

		@Override
		public void onUIRefreshPrepare() {
			tipView.setText("prepare");
		}

		@Override
		public void onUIRefreshBegin() {
			tipView.setText("begin");
		}

		@Override
		public void onUIRefreshComplete() {
			tipView.setText("complete");
		}

		@Override
		public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportFragmentManager().beginTransaction().add(R.id.main_content, new HomeFragment()).commit();
	}

//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//		return super.onCreateOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.id_action_add:
//				mBaseAdapter.add("1");
//				break;
//			case R.id.id_action_delete:
//				mBaseAdapter.remove(2);
//				break;
//			case R.id.id_action_gridview:
//				mPullLayout.setOrientation(PullLayout.VERTICAL);
//				mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
//				break;
//			case R.id.id_action_listview:
//				mPullLayout.setOrientation(PullLayout.VERTICAL);
//				mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//				break;
//			case R.id.id_action_Horizontalistview:
//				mPullLayout.setOrientation(PullLayout.HORIZONTAL);
//				mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//				break;
//			case R.id.id_action_horizontalGridView:
//				mPullLayout.setOrientation(PullLayout.HORIZONTAL);
//				mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4, GridLayoutManager.HORIZONTAL, false));
//				break;
//			case R.id.id_action_staggeredgridview:
////				Intent intent = new Intent(this , StaggeredGridLayoutActivity.class);
////				startActivity(intent);
//				break;
//			case R.id.id_action_viewpage:
//				startActivity(new Intent(this, ViewPageActivity.class));
//				break;
//		}
//		return true;
//	}
}
