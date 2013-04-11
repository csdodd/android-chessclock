package com.chess.ui.fragments;

import android.app.Activity;
import com.chess.backend.LiveChessService;
import com.chess.ui.activities.LiveBaseActivity;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 13:06
 */
public class LiveBaseFragment extends CommonLogicFragment {

	protected LiveBaseActivity liveBaseActivity;
	protected LiveChessService liveService;
	protected boolean isLCSBound;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		liveBaseActivity = (LiveBaseActivity) activity;
	}

	protected LiveChessService getLiveService(){
		return liveBaseActivity.getLiveService();
	}

	protected void  onLiveServiceConnected() {
	}
}
