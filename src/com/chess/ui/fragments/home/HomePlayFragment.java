package com.chess.ui.fragments.home;

import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.GetAndSaveUserStats;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.AppConstants;
import com.chess.statics.IntentConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.configs.CompGameConfig;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.comp.GameCompFragment;
import com.chess.ui.fragments.comp.GameCompFragmentTablet;
import com.chess.ui.fragments.daily.DailyGameOptionsFragment;
import com.chess.ui.fragments.friends.ChallengeFriendFragment;
import com.chess.ui.fragments.live.LiveGameOptionsFragment;
import com.chess.ui.fragments.live.LiveGameWaitFragment;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.widgets.RelLayout;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 18:29
 */
public class HomePlayFragment extends CommonLogicFragment implements SlidingMenu.OnOpenedListener{

	private TextView liveRatingTxt;
	private TextView dailyRatingTxt;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private DailyGameConfig.Builder dailyGameConfigBuilder;
//	private LiveGameConfig.Builder liveGameConfigBuilder;
	private int positionMode;
	private List<View> liveOptionsGroup;
	private HashMap<Integer, Button> liveButtonsModeMap;
	private boolean liveOptionsVisible;
	private Button liveTimeSelectBtn;
	private String[] newGameButtonsArray;

	private LinearLayout dailyGameQuickOptions;
	private RelLayout liveOptionsView;
	private boolean liveFullOptionsVisible;
	private boolean dailyFullOptionsVisible;
	private LiveGameOptionsFragment liveGameOptionsFragment;
	private DailyGameOptionsFragment dailyGameOptionsFragment;
	private TextView liveExpandIconTxt;
	private TextView dailyExpandIconTxt;
	private IntentFilter statsUpdateFilter;
	private boolean statsLoaded;
	private StatsSavedReceiver statsSavedReceiver;

