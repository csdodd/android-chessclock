package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.TacticItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;

import java.util.ArrayList;
import java.util.List;


//public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticItemOld, Long> {
public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticItem.Data, Long> {

	private final String username;
	private ContentResolver contentResolver;
	private final List<TacticItem.Data> tacticsBatch;
	private static String[] arguments = new String[2];

	public SaveTacticsBatchTask(TaskUpdateInterface<TacticItem.Data> taskFace, List<TacticItem.Data> tacticsBatch,
								ContentResolver resolver) {
		super(taskFace);
		this.tacticsBatch = new ArrayList<TacticItem.Data>();
		this.tacticsBatch.addAll(tacticsBatch);
		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		username = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (TacticItem.Data tacticItem : tacticsBatch) {
			tacticItem.setUser(username);
			arguments[0] = String.valueOf(tacticItem.getId());
			arguments[1] = username;

			Uri uri = DbScheme.uriArray[DbScheme.Tables.TACTICS_BATCH.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
					DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments, null);

			ContentValues values = DbDataManager.putTacticItemToValues(tacticItem);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		return StaticData.RESULT_OK;
	}


}
