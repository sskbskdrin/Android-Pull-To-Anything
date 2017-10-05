package cn.sskbskdrin.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import cn.sskbskdrin.base.BaseFragmentActivity;
import cn.sskbskdrin.demo.fragment.EnableFragment;
import cn.sskbskdrin.demo.fragment.FrameFragment;
import cn.sskbskdrin.demo.fragment.GridFragment;
import cn.sskbskdrin.demo.fragment.HideBarFragment;
import cn.sskbskdrin.demo.fragment.ListFragment;
import cn.sskbskdrin.demo.fragment.NoneFragment;
import cn.sskbskdrin.demo.fragment.PinFragment;
import cn.sskbskdrin.demo.fragment.PinTopFragment;
import cn.sskbskdrin.demo.fragment.PullFragment;
import cn.sskbskdrin.demo.fragment.RecyclerViewFragment;
import cn.sskbskdrin.demo.fragment.ScrollFragment;
import cn.sskbskdrin.demo.fragment.StoreFragment;
import cn.sskbskdrin.demo.fragment.TabsFragment;
import cn.sskbskdrin.demo.fragment.TextFragment;
import cn.sskbskdrin.demo.fragment.ViewPagerFragment;
import cn.sskbskdrin.demo.fragment.WebFragment;
import cn.sskbskdrin.demo.fragment.ZoomFragment;

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
		if (fragment == null) finish();
		showTitle(name);
		getSupportFragmentManager().beginTransaction().add(R.id.content, fragment).commit();
	}

	private Fragment getFragment(String name) {
		if ("grid".equals(name)) {
			return new GridFragment();
		} else if ("text".equals(name)) {
			return new TextFragment();
		} else if ("Recycler".equals(name)) {
			return new RecyclerViewFragment();
		} else if ("zoom".equals(name)) {
			return new ZoomFragment();
		} else if ("frame".equals(name)) {
			return new FrameFragment();
		} else if ("scroll".equals(name)) {
			return new ScrollFragment();
		} else if ("list".equals(name)) {
			return new ListFragment();
		} else if ("viewpager".equals(name)) {
			return new ViewPagerFragment();
		} else if ("web".equals(name)) {
			return new WebFragment();
		} else if ("hideBar".equals(name)) {
			return new HideBarFragment();
		} else if ("enable".equals(name)) {
			return new EnableFragment();
		} else if ("pinTop".equals(name)) {
			return new PinTopFragment();
		} else if ("pin".equals(name)) {
			return new PinFragment();
		} else if ("pull".equals(name)) {
			return new PullFragment();
		} else if ("store".equals(name)) {
			return new StoreFragment();
		} else if ("tabs".equals(name)) {
			return new TabsFragment();
		} else {
			return new NoneFragment();
		}
	}

}
