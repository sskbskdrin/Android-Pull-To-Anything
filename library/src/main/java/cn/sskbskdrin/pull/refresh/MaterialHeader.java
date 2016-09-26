package cn.sskbskdrin.pull.refresh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullRefreshHolder;
import cn.sskbskdrin.pull.PullUIHandler;
import cn.sskbskdrin.pull.PullUIHandlerHook;

public class MaterialHeader extends View implements PullUIHandler {

	private MaterialProgressDrawable mDrawable;
	private float mScale = 1f;

	private Animation mScaleAnimation = new Animation() {
		@Override
		public void applyTransformation(float interpolatedTime, Transformation t) {
			mScale = 1f - interpolatedTime;
			mDrawable.setAlpha((int) (255 * mScale));
			invalidate();
		}
	};

	public MaterialHeader(Context context) {
		super(context);
		initView();
	}

	public MaterialHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public MaterialHeader(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	public void setRefreshHolder(PullRefreshHolder holder, PullRefreshHolder.Direction direction) {

		final PullUIHandlerHook mPtrUIHandlerHook = new PullUIHandlerHook(direction) {
			@Override
			public void run() {
				startAnimation(mScaleAnimation);
			}
		};

		mScaleAnimation.setDuration(200);
		mScaleAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mPtrUIHandlerHook.resume();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		holder.setPullUIHandlerHook(mPtrUIHandlerHook);
	}

	private void initView() {
		mDrawable = new MaterialProgressDrawable(getContext(), this);
		mDrawable.setBackgroundColor(Color.WHITE);
		mDrawable.setCallback(this);
	}

	@Override
	public void invalidateDrawable(Drawable dr) {
		if (dr == mDrawable) {
			invalidate();
		} else {
			super.invalidateDrawable(dr);
		}
	}

	public void setColorSchemeColors(int[] colors) {
		mDrawable.setColorSchemeColors(colors);
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = mDrawable.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();
		int width = mDrawable.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight();
//		+ getPaddingTop() + getPaddingBottom();
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
//		widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int size = mDrawable.getIntrinsicHeight();
		mDrawable.setBounds(0, 0, size, size);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int saveCount = canvas.save();
		Rect rect = mDrawable.getBounds();
		int l = getPaddingLeft() + (getMeasuredWidth() - mDrawable.getIntrinsicWidth()) / 2;
		canvas.translate(l, getPaddingTop());
		canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
		mDrawable.draw(canvas);
		canvas.restoreToCount(saveCount);
	}

	@Override
	public int getRefreshExtent() {
		return mDrawable.getIntrinsicWidth() + getPaddingTop() * 2;
	}

	@Override
	public void onUIReset() {
		mScale = 1f;
		mDrawable.stop();
	}

	@Override
	public void onUIRefreshPull() {

	}

	@Override
	public void onUIRefreshPrepare() {
	}

	@Override
	public void onUIRefreshBegin() {
		mDrawable.setAlpha(255);
		mDrawable.start();
	}

	@Override
	public void onUIRefreshComplete() {
		mDrawable.stop();
	}

	@Override
	public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {

		float off = Math.abs(offsetX);
		if (off == 0)
			off = Math.abs(offsetY);

		float percent = Math.min(1f, off / getRefreshExtent());

		if (status == PullRefreshHolder.STATUS_PULL) {
			mDrawable.setAlpha((int) (255 * percent));
			mDrawable.showArrow(true);

			float strokeStart = ((percent) * .8f);
			mDrawable.setStartEndTrim(0f, Math.min(0.8f, strokeStart));
			mDrawable.setArrowScale(Math.min(1f, percent));

			// magic
			float rotation = (-0.25f + .4f * percent + percent * 2) * .5f;
			mDrawable.setProgressRotation(rotation);
			invalidate();
		}
	}
}
