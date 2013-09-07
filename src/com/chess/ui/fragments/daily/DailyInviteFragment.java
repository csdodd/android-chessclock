package com.chess.ui.fragments.daily;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.RestHelper;
import com.chess.model.DataHolder;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.DailyChallengeItem;
import com.chess.backend.entity.api.UserItem;
import com.chess.backend.image_load.ImageDownloaderToListener;
import com.chess.backend.image_load.ImageReadyListenerLight;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameNetworkFace;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.game_controls.ControlsDailyView;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.06.13
 * Time: 13:56
 */
public class DailyInviteFragment extends GameBaseFragment implements GameNetworkFace {

	private static final int CREATE_CHALLENGE_UPDATE = 2;
	private static final String ERROR_TAG = "send request failed popup";
	protected int AVATAR_SIZE = 48;

	private ControlsDailyView controlsDailyView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private String[] countryNames;
	private int[] countryCodes;
	private GameDailyUpdatesListener createChallengeUpdateListener;
	private ImageDownloaderToListener imageDownloader;
	private DailyChallengeItem.Data challengeItem;
	private GameBaseFragment.LabelsConfig labelsConfig;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private ChessBoardDailyView boardView;
	private TextView inviteDetails1Txt;
	private TextView inviteTitleTxt;
	private int successToastMsgId;
	private DailyUpdateListener challengeInviteUpdateListener;

	public DailyInviteFragment() { }

	public static DailyInviteFragment createInstance(DailyChallengeItem.Data challengeItem) {
		DailyInviteFragment fragment = new DailyInviteFragment();
		fragment.challengeItem = challengeItem;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_daily_invite_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if(challengeItem.getGameType() == RestHelper.V_GAME_CHESS_960) {
			setTitle(R.string.daily_960);
		} else {
			setTitle(R.string.daily);
		}

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		init();
		adjustBoard();
	}

	@Override
	public Boolean isUserColorWhite() {
		return null;
	}

	@Override
	public Long getGameId() {
		return null;
	}

	@Override
	public void showOptions(View view) {

	}

	@Override
	public void newGame() {

	}

	@Override
	public void switch2Analysis() {

	}

	@Override
	public void updateAfterMove() {

	}

	@Override
	public void invalidateGameScreen() {

	}

	@Override
	public String getWhitePlayerName() {
		return null;
	}

	@Override
	public String getBlackPlayerName() {
		return null;
	}

	@Override
	public boolean currentGameExist() {
		return false;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardOnline.getInstance(this);
	}

	@Override
	public void toggleSides() {

	}

	@Override
	protected void restoreGame() {

	}

	public void init() {
		labelsConfig = new GameBaseFragment.LabelsConfig();
		createChallengeUpdateListener = new GameDailyUpdatesListener(CREATE_CHALLENGE_UPDATE);
		challengeInviteUpdateListener = new DailyUpdateListener();

		imageDownloader = new ImageDownloaderToListener(getActivity());

		countryNames = getResources().getStringArray(R.array.new_countries);
		countryCodes = getResources().getIntArray(R.array.new_country_ids);
	}

