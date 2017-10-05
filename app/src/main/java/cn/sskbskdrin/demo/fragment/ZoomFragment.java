package cn.sskbskdrin.demo.fragment;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.sskbskdrin.demo.R;
import cn.sskbskdrin.pull.PullLayout;
import cn.sskbskdrin.pull.PullPositionChangeListener;
import cn.sskbskdrin.pull.PullRefreshCallback;

/**
 * Created by ayke on 2016/9/26 0026.
 */

public class ZoomFragment extends BaseFragment {
	ImageView content;

	int height = 0;

	@Override
	protected int getLayoutId() {
		return R.layout.zoom_layout;
	}

	@Override
	protected void initData() {
		content = $(R.id.zoom_content);
		mPullLayout.addPullPositionChangeListener(new PullPositionChangeListener() {
			@Override
			public void onUIPositionChange(int dx, int dy, int offsetX, int offsetY, int status) {
				if (offsetY > 0) {
					if (height == 0)
						height = content.getMeasuredHeight();
					if (height == 0)
						return;
					if (offsetY > 200) {
						((ViewGroup.MarginLayoutParams) content.getLayoutParams()).topMargin = offsetY - 200;
					} else {
						((ViewGroup.MarginLayoutParams) content.getLayoutParams()).topMargin = 0;
						content.getLayoutParams().height = height + offsetY;
					}
					content.requestLayout();
				}
			}
		});
	}
}
