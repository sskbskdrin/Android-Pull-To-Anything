package cn.sskbskdrin.pull;

/**
 * Created by ayke on 2016/9/19 0019.
 */
public interface PullPositionChangeListener {
    void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status);
}
