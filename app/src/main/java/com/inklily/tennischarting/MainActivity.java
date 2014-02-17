package com.inklily.tennischarting;
/**
 * TODO
 * ====
 *
 * 1. Serve screen
 *    b. Display 1st/2nd serve
 *    c. Net approach (up swipe)
 *    d. Unknown direction (LR swipe)
 * 2. Location screen
 *    a. Unknown direction (LR swipe)
 *    b. Net approach (up swipe)
 * 4. Point end screen
 *    a. Speculative scores
 *    b. Player wins game/set match
 * 5. Match review screen
 *    a. Score for each point
 *    b. Add point.
 *    c. Insert point
 *    d. Delete all matches
 * 6. New match screen
 *    a. Need to be able to access from review
 *    c. Need to remove from stack after starting match
 * 7. Settings screen
 *    a. Strokes only
 *    b. Depth on return
 *    c. Depth everywhere
 * 8. Help
 * 9. Data model
 *    b. Test first serves
 */

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        findViewById(R.id.btn_review_matches).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, MatchReviewActivity.class);
                startActivity(it);
            }
        });

		findViewById(R.id.btn_chart_new_match).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(MainActivity.this, MatchInfoActivity.class);
				startActivity(it);
			}
		});

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
