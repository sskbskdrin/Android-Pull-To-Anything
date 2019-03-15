package cn.sskbskdrin.demo.fragment;

import android.support.annotation.CallSuper;
import android.view.ViewGroup;

import cn.sskbskdrin.base.IFragment;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshCallback;
import cn.sskbskdrin.pull.PullRefreshHolder;

/**
 * Created by ayke on 2016/10/19 0019.
 */

public abstract class BaseFragment extends IFragment implements PullRefreshCallback {

    protected PullLayout mPullLayout;
    protected PullRefreshHolder mPullRefreshHolder;

    @CallSuper
    @Override
    protected void initView() {
        if (mRootView instanceof PullLayout) {
            mPullLayout = (PullLayout) mRootView;
        } else {
            for (int i = 0; i < ((ViewGroup) mRootView).getChildCount(); i++) {
                if (((ViewGroup) mRootView).getChildAt(i) instanceof PullLayout) {
                    mPullLayout = (PullLayout) ((ViewGroup) mRootView).getChildAt(i);
                    break;
                }
            }
        }
        if (mPullLayout == null) return;
        mPullRefreshHolder = mPullLayout.getPullRefreshHolder();
        mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.LEFT, this);
        mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.TOP, this);
        mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.RIGHT, this);
        mPullRefreshHolder.addPullRefreshCallback(PullLayout.Direction.BOTTOM, this);
    }

    @Override
    public void onUIRefreshBegin(final PullLayout.Direction direction) {
        mRootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (direction) {
                    case TOP:
                        refreshTop();
                        break;
                    case LEFT:
                        refreshLeft();
                        break;
                    case RIGHT:
                        refreshRight();
                        break;
                    case BOTTOM:
                        refreshBottom();
                        break;
                    default:
                }
                mPullRefreshHolder.refreshComplete(direction);
            }
        }, 1000);
    }

    protected void refreshLeft() {
    }

    protected void refreshTop() {
    }

    protected void refreshRight() {
    }

    protected void refreshBottom() {
    }
}
