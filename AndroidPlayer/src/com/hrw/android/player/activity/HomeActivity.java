package com.hrw.android.player.activity;

import java.util.List;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.hrw.android.player.BelmotPlayer;
import com.hrw.android.player.R;
import com.hrw.android.player.broadcastreceiver.UpdateUiBroadcastReceiver;
import com.hrw.android.player.component.dialog.ExitDialog;
import com.hrw.android.player.component.menu.CommonPopupWindowMenu;
import com.hrw.android.player.dao.AudioDao;
import com.hrw.android.player.dao.impl.AudioDaoImpl;
import com.hrw.android.player.utils.Constants;

public class HomeActivity extends TabActivity {
	private static final String TAG = "HomeActivity";
	private BelmotPlayer belmotPlayer;
	ImageButton tab_main;
	ImageButton tab_random;
	ImageButton tab_search;
	ImageButton tab_menu;
	View tabButtonSelectd;
	LinearLayout pop_menu_layout;
	CommonPopupWindowMenu popWindow;
	private AudioDao audioDao;

	/**
	 * Launch Home activity helper
	 * 
	 * @param c
	 *            context where launch home from (used by SplashscreenActivity)
	 */
	public static void launch(Context c) {
		Intent intent = new Intent(c, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		/*Intent.FLAG_ACTIVITY_CLEAR_TOP:如果在当前Task中，有要启动的Activity，那么
		 * 把该Acitivity之前的所有Activity都关掉，并把此Activity置前以避免创建
		 * Activity的实例*/
		/*具有很大的灵活性，常用的就是实现TabHost分页效果，但很好的避免的TabHost的缺点，
		 * 如title等*/
		
		c.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_activity);
		super.onCreate(savedInstanceState);
		initTopButton();
		initTabHost();
		initBottomMenu();
		registerReceiver();
		if (null == belmotPlayer) {
			belmotPlayer = BelmotPlayer.getInstance();
		}
		if (null == audioDao) {
			audioDao = new AudioDaoImpl(this);
		}
	}

	private void registerReceiver() {
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(Constants.UPDATE_UI_ACTION);
		BroadcastReceiver updateUiBroadcastReceiver = new UpdateUiBroadcastReceiver(
				this);
		registerReceiver(updateUiBroadcastReceiver, localIntentFilter);
	}

	private void initTabHost() {
		Intent intent = new Intent(this, MenuListActivity.class);
		TabHost tabHost = getTabHost();//定义一个tabHost，在TabActivity里面使用getTabhost()获取当前Activity的TabHost
		//每个选项卡都有一个选项卡指示符,内容和tag标签,以便于记录.
		TabHost.TabSpec tabSpec = tabHost.newTabSpec(
				//获取一个新的TabHost.TabSpec，并关联到当前tab host
				Constants.TAB_SPEC_TAG.MAIN_SPEC_TAG.getId()).setIndicator(
				Constants.TAB_SPEC_TAG.MAIN_SPEC_TAG.getId());
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);// addTab新增一个选项卡public void addTab (TabHost.TabSpec tabSpec)
		//tabSpec指定怎样创建指示符和内容
		//when tabhost add a tab,it seems that current
		// TabActivity lose focus and the tabSpec get
		// focus.and current TabActivity can't invoke
		// onCreateOptionsMenu and onMenuOpened.
		tabHost.setCurrentTab(0);//设置当前的选项卡的id
	}

	private void initTopButton() {
		final Intent toPlayerActivity = new Intent(this, PlayerActivity.class);
		//定义一个向PlayActivity跳转的Intent
		ImageButton player_button = (ImageButton) findViewById(R.id.player);
		player_button.setOnTouchListener(new OnTouchListener() {
			//设置ImageButton player_button的单击事件监听
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					//ImageButton player_button弹起来时候的操作
					startActivityForResult(toPlayerActivity,
							Constants.MENU_TO_PLAYER_REQUEST_CODE);
					/*startActivityForResult()A-Activity需要在B-Activtiy中执
					*行一些数据操作，而B-Activity又要将，执行操作数据的结果返回
					*给A-Activtiy*/
				}
				return false;
			}

		});
	}

	private void switchActivity(Intent intent, String tag, String label) {
		TabHost.TabSpec tab_spec_music_list = this.getTabHost().newTabSpec(tag)
				.setIndicator(label);
		tab_spec_music_list.setContent(intent);
		this.getTabHost().addTab(tab_spec_music_list);
		this.getTabHost().setCurrentTabByTag(tag);
	}

	OnClickListener bottomMenuOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean selected = v.isSelected();
			if (!selected) {
				tabButtonSelectd.setSelected(false);
				v.setSelected(true);
				tabButtonSelectd = v;
				switch (v.getId()) {
				case R.id.tab_main: {
					Intent toMainActivity = getIntent();
					switchActivity(toMainActivity,
							Constants.TAB_SPEC_TAG.MAIN_SPEC_TAG.getId(),
							Constants.TAB_SPEC_TAG.MAIN_SPEC_TAG.getId());
					break;
				}
				case R.id.tab_random: {
					if (belmotPlayer.getPlayerEngine().isPlaying()) {
						belmotPlayer.getPlayerEngine().stop();
					}
					List<String> musicList = audioDao.getLocalAudioPathList();
					belmotPlayer.getPlayerEngine().setMediaPathList(musicList);
					belmotPlayer.getPlayerEngine().setPlayingPath(
							musicList.get(0));
					belmotPlayer.getPlayerEngine().play();
					break;
				}
				case R.id.tab_search: {
					Intent toSearchMusicActivity = new Intent(
							getApplicationContext(), SearchMusicActivity.class);
					switchActivity(toSearchMusicActivity,
							"toSearchMusicActivity", "toSearchMusicActivity");
					break;
				}
				}
			}

			// else {
			// TODO: do something...
			// v.setSelected(true);
			// }

			// v.setSelected(true);
			// v.setBackgroundResource(R.drawable.tab_net_media_pressed);
		}
	};

	private void initBottomMenu() {
		// TODO It is the first button on the bottom.
		tab_main = (ImageButton) findViewById(R.id.tab_main);
		tab_random = (ImageButton) findViewById(R.id.tab_random);
		tab_search = (ImageButton) findViewById(R.id.tab_search);
		tab_menu = (ImageButton) findViewById(R.id.tab_menu);

		tab_main.setSelected(true);
		tabButtonSelectd = tab_main;
		// tab_random.setPressed(true);监听button事件
		tab_main.setOnClickListener(bottomMenuOnClickListener);
		tab_random.setOnClickListener(bottomMenuOnClickListener);
		tab_search.setOnClickListener(bottomMenuOnClickListener);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// localTabHost1.setCurrentTab(0);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(state);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				ExitDialog.getExitDialog(this).create().show();
				return true;
			}
		}

		// else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
		// if (!popWindow.isShowing()) {
		// popWindow.showAtLocation(findViewById(R.id.bottom_bar),
		// Gravity.BOTTOM, 0, 55);
		// } else {
		// popWindow.dismiss();
		// }
		// return true;
		// }

		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.MENU_TO_PLAYER_REQUEST_CODE) {
			// Bundle bundle = data.getExtras();
			switch (resultCode) {
			case Constants.MENU_TO_PLAYER_RESULT_CODE:
				// File file = (File) bundle.get("filePath");
				// if (null != file) {
				// /music_list.add(file.getName());
				// }
				break;
			}
		}
	}
}
