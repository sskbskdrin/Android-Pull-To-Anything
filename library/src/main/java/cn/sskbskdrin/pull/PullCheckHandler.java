package cn.sskbskdrin.pull;

import android.view.View;

/**
 * Created by sskbskdrin on 2016/九月/17 下午12:56.
 */
public interface PullCheckHandler {

    boolean checkCanDoPullLeft(final PullLayout frame, final View content);

    boolean checkCanDoPullTop(final PullLayout frame, final View content);

    boolean checkCanDoPullRight(final PullLayout frame, final View content);

    boolean checkCanDoPullBottom(final PullLayout frame, final View content);

}
