package cn.sskbskdrin.pull;

/**
 * Created by ayke on 2016/9/19 0019.
 */
public interface PullRefreshCallback {

    /**
     * perform refreshing UI
     *
     * @param direction 开始刷新的位置
     */
    void onUIRefreshBegin(PullLayout.Direction direction);
}