	private void adjustBoard() {
		DailyChallengeItem.Data currentGame = challengeItem;
		challengeItem.getOpponentDrawCount();
		challengeItem.getOpponentWinCount();
		challengeItem.getOpponentLossCount();
		challengeItem.isRated();
		challengeItem.getColor(); // TODO tell server to rename the same
		int daysPerMove = challengeItem.getDaysPerMove();

		{ // overlay fill
			inviteTitleTxt.setText(getString(R.string.vs) + StaticData.SYMBOL_SPACE + challengeItem.getOpponentUsername()
					+ StaticData.SYMBOL_SPACE + StaticData.SYMBOL_LEFT_PAR + challengeItem.getOpponentRating() + StaticData.SYMBOL_RIGHT_PAR);

			String detailsStr1;
			if (challengeItem.isRated()) {
				detailsStr1 = getString(R.string.rated);
			} else {
				detailsStr1 = getString(R.string.unrated);
			}

			detailsStr1 += StaticData.SYMBOL_LEFT_PAR + "W +" + challengeItem.getOpponentWinCount()
					+ "/L -" + challengeItem.getOpponentLossCount() + "/D -"
					+ challengeItem.getOpponentDrawCount() + StaticData.SYMBOL_RIGHT_PAR;

			detailsStr1 += StaticData.SYMBOL_NEW_STR + daysPerMove + StaticData.SYMBOL_SPACE + getString(R.string.days_move);
			String color = getString(R.string.random);
			if (challengeItem.getColor() == RestHelper.P_BLACK) {
				color = getString(R.string.black);
			} else if (challengeItem.getColor() == RestHelper.P_WHITE) {
				color = getString(R.string.white);
			}
			detailsStr1 += StaticData.SYMBOL_NEW_STR + getString(R.string.i_play_as) + StaticData.SYMBOL_SPACE + color;

			inviteDetails1Txt.setText(detailsStr1);
		}

		labelsConfig.userSide = ChessBoard.WHITE_SIDE;
		labelsConfig.topPlayerName = currentGame.getOpponentUsername();
		labelsConfig.topPlayerRating = String.valueOf(currentGame.getOpponentRating());
		labelsConfig.bottomPlayerName = getAppData().getUsername();
		labelsConfig.bottomPlayerRating = String.valueOf(getAppData().getUserDailyRating());
		labelsConfig.topPlayerAvatar = currentGame.getOpponentAvatar();
		labelsConfig.bottomPlayerAvatar = getAppData().getUserAvatar();

		DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);

		controlsDailyView.enableGameControls(true);

		topPanelView.setTimeRemain(getString(R.string.days_arg, daysPerMove));
		bottomPanelView.setTimeRemain(getString(R.string.days_arg, daysPerMove));
		topPanelView.showTimeLeftIcon(false);
		bottomPanelView.showTimeLeftIcon(false);

		bottomPanelView.setPlayerPremiumIcon(AppUtils.getPremiumIcon(getAppData().getUserPremiumStatus()));

		// set opponent data info
		topPanelView.setPlayerName(challengeItem.getOpponentUsername());
		topPanelView.setPlayerRating(String.valueOf(challengeItem.getOpponentRating()));

		// set user data info
		bottomPanelView.setPlayerName(getUsername());

//		bottomPanelView.setPlayerRating();

