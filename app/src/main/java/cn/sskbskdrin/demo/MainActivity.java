package cn.sskbskdrin.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.BaseFragmentActivity;
import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;
import cn.sskbskdrin.pull.PullUIHandler;
import cn.sskbskdrin.pull.PullUIHandlerHook;
import cn.sskbskdrin.pull.refresh.ClassicHeader;
import cn.sskbskdrin.pull.refresh.MaterialHeader;
import cn.sskbskdrin.utils.ToastUtil;

public class MainActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = "MainActivity";
    private PullLayout mPullLayout;
    private IBaseAdapter<String> mBaseAdapter;
    private TextView mTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showTitle(R.string.app_name);

        mTip = (TextView) findViewById(R.id.main_tip);
        GridView listView = $(R.id.main_grid);
        final List<String> list = new ArrayList<>();
        list.add("list");
        list.add("grid");
        list.add("Recycler");
        list.add("scroll");
        list.add("text");
        list.add("frame");
        list.add("viewpager");
        list.add("web");
        list.add("zoom");
        list.add("hideBar");
        list.add("enable");
        list.add("pin");
        list.add("pinTop");
        list.add("pull");
        list.add("store");
        list.add("none");
        list.add("tabs");
        for (int i = 0; i < 20; i++) {
            //			list.add("" + i);
        }
        mBaseAdapter = new Adapter(this, list);
        listView.setAdapter(mBaseAdapter);
        listView.setOnItemClickListener(this);

        mPullLayout = $(R.id.main_pull);
        final PullRefreshHolder holder = mPullLayout.getPullRefreshHolder();
        MaterialHeader header = $(R.id.main_bottom);
        header.setRefreshHandler(holder);
        holder.setRefreshShowView(PullLayout.Direction.TOP, false);
        holder.addPullRefreshCallback(PullLayout.Direction.TOP, new PullRefreshCallback() {
            @Override
            public void onUIRefreshBegin(PullLayout.Direction direction) {
                mPullLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.refreshComplete(PullLayout.Direction.TOP);
                        //						ToastUtil.show(getBaseContext(), "刷新完成");
                    }
                }, 2000);
            }
        });

        holder.addPullRefreshCallback(PullLayout.Direction.BOTTOM, new PullRefreshCallback() {
            @Override
            public void onUIRefreshBegin(PullLayout.Direction direction) {
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

        final ClassicHeader header1 = new ClassicHeader(this);
//        $(R.id.main_test);
        holder.addUIHandler(PullLayout.Direction.TOP, new PullUIHandler() {
            @Override
            public void onUIReset() {
                mTip.setText("reset");
                header1.onUIReset();
            }

            @Override
            public void onUIRefreshPull() {
                mTip.setText("pull");
                header1.onUIRefreshPull();
            }

            @Override
            public void onUIRefreshPrepare() {
                mTip.setText("Prepare");
                header1.onUIRefreshPrepare();
            }

            @Override
            public void onUIRefreshComplete() {
                mTip.setText("complete");
                header1.onUIRefreshComplete();
            }

            @Override
            public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {
            }

            @Override
            public void onUIRefreshBegin(PullLayout.Direction direction) {
                mTip.setText("begin");
                header1.onUIRefreshBegin(direction);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra("fragment", mBaseAdapter.getItem(position));
        startActivity(intent);
    }
}
