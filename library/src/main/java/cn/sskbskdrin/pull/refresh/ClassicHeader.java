package cn.sskbskdrin.pull.refresh;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.sskbskdrin.pull.PullUIHandler;
import cn.sskbskdrin.pull.R;

public class ClassicHeader extends FrameLayout implements PullUIHandler {

	private final static String KEY_SharedPreferences = "pull_classic_last_update";
	private static SimpleDateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private int mRotateAniTime = 150;
	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;
	private TextView mTitleTextView;
	private View mRotateView;
	private View mProgressBar;
	private long mLastUpdateTime = -1;
	private TextView mLastUpdateTextView;
	private String mLastUpdateTimeKey;
	private boolean mShouldShowLastUpdate;

	private LastUpdateTimeUpdater mLastUpdateTimeUpdater = new LastUpdateTimeUpdater();

	public ClassicHeader(Context context) {
		super(context);
		initViews(null);
	}

	public ClassicHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews(attrs);
	}

	public ClassicHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews(attrs);
	}

	protected void initViews(AttributeSet attrs) {
		buildAnimation();
		View.inflate(getContext(), R.layout.pull_classic_default_header, this);

		mRotateView = findViewById(R.id.header_rotate_view);

		mTitleTextView = (TextView) findViewById(R.id.header_rotate_view_header_title);
		mLastUpdateTextView = (TextView) findViewById(R.id.header_rotate_view_header_last_update);
		mProgressBar = findViewById(R.id.header_rotate_view_progressbar);

		resetView();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mLastUpdateTimeUpdater != null) {
			mLastUpdateTimeUpdater.stop();
		}
	}

	public void setRotateAniTime(int time) {
		if (time == mRotateAniTime || time == 0) {
			return;
		}
		mRotateAniTime = time;
		buildAnimation();
	}

	/**
	 * Specify the last update time by this key string
	 *
	 * @param key
	 */
	public void setLastUpdateTimeKey(String key) {
		if (TextUtils.isEmpty(key)) {
			return;
		}
		mLastUpdateTimeKey = key;
	}

	/**
	 * Using an object to specify the last update time.
	 *
	 * @param object
	 */
	public void setLastUpdateTimeRelateObject(Object object) {
		setLastUpdateTimeKey(object.getClass().getName());
	}

	private void buildAnimation() {
		mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(mRotateAniTime);
		mFlipAnimation.setFillAfter(true);

		mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
		mReverseFlipAnimation.setDuration(mRotateAniTime);
		mReverseFlipAnimation.setFillAfter(true);
	}

	private void resetView() {
		hideRotateView();
		mProgressBar.setVisibility(INVISIBLE);
	}

	private void hideRotateView() {
		mRotateView.clearAnimation();
		mRotateView.setVisibility(INVISIBLE);
	}

	public int getRefreshExtent() {
		return getMeasuredHeight();
	}

	@Override
	public void onUIReset() {
		resetView();
		mShouldShowLastUpdate = true;
		tryUpdateLastUpdateTime();
	}

	@Override
	public void onUIRefreshPull() {
		if (mRotateView != null && mRotateView.getVisibility() == VISIBLE) {
			mRotateView.clearAnimation();
			mRotateView.startAnimation(mReverseFlipAnimation);
		}
		mTitleTextView.setText(getResources().getString(R.string.cube_ptr_pull_down_to_refresh));
		requestLayout();

		mShouldShowLastUpdate = true;
		tryUpdateLastUpdateTime();
		mLastUpdateTimeUpdater.start();

		mProgressBar.setVisibility(INVISIBLE);

		mRotateView.setVisibility(VISIBLE);
		mTitleTextView.setVisibility(VISIBLE);
	}

	@Override
	public void onUIRefreshPrepare() {
		if (mRotateView != null) {
			mRotateView.clearAnimation();
			mRotateView.startAnimation(mFlipAnimation);
		}
		mTitleTextView.setText(getResources().getString(R.string.cube_ptr_release_to_refresh));
	}

	@Override
	public void onUIRefreshBegin() {
		mShouldShowLastUpdate = false;
		hideRotateView();
		mProgressBar.setVisibility(VISIBLE);
		mTitleTextView.setVisibility(VISIBLE);
		mTitleTextView.setText(R.string.cube_ptr_refreshing);

		tryUpdateLastUpdateTime();
		mLastUpdateTimeUpdater.stop();
	}

	@Override
	public void onUIRefreshComplete() {

		hideRotateView();
		mProgressBar.setVisibility(INVISIBLE);

		mTitleTextView.setVisibility(VISIBLE);
		mTitleTextView.setText(getResources().getString(R.string.cube_ptr_refresh_complete));

		// update last update time
		SharedPreferences sharedPreferences = getContext().getSharedPreferences(KEY_SharedPreferences, 0);
		if (!TextUtils.isEmpty(mLastUpdateTimeKey)) {
			mLastUpdateTime = new Date().getTime();
			sharedPreferences.edit().putLong(mLastUpdateTimeKey, mLastUpdateTime).commit();
		}
	}

	private void tryUpdateLastUpdateTime() {
		if (TextUtils.isEmpty(mLastUpdateTimeKey) || !mShouldShowLastUpdate) {
			mLastUpdateTextView.setVisibility(GONE);
		} else {
			String time = getLastUpdateTime();
			if (TextUtils.isEmpty(time)) {
				mLastUpdateTextView.setVisibility(GONE);
			} else {
				mLastUpdateTextView.setVisibility(VISIBLE);
				mLastUpdateTextView.setText(time);
			}
		}
	}

	private String getLastUpdateTime() {

		if (mLastUpdateTime == -1 && !TextUtils.isEmpty(mLastUpdateTimeKey)) {
			mLastUpdateTime = getContext().getSharedPreferences(KEY_SharedPreferences, 0).getLong(mLastUpdateTimeKey, -1);
		}
		if (mLastUpdateTime == -1) {
			return null;
		}
		long diffTime = new Date().getTime() - mLastUpdateTime;
		int seconds = (int) (diffTime / 1000);
		if (diffTime < 0) {
			return null;
		}
		if (seconds <= 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(getContext().getString(R.string.cube_ptr_last_update));

		if (seconds < 60) {
			sb.append(seconds + getContext().getString(R.string.cube_ptr_seconds_ago));
		} else {
			int minutes = (seconds / 60);
			if (minutes > 60) {
				int hours = minutes / 60;
				if (hours > 24) {
					Date date = new Date(mLastUpdateTime);
					sb.append(sDataFormat.format(date));
				} else {
					sb.append(hours + getContext().getString(R.string.cube_ptr_hours_ago));
				}

			} else {
				sb.append(minutes + getContext().getString(R.string.cube_ptr_minutes_ago));
			}
		}
		return sb.toString();
	}

	@Override
	public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {
	}

	private class LastUpdateTimeUpdater implements Runnable {

		private boolean mRunning = false;

		private void start() {
			if (TextUtils.isEmpty(mLastUpdateTimeKey)) {
				return;
			}
			mRunning = true;
			run();
		}

		private void stop() {
			mRunning = false;
			removeCallbacks(this);
		}

		@Override
		public void run() {
			tryUpdateLastUpdateTime();
			if (mRunning) {
				postDelayed(this, 1000);
			}
		}
	}
}
