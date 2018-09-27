package cn.sskbskdrin.pull;

/**
 * Created by ayke on 2016/9/19 0019.
 */
public interface PullRefreshCallback {

    /**
     * perform refreshing UI
     *
     * @param direction
     */
    void onUIRefreshBegin(PullLayout.Direction direction);
}
