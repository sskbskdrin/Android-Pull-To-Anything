package cn.sskbskdrin.pull;

/**
 * Created by ayke on 2016/9/20 0020.
 */

/**
 * 刷新完成时在UI线程执行的回调方法，调用resume()恢复UI
 */
public abstract class PullUIHandlerHook implements Runnable {

	private Runnable mResumeAction;

	private PullLayout.Direction mDirection;

	public PullUIHandlerHook(PullLayout.Direction direction) {
		mDirection = direction;
	}

	void takeOver() {
		run();
	}

	PullLayout.Direction getDirection() {
		return mDirection;
	}

	public void resume() {
		if (mResumeAction != null) {
			mResumeAction.run();
		}
	}

	void setResumeAction(Runnable runnable) {
		mResumeAction = runnable;
	}
}
