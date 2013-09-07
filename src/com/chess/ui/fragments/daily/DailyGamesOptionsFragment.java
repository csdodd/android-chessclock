package com.chess.ui.fragments.daily;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.*;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.DailySeekItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonGlassyDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.04.13
 * Time: 14:26
 */
public class DailyGamesOptionsFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemSelectedListener {

	private static final String ERROR_TAG = "send request failed popup";

	private static final int MIN_RATING_DIFF = 200;
	private static final int MAX_RATING_DIFF = 200;
	private static final int MIN_RATING_MIN = 1000;
	private static final int MIN_RATING_MAX = 2000;
	private static final int MAX_RATING_MIN = 1000;
	private static final int MAX_RATING_MAX = 2400;

	private DailyGamesButtonsAdapter dailyGamesButtonsAdapter;
	private DailyGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private List<SelectionItem> friendsList;
	private int dailyRating;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new DailyGameConfig.Builder();

		{ // load friends from DB
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(), DbScheme.Tables.FRIENDS));

			friendsList = new ArrayList<SelectionItem>();
			friendsList.add(new SelectionItem(null, getString(R.string.random)));
			if (cursor != null && cursor.moveToFirst()) {
				do{
					friendsList.add(new SelectionItem(null, DbDataManager.getString(cursor, DbScheme.V_USERNAME)));
				}while (cursor.moveToNext());
			}

			friendsList.get(0).setChecked(true);
		}
		dailyRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal(), getUsername());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_option_daily_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);

		RoboSpinner opponentSpinner = (RoboSpinner) view.findViewById(R.id.opponentSpinner);
		Resources resources = getResources();

		OpponentsAdapter selectionAdapter = new OpponentsAdapter(getActivity(), friendsList);
		opponentSpinner.setAdapter(selectionAdapter);
		opponentSpinner.setOnItemSelectedListener(this);

		opponentSpinner.setSelection(0);

		{// options setup
			{// Mode adapter init
				int[] newGameButtonsArray = resources.getIntArray(R.array.days_per_move_array);
				List<DailyGameButtonItem> newGameButtonItems = new ArrayList<DailyGameButtonItem>();
				for (int label : newGameButtonsArray) {
					newGameButtonItems.add(new DailyGameButtonItem(label, getContext()));
				}
				int dailyMode = getAppData().getDefaultDailyMode();
				newGameButtonItems.get(dailyMode).checked = true;

				GridView gridView = (GridView) view.findViewById(R.id.dailyGamesModeGrid);
				dailyGamesButtonsAdapter = new DailyGamesButtonsAdapter(this, newGameButtonItems);
				gridView.setAdapter(dailyGamesButtonsAdapter);
			}

			// rated games switch
			ratedGameSwitch = (SwitchButton) view.findViewById(R.id.ratedGameSwitch);

			{// Rating part
				int minRatingDefault = dailyRating - MIN_RATING_DIFF;
				int maxRatingDefault = dailyRating + MAX_RATING_DIFF;

				minRatingBtn = (RoboRadioButton) view.findViewById(R.id.minRatingBtn);
				minRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
				minRatingBtn.setText(String.valueOf(minRatingDefault));

				maxRatingBtn = (RoboRadioButton) view.findViewById(R.id.maxRatingBtn);
				maxRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
				maxRatingBtn.setText(String.valueOf(maxRatingDefault));

				// set checked minRating Button
				minRatingBtn.setChecked(true);

				SeekBar ratingBar = (SeekBar) view.findViewById(R.id.ratingBar);
				ratingBar.setOnSeekBarChangeListener(ratingBarChangeListener);
				ratingBar.setProgressDrawable(new RatingProgressDrawable(getContext(), ratingBar));
			}
			view.findViewById(R.id.playBtn).setOnClickListener(this);
		}
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateDailyMode(getAppData().getDefaultDailyMode());
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = getDailyGameConfig();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getAdapter() instanceof OpponentsAdapter) {
			SelectionItem opponent = (SelectionItem) parent.getItemAtPosition(position);
			gameConfigBuilder.setOpponentName(opponent.getText());
		} else {
			updateDailyMode(getAppData().getDefaultDailyMode());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void updateDailyMode(final int position) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				dailyGamesButtonsAdapter.checkButton(position);
				// set value to builder
				gameConfigBuilder.setDaysPerMove(dailyGamesButtonsAdapter.getItem(position).days);
			}
		}, 250);
	}


	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}
	}

	private CompoundButton.OnCheckedChangeListener ratingSelectionChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView.getId() == R.id.minRatingBtn && isChecked) {
				minRatingBtn.setChecked(true);
				maxRatingBtn.setChecked(false);

			} else if (buttonView.getId() == R.id.maxRatingBtn && isChecked){
				maxRatingBtn.setChecked(true);
				minRatingBtn.setChecked(false);
			}
		}
	};

	private SeekBar.OnSeekBarChangeListener ratingBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			TextView checkedButton;
			int minRating;
			int maxRating;
			if (maxRatingBtn.isChecked()) {
				checkedButton = maxRatingBtn;
				minRating = MAX_RATING_MIN;
				maxRating = MAX_RATING_MAX;
			} else {
				checkedButton = minRatingBtn;
				minRating = MIN_RATING_MIN;
				maxRating = MIN_RATING_MAX;
			}
			// get percent progress and convert it to values

			int diff = minRating;
			float factor = (maxRating - minRating) / 100; // (maxRating - minRating) / maxSeekProgress
			// progress - percent
			int value = (int) (factor * progress) + diff; // k * x + b

			checkedButton.setText(String.valueOf(value ));

			if (maxRatingBtn.isChecked()) {
				gameConfigBuilder.setMaxRating(value);
				gameConfigBuilder.setMinRating(Integer.parseInt(minRatingBtn.getText().toString()));
			} else {
				gameConfigBuilder.setMinRating(value);
				gameConfigBuilder.setMaxRating(Integer.parseInt(maxRatingBtn.getText().toString()));
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};


	private class DailyGameButtonItem {
		public boolean checked;
		public int days;
		public String label;

		DailyGameButtonItem(int label, Context context){
			this.days = label;
			this.label = context.getString(R.string.days_arg, label);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == DailyGamesButtonsAdapter.BUTTON_ID) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			updateDailyMode(position);
			getAppData().setDefaultDailyMode(position);
		} else if (view.getId() == R.id.dailyHeaderView){
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.playBtn){
			createDailyChallenge();
		}
	}

	private class DailyGamesButtonsAdapter extends ItemsAdapter<DailyGameButtonItem> {

		private ItemClickListenerFace clickListenerFace;
		public static final int BUTTON_ID = 0x00001234;
		private ColorStateList textColor;

		public DailyGamesButtonsAdapter(ItemClickListenerFace clickListenerFace, List<DailyGameButtonItem> itemList) {
			super(clickListenerFace.getMeContext(), itemList);
			this.clickListenerFace = clickListenerFace;

			textColor = getResources().getColorStateList(R.color.text_controls_icons);
		}

		@Override
		protected View createView(ViewGroup parent) {
			RoboButton button = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
			button.setId(BUTTON_ID);
			button.setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.new_daily_grid_button_width));
			button.setDrawableStyle(R.style.Button_Glassy);
			button.setTextColor(textColor);
			button.setOnClickListener(clickListenerFace);
			return button;
		}

		@Override
		protected void bindView(DailyGameButtonItem item, int pos, View convertView) {
			convertView.setTag(itemListId, pos);

			((RoboButton)convertView).setText(item.label);

			Drawable background = convertView.getBackground();
			if (item.checked) {
				((RoboButton)convertView).setTextColor(Color.WHITE);
				background.mutate().setState(ButtonGlassyDrawable.STATE_SELECTED);
			} else {
				((RoboButton)convertView).setTextColor(textColor);
				background.mutate().setState(ButtonGlassyDrawable.STATE_ENABLED);
			}
		}

		public void checkButton(int checkedPosition){
			for (DailyGameButtonItem item : itemsList) {
				item.checked = false;
			}

			itemsList.get(checkedPosition).checked = true;
			notifyDataSetChanged();
		}
	}

	public DailyGameConfig getDailyGameConfig(){
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());

		return gameConfigBuilder.build();
	}

	public class OpponentsAdapter extends ItemsAdapter<SelectionItem> {

		public OpponentsAdapter(Context context, List<SelectionItem> items) {
			super(context, items);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_game_opponent_spinner_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.textTxt = (TextView) view.findViewById(R.id.opponentNameTxt);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.textTxt.setText(item.getText());
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			DropViewHolder holder = new DropViewHolder();
			if (convertView == null) {
				convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false);
				holder.textTxt = (TextView) convertView.findViewById(android.R.id.text1);

				convertView.setTag(holder);
			} else {
				holder = (DropViewHolder) convertView.getTag();
			}

			holder.textTxt.setTextColor(context.getResources().getColor(R.color.black));
			holder.textTxt.setText(itemsList.get(position).getText());

			return convertView;
		}

		private class ViewHolder {
			TextView textTxt;
		}

		private class DropViewHolder {
			TextView textTxt;
		}

	}
}
