package com.chess.backend.statics;

public class AppConstants {
	public static final String FACEBOOK_APP_ID = "2427617054";
	public static final String BUGSENSE_API_KEY = "609b3b0e";

	/*Screen Features*/
	public static final String SMALL_SCREEN = "small_screen";

	public static final String GAME_MODE = "game_mode";

	public static final String USER_TOKEN = "user_token";
	public static final String USER_IS_GUEST = "user_is_guest";
	public static final String IS_LIVE_CHESS_ON = "is_live_chess_mode_on";
	public static final String CHALLENGE_INITIAL_TIME = "initial_time";
	public static final String CHALLENGE_BONUS_TIME = "bonus_time";
	public static final String CHALLENGE_MIN_RATING = "min_rating";
	public static final String CHALLENGE_MAX_RATING = "max_rating";
	public static final String SAVED_COMPUTER_GAME = "saving";
	public static final int CHALLENGE_ISSUE_DELAY = 2000;

	/* Online games*/
	public static final String USER_OFFERED_DRAW_FOR_GAME = "user offered draw for game";

	/*Tactics constants*/
	public static final String SAVED_TACTICS_ITEM = "saved tactics game";
	public static final String SAVED_TACTICS_CURRENT_PROBLEM = "saved tactics current puzzle number";
	public static final String SAVED_TACTICS_BATCH = "saved tactics batch";
	public static final String SAVED_TACTICS_RESULT_ITEM = "saved tactic result";
	public static final String SAVED_TACTICS_ID = "saved tactic id";
	public static final String SAVED_TACTICS_RETRY = "saved tactic retry";
	public static final String SPENT_SECONDS_TACTICS = "spent seconds in tactics";

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String API_VERSION = "api_version";
	public static final String PREF_COMPUTER_STRENGTH = "strength";
	public static final String PREF_ACTION_AFTER_MY_MOVE = "aim";
	public static final String USER_PREMIUM_STATUS = "premium_status";
	public static final String PREF_SOUNDS = "enableSounds";
	public static final String PREF_SHOW_SUBMIT_MOVE_LIVE = "ssblive";
	public static final String PREF_SHOW_SUBMIT_MOVE = "ssb";
	public static final String PREF_NOTIFICATION = "notifE";
	public static final String PREF_BOARD_COORDINATES = "coords";
	public static final String PREF_BOARD_SQUARE_HIGHLIGHT = "highlights";
	public static final String PREF_BOARD_TYPE = "boardBitmap";
	public static final String PREF_PIECES_SET = "piecesBitmap";
    public static final String PREF_VIDEO_SKILL_LEVEL = "video skill level";
    public static final String PREF_VIDEO_CATEGORY = "video category";

    public static final String PREF_TEMP_TOKEN_GCM = "temporary token for gcm";


	public static final String FULLSCREEN_AD_ALREADY_SHOWED = "fullscreen_ad_showed";
	public static final String USER_SESSION_ID = "user_session_id";
	public static final String FIRST_TIME_START = "first_time_start";
	public static final String START_DAY = "start_day";
	public static final String LAST_ACTIVITY_PAUSED_TIME = "last_activity_aause_time";
	public static final String ADS_SHOW_COUNTER = "ads_show_counter";
	public static final String MATOMY_AD = "matomy";
	public static final String RESPONSE = "response";
	public static final int UPGRADE_SHOW_COUNTER = 10;

	public static final String ID = "id";
	public static final String EXTRA_WEB_URL = "extras weblink url";
	public static final String EXTRA_TITLE = "screen title";

	public final static int GAME_MODE_COMPUTER_VS_HUMAN_WHITE = 0;
	public final static int GAME_MODE_COMPUTER_VS_HUMAN_BLACK = 1;
	public final static int GAME_MODE_HUMAN_VS_HUMAN = 2;
	public final static int GAME_MODE_COMPUTER_VS_COMPUTER = 3;
	public final static int GAME_MODE_VIEW_FINISHED_ECHESS = 5;


	//public static final String DEFAULT_GAMEBOARD_CASTLE = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";



	/* LCC LOG */

	public static final String GAME_LISTENER_IGNORE_OLD_GAME_ID = "GAME LISTENER: ignore old game id=";

	/* Messages */
	/**
	 * Use DB stored value for particular game
	 */
	@Deprecated
	public static final String OPPONENT = "opponent"; // TODO create logic to get quick way of one value from DB
	public static final String WARNING = ", warning: ";
	public static final String CHALLENGE = ", challenge: ";
	public static final String LISTENER = ": listener=";

	/* Stuff */
	public static final String EMAIL_MOBILE_CHESS_COM = "mobile@chess.com";
	public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	public static final String MIME_TYPE_MESSAGE_RFC822 = "message/rfc822";

    public static final String CURRENT_LOCALE = "current locale of screen";

	public static final String BUGSENSE_DEBUG_APP_API_REQUEST = "APP_API_REQUEST";
	public static final String BUGSENSE_DEBUG_APP_API_RESPONSE = "APP_API_RESPONSE";

	public static final String EXCEPTION = "exception";

	/*Email Feedback*/
	public static final String VERSION_CODE = "versionCode ";
	public static final String VERSION_NAME = "versionName ";
	public static final String SDK_API = "API ";

	/* GCM */
	public static final String GCM_RETRY_TIME = "GCM retry time";
	public static final String GCM_REGISTERED_ON_SERVER = "registered on chess GCM server";
	public static final String GCM_SAVED_TOKEN = "saved token on server";

	public static final String NOTIFICATION = "notification";

}