	public HomePlayFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, CENTER_MODE);
		setArguments(bundle);
	}

	public static HomePlayFragment createInstance(int mode) {
		HomePlayFragment fragment = new HomePlayFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			positionMode = getArguments().getInt(MODE);
		} else {
			positionMode = savedInstanceState.getInt(MODE);
		}

		dailyGameConfigBuilder = new DailyGameConfig.Builder();
		createChallengeUpdateListener = new CreateChallengeUpdateListener();

		getActivityFace().addOnOpenMenuListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_play_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		RelativeLayout liveHomeOptionsFrame = (RelativeLayout) view.findViewById(R.id.liveHomeOptionsFrame);

		liveExpandIconTxt = (TextView) view.findViewById(R.id.liveExpandIconTxt);
		dailyExpandIconTxt = (TextView) view.findViewById(R.id.dailyExpandIconTxt);
		liveOptionsView = (RelLayout) view.findViewById(R.id.liveOptionsView);
		dailyGameQuickOptions = (LinearLayout) view.findViewById(R.id.dailyGameQuickOptions);

		if (positionMode == CENTER_MODE) {
			inflater.inflate(R.layout.new_home_live_options_view, liveHomeOptionsFrame, true);
			liveExpandIconTxt.setText(R.string.ic_right);
			dailyExpandIconTxt.setText(R.string.ic_right);
		} else {
			inflater.inflate(R.layout.new_right_live_options_view, liveHomeOptionsFrame, true);
			View liveHeaderView = view.findViewById(R.id.liveHeaderView);
			View dailyHeaderView = view.findViewById(R.id.dailyHeaderView);
			View vsCompHeaderView = view.findViewById(R.id.vsCompHeaderView);
			ButtonDrawableBuilder.setBackgroundToView(liveHeaderView, R.style.ListItem_Header_Dark);
			ButtonDrawableBuilder.setBackgroundToView(dailyHeaderView, R.style.ListItem_Header_2_Dark);
			ButtonDrawableBuilder.setBackgroundToView(vsCompHeaderView, R.style.ListItem_Header_Dark);
		}

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		setRatings();
		loadRecentOpponents();

		if (statsUpdateFilter != null) {
			statsSavedReceiver = new StatsSavedReceiver();
			registerReceiver(statsSavedReceiver, statsUpdateFilter);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivityFace().removeOnOpenMenuListener(this);

		if (statsUpdateFilter != null) {
			unRegisterMyReceiver(statsSavedReceiver);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	private class StatsSavedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			statsLoaded = true;
			showLoadingProgress(false);

			setRatings();
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveTimeSelectBtn) {
			toggleLiveOptionsView();
		} else if (view.getId() == R.id.liveHeaderView) {
			if (positionMode == RIGHT_MENU_MODE) {
				liveFullOptionsVisible = !liveFullOptionsVisible;
				liveOptionsView.setVisibility(liveFullOptionsVisible ? View.GONE : View.VISIBLE);
				if (liveFullOptionsVisible) {
					liveExpandIconTxt.setText(R.string.ic_up);
					if (liveGameOptionsFragment == null) {
						liveGameOptionsFragment = new LiveGameOptionsFragment();
					}

					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.replace(R.id.liveOptionsFrame, liveGameOptionsFragment).commit();
				} else {
					liveExpandIconTxt.setText(R.string.ic_down);
					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.remove(liveGameOptionsFragment).commit();
				}
			} else {
				getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(CENTER_MODE));
				getActivityFace().toggleRightMenu();
			}

		} else if (view.getId() == R.id.livePlayBtn) {
			createLiveChallenge();
			if (positionMode == RIGHT_MENU_MODE) {
				getActivityFace().toggleRightMenu();
			}
		} else if (view.getId() == R.id.dailyHeaderView) {
			if (positionMode == RIGHT_MENU_MODE) {
				dailyFullOptionsVisible = !dailyFullOptionsVisible;
				dailyGameQuickOptions.setVisibility(dailyFullOptionsVisible ? View.GONE : View.VISIBLE);
				if (dailyFullOptionsVisible) {
					dailyExpandIconTxt.setText(R.string.ic_up);
					if (dailyGameOptionsFragment == null) {
						dailyGameOptionsFragment = new DailyGameOptionsFragment();
					}

					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.replace(R.id.dailyOptionsFrame, dailyGameOptionsFragment).commit();
				} else {
					dailyExpandIconTxt.setText(R.string.ic_down);
					FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
					transaction.remove(dailyGameOptionsFragment).commit();
				}
			} else {
				getActivityFace().changeRightFragment(DailyGameOptionsFragment.createInstance(CENTER_MODE));
				getActivityFace().toggleRightMenu();
			}

		} else if (view.getId() == R.id.dailyPlayBtn) {
			createDailyChallenge();
		} else if (view.getId() == R.id.inviteFriendView1) {
			dailyFullOptionsVisible = true;
			dailyGameQuickOptions.setVisibility(View.GONE);
			dailyExpandIconTxt.setText(R.string.ic_up);
			if (dailyGameOptionsFragment == null) {
				dailyGameOptionsFragment = DailyGameOptionsFragment.createInstance(RIGHT_MENU_MODE, firstFriendUserName);
			}

			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.replace(R.id.dailyOptionsFrame, dailyGameOptionsFragment).commit();
		} else if (view.getId() == R.id.inviteFriendView2) {
			dailyFullOptionsVisible = true;
			dailyGameQuickOptions.setVisibility(View.GONE);
			dailyExpandIconTxt.setText(R.string.ic_up);
			dailyGameOptionsFragment = DailyGameOptionsFragment.createInstance(RIGHT_MENU_MODE, secondFriendUserName);

			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.replace(R.id.dailyOptionsFrame, dailyGameOptionsFragment).commit();
		} else if (view.getId() == R.id.playFriendView) {
			ChallengeFriendFragment challengeFriendFragment;
			if (positionMode == CENTER_MODE) {
				challengeFriendFragment = ChallengeFriendFragment.createInstance(CENTER_MODE);
				getActivityFace().toggleRightMenu();
			} else {
				challengeFriendFragment = new ChallengeFriendFragment();
			}
			getActivityFace().changeRightFragment(challengeFriendFragment);
		} else if (view.getId() == R.id.vsCompHeaderView) {
			CompGameConfig.Builder gameConfigBuilder = new CompGameConfig.Builder();
			CompGameConfig compGameConfig = gameConfigBuilder.setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE).build();

			if (!isTablet) {
				getActivityFace().openFragment(GameCompFragment.createInstance(compGameConfig));
			} else {
				getActivityFace().openFragment(GameCompFragmentTablet.createInstance(compGameConfig));
			}

			if (positionMode == RIGHT_MENU_MODE) {
				getActivityFace().toggleRightMenu();
			}
		} else {
			handleLiveModeClicks(view);
		}
	}

	private void handleLiveModeClicks(View view) {
		int id = view.getId();
		boolean liveModeButton = false;
		for (Button button : liveButtonsModeMap.values()) {
			if (id == button.getId()) {
				liveModeButton = true;
				break;
			}
		}

		if (liveModeButton) {
			for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
				Button button = buttonEntry.getValue();
				button.setSelected(false);
				if (id == button.getId()) {
					setDefaultQuickLiveMode(view, buttonEntry.getKey());
				}
			}
		}
	}

	private void setDefaultQuickLiveMode(View view, int mode) {
		view.setSelected(true);
		liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[mode]));
		getAppData().setDefaultLiveMode(mode);
	}

	private void toggleLiveOptionsView() {
		liveOptionsVisible = !liveOptionsVisible;
		for (View view : liveOptionsGroup) {
			view.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
		}

		int selectedLiveTimeMode = getAppData().getDefaultLiveMode();
		for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
			Button button = buttonEntry.getValue();
			button.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
			if (liveOptionsVisible) {
				if (selectedLiveTimeMode == buttonEntry.getKey()) {
					button.setSelected(true);
				}
			}
		}
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = dailyGameConfigBuilder.build();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.challenge_created, R.string.you_will_notified_when_game_starts);
		}
	}

	@Override
	public void onOpened() {

	}

	@Override
	public void onOpenedRight() {
		if (getActivity() == null) {
			return;
		}
		if (positionMode == RIGHT_MENU_MODE && !isPaused) {
			setRatings();
			loadRecentOpponents();
		}
	}

	private void setRatings() {
		if (liveRatingTxt == null) { // if we have closed this view
			return;
		}

		// set live rating
		int liveRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), getUsername());
		liveRatingTxt.setText(String.valueOf(liveRating));

		// set daily rating
		int dailyRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal(), getUsername());
		dailyRatingTxt.setText(String.valueOf(dailyRating));

		if (liveRating == 0 || dailyRating == 0 && !statsLoaded) { // if stats were not save
			showLoadingProgress(true);

			getActivity().startService(new Intent(getActivity(), GetAndSaveUserStats.class));

			statsUpdateFilter = new IntentFilter(IntentConstants.STATS_SAVED);

			statsLoaded = false;
		}  else {
			statsLoaded = true;
		}

	}

	private void createLiveChallenge() {
		LiveGameConfig gameConfig = getAppData().getLiveGameConfigBuilder().build();
		getActivityFace().openFragment(LiveGameWaitFragment.createInstance(gameConfig));
	}

	private void widgetsInit(View view) {
		int darkBtnColor = getResources().getColor(R.color.stats_label_grey);

		inviteFriendView1 = view.findViewById(R.id.inviteFriendView1);
		inviteFriendView2 = view.findViewById(R.id.inviteFriendView2);
		friendUserName1Txt = (TextView) view.findViewById(R.id.friendUserName1Txt);
		friendRealName1Txt = (TextView) view.findViewById(R.id.friendRealName1Txt);
		friendUserName2Txt = (TextView) view.findViewById(R.id.friendUserName2Txt);
		friendRealName2Txt = (TextView) view.findViewById(R.id.friendRealName2Txt);
		TextView vsRandomTxt = (TextView) view.findViewById(R.id.vsRandomTxt);
		TextView challengeFriendTxt = (TextView) view.findViewById(R.id.challengeFriendTxt);

		if (positionMode == CENTER_MODE) { // we use white background and dark titles for centered mode
			int darkTextColor = getResources().getColor(R.color.new_subtitle_dark_grey);

			View homePlayScrollView = view.findViewById(R.id.homePlayScrollView);
			homePlayScrollView.setBackgroundResource(R.color.white);

			TextView liveChessHeaderTxt = (TextView) view.findViewById(R.id.liveChessHeaderTxt);
			Button liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
			TextView dailyChessHeaderTxt = (TextView) view.findViewById(R.id.dailyChessHeaderTxt);
			TextView vsComputerHeaderTxt = (TextView) view.findViewById(R.id.vsComputerHeaderTxt);

			liveChessHeaderTxt.setTextColor(darkTextColor);
			liveTimeSelectBtn.setTextColor(darkBtnColor);
			dailyChessHeaderTxt.setTextColor(darkTextColor);
			vsRandomTxt.setTextColor(darkTextColor);
			friendUserName1Txt.setTextColor(darkTextColor);
			friendRealName1Txt.setTextColor(darkTextColor);
			friendUserName2Txt.setTextColor(darkTextColor);
			friendRealName2Txt.setTextColor(darkTextColor);
			challengeFriendTxt.setTextColor(darkTextColor);
			vsComputerHeaderTxt.setTextColor(darkTextColor);
		} else {
//			vsRandomTxt.setTextColor(themeFontColorStateList);
//			friendUserName1Txt.setTextColor(themeFontColorStateList);
//			friendRealName1Txt.setTextColor(themeFontColorStateList);
//			friendUserName2Txt.setTextColor(themeFontColorStateList);
//			friendRealName2Txt.setTextColor(themeFontColorStateList);
//			challengeFriendTxt.setTextColor(themeFontColorStateList);
		}

		liveRatingTxt = (TextView) view.findViewById(R.id.liveRatingTxt);
		dailyRatingTxt = (TextView) view.findViewById(R.id.dailyRatingTxt);

		liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
		liveTimeSelectBtn.setOnClickListener(this);
		view.findViewById(R.id.livePlayBtn).setOnClickListener(this);
		view.findViewById(R.id.dailyPlayBtn).setOnClickListener(this);
		view.findViewById(R.id.playFriendView).setOnClickListener(this);
		view.findViewById(R.id.liveHeaderView).setOnClickListener(this);
		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);
		view.findViewById(R.id.vsCompHeaderView).setOnClickListener(this);

		{ // live options
			if (JELLY_BEAN_PLUS_API) {
				ViewGroup liveOptionsView = (ViewGroup) view.findViewById(R.id.homePlayLinLay);
				LayoutTransition layoutTransition = liveOptionsView.getLayoutTransition();
				layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
			}

			liveOptionsGroup = new ArrayList<View>();
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelStandardTxt));
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelBlitzTxt));
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelBulletTxt));

			liveButtonsModeMap = new HashMap<Integer, Button>();
			liveButtonsModeMap.put(0, (Button) view.findViewById(R.id.standard1SelectBtn));
			liveButtonsModeMap.put(1, (Button) view.findViewById(R.id.blitz1SelectBtn));
			liveButtonsModeMap.put(2, (Button) view.findViewById(R.id.blitz2SelectBtn));
			liveButtonsModeMap.put(3, (Button) view.findViewById(R.id.bullet1SelectBtn));
			liveButtonsModeMap.put(4, (Button) view.findViewById(R.id.standard2SelectBtn));
			liveButtonsModeMap.put(5, (Button) view.findViewById(R.id.blitz3SelectBtn));
			liveButtonsModeMap.put(6, (Button) view.findViewById(R.id.blitz4SelectBtn));
			liveButtonsModeMap.put(7, (Button) view.findViewById(R.id.bullet2SelectBtn));

			int mode = getAppData().getDefaultLiveMode();
			darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
				int key = buttonEntry.getKey();
				buttonEntry.getValue().setText(getLiveModeButtonLabel(newGameButtonsArray[key]));
				buttonEntry.getValue().setOnClickListener(this);

				if (positionMode == CENTER_MODE) {
					buttonEntry.getValue().setTextColor(darkBtnColor);
				}

				if (key == mode) {
					setDefaultQuickLiveMode(buttonEntry.getValue(), buttonEntry.getKey());
				}
			}
		}
	}

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(Symbol.SLASH)) { // "5 | 2"
			return label;
		} else { // "10 min"
			return getString(R.string.min_arg, label);
		}
	}
}
/*
page 9 is the "New Game" screen. that should show if they have NO games (in progress, or completed).
it should also show if they click on the NEW GAME button http://i.imgur.com/QOz3DQB.png  HomePlaySetupFragment

if they have games where it is their turn to move, show screen 10 - hide the NEW GAME button and just show the games.

if they have no moves to make, show the NEW GAME button as in screen 11.

screen 12 shows the completed games. those are ALWAYS there, at the bottom of games in progress.
	 */

/*
those are to challenge your friend to play. it just creates a challenge.

those should be random friends who have been online in the last 30 days. (new api! :D)

never display more than 2. but if only 1, or none, show only 1, or none :)

(Friend Name) means their real name. so, for dallin, i would see:

ignoble (Dallin)    [invite]

invite button will just automatically create a challenge, and then show a success message!

play a friend i think is supposed to open friends screen, yes.

Auto-Match should be just a random, open, rated, 3-day seek.
	 */