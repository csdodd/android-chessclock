package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListChallengeItem;
import com.chess.ui.adapters.OnlineChallengesGamesAdapter;
import com.chess.utilities.ChessComApiParser;

import java.util.ArrayList;

public class OnlineNewGameActivity extends LiveBaseActivity implements OnItemClickListener {

	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";

	private ListView openChallengesListView;
	private ArrayList<GameListChallengeItem> gameListItems = new ArrayList<GameListChallengeItem>();
	private OnlineChallengesGamesAdapter gamesAdapter = null;
	private GameListChallengeItem gameListElement;
	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private int successToastMsgId;

	private LoadItem listLoadItem;
	private ListUpdateListener listUpdateListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_new_game);

		init();

		openChallengesListView = (ListView) this.findViewById(R.id.openChallenges);
		openChallengesListView.setAdapter(gamesAdapter);
		openChallengesListView.setOnItemClickListener(this);

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		findViewById(R.id.challengecreate).setOnClickListener(this);

		initUpgradeAndAdWidgets();
		/*moPubView = (MoPubView) findViewById(R.id.mopub_adview);
        MopubHelper.showBannerAd(upgradeBtn, moPubView, this);*/
	}

	private void init() {
		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();

		listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

		listUpdateListener = new ListUpdateListener();

		showActionRefresh = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateList();
	}

	private void updateList(){
		new GetStringObjTask(listUpdateListener).executeTask(listLoadItem);
	}

	private class ListUpdateListener extends ChessUpdateListener {

		@Override
		public void showProgress(boolean show) {
			getActionBarHelper().setRefreshActionItemState(show);
		}

		@Override
		public void updateData(String returnedObj) {
			gameListItems.clear();
			gameListItems.addAll(ChessComApiParser.getChallengesGames(returnedObj));

			if (gamesAdapter == null) {
				gamesAdapter = new OnlineChallengesGamesAdapter(getContext(),  gameListItems);
				openChallengesListView.setAdapter(gamesAdapter);
			}

			gamesAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));

		} else if (view.getId() == R.id.friendchallenge) {
			startActivity(new Intent(this, OnlineFriendChallengeActivity.class));

		} else if (view.getId() == R.id.challengecreate) {
			startActivity(new Intent(this, OnlineOpenChallengeActivity.class));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateList();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if(tag.equals(CHALLENGE_ACCEPT_TAG)){
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, gameListElement.getGameId());
			successToastMsgId = R.string.challengeaccepted;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if(tag.equals(CHALLENGE_ACCEPT_TAG)){
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, gameListElement.getGameId());
			successToastMsgId = R.string.challengedeclined;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
		super.onNegativeBtnClick(fragment);
	}


	@Override
	public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
		gameListElement = gameListItems.get(pos);
		String title = gameListElement.getOpponentUsername() + StaticData.SYMBOL_NEW_STR
				+ getString(R.string.win_) + StaticData.SYMBOL_SPACE + gameListElement.getOpponentWinCount()
				+ StaticData.SYMBOL_NEW_STR
				+ getString(R.string.loss_) + StaticData.SYMBOL_SPACE + gameListElement.getOpponentLossCount()
				+ StaticData.SYMBOL_NEW_STR
				+ getString(R.string.draw_) + StaticData.SYMBOL_SPACE + gameListElement.getOpponentDrawCount();

		popupItem.setPositiveBtnId(R.string.accept);
		popupItem.setNegativeBtnId(R.string.decline);
		showPopupDialog(title, CHALLENGE_ACCEPT_TAG);
	}

	private class ChallengeInviteUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			if(isPaused)
				return;

			showToast(successToastMsgId);
		}
	}
}
