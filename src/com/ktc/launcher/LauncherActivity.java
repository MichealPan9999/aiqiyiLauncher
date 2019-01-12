package com.ktc.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ktc.launcher.constants.Constants;
import com.ktc.launcher.ui.AppWidget;
import com.ktc.launcher.utils.LogUtils;
import com.ktc.launcher.utils.NetTool;
import com.ktc.launcher.utils.ScaleAnimEffect;
import com.ktc.launcher.utils.ToastUtils;
import com.ktc.launcher.utils.Utils;
import com.mstar.android.MIntent;
import com.mstar.android.MKeyEvent;
import com.mstar.android.tv.TvChannelManager;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvPictureManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.EnumScalerWindow;
import com.mstar.android.tvapi.common.vo.VideoWindowType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.qiyi.tv.client.ConnectionListener;
import com.qiyi.tv.client.ErrorCode;
import com.qiyi.tv.client.QiyiClient;
import com.qiyi.tv.client.Result;
import com.qiyi.tv.client.data.Media;
import com.qiyi.tv.client.data.Picture;
import com.qiyi.tv.client.feature.common.PictureType;
import com.qiyi.tv.client.feature.common.RecommendationType;
import com.qiyi.tv.tw.nexgen.R;
import com.viewpagerindicator.CirclePageIndicator;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import cn.ktc.library.update.Update;
import cn.ktc.library.update.Version;

public class LauncherActivity extends Activity implements OnClickListener{
	private static final String TAG = "LauncherActivity";
	private static final String STR_STATUS_NONE = "0";
	private static final String STR_STATUS_SUSPENDING = "1";
	private boolean Carousel_Focus = false;
	// ////////////////////////////net////////////////////////
	private boolean mWireFlag = false;
	boolean mWifiEnabled = false;
	boolean mWifiConnected = false;
	int mWifiRssi = 0;
	int mWifiLevel = 0;
	private int[] WifiIconArray;
	public final static int WIFI_LEVEL_COUNT = 4;
	private WifiManager mWifiManager;
	private NetTool mNetTool;
	private String mWifiSsid;
	private boolean isRunning = false;
	private Boolean bExitThread = false;
	//public final static String PPPOE_STATE_ACTION = "android.net.pppoe.PPPOE_STATE_ACTION";
	public final static String PPPOE_STATE_ACTION = "com.mstar.android.pppoe.PPPOE_STATE_ACTION";
	public final static String PPPOE_STATE_STATUE = "PppoeStatus";
	public static final String PPPOE_STATE_CONNECT = "connect";
	public static final String PPPOE_STATE_DISCONNECT = "disconnect";
	public static final String PPPOE_STATE_AUTHFAILED = "authfailed";
	public static final String PPPOE_STATE_FAILED = "failed";
	// ////////////////notification////////////////////////
	private final int USB_STATE_ON = 100001; // usb storage on
	private final int USB_STATE_OFF = 100002; // usb storage off
	private final int UPDATE_USB_ICON = 100005; // update usb icon
	private final int USB_STATE_CHANGE = 100006; // usb change state
	private ImageView usb_image;
	public boolean mUsbFlag = false;
	public int usbDeviceCount = 0;
	private int ThirdPartyDtvValue = 0;
	private ImageView netStatus;
	private Boolean bSync = true;
	private boolean homeAcivityIsForeground = true;
	private TvCommonManager commonService;
	private TvChannelManager tvChannelManager;

	private final String FILE_FLY_LAUNCH = "com.jrm.filefly.action";
	private int toChangeInputSource = TvCommonManager.INPUT_SOURCE_NONE;
	private HandlerThread handlerThread;
	private Handler HandlerUpdata;
	//private InputSourceThread mInputSourceThread;
	private CheckNewVersionTask mCheckNewVersionTask;

	private int panel_height;
	private int panel_width;
	private Boolean isPass = false;

	private Dialog alertDialog;
	private Boolean NeedDelay = true;
	private boolean activityIsRun = false;

	// lixq 20151119 start
	// 修改launcher判断第一次开机方法。避免android清理掉launcher，导致的从应用退出后进入到电视界面
	private boolean isPowerOn = false;
	private final String IS_POWER_ON_PROPERTY = "mstar.launcher.1stinit";
	// lixq 20151119 end
	private RelativeLayout rl_bg;
	private TextView tv_time, time_colon, tv_main_date,tv_am_zh, tv_am;
	//private Button tvButton;
	// tv show
	private SurfaceView surfaceView = null;
	private android.view.SurfaceHolder.Callback callback;
	private boolean hasTVClick = false;
	private Boolean bSystemShutdown = false;
	private boolean createsurface = false;
	private static final int SCALE_NONE = 0;
	private static final int SCALE_FULL = 1;

	private static final int SCALE_SMALL = 2;
	private int fullScale = SCALE_NONE;
	private Handler mInputSourceHandler;
	private FrameLayout[] app_fls;
	private FrameLayout app_fl;
	public AppWidget[] app_typeLogs;
	private ImageView[] appbgs;
	private TextView[] app_tvs;
	private ScaleAnimEffect animEffect;
	private Intent jumpIntent;

	private static final int SCROLL_TO_NEXT = 1;
	private static final int UPDATE_VIEWPAGER = 2;
	private static final int UPDATE_NETWORK_UI = 3;

