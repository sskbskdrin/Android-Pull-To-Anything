package cn.sskbskdrin.pull;

/**
 * Created by ayke on 2016/9/20 0020.
 */

public abstract class PullUIHandlerHook implements Runnable {

	private Runnable mResumeAction;

	private PullRefreshHolder.Direction mDirection;

	public PullUIHandlerHook(PullRefreshHolder.Direction direction) {
		mDirection = direction;
	}

	protected void takeOver() {
		run();
	}

	public PullRefreshHolder.Direction getDirection() {
		return mDirection;
	}

	public void resume() {
		if (mResumeAction != null) {
			mResumeAction.run();
		}
	}

	protected void setResumeAction(Runnable runnable) {
		mResumeAction = runnable;
	}
}
