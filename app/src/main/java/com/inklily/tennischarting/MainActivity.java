package com.inklily.tennischarting;
/**
 * TODO
 * ====
 *
 * 5. Match review screen
 *    b. Add point.
 *    c. Insert point
 *    d. Delete all matches
 * 6. New match screen
 *    a. Need to be able to access from review
 *    b. Need to require player names to be entered
 * 9. Data model
 *    b. Test first serves
 * 10. Other
 *    b. Need better undo
 *    d. Need to generate resources for all screen densities
 */

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

    private class LaunchActivityListener implements OnClickListener {
        private Class<? extends Activity> act;
        public LaunchActivityListener(Class<? extends Activity> activity) {
            this.act = activity;
        }

        @Override
        public void onClick(View v) {
            Intent it = new Intent(MainActivity.this, act);
            startActivity(it);
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        findViewById(R.id.btn_review_matches).setOnClickListener(new LaunchActivityListener(MatchReviewActivity.class));
		findViewById(R.id.btn_chart_new_match).setOnClickListener(new LaunchActivityListener(MatchInfoActivity.class));
        findViewById(R.id.btn_help).setOnClickListener(new LaunchActivityListener(HelpActivity.class));
        findViewById(R.id.btn_settings).setOnClickListener(new LaunchActivityListener(SettingsActivity.class));

        // Spin up the db to prevent delays
        SQLiteMatchStorage.getGlobalInstance(this.getApplication());
    }

    @Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
