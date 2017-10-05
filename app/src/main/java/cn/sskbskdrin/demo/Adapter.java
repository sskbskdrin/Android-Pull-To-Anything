package cn.sskbskdrin.demo;

import android.content.Context;

import java.util.List;

import cn.sskbskdrin.base.IBaseAdapter;
import cn.sskbskdrin.base.ViewHolder;

/**
 * Created by ayke on 2016/10/9 0009.
 */

public class Adapter extends IBaseAdapter<String> {

	public Adapter(Context context, List<String> list) {
		super(context, list, R.layout.item_home);
	}

	@Override
	public void bindViewHolder(ViewHolder holder, String item) {
		holder.setText(R.id.id_num, item);
	}
}