	private TextView qiyiTitle;
	private RelativeLayout title_bg;
	private ViewPager mViewPager;
	private CirclePageIndicator mCirclePageIndicator;
	private ArrayList<View> mViewContainer = new ArrayList<View>();
	private MyBasePagerAdapter myBasePagerAdapter;
	private ImageView image;
	// 设置自动轮播时间为10秒
	private static final int AUTO_SCROLL_TIME = 3; // 10s
	private ScheduledExecutorService mScheduExec;
	private int mCurrentPosition=0;
	private boolean isViewPagerFocused;
	private Handler handler=new  Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCROLL_TO_NEXT:
				if (mediaList != null && mediaList.size() > 0) {
					qiyiTitle
							.setText(mediaList.get(mCurrentPosition).getName());
				}
				mViewPager.setCurrentItem(mCurrentPosition, true);
				break;
			case UPDATE_VIEWPAGER:
				if (mViewContainer.size() > 0) {
					mViewContainer = new ArrayList<View>();
				}
				for (int i = 0; i < listUrl.size(); i++) {
					image = new ImageView(getApplicationContext());
					image.setScaleType(ScaleType.CENTER_CROP);
					// image.setImageBitmap(bitmap);
					DisplayImageOptions options = new DisplayImageOptions.Builder()
							.showImageOnLoading(R.drawable.fail)
							.showImageForEmptyUri(R.drawable.fail)
							.showImageOnFail(R.drawable.fail)
							.cacheInMemory(true).cacheOnDisk(true)
							.considerExifParams(true)
							.displayer(new SimpleBitmapDisplayer()).build();
					ImageLoader.getInstance().displayImage(listUrl.get(i),
							image, options);
					image.setOnClickListener(listener);
					mViewContainer.add(image);
				}
				myBasePagerAdapter = new MyBasePagerAdapter(mViewContainer);
				initViewPager();
			case UPDATE_NETWORK_UI:
				updateNetUI();
				break;
			default:
				break;
			}
		}
	};

	private QiyiClient mQiyiClient = null;
	private ArrayList<String> listUrl;
	private List<Media> mediaList;
	private ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onError(int code) {
			LogUtils.d(TAG, "onError" + ",Code: " + code);
		}

		@Override
		public void onDisconnected() {
			LogUtils.d(TAG, "onDisconnected");
		}

		@Override
		public void onConnected() {
			LogUtils.d(TAG, "onConnected");
		}

		@Override
		public void onAuthSuccess() {
			// 认证成功
			LogUtils.d(TAG, "onAuthSuccess");
			new Thread(new Runnable() {

				@Override
				public void run() {
					List<Media> recommendation = getRecommendation();
					mediaList = recommendation;
					listUrl = new ArrayList<String>();
					if (mediaList != null && mediaList.size() > 0) {
						for (int i = 0; i < mediaList.size(); i++) {
							Media media = mediaList.get(i);
							listUrl.add(media.getPicUrl());
						}
					}
					if (listUrl.size() > 0 && listUrl != null) {
						Message message = handler.obtainMessage();
						message.what = UPDATE_VIEWPAGER;
						handler.sendMessage(message);
					}

					if (mediaList != null && mediaList.size() > 0) {
						SeriaMedia seriaMedia = new SeriaMedia();
						seriaMedia.seriaMedia(mediaList);
					}

				}

			}).start();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SystemProperties.set("persist.sys.sync.ime" , "true");
		commonService = TvCommonManager.getInstance();
		tvChannelManager = TvChannelManager.getInstance();
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		setContentView(R.layout.activity_main);
		updateWallpaperVisibility(false);
		findView();
		try {
			InitData();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		InitHandler();
		loadMainUI();
		registNetReceiver();
		registUsbReceiver();
		
		initViewPager();
		initListener();
		//lixq 20161021 start
		setSubtitleEncode();
		//lixq 20161021 end

	}

	private void initAiQiYi() {
		mQiyiClient = QiyiClient.instance();
		mQiyiClient.initialize(getApplicationContext(),
				"h65nzksh5anoxrdnvvl29r2jws522uz5o8jomylouogo8vrv",
				"com.qiyi.tv.tw.chimeiopi"); // com.qiyi.tv.tw.chimeiopi
												// com.qiyi.tv.tw.nexgen

		mQiyiClient.setListener(mConnectionListener);
		mQiyiClient.connect();
	}

	private List<Media> getRecommendation() {
		// 获取首页推荐
		LogUtils.d(TAG, "getRecommendation() begin");
		int position = RecommendationType.EXTRUDE;
		Result<List<Media>> result = mQiyiClient.getRecommendation(position);
		List<Media> mediaList = null;
		if(result !=null)
		{
			int code = result.code;
			mediaList = result.data;
		}
		return mediaList;
	}

	public void InitData() throws MalformedURLException {
		try {
			panel_height = TvManager.getInstance().getPictureManager()
					.getPanelWidthHeight().height;
			panel_width = TvManager.getInstance().getPictureManager()
					.getPanelWidthHeight().width;
			System.out.println(panel_height + "  " + panel_width);
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isPowerOn = isPowerOn();
		WifiIconArray = new int[] { R.drawable.wifi_signal_0, R.drawable.wifi_signal_1, R.drawable.wifi_signal_2,
				R.drawable.wifi_signal_3 };
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mNetTool = new NetTool(getApplicationContext());
		animEffect = new ScaleAnimEffect();

		SeriaMedia seriaMedia = new SeriaMedia();
		File mediaFile = new File(
				"/data/data/com.qiyi.tv.tw.nexgen/files/media.txt");
		if (mediaFile.exists()) {
			String path = "/data/data/com.qiyi.tv.tw.nexgen/files/media.txt";
			mediaList = seriaMedia.deseriaMedia(path);
			listUrl = new ArrayList<String>();
			if (mediaList != null && mediaList.size() > 0) {
				for (int i = 0; i < mediaList.size(); i++) {
					Media media = mediaList.get(i);
					listUrl.add(media.getPicUrl());
				}
			}
			if (listUrl.size() > 0 && listUrl != null) {
				if (mViewContainer.size() > 0) {
					mViewContainer = new ArrayList<View>();
				}
				for (int i = 0; i < listUrl.size(); i++) {
					image = new ImageView(getApplicationContext());
					image.setScaleType(ScaleType.CENTER_CROP);
					DisplayImageOptions options = new DisplayImageOptions.Builder()
							.showImageOnLoading(R.drawable.fail)
							.showImageForEmptyUri(R.drawable.fail)
							.showImageOnFail(R.drawable.fail)
							.cacheInMemory(true).cacheOnDisk(true)
							.considerExifParams(true)
							.displayer(new SimpleBitmapDisplayer()).build();
					ImageLoader.getInstance().displayImage(listUrl.get(i),
							image, options);
					image.setOnClickListener(listener);
					mViewContainer.add(image);
				}
				myBasePagerAdapter = new MyBasePagerAdapter(mViewContainer);
				qiyiTitle.setText(mediaList.get(mCurrentPosition).getName());
				return;
			} else {
				try{
					mediaFile.delete();
				} catch (Exception e){
				
				}
				
				
			}
		}
			Log.i(TAG, "media.txt is not exist");
			String path = "assets";
			mediaList = seriaMedia.deseriaMedia(path);
			List<Bitmap> bitmapList = getBitmapListFromAssets();
			for (int i = 0; i < bitmapList.size(); i++) {
				ImageView image = new ImageView(getApplicationContext());
				image.setImageBitmap(bitmapList.get(i));
				image.setScaleType(ScaleType.CENTER_CROP);
				image.setOnClickListener(listener);
				mViewContainer.add(image);
			}
			myBasePagerAdapter = new MyBasePagerAdapter(mViewContainer);
			if (mediaList != null && mediaList.size() > 0)
			qiyiTitle.setText(mediaList.get(mCurrentPosition).getName());

	}

	public void InitHandler() {
		handlerThread = new HandlerThread("Launcher");
		handlerThread.start();
		mInputSourceHandler = new Handler(handlerThread.getLooper());
		//mInputSourceThread = new InputSourceThread();
		/*if (ThirdPartyDtvValue == 0) {
			mInputSourceHandler.post(mInputSourceThread);
		}*/
	}

	public void findView() {
		rl_bg = (RelativeLayout) findViewById(R.id.rl_bg);
		tv_time = (TextView) findViewById(R.id.tv_main_time);
		time_colon = (TextView) findViewById(R.id.time_colon);
		tv_am_zh = (TextView) findViewById(R.id.time_am_zh);
		tv_am = (TextView) findViewById(R.id.time_am);
		// iv_net_state = (ImageView) findViewById(R.id.iv_net_state);
		tv_main_date = (TextView) findViewById(R.id.tv_main_date);
		netStatus = (ImageView) findViewById(R.id.topbar_net_status);
		usb_image = (ImageView) findViewById(R.id.topbar_usb_status);

		title_bg = (RelativeLayout) findViewById(R.id.ll_text_and_indicator);
		qiyiTitle = (TextView) findViewById(R.id.aiqiyi_title);
		mViewPager = (ViewPager) findViewById(R.id.viewpager_aiqiyi);
		mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (homeAcivityIsForeground == false) {
				homeAcivityIsForeground = true;
				homeHandler.removeMessages(WindowMessageID.REFLESH_TIME);
				homeHandler.sendEmptyMessage(WindowMessageID.REFLESH_TIME);
			}
			return true;
		}
		if (event.getKeyCode() == MKeyEvent.KEYCODE_ASPECT_RATIO) {
			return true;
		}
		if ((event.getKeyCode() == KeyEvent.KEYCODE_TV_INPUT)) {
			event.setSource(KeyEvent.ACTION_MULTIPLE);
			changeInputSource("com.mstar.tv.tvplayer.ui");
			{
				short sourcestatus[] = null;
				try {
					sourcestatus = TvManager.getInstance().setTvosCommonCommand("SetAutoSleepOnStatus");
				} catch (TvCommonException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ComponentName componentName = new ComponentName("com.mstar.tv.tvplayer.ui",
					"com.mstar.tv.tvplayer.ui.RootActivity");
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(componentName);
			//lixq 2016.11.01 add for issue ERP Q161018001 start
		    intent.putExtra("isPowerOn", false);
			//lixq 2016.11.01 add for issue ERP Q161018001 end
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			LauncherActivity.this.startActivity(intent);
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStop() {
		LogUtils.i(TAG, "----------onStop----------");
		super.onStop();
		//bExitThread = true;
	}

	@Override
	protected void onPause() {
		LogUtils.i(TAG, "----------onPause----------");

		// 使能待机
		try {
			TvManager.getInstance().setTvosCommonCommand("SetAutoSleepOnStatus");
		} catch (TvCommonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 使能home键
		handlertv.postDelayed(enable_homekey, 800);
		if (surfaceView != null) {
			if (bSystemShutdown == false) {
				if (STR_STATUS_NONE.equals(SystemProperties.get("mstar.str.suspending", "0"))) {
					// synchronized (bSync) {
					// fullScale = SCALE_FULL;
					// }
				}

				if (createsurface) {
					if (surfaceView.getVisibility() == View.VISIBLE) {
						surfaceView.setBackgroundColor(Color.BLACK);
						surfaceView.setVisibility(View.INVISIBLE);
					}

					if (bSystemShutdown == false) {
						synchronized (bSync) {
							fullScale = SCALE_FULL;
						}
					} else {
						bSystemShutdown = false;
					}
				}

			} else {
				bSystemShutdown = false;
			}
		} else {
			LogUtils.d(TAG, "---- removeCallbacks(handlerRuntv) ----");
			//handlertv.removeCallbacks(handlerRuntv);
		}
		isRunning = false;
		activityIsRun = false;
		super.onPause();
	}

	Runnable pip_thread = new Runnable() {
		@Override
		public void run() {
			try {
				if (surfaceView.getVisibility() != View.VISIBLE) {
					surfaceView.setVisibility(View.VISIBLE);
					surfaceView.setBackgroundColor(Color.TRANSPARENT);
				}
				LogUtils.i("Hisa", "..PipThread..");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void updateNetUI() {
        Log.d(TAG, "updateNetUI: ");
		String netState = mNetTool.getNetType();
        Log.d(TAG, "netState: ---------"+netState);
		if (netState != null) {
			if (netState.equals("Wifi")) {

				// lixq 20160517 start
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				mWifiConnected = networkInfo != null && networkInfo.isConnected();
				if (mWifiConnected) {
					WifiManager wifiManager = mNetTool.getWifiManager();
					WifiInfo info = wifiManager.getConnectionInfo();
					int mWifiLevel = WifiManager.calculateSignalLevel(info.getRssi(), 4);
					netStatus.setImageResource(WifiIconArray[mWifiLevel]);
					initAiQiYi();
				} else {
					netStatus.setImageResource(R.drawable.com_status_unlink);
				}
				// lixq 20160517 end
			} else if (netState.equals("Ethernet")) {
				netStatus.setImageResource(R.drawable.com_status_link);
				initAiQiYi();
			} else if (netState.equals("Pppoe")) {
                netStatus.setImageResource(R.drawable.pppoe_conected);
                initAiQiYi();
            } else {
				netStatus.setImageResource(R.drawable.com_status_unlink);
			}
		} else {
			netStatus.setImageResource(R.drawable.com_status_unlink);
		}
	}

	private void loadMainUI() {
		app_fls = new FrameLayout[4];
		app_typeLogs = new AppWidget[4];
		appbgs = new ImageView[5];
		app_tvs = new TextView[4];
		app_fls[0] = (FrameLayout) findViewById(R.id.app_fl_1);
		app_fls[1] = (FrameLayout) findViewById(R.id.app_fl_2);
		app_fls[2] = (FrameLayout) findViewById(R.id.app_fl_3);
		app_fls[3] = (FrameLayout) findViewById(R.id.app_fl_4);

		app_typeLogs[0] = (AppWidget) findViewById(R.id.app_iv_1);
		app_typeLogs[1] = (AppWidget) findViewById(R.id.app_iv_2);
		app_typeLogs[2] = (AppWidget) findViewById(R.id.app_iv_3);
		app_typeLogs[3] = (AppWidget) findViewById(R.id.app_iv_4);
		Drawable ic_multimedia = getResources().getDrawable(R.drawable.ic_multimedia);
		Drawable tv_launcher = getResources().getDrawable(R.drawable.tv_launcher);
		Drawable all_apps = getResources().getDrawable(R.drawable.localapps);
		Drawable ic_settings = getResources().getDrawable(R.drawable.ic_settings);
		app_typeLogs[0].setAppIcon(ic_multimedia);
		app_typeLogs[1].setAppIcon(tv_launcher);
		app_typeLogs[2].setAppIcon(all_apps);
		app_typeLogs[3].setAppIcon(ic_settings);
		app_typeLogs[0].setAppName(getResources().getString(R.string.multimedia));
		app_typeLogs[1].setAppName(getResources().getString(R.string.tv));
		app_typeLogs[2].setAppName(getResources().getString(R.string.application_center));
		app_typeLogs[3].setAppName(getResources().getString(R.string.settings));
		// appbgs[0] = (ImageView) view.findViewById(R.id.app_bg_0);
		// appbgs[0] = (ImageView) view.findViewById(R.id.app_bg_0);
		appbgs[1] = (ImageView) findViewById(R.id.app_bg_1);
		appbgs[2] = (ImageView) findViewById(R.id.app_bg_2);
		appbgs[3] = (ImageView) findViewById(R.id.app_bg_3);
		appbgs[4] = (ImageView) findViewById(R.id.app_bg_4);
		app_tvs[0] = (TextView) findViewById(R.id.app_re_1);
		app_tvs[1] = (TextView) findViewById(R.id.app_re_2);
		app_tvs[2] = (TextView) findViewById(R.id.app_re_3);
		app_tvs[3] = (TextView) findViewById(R.id.app_re_4);
		for (int i = 0; i < app_typeLogs.length; i++) {
			final int which = i;
			appbgs[i + 1].setVisibility(View.GONE);
			app_typeLogs[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.app_iv_1:
						// 多媒体
						jumpIntent = getPackageManager().getLaunchIntentForPackage("com.jrm.localmm");
						if (jumpIntent == null) {
							ToastUtils.showMessageShort(getApplicationContext(), "您可能未安装该应用。");
						} else {
							jumpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
									| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							startActivity(jumpIntent);
						}
						break;
					case R.id.app_iv_2:
						// TV
						try {
							TvManager.getInstance()
									.setTvosCommonCommand("SetAutoSleepOnStatus");
						} catch (TvCommonException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						   ComponentName componentName = new ComponentName(
				                    "com.mstar.tv.tvplayer.ui", "com.mstar.tv.tvplayer.ui.RootActivity");
				            Intent intent = new Intent(Intent.ACTION_MAIN);
				            intent.addCategory(Intent.CATEGORY_LAUNCHER);
				            intent.setComponent(componentName);
				            //lixq 2016.11.01 add for issue ERP Q161018001 start
						    intent.putExtra("isPowerOn", false);
							//lixq 2016.11.01 add for issue ERP Q161018001 end
				            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				            startActivity(intent);
						break;
					case R.id.app_iv_3:
						startActivity(new Intent(LauncherActivity.this, AllAppListActivity.class));
						break;
					case R.id.app_iv_4:
						// 设置
						jumpIntent = getPackageManager().getLaunchIntentForPackage("com.android.tv.settings");
						if (jumpIntent == null) {
							ToastUtils.showMessageShort(getApplicationContext(), "您可能未安装该应用。");
						} else {
							jumpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
									| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							startActivity(jumpIntent);
						}
						break;
					}
					Message msg = changeSourceHandler.obtainMessage(Constants.SETSOURCE_MS);
					Bundle b = new Bundle();
					b.putString("packAgeName", "com.android.mslauncher.AllAppListActivity");
					msg.setData(b);
					changeSourceHandler.sendMessage(msg);
					exitHomeSource();
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				}
			});
			app_typeLogs[i].setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						showOnFocusAnimation(v);
						appbgs[which + 1].setVisibility(View.VISIBLE);
					} else {
						showLoseFocusAinimation(v);
						// 将白框隐藏
						appbgs[which + 1].setVisibility(View.GONE);
					}
					for (TextView tv : app_tvs) {
						if (tv.getVisibility() != View.GONE) {
							tv.setVisibility(View.GONE);
						}
					}
				}
			});
		}

		homeHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME, 1000);// 刷新时间
		if (activityIsRun == true) {
			SystemProperties.set("mstar.str.storage", "0");
			if (surfaceView == null) {
				// LogUtils.i(TAG, "<<<<<<---------- surfaceView ==
				// null---------->>>>>");
				// mcontent.setVisibility(View.VISIBLE);
				//handlertv.postDelayed(handlerRuntv, 300);
				BackHomeSource();
			} else {
				// LogUtils.i(TAG, "<<<<<<---------- surfaceView !=
				// null---------->>>>>");
				BackHomeSource();
				Message tmpMsg = new Message();
				tmpMsg.what = SCALE_SMALL;
				scaleWinHandler.sendMessageDelayed(tmpMsg, 500);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		update();
		app_typeLogs[0].setAppName(getResources().getString(R.string.multimedia));
		app_typeLogs[1].setAppName(getResources().getString(R.string.tv));
		app_typeLogs[2].setAppName(getResources().getString(R.string.application_center));
		app_typeLogs[3].setAppName(getResources().getString(R.string.settings));
		
		SystemProperties.set("mstar.str.storage", "0");
		try {
			/*
			 * TvManager.getInstance().setTvosCommonCommand(
			 * "SetAutoSleepOffStatus");
			 */
			/*
			 * Settings.System.putInt(getContentResolver(),
			 * "home_hot_key_disable", 1);
			 */
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// Hisa 2016.03.04 add Freeze function start
		TvPictureManager.getInstance().unFreezeImage();
		Intent intentCancel = new Intent();// 取消静像菜单
		intentCancel.setAction(MIntent.ACTION_FREEZE_CANCEL_BUTTON);
		sendBroadcast(intentCancel);
		// Hisa 2016.03.04 add Freeze function end
		if (isPowerOn == true && ThirdPartyDtvValue == 0) {
			LogUtils.i(TAG, "<<<<<<----------PowerOn == true ---------->>>>>");
			SystemProperties.set("com.jrm.localmm","true");
			isPowerOn = false;
			ComponentName componentName = new ComponentName("com.mstar.tv.tvplayer.ui",
					"com.mstar.tv.tvplayer.ui.RootActivity");
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(componentName);
			intent.putExtra("isPowerOn", true);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			startActivity(intent);
			handlertv.postDelayed(enable_homekey, 800);
			// 使能待机
			try {
				TvManager.getInstance().setTvosCommonCommand("SetAutoSleepOnStatus");
			} catch (TvCommonException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			activityIsRun = true;
			/*
			 * LogUtils.i(TAG,
			 * "<<<<<<----------mstar.str.suspending == STR_STATUS_NONE---------->>>>>"
			 * ); TvPictureManager tvPictureManager =
			 * TvPictureManager.getInstance(); tvPictureManager.unFreezeImage();
			 * movieWidget.startRun(); appWidget.InitAppWidget();
			 * weatherHodler.setWeather();
			 */
			if(handler.hasMessages(UPDATE_NETWORK_UI))
			{
				handler.removeMessages(UPDATE_NETWORK_UI);
			}
			handler.sendEmptyMessageDelayed(UPDATE_NETWORK_UI,1000);
			// Hisa 2015.11.05 进入app后时间标志闪动异常 start
			homeHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME, 1000);
			// Hisa 2015.11.05 进入app后时间标志闪动异常 end
			/*
			 * if (isBoot == 0) isBoot++;
			 */

			// update();
			/*
			 * Intent it = new Intent("com.biaoqi.stb.launcherk.onresume");
			 * sendBroadcast(it); handlertv.removeCallbacks(enable_homekey);
			 * Settings.System.putInt(getContentResolver(),
			 * "home_hot_key_disable", 1);
			 */
			if (surfaceView == null) {/*
				// LogUtils.i(TAG, "<<<<<<---------- surfaceView ==
				// null---------->>>>>");
				// mcontent.setVisibility(View.VISIBLE);
				// handlertv.postDelayed(handlerRuntv, 300);
				try {
					// setPipscale();
					LogUtils.i("Hisa", "openSurfaceView");
					final RelativeLayout surfaceViewLayout = (RelativeLayout) findViewById(R.id.tv_surfaceview_layout);
					surfaceView = new SurfaceView(getApplicationContext());
					surfaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
							ViewGroup.LayoutParams.FILL_PARENT));
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							//openSurfaceView();
							//setPipscale();
						}
					}, 600);
					surfaceViewLayout.addView(surfaceView);
				
					createsurface = true;
					if (surfaceView != null)
						surfaceView.setBackgroundColor(Color.TRANSPARENT);
				} catch (Exception e) {
					e.printStackTrace();
				}
				BackHomeSource();
			*/} else {

				// LogUtils.i(TAG, "<<<<<<---------- surfaceView !=
				// null---------->>>>>");
				BackHomeSource();
				Message tmpMsg = new Message();
				tmpMsg.what = SCALE_SMALL;
				scaleWinHandler.sendMessageDelayed(tmpMsg, 500);
			}
			// 设置不待机
			try {
				TvManager.getInstance().setTvosCommonCommand("SetAutoSleepOffStatus");
			} catch (TvCommonException e) {
				e.printStackTrace();
			}
			// 禁止home键
			Settings.System.putInt(getContentResolver(), "home_hot_key_disable", 1);
		}
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		isRunning = true;
		if (CheckUsbIsExist()) {
			usb_image.setVisibility(View.VISIBLE);
		} else {
			usb_image.setVisibility(View.GONE);
		}
	}

	Handler handlertv = new Handler();
	// delay enableHomekey
	Runnable enable_homekey = new Runnable() {

		@Override
		public void run() {
			Settings.System.putInt(getContentResolver(), "home_hot_key_disable", 0);
		}
	};

	@Override
	protected void onDestroy() {
		LogUtils.i(TAG, "----------onDestroy----------");
		/*if (surfaceView != null) {
			surfaceView.getHolder().removeCallback((android.view.SurfaceHolder.Callback) callback);
			surfaceView = null;
		}*/
		bExitThread = true;
		if(handler.hasMessages(UPDATE_NETWORK_UI))
		{
			handler.removeMessages(UPDATE_NETWORK_UI);
		}
		unregisterReceiver(mNetReceiver);
		unregisterReceiver(mUsbReceiver);
		if (null!=mScheduExec) {
			mScheduExec.shutdown();
		}
		super.onDestroy();
	}

	private void doStartApplicationWithPackageName(String packagename) {

		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageinfo = null;
		try {
			packageinfo = getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return;
		}

		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		// 通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			// packagename = 参数packname
			String packageName = resolveinfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
			String className = resolveinfo.activityInfo.name;
			// LAUNCHER Intent
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			// 设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);

			intent.setComponent(cn);
			startActivity(intent);
		}
	}

	private Handler scaleWinHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCALE_FULL:
				synchronized (bSync) {
					fullScale = SCALE_FULL;
				}
				break;
			case SCALE_SMALL:
				synchronized (bSync) {
					fullScale = SCALE_SMALL;
				}

				handlertv.postDelayed(pip_thread, 100); // delay to show tv
														// window, wait
				break;
			}
		}
	};

	public void changeInputSource(String packName) {
		LogUtils.i(TAG, "changeInputSource------------" + packName);
		if (packName != null) {
			if (packName.contentEquals("com.mstar.tv.tvplayer.ui") || packName.contentEquals("mstar.factorymenu.ui")
					|| packName.contentEquals("com.tvos.pip") || packName.contentEquals("com.mstar.tvsetting.hotkey")
					|| packName.contentEquals("com.babao.tvju") || packName.contentEquals("com.babao.socialtv")
					|| packName.contentEquals("com.mstar.appdemo")) {
				LogUtils.i(TAG, "------------TV AP");
			} else {
				synchronized (bSync) {
					if (STR_STATUS_SUSPENDING.equals(SystemProperties.get("mstar.str.suspending", "0"))) {
						SystemProperties.set("mstar.str.storage", "1");
					}
					toChangeInputSource = TvCommonManager.INPUT_SOURCE_STORAGE;
				}
			}
		}
	}

	private void updateWallpaperVisibility(boolean visible) {
		int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
		int curflags = getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
		if (wpflags != curflags) {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			WallpaperManager.getInstance(this).suggestDesiredDimensions(metrics.widthPixels, metrics.heightPixels);

			getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		}
	}

	public void BackHomeSource() {
		LogUtils.v(TAG, "======backhomesource=======");
		synchronized (bSync) {
			LogUtils.v(TAG, "========bSync===========");
			toChangeInputSource = TvCommonManager.INPUT_SOURCE_ATV;
			LogUtils.v(TAG, "========toChangeInputSource============"+toChangeInputSource);
		}
		/*if (mInputSourceHandler.hasCallbacks(mInputSourceThread)) {
			mInputSourceHandler.post(mInputSourceThread);
		}*/
	}

	class FileFlyReceiver extends BroadcastReceiver {
		public FileFlyReceiver() {
			LogUtils.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>  this is box");
		}

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			LogUtils.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>  this is box receive com.jrm.filefly.action");
			String action = arg1.getAction();
			if (FILE_FLY_LAUNCH.equals(action)) {
				synchronized (bSync) {
					if (STR_STATUS_SUSPENDING.equals(SystemProperties.get("mstar.str.suspending", "0"))) {
						SystemProperties.set("mstar.str.storage", "1");
					}
					toChangeInputSource = TvCommonManager.INPUT_SOURCE_STORAGE;
				}
			}
		}
	}

	private void exitHomeSource() {
		synchronized (bSync) {
			toChangeInputSource = TvCommonManager.INPUT_SOURCE_STORAGE;
			fullScale = SCALE_FULL;
			TvCommonManager.getInstance().setInputSource(TvCommonManager.INPUT_SOURCE_STORAGE);
		}
		// mInputSourceHandler.post(new InputSourceThread());
	}

	private Handler changeSourceHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.SETSOURCE_MS:
				String packAgeName = (String) msg.getData().get("packAgeName");
				changeInputSource(packAgeName);
				break;
			}
		};
	};

	/**
	 * 注册网络状态改变广播 @ param @ return void
	 */
	private void registNetReceiver() {
		// ethernet status changed
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE_IMMEDIATE");
		// wifi status changed
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// pppoe status changed
		intentFilter.addAction(PPPOE_STATE_ACTION);
		registerReceiver(mNetReceiver, intentFilter);
	}

	private void registUsbReceiver() {
		IntentFilter usbFilter = new IntentFilter();
		usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		usbFilter.addDataScheme("file");
		registerReceiver(mUsbReceiver, usbFilter);
	}

	private void usbInAndOut() {
		Animation mAnimation;
		mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.usb_in_out);
		mAnimation.setRepeatCount(5);
		usb_image.startAnimation(mAnimation);
	}

	void updateUsbMassStorageNotification(boolean available) {
		/*if (available) {
			usb_image.setVisibility(View.VISIBLE);
		} else {
			usb_image.setVisibility(View.GONE);
		}*/
		if (CheckUsbIsExist()) {
			usb_image.setVisibility(View.VISIBLE);
		} else {
			usb_image.setVisibility(View.GONE);
		}
	}

	public Handler mUsbHanler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case USB_STATE_ON:
				mUsbHanler.sendEmptyMessage(USB_STATE_CHANGE);
				break;
			case USB_STATE_OFF:
				if (usbDeviceCount < 1) {
					mUsbFlag = false;
					mUsbHanler.sendEmptyMessage(UPDATE_USB_ICON);
				} else {
					mUsbFlag = true;
					mUsbHanler.sendEmptyMessage(USB_STATE_CHANGE);
				}
				break;
			case USB_STATE_CHANGE:
				usbInAndOut();
				mUsbHanler.sendEmptyMessage(UPDATE_USB_ICON);
				break;
			case UPDATE_USB_ICON:
				updateUsbMassStorageNotification(mUsbFlag);
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				mUsbFlag = true;
				++usbDeviceCount;
				if (("0".equals(SystemProperties.get("mstar.audio.init", "0")))
						&& (SystemProperties.getBoolean("mstar.str.enable", false))) {
					mUsbHanler.sendEmptyMessageAtTime(USB_STATE_ON, 5000);
				} else {
					mUsbHanler.sendEmptyMessage(USB_STATE_ON);
				}
			} else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {// remove
				mUsbFlag = false;
				--usbDeviceCount;
				if (("0".equals(SystemProperties.get("mstar.audio.init", "0")))
						&& (SystemProperties.getBoolean("mstar.str.enable", false))) {
					// mUsbHanler.sendEmptyMessageAtTime(USB_STATE_ON, 5000);
				} else {
					mUsbHanler.sendEmptyMessage(USB_STATE_OFF);
				}
			}
		}
	};

	public void update() {
		if (mNetTool.isNetworkConnected(getApplicationContext())) {
			if (!isPass) {
				updateApk();
			}
		} else {
			LogUtils.e(Constants.TAG, "Launcher movie update,Network not available");
		}
	}

	/**
	 * 网络状态发生改变的广播
	 */
	private BroadcastReceiver mNetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			LogUtils.i(Constants.TAG, "------mNetReceiver");
			String action = intent.getAction();
            Log.d(TAG, "action:======= "+action);
			/*
			 * if (action.equals(EthernetManager.ETHERNET_STATE_CHANGED_ACTION))
			 * { final int event = intent.getIntExtra(
			 * EthernetManager.EXTRA_ETHERNET_STATE,
			 * EthernetManager.ETHERNET_STATE_UNKNOWN); LogUtils.i(Constants.TAG,
			 * "------mNetReceiver  ETHERNET_STATE_CHANGED_ACTION:" + event);
			 * switch (event) { case EthernetStateTracker.EVENT_HW_CONNECTED:
			 * case
			 * EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED://
			 * ethernet // link if (activityIsRun) {
			 * netStatus.setImageResource(R.drawable.com_status_link); mWireFlag
			 * = true; // weatherHodler.setWeather(); update(); } break; case
			 * EthernetStateTracker.EVENT_HW_DISCONNECTED: case
			 * EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED: //
			 * ethernet // unlink if (activityIsRun) { netStatus
			 * .setImageResource(R.drawable.com_status_unlink); mWireFlag =
			 * false; } break; default: if (activityIsRun) { netStatus
			 * .setImageResource(R.drawable.com_status_unlink); } break; } }
			 */
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(TAG, "onReceive: 1");
				final NetworkInfo networkInfo = (NetworkInfo) intent
						.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				final boolean Connected = networkInfo != null && networkInfo.isConnected();
				if (Connected && (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)) { // ethernet
                    Log.d(TAG, "onReceive: 2");															// connected
					//// mEthernetFlag = true;
					if (activityIsRun) {
						if(handler.hasMessages(UPDATE_NETWORK_UI))
						{
							handler.removeMessages(UPDATE_NETWORK_UI);
						}
						handler.sendEmptyMessageDelayed(UPDATE_NETWORK_UI,1000);
						mWireFlag = true;
						update();
					}
					netStatus.setImageResource(R.drawable.com_status_link);
				} else { // ethernet
							// disconnected
					//// mEthernetFlag = false;
					if (activityIsRun) {
						if(handler.hasMessages(UPDATE_NETWORK_UI))
						{
							handler.removeMessages(UPDATE_NETWORK_UI);
						}
						handler.sendEmptyMessageDelayed(UPDATE_NETWORK_UI,1000);
						mWireFlag = false;
					}
					netStatus.setImageResource(R.drawable.com_status_unlink);
				}
			} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)
					|| action.equals(WifiManager.RSSI_CHANGED_ACTION)
					|| action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                Log.d(TAG, "onReceive: 3");
				if (activityIsRun) {
					updateWifiStatr(intent);
					// weatherHodler.setWeather();
					update();
				}
			} else if (action.equals(PPPOE_STATE_ACTION)) {
				String pppoeState = intent.getStringExtra(PPPOE_STATE_STATUE);
                Log.d(TAG, "onReceive:4");
				if (pppoeState.equals(PPPOE_STATE_CONNECT)) {// pppoe
                    Log.d(TAG, "onReceive: 5");					// link
                    Log.d(TAG, "activityIsRun5:======= "+activityIsRun);
					if (activityIsRun) {
						netStatus.setImageResource(R.drawable.pppoe_conected);
					}
				} else if (pppoeState.equals(PPPOE_STATE_DISCONNECT)) {// pppoe
																		// unlink
					if (activityIsRun) {
						netStatus.setImageResource(R.drawable.com_status_unlink);
					}
				} else if (pppoeState.equals(PPPOE_STATE_AUTHFAILED)) {// pppoe
																		// authfailed
					// netStatus.setImageResource(R.drawable.com_status_unlink);
				} else if (pppoeState.equals(PPPOE_STATE_FAILED)) {// pppoe
																	// state
																	// failed
					if (activityIsRun) {
						netStatus.setImageResource(R.drawable.com_status_unlink);
					}
				}
			}
		}
	};

	private void updateWifiStatr(Intent intent) {
		final String action = intent.getAction();
		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			mWifiEnabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
		} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			final NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			boolean wasConnected = mWifiConnected;
			mWifiConnected = networkInfo != null && networkInfo.isConnected();
			if (mWifiConnected && !wasConnected) {
				WifiInfo info = (WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
				if (info == null) {
					info = mWifiManager.getConnectionInfo();
				}
				if (info != null) {
					mWifiSsid = huntForSsid(info);
				} else {
					mWifiSsid = null;
				}
			} else if (!mWifiConnected) {
				mWifiSsid = null;
			}
			mWifiLevel = 0;
			mWifiRssi = -200;
		} else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			if (mWifiConnected) {
				mWifiRssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
				mWifiLevel = WifiManager.calculateSignalLevel(mWifiRssi, 4);
			}
		}

		if (mWifiConnected) {
			netStatus.setImageResource(WifiIconArray[mWifiLevel]);
		} else {
			netStatus.setImageResource(R.drawable.com_status_unlink);
		}

	}

	private String huntForSsid(WifiInfo info) {
		String ssid = info.getSSID();
		if (ssid != null) {
			return ssid;
		}
		List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration net : networks) {
			if (net.networkId == info.getNetworkId()) {
				return net.SSID;
			}
		}
		return null;
	}

	public int updataNetAndUsbIcon() {
		LogUtils.i(Constants.TAG, "----updataNetAndUsbIcon");
		int netType = -1;

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_WIFI) {
			LogUtils.i(Constants.TAG, "----TYPE_WIFI");
		} else if (nType == ConnectivityManager.TYPE_ETHERNET) {
			LogUtils.i(Constants.TAG, "----TYPE_ETHERNET");
		}
		// else if(nType==ConnectivityManager.TYPE_PPPOE){
		// LogUtils.i(Constants.TAG, "----TYPE_PPPOE");
		// }
		else {
			LogUtils.i(Constants.TAG, "----else");
		}
		return 0;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	Runnable changeInputSource_thread = new Runnable() {
		@Override
		public void run() {
			PackageManager packageManager = getPackageManager();
			Intent intent = null;
			changeInputSource("com.mstar.tv.tvplayer.ui");
			{
				short sourcestatus[] = null;
				try {
					sourcestatus = TvManager.getInstance().setTvosCommonCommand("SetAutoSleepOnStatus");
				} catch (TvCommonException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			intent = packageManager.getLaunchIntentForPackage("com.mstar.tv.tvplayer.ui");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			startActivity(intent);
		}
	};

	public void updateApk() {
		if (mCheckNewVersionTask != null && mCheckNewVersionTask.getStatus() != AsyncTask.Status.FINISHED) {
			mCheckNewVersionTask.cancel(true);
		}
		// 判断是否有新版本,有提示更新，无返回null
		mCheckNewVersionTask = new CheckNewVersionTask();
		mCheckNewVersionTask.execute();
	}

	/**
	 * 检查新版本异步任务
	 *
	 * @author yejb
	 */
	private class CheckNewVersionTask extends AsyncTask<Void, Void, Version> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Version doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return new Update(getApplicationContext()).hasNewVersion();
		}

		@Override
		protected void onPostExecute(Version result) {
			// TODO Auto-generated method stub
			if (result != null) {
				if (result != null) {
					showNewVersionDialog(result);
				}
			}
			super.onPostExecute(result);
		}
	}

	/**
	 * 显示是否下载新版本对话框
	 *
	 * @param version
	 */
	private void showNewVersionDialog(final Version version) {
		if (alertDialog == null) {
			alertDialog = new AlertDialog.Builder(this).setTitle(R.string.new_version_text)
					.setMessage(version.getIntroduction())
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Update update = new Update(LauncherActivity.this);
							update.setVersion(version);
							update.checkUpdate();
						}
					}).setNeutralButton(R.string.skip_this_version, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							isPass = true;
						}
					}).create();
		}
		if (!alertDialog.isShowing()) {
			alertDialog.show();
		}
	}

	Handler handlerChangePip = new Handler() {
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			String packName = b.getString("packName");
			changeInputSource(packName);
		}
	};

	Handler handlerDelay = new Handler() {
		public void handleMessage(Message msg) {
			NeedDelay = false;
		}
	};

	// calendar

	// 小于10前面补0
	private static String getTimeDay(int i) {

		if (i < 10) {
			String s = "0" + String.valueOf(i);
			return s;
		} else {
			return String.valueOf(i);
		}

	}

	String getMonthString(int mouth) {
		String[] monthName = getResources().getStringArray(R.array.month);
		switch (mouth) {
		case Calendar.JANUARY:
			return monthName[0];
		case Calendar.FEBRUARY:
			return monthName[1];
		case Calendar.MARCH:
			return monthName[2];
		case Calendar.APRIL:
			return monthName[3];
		case Calendar.MAY:
			return monthName[4];
		case Calendar.JUNE:
			return monthName[5];
		case Calendar.JULY:
			return monthName[6];
		case Calendar.AUGUST:
			return monthName[7];
		case Calendar.SEPTEMBER:
			return monthName[8];
		case Calendar.OCTOBER:
			return monthName[9];
		case Calendar.NOVEMBER:
			return monthName[10];
		case Calendar.DECEMBER:
			return monthName[11];
		default:
			return null;
		}
	}

	String getWeekString(int week) {
		String[] weekName = getResources().getStringArray(R.array.week);
		switch (week) {
		case Calendar.SUNDAY:
			return weekName[0];
		case Calendar.MONDAY:
			return weekName[1];
		case Calendar.TUESDAY:
			return weekName[2];
		case Calendar.WEDNESDAY:
			return weekName[3];
		case Calendar.THURSDAY:
			return weekName[4];
		case Calendar.FRIDAY:
			return weekName[5];
		case Calendar.SATURDAY:
			return weekName[6];
		default:
			return null;
		}
		
	}
	
	public String getDate() {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String month = getMonthString(cal.get(Calendar.MONTH));
		String week = getWeekString(cal.get(Calendar.DAY_OF_WEEK));
		String day;
		if (month.endsWith("月")) {
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + "日";
			return month + day + "\n" + week;
		} else {
			int i = cal.get(Calendar.DAY_OF_MONTH);//[1,31],day
			if(i == 1){
				day = i+"st ";
			}else if(i == 2){
				day = i+"nd ";
			}else if(i == 3){
				day = i+"rd ";
			}else{
				day = i+"th ";
			}
			return day + month + "\n" + week;
		}
	}
	
	/**
	 * 获取当前时间
	 *
	 * @return 时间字符串 24小时制
	 * @author drowtram
	 */
	public String getStringTime() {
		Time t = new Time();
		t.setToNow(); // 取得系统时间。
		final Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String month = getMonthString(cal.get(Calendar.MONTH));
		
		
		String strTimeFormat = Settings.System.getString(getContentResolver(), Settings.System.TIME_12_24);
		String time = null;
		if((strTimeFormat!=null) && strTimeFormat.equals("24") ){
			String hour = t.hour < 10 ? "0" + (t.hour) : t.hour + ""; // 默认24小时制
			String minute = t.minute < 10 ? "0" + (t.minute) : t.minute + "";
			time = hour + " " + minute;
			tv_am_zh.setVisibility(View.GONE);
			tv_am.setVisibility(View.GONE);
		}else{
			String hour = getHour_12(t.hour);
			String minute = getTimeDay(t.minute);
			if(t.hour < 12){
				tv_am_zh.setText(R.string.time_am);
				tv_am.setText(R.string.time_am);
				if (month.endsWith("月")) {
					tv_am_zh.setVisibility(View.VISIBLE);
					tv_am.setVisibility(View.GONE);
				} else {
					tv_am_zh.setVisibility(View.GONE);
					tv_am.setVisibility(View.VISIBLE);
				}
			    time = hour + " " + minute;
			}else{
				tv_am_zh.setText(R.string.time_pm);
				tv_am.setText(R.string.time_pm);
				if (month.endsWith("月")) {
					tv_am_zh.setVisibility(View.VISIBLE);
					tv_am.setVisibility(View.GONE);
				} else {
					tv_am_zh.setVisibility(View.GONE);
					tv_am.setVisibility(View.VISIBLE);
				}
				time = hour + " " + minute;
			}
		}
		return time;
	}
	private static String getHour_12(int i) {
		int hour = 0;
		if(i > 12){
			hour = i - 12;
		} else {
			hour = i;
		}
		if (hour < 10) {
			return ("0" + String.valueOf(hour));
		} else {
			return String.valueOf(hour);
		}
	}
	

	/**
	 * Implementation of the method from SysShutDownReceiver.Callbacks.
	 */
	public void setSysShutdown() {
		Log.v(TAG, "------------setSysShutdown=");
		bSystemShutdown = true;
	}

	// lixq 20151119 start
	public boolean isPowerOn() {
		Log.d(TAG, "Is Fist Power On: " + (SystemProperties.getBoolean(IS_POWER_ON_PROPERTY, false)));
		if (!SystemProperties.getBoolean(IS_POWER_ON_PROPERTY, false)) {
			SystemProperties.set(IS_POWER_ON_PROPERTY, "true");
			return true;
		} else {
			return false;
		}
	}
	// lixq 20151119 end

	// lixq 20160517 Add to check whether mount the USB start
	private boolean CheckUsbIsExist() {
		// TODO Auto-generated method stub
		boolean ret = false;
		StorageManager storageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] volumes = storageManager.getVolumeList();
		if (volumes != null && volumes.length > 1) {
			String path = "";
			// List all your mount disk
			for (StorageVolume item : volumes) {
				path = item.getPath();
				String state = storageManager.getVolumeState(path);
				// Mount is not successful
				if (state == null || !state.equals(Environment.MEDIA_MOUNTED)) {
					return ret;
				}
			}
			ret = true;
		}
		return ret;
	}
	// lixq 20160517 Add to check whether mount the USB end

	private void onMessage(final Message msg) {
		if (msg != null) {
			switch (msg.what) {
			case WindowMessageID.REFLESH_TIME:
				// Hisa 2015.11.05 进入app后时间标志闪动异常 start
				if ((activityIsRun == false) || (homeAcivityIsForeground == false)) {
					homeHandler.removeMessages(WindowMessageID.REFLESH_TIME);
					break;
				}
				// Hisa 2015.11.05 进入app后时间标志闪动异常 start
				tv_time.setText(getStringTime());
				tv_main_date.setText(getDate());
				if (time_colon.getVisibility() == View.VISIBLE) {
					time_colon.setVisibility(View.GONE);
				} else {
					time_colon.setVisibility(View.VISIBLE);
				}
				homeHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME, 1000);
				break;
			}
		}
	}

	private Handler homeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 调用窗口消息处理函数
			onMessage(msg);
		}
	};

	/**
	 * @class WindowMessageID
	 * @brief 内部消息ID定义类。
	 * @author joychang
	 */
	private class WindowMessageID {

		/**
		 * @brief 刷新数据。
		 */
		public static final int REFLESH_TIME = 0x00000005;
	}

	private void showOnFocusAnimation(final View view) {
		view.bringToFront();// 将当前FrameLayout置为顶层
		Animation mtAnimation = null;
		Animation msAnimation = null;
		mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
		msAnimation = animEffect.ScaleAnimation(1.0F, 1.15F, 1.0F, 1.15F);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(msAnimation);
		set.addAnimation(mtAnimation);
		set.setFillAfter(true);
		view.startAnimation(set);
	}

	/**
	 * 失去焦点缩小
	 *
	 * @param paramInt
	 */
	private void showLoseFocusAinimation(final View view) {
		Animation mAnimation = null;
		Animation mtAnimation = null;
		Animation msAnimation = null;
		AnimationSet set = null;
		mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
		msAnimation = animEffect.ScaleAnimation(1.15F, 1.0F, 1.15F, 1.0F);
		set = new AnimationSet(true);
		set.addAnimation(msAnimation);
		set.addAnimation(mtAnimation);
		set.setFillAfter(true);
		view.startAnimation(set);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		handlertv.postDelayed(changeInputSource_thread, 100); // zjd,20140814.
																// delay to
																// change,wait
																// pip_thread
																// finish
	}
	
	private void initViewPager() {
		mViewPager.setAdapter(myBasePagerAdapter);
		mCirclePageIndicator.setViewPager(mViewPager);
	}

	private void initListener() {
		mViewPager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				int code = ErrorCode.ERROR_UNKNOWN;
				if (mediaList != null && mediaList.size() > 0) {
					if (mQiyiClient != null) {
						code = mQiyiClient.openMedia(mediaList
								.get(mCurrentPosition));
					} else {
						Intent intent = new Intent("qimei.aiqiyi");
						getApplicationContext().sendBroadcast(intent);
					}

				}
			}
		});
		
		mViewPager.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				//判断是否获得焦点
				if (hasFocus) {
					//获得焦点  停止轮播
					stopAutoScroll();
				}else {
					//失去焦点  开始轮播
					startAutoScroll();
				}

			}
		});
		
		mViewPager.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View arg0, int keycode, KeyEvent keyEvent) {
				// TODO Auto-generated method stub
				
				
				if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
				
					if (keycode == KeyEvent.KEYCODE_DPAD_LEFT){
						if (mCurrentPosition == 0) {
							mCurrentPosition = mediaList.size() - 1;
							mViewPager.setCurrentItem(mCurrentPosition, false);
							return true;
						}
					} else if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT){
						if (mCurrentPosition == mediaList.size() - 1) {
							mCurrentPosition = 0;
							mViewPager.setCurrentItem(mCurrentPosition, false);
							return true;
						}
					}
				}
				return false;
			}
		});
		mViewPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				
				mCurrentPosition = position;
				if (mediaList != null && mediaList.size() > 0) {
					qiyiTitle
							.setText(mediaList.get(mCurrentPosition).getName());
				}

			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
			

			}
		});
		isViewPagerFocused = mViewPager.isFocused();
		if (!isViewPagerFocused) {
			startAutoScroll();
		} else {
			stopAutoScroll();
		}
	}
	
	private Runnable autoScrollTask = new Runnable() {
		public void run() {
			if (null != mViewPager) {
				mCurrentPosition++;
				mCurrentPosition %= mViewContainer.size();
				Message message = handler.obtainMessage();
				message.what = SCROLL_TO_NEXT;
				handler.sendMessage(message);
			}
		}

	};

	private void startAutoScroll() {
			mScheduExec = Executors.newScheduledThreadPool(1);
			mScheduExec.scheduleWithFixedDelay(autoScrollTask, 1000 * AUTO_SCROLL_TIME, 1000 * AUTO_SCROLL_TIME, TimeUnit.MILLISECONDS);
	}
	private void stopAutoScroll() {
		if (null!=mScheduExec) {
			mScheduExec.shutdown();
		}
	}

	public class SeriaMedia implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public void seriaMedia(List<Media> mediaList) {
			File mediaFile = null;
			try {
				FileOutputStream outStream = getApplicationContext()
						.openFileOutput("media.txt", Context.MODE_PRIVATE);
				mediaFile = new File(
						"/data/data/com.qiyi.tv.tw.nexgen/files/media.txt");
				if (!mediaFile.exists()) {
					outStream = getApplicationContext().openFileOutput(
							"media.txt", Context.MODE_PRIVATE);
				}
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(mediaFile));
				oos.writeObject(mediaList);
				oos.flush();
				oos.close();
			} catch (Exception e) {
				// TODO: handle exception
			}

			try {
				String command = "chmod 777 " + mediaFile;
				Log.i(TAG, "command = " + command);
				Runtime runtime = Runtime.getRuntime();
				Process proc = runtime.exec(command);
			} catch (IOException e) {
				Log.i(TAG, "chmod fail!!!!");
				e.printStackTrace();
			}
		}

		public List<Media> deseriaMedia(String path) {
			ObjectInputStream oin = null;// 局部变量必须要初始化
			try {
				if (path.equals("assets")) {
					oin = new ObjectInputStream(getAssets().open("media.txt"));
				} else {
					oin = new ObjectInputStream(new FileInputStream(path));
				}

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			List<Media> list = null;
			List<Media> mediaList = new ArrayList<Media>();
			try {
				list = (List<Media>) oin.readObject();// 由Object对象向下转型为
															// List<Media>
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (path.equals("assets")) {
				for (int i = 0; i < 8; i++) {
					mediaList.add(list.get(i));
				}
			} else {
				mediaList = list;
			}
			return mediaList;
		}

	}

	public List<Bitmap> getBitmapListFromAssets()
	{
		List<Bitmap> bitmapList=new ArrayList<Bitmap>();
		try {
			for (int i = 1; i < 9; i++) {
				InputStream inputStream = getResources().getAssets().open("aiqiyi_picture_0"+i+".jpg");
				Bitmap bitmap =  BitmapFactory.decodeStream(inputStream);
				bitmapList.add(bitmap);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		
		return bitmapList;
	}
	//lixq 20161021 start
	private void setSubtitleEncode() {
		Configuration config = null;
		IActivityManager am = ActivityManagerNative.getDefault();
		try {
			config = am.getConfiguration();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (config == null)
			return;
		String currLanguage = "en";
		String currCountry = "US";
		currLanguage = config.locale.getLanguage();
		currCountry = config.locale.getCountry();
		Log.v(TAG, currLanguage + "=========" + currCountry);

		if (currLanguage.equals("zh") && currCountry.equals("TW")) {
			SystemProperties.set("ms.subtitle.language", "windows-950");
			Log.i(TAG, "change subtitle to taiwan");
		} else if (currLanguage.equals("en"))
		{
			SystemProperties.set("ms.subtitle.language", "windows-1252");
			Log.i(TAG, "change subtitle to en");
		}
	}
	//lixq 20161021 end
	
	View.OnClickListener listener = new View.OnClickListener(){
		@Override
        public void onClick(View v) {
            int code = ErrorCode.ERROR_UNKNOWN;
			if (mediaList != null && mediaList.size() > 0) {
				if (mQiyiClient != null) {
					code = mQiyiClient.openMedia(mediaList
						.get(mCurrentPosition));
				} else {
					Intent intent = new Intent("qimei.aiqiyi");
					getApplicationContext().sendBroadcast(intent);
				}
			}
        }
	};
}
