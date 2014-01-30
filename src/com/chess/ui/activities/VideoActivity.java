package com.chess.ui.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import com.chess.R;
import com.chess.statics.AppConstants;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 15.01.14
 * Time: 11:55
 */
public class VideoActivity extends Activity implements View.OnFocusChangeListener, View.OnTouchListener {

	public static final String SEEK_POSITION = "seek_position";

	private VideoView videoView;
	private MediaController mediaController;
	private boolean stopPlay;
	private int seekPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.video_player_frame);

		mediaController = new MediaController(this);
		mediaController.show(1);

		videoView = (VideoView) findViewById(R.id.videoView);

		videoView.setVideoURI(Uri.parse(getIntent().getStringExtra(AppConstants.VIDEO_LINK)));
		videoView.setMediaController(mediaController);
		videoView.requestFocus();
		hideStatusBar();

		videoView.start();
		videoView.setOnFocusChangeListener(this);
		videoView.setOnTouchListener(this);

		if (savedInstanceState != null) {
			seekPosition = savedInstanceState.getInt(SEEK_POSITION);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!stopPlay) {
			playVideoFromPos(seekPosition);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		seekPosition = videoView.getCurrentPosition();

		videoView.pause();
		stopPlay = true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SEEK_POSITION, seekPosition);
	}

	private void playVideoFromPos(int pos) {
		if (pos != 0) {
			pos -= 200; // we rewind a little back (2sec)
		}
		videoView.seekTo(pos);
		videoView.start();
	}

	private void hideStatusBar() {
		if (AppUtils.HONEYCOMB_PLUS_API) {
			videoView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		}

		if (AppUtils.ICS_PLUS_API) {
			videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v.getId() == R.id.videoView) {
			if (!hasFocus) {
				hideStatusBar();
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!mediaController.isShowing()) {
			hideStatusBar();
		}
		return false;
	}
}
