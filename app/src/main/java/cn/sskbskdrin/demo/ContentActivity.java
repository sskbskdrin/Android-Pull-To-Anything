package cn.sskbskdrin.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import cn.sskbskdrin.base.BaseFragmentActivity;
import cn.sskbskdrin.demo.fragment.GridFragment;
import cn.sskbskdrin.demo.fragment.ListFragment;

public class ContentActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		Intent intent = getIntent();
		String name = "";
		if (intent != null) {
			name = intent.getStringExtra("fragment");
		}
		Fragment fragment = getFragment(name);
		if (fragment == null)
			finish();
		showTitle(name);
		getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).commit();
	}

	private Fragment getFragment(String name) {
		if ("grid".equals(name)) {
			return new GridFragment();
		} else if ("list".equals(name)) {
			return new ListFragment();
		} else {
			return null;
		}
	}

}
