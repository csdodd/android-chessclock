package com.chess.ui.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.Web;
import com.chess.backend.WebService;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameItem;
import com.chess.ui.activities.HomeScreenActivity;
import com.chess.ui.activities.LoginScreenActivity;
import com.chess.ui.interfaces.BoardToGameActivityFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

public abstract class CoreActivity extends Activity implements BoardToGameActivityFace {

	protected final static int INIT_ACTIVITY = -1;
	protected final static int ERROR_SERVER_RESPONSE = -2;

	protected MainApp mainApp;
	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	protected String response = StaticData.SYMBOL_EMPTY;
	protected String responseRepeatable = StaticData.SYMBOL_EMPTY;
	protected BackgroundChessDrawable backgroundChessDrawable;

    public boolean mIsBound;
	public WebService appService = null;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;


	public abstract void update(int code);

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		backgroundChessDrawable =  new BackgroundChessDrawable(this);
		mainApp = (MainApp) getApplication();
		extras = getIntent().getExtras();

		// get global Shared Preferences
//		if (preferences == null) {
//			mainApp.setSharedData(getSharedPreferences(StaticData.SHARED_DATA_NAME, MODE_PRIVATE));
//			mainApp.setSharedDataEditor(preferences.edit());
//		}

		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();


		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		View mainView = findViewById(R.id.mainView);
		if(mainView != null)
			mainView.setBackgroundDrawable(backgroundChessDrawable);
	}

	public boolean doBindService() {
		mIsBound = bindService(new Intent(this, WebService.class), onService,
				Context.BIND_AUTO_CREATE);
		return mIsBound;
	}

	public void doUnbindService() {
		if (mIsBound) {
			unbindService(onService);
			mIsBound = false;
		}
	}

	public ServiceConnection onService = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			appService = ((WebService.LocalBinder) rawBinder).getService();
			update(INIT_ACTIVITY); // TODO send broadcast or call local method, but with readable arguments
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			appService = null;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		doBindService();
		registerReceivers();

		if (preferences.getLong(AppConstants.FIRST_TIME_START, 0) == 0) {
			preferencesEditor.putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
		long startDay = preferences.getLong(AppConstants.START_DAY, 0);
		if (startDay == 0 || !DateUtils.isToday(startDay)) {
			checkUpdate();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		doUnbindService();
		if (appService != null && appService.getRepeatableTimer() != null) {
			appService.stopSelf();
			appService.getRepeatableTimer().cancel();
			appService = null;
		}

		unRegisterReceivers();

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		//mainApp.setForceBannerAdOnFailedLoad(false);

		if (progressDialog != null)
			progressDialog.dismiss();
	}


	private void registerReceivers(){
		registerReceiver(receiver, new IntentFilter(IntentConstants.BROADCAST_ACTION));
		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
		registerReceiver(lccReconnectingInfoReceiver, new IntentFilter(IntentConstants.FILTER_RECONNECT_INFO));
 		registerReceiver(informAndExitReceiver, new IntentFilter(IntentConstants.FILTER_EXIT_INFO));
		registerReceiver(obsoleteProtocolVersionReceiver, new IntentFilter(IntentConstants.FILTER_PROTOCOL_VERSION));
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));
	}

	private void unRegisterReceivers(){
		unregisterReceiver(receiver);
		unregisterReceiver(lccLoggingInInfoReceiver);
		unregisterReceiver(lccReconnectingInfoReceiver);
		unregisterReceiver(informAndExitReceiver);
		unregisterReceiver(obsoleteProtocolVersionReceiver);
		unregisterReceiver(infoMessageReceiver);
	}

	private void checkUserTokenAndStartActivity() {
		if (!AppData.getUserName(getContext()).equals(StaticData.SYMBOL_EMPTY)) {
			final Intent intent = new Intent(this, HomeScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			startActivity(new Intent(this, LoginScreenActivity.class));
		}
	}
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// getting extras
			Bundle rExtras = intent.getExtras();
			boolean repeatable;
			String resp;
			int retCode;
			try {
				repeatable = rExtras.getBoolean(AppConstants.REPEATABLE_TASK);
				resp = rExtras.getString(AppConstants.REQUEST_RESULT);
				if (repeatable) {
					responseRepeatable = resp;
				} else {
					response = resp;
				}

				retCode = rExtras.getInt(AppConstants.CALLBACK_CODE);
			} catch (Exception e) {
				update(ERROR_SERVER_RESPONSE);
				return;
			}

			if (Web.getStatusCode() == -1)
				mainApp.noInternet = true;
			else {
				if (mainApp.noInternet) { /* mainApp.showToast("Online mode!"); */
					mainApp.offline = false;
				}
				mainApp.noInternet = false;
			}

			if (resp.contains(RestHelper.R_SUCCESS))
				update(retCode);
			else {
				if (resp.length() == 0) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				String title = getString(R.string.error);
				String message;
				if (resp.contains(RestHelper.R_ERROR)) {
					message = resp.split("[+]")[1];
				} else {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				if (message == null || message.trim().equals(StaticData.SYMBOL_EMPTY)) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
						.setMessage(message)
						.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								update(ERROR_SERVER_RESPONSE);
							}
						}).create().show();
			}
		}
	};


	public BroadcastReceiver lccReconnectingInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	public BroadcastReceiver informAndExitReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String message = intent.getExtras().getString(AppConstants.MESSAGE);
			if (message == null || message.trim().equals(StaticData.SYMBOL_EMPTY)) {
				return;
			}
			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE)).setMessage(message)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
                            final String password = preferences.getString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
                            final Class clazz = (password == null || password.equals(StaticData.SYMBOL_EMPTY)) ? LoginScreenActivity.class : HomeScreenActivity.class;
                            final Intent intent = new Intent(getContext(), clazz);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mainApp.startActivity(intent);
						}
					}).create().show();
		}
	};

	public BroadcastReceiver obsoleteProtocolVersionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
					.setTitle(R.string.version_check)
					.setMessage(R.string.version_is_obsolete_update)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							final Handler handler = new Handler();
							handler.post(new Runnable() {
								@Override
								public void run() {
									startActivity(new Intent(Intent.ACTION_VIEW, Uri
											.parse(RestHelper.PLAY_ANDROID_HTML)));
								}
							});
							final Intent intent = new Intent(getContext(), HomeScreenActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mainApp.startActivity(intent);
						}
					}).create().show();
		}
	};

	private final BroadcastReceiver infoMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info(AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			final TextView messageView = new TextView(context);
			messageView.setMovementMethod(LinkMovementMethod.getInstance());
			messageView.setText(Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE)));
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			messageView.setGravity(Gravity.CENTER);

			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(true)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE)).setView(messageView)
					.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, int whichButton) {
							final Handler handler = new Handler();
							handler.post(new Runnable() {
								@Override
								public void run() {
									dialog.dismiss();
								}
							});
						}
					}).create().show();
		}
	};

	private final BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info(AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);
			manageConnectingIndicator(enable, intent.getExtras().getString(AppConstants.MESSAGE));
		}
	};

	private void manageConnectingIndicator(boolean enable, String message) {

	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) { // TODO handle unregister receiver correctly
		try {
			super.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// hack for Android's IllegalArgumentException: Receiver not registered
		}
	}

	public Boolean isUserColorWhite() {
		try {
			return mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME).toLowerCase()
					.equals(AppData.getUserName(getContext()));
		} catch (Exception e) {
			return null;
		}
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FlurryData.API_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}
	/*
	 * protected void showGameEndAds(LinearLayout adviewWrapper) { if
	 * (mainApp.isAdviewPaused()) {
	 * MobclixHelper.resumeAdview(getRectangleAdview(), mainApp); } }
	 */

	/*
	 * protected void showAds(MobclixMMABannerXLAdView adview) {
	 * if(!isShowAds()) { adview.setVisibility(View.GONE); } else {
	 * adview.setVisibility(View.VISIBLE);
	 * //adview.setAdUnitId("agltb3B1Yi1pbmNyDQsSBFNpdGUYmrqmAgw");
	 * //adview.setAdUnitId("agltb3B1Yi1pbmNyDAsSBFNpdGUYkaoMDA"); //test
	 * //adview.loadAd(); } }
	 */

	private void checkUpdate() {  // TODO show progress
        new CheckUpdateTask(this).execute(AppConstants.URL_GET_ANDROID_VERSION);
	}

	protected void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int msgId){
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}

	@Override
	public GameItem getCurrentGame() {
		return mainApp.getCurrentGame();
	}

	protected Context getContext(){
		return this;
	}
}