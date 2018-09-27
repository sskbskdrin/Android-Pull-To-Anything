package cn.sskbskdrin.pull;

/**
 * Created by sskbskdrin on 2016/九月/17 下午5:03.
 */
public interface PullUIHandler extends PullPositionChangeListener, PullRefreshCallback {

    /**
     * When the content view has reached top and refresh has been completed, view will be reset.
     */
    void onUIReset();

    void onUIRefreshPull();

    /**
     * prepare for loading
     */
    void onUIRefreshPrepare();

    /**
     * perform UI after refresh
     */
    void onUIRefreshComplete();
}