		{ // get users info // TODO check info from local DB
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken());
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.CURRENT_USER)).executeTask(loadItem);
		}

		{ // get opponent info
			LoadItem loadItem = LoadHelper.getUserInfo(getUserToken(), labelsConfig.topPlayerName);
			new RequestJsonTask<UserItem>(new GetUserUpdateListener(GetUserUpdateListener.OPPONENT)).executeTask(loadItem);
		}

		imageDownloader.download(labelsConfig.topPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.TOP_AVATAR), AVATAR_SIZE);
		imageDownloader.download(labelsConfig.bottomPlayerAvatar, new ImageUpdateListener(ImageUpdateListener.BOTTOM_AVATAR), AVATAR_SIZE);
	}


	private class GetUserUpdateListener extends ChessUpdateListener<UserItem> {

		static final int CURRENT_USER = 0;
		static final int OPPONENT = 1;

		private int itemCode;

		public GetUserUpdateListener(int itemCode) {
			super(UserItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void updateData(UserItem returnedObj) {
			super.updateData(returnedObj);
			UserItem.Data userInfo = returnedObj.getData();
			if (itemCode == CURRENT_USER) {
				bottomPanelView.setPlayerRating(String.valueOf(userInfo.getPoints()));
				labelsConfig.bottomPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, userInfo.getCountryId());
				bottomPanelView.setPlayerFlag(labelsConfig.bottomPlayerCountry);
				bottomPanelView.setPlayerPremiumIcon(userInfo.getPremiumStatus());
			} else if (itemCode == OPPONENT) {
				labelsConfig.topPlayerCountry = AppUtils.getCountryIdByName(countryNames, countryCodes, userInfo.getCountryId());
				topPanelView.setPlayerFlag(labelsConfig.topPlayerCountry);
				topPanelView.setPlayerPremiumIcon(userInfo.getPremiumStatus());
			}
		}
	}

	@Override
	public void showSubmitButtonsLay(boolean show) {

	}

	@Override
	public void switch2Chat() {

	}

	@Override
	public void playMove() {
		acceptChallenge();
	}

	@Override
	public void cancelMove() {
		declineChallenge();
	}

	private void acceptChallenge() {
		LoadItem loadItem = LoadHelper.acceptChallenge(getUserToken(), challengeItem.getGameId());
		successToastMsgId = R.string.challenge_accepted;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private void declineChallenge() {
		LoadItem loadItem = LoadHelper.declineChallenge(getUserToken(), challengeItem.getGameId());
		successToastMsgId = R.string.challenge_declined;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private class DailyUpdateListener extends ChessLoadUpdateListener<BaseResponseItem> {

		public DailyUpdateListener() {
			super(BaseResponseItem.class);
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			showToast(successToastMsgId);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (resultCode == StaticData.NO_NETWORK || resultCode == StaticData.UNKNOWN_ERROR) {
				showToast(R.string.host_unreachable_load_local);
			}
		}
	}

	private class GameDailyUpdatesListener extends ChessLoadUpdateListener<BaseResponseItem> {
		private int listenerCode;

		private GameDailyUpdatesListener(int listenerCode) {
			super(BaseResponseItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			switch (listenerCode) {
				case CREATE_CHALLENGE_UPDATE:
					showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
					break;
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(ERROR_TAG)) {
			backToLoginFragment();
		}
		super.onPositiveBtnClick(fragment);
	}

	private void widgetsInit(View view) {

		{ // invite overlay setup
			View inviteOverlay = view.findViewById(R.id.inviteOverlay);

			int sideInset = getResources().getDisplayMetrics().widthPixels / 8;
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(sideInset, sideInset * 2, sideInset, 0);
			params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.boardView);
			inviteOverlay.setLayoutParams(params);

			inviteDetails1Txt = (TextView) view.findViewById(R.id.inviteDetails1Txt);
			inviteTitleTxt = (TextView) view.findViewById(R.id.inviteTitleTxt);
		}
		controlsDailyView = (ControlsDailyView) view.findViewById(R.id.controlsNetworkView);

		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
		bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

//		controlsDailyView.enableGameControls(false);
		controlsDailyView.showSubmitButtons(true);
		boardView = (ChessBoardDailyView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsDailyView);

		setBoardView(boardView);

		boardView.lockBoard(true);
	}

	private class ImageUpdateListener extends ImageReadyListenerLight {

		private static final int TOP_AVATAR = 0;
		private static final int BOTTOM_AVATAR = 1;
		private int code;

		private ImageUpdateListener(int code) {
			this.code = code;
		}

		@Override
		public void onImageReady(Bitmap bitmap) {
			Activity activity = getActivity();
			if (activity == null/* || bitmap == null*/) {
				Log.e("TEST", "ImageLoader bitmap == null");
				return;
			}
			switch (code) {
				case TOP_AVATAR:
					labelsConfig.topAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.topAvatar.setSide(labelsConfig.getOpponentSide());
					topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
					topPanelView.invalidate();

					break;
				case BOTTOM_AVATAR:
					labelsConfig.bottomAvatar = new BoardAvatarDrawable(activity, bitmap);

					labelsConfig.bottomAvatar.setSide(labelsConfig.userSide);
					bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);
					bottomPanelView.invalidate();
					break;
			}
		}
	}

}
