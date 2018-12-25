package cn.sskbskdrin.pull.refresh;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullUIHandler;
import cn.sskbskdrin.pull.R;

public class ClassicHeader extends FrameLayout implements PullUIHandler {

    private final static String KEY_SharedPreferences = "pull_classic_last_update";
    private int mRotateAniTime = 150;
    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private TextView mTitleTextView;
    private TextView mLastUpdateTextView;
    private View mRotateView;
    private View mProgressBar;

    private String mTime;

    public ClassicHeader(Context context) {
        this(context, null);
    }

    public ClassicHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(R.layout.pull_classic_header);
    }

    protected void initViews(int layoutId) {
        buildAnimation();
        View.inflate(getContext(), layoutId, this);

        mRotateView = findViewById(R.id.pull_header_rotate_view);

        mTitleTextView = (TextView) findViewById(R.id.pull_header_title);
        mLastUpdateTextView = (TextView) findViewById(R.id.pull_header_last_update);
        mProgressBar = findViewById(R.id.pull_header_progressbar);
        resetView();
    }

    public void setRotateAniTime(int time) {
        if (time == mRotateAniTime || time == 0) {
            return;
        }
        mRotateAniTime = time;
        buildAnimation();
    }

    public void setUpdateTime(String time) {
        mTime = time;
        if (mLastUpdateTextView != null) {
            mLastUpdateTextView.setText("上次更新：" + time);
        }
    }

    private void buildAnimation() {
        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation
            .RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation
            .RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setFillAfter(true);
    }

    private void resetView() {
        hideRotateView();
        if (mProgressBar != null) {
            mProgressBar.setVisibility(INVISIBLE);
        }
        setVisibility(INVISIBLE);
    }

    private void hideRotateView() {
        if (mRotateView != null) {
            mRotateView.clearAnimation();
            mRotateView.setVisibility(INVISIBLE);
        }
    }

    public int getRefreshExtent() {
        return getMeasuredHeight();
    }

    @Override
    public void onUIReset() {
        resetView();
    }

    @Override
    public void onUIRefreshPull() {
        setVisibility(VISIBLE);
        if (mRotateView != null) {
            if (mRotateView.getVisibility() == VISIBLE) {
                mRotateView.clearAnimation();
                mRotateView.startAnimation(mReverseFlipAnimation);
            }
            mRotateView.setVisibility(VISIBLE);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setText(R.string.pull_down_to_refresh);
            mTitleTextView.setVisibility(VISIBLE);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(INVISIBLE);
        }
        if (!TextUtils.isEmpty(mTime)) {
            if (mLastUpdateTextView != null) {
                mLastUpdateTextView.setVisibility(VISIBLE);
            }
            setUpdateTime(mTime);
        }
    }

    @Override
    public void onUIRefreshPrepare() {
        if (mRotateView != null) {
            mRotateView.clearAnimation();
            mRotateView.startAnimation(mFlipAnimation);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setText(R.string.pull_release_to_refresh);
        }
        if (!TextUtils.isEmpty(mTime)) {
            mLastUpdateTextView.setVisibility(VISIBLE);
            setUpdateTime(mTime);
        }
    }

    @Override
    public void onUIRefreshBegin(PullLayout.Direction direction) {
        hideRotateView();
        if (mProgressBar != null) {
            mProgressBar.setVisibility(VISIBLE);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setVisibility(VISIBLE);
            mTitleTextView.setText(R.string.pull_refreshing);
        }
        if (mLastUpdateTextView != null) {
            mLastUpdateTextView.setVisibility(GONE);
        }
    }

    @Override
    public void onUIRefreshComplete() {
        hideRotateView();
        if (mProgressBar != null) {
            mProgressBar.setVisibility(INVISIBLE);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setVisibility(VISIBLE);
            mTitleTextView.setText(getResources().getString(R.string.pull_refresh_complete));
        }
    }

    @Override
    public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {
    }
}
