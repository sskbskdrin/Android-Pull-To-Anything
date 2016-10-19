package cn.sskbskdrin.pull;

import android.view.View;
import android.widget.AbsListView;

/**
 * Created by sskbskdrin on 2016/九月/17 下午1:22.
 */
public class PullDefaultCheckHandler implements PullCheckHandler {
	@Override
	public boolean checkCanDoPullLeft(PullLayout frame, View content) {
		return !checkLeft(content);
	}

	@Override
	public boolean checkCanDoPullTop(PullLayout frame, View content) {
		return !checkTop(content);
	}

	@Override
	public boolean checkCanDoPullRight(PullLayout frame, View content) {
		return !checkRight(content);
	}

	@Override
	public boolean checkCanDoPullBottom(PullLayout frame, View content) {
		return !checkBottom(content);
	}

	private boolean checkLeft(View content) {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			return content.getScrollX() > 0;
		} else {
			return content.canScrollHorizontally(-1);
		}
	}

	private boolean checkTop(View content) {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (content instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) content;
				return absListView.getChildCount() > 0
						&& (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
						.getTop() < absListView.getPaddingTop());
			} else {
				return content.getScrollY() > 0;
			}
		} else {
			return content.canScrollVertically(-1);
		}
	}

	private boolean checkRight(View content) {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			return content.getScrollX() < 0;
		} else {
			return content.canScrollHorizontally(1);
		}
	}

	private boolean checkBottom(View content) {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (content instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) content;
				return absListView.getChildCount() > 0
						&& (absListView.getLastVisiblePosition() < absListView.getAdapter().getCount() - 1 ||
						absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getPaddingBottom());
			} else {
				return content.getScrollY() < 0;
			}
		} else {
			return content.canScrollVertically(1);
		}
	}

}
