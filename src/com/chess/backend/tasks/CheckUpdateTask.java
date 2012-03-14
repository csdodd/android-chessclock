package com.chess.backend.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.chess.R;
import com.chess.activities.LoginScreenActivity;
import com.chess.core.AppConstants;
import com.chess.core.MainApp;
import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * CheckUpdateTask class
 *
 * @author alien_roger
 * @created at: 14.03.12 5:35
 */
public class CheckUpdateTask extends AsyncTask<String, Void, Boolean> {

	private Activity context;
	private MainApp mainApp;

	public CheckUpdateTask(Activity context,MainApp mainApp){

		this.context = context;
		this.mainApp = mainApp;
	}
	
	@Override
	protected Boolean doInBackground(String... urls) {
		Boolean force = null;
		try {
			URL updateURL = new URL(urls[0]);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			final String s = new String(baf.toByteArray());
			String[] valuesArray = s.trim().split("\\|", 2);

			int actualVersion = context.getPackageManager().getPackageInfo("com.chess", 0).versionCode;
			System.out.println("LCCLOG: valuesArray[1].trim() " + valuesArray[1].trim());

			int minimumVersion = Integer.valueOf(valuesArray[0].trim());
			int prefferedVersion = Integer.valueOf(valuesArray[1].trim());


			if (actualVersion < prefferedVersion) {
				force = false;
			}
			if (actualVersion < minimumVersion) {
				force = true;
			}


		} catch (Exception e) {
		}
		return force;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if (result != null) {
			final boolean forceFlag = result;
			new AlertDialog.Builder(context).setIcon(R.drawable.ic_launcher).setTitle("update Check")
					.setMessage("An update is available! Please update").setCancelable(false)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							// Intent intent = new
// Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.chess"));
							if (forceFlag) {
								mainApp.getSharedDataEditor().putLong(AppConstants.START_DAY, 0);
								mainApp.getSharedDataEditor().commit();
								context.startActivity(new Intent(context, LoginScreenActivity.class));
								context.finish();
							}
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse("market://details?id=com.chess"));
							context.startActivity(intent);

						}
					}).show();
		}

	}
}
