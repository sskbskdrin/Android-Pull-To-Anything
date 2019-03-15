package cn.sskbskdrin.demo.fragment;

import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.demo.Adapter;
import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
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
        if (listView.getFooterViewsCount() == 0) {
            final MaterialHeader footer = new MaterialHeader(getContext());
            footer.setPadding(0, 20, 0, 20);
            listView.addFooterView(footer);
            mPullRefreshHolder.addUIHandler(PullLayout.Direction.BOTTOM, footer);
            mPullRefreshHolder.setRefreshThreshold(PullLayout.Direction.BOTTOM, 100);
            mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.BOTTOM, this);
        }
    }

    @Override
    protected void refreshTop() {
        list.add(0, "refresh header");
        mAdapter.updateList(list);
    }

    @Override
    protected void refreshBottom() {
        list.add("refresh footer");
        mAdapter.updateList(list);
    }
}
