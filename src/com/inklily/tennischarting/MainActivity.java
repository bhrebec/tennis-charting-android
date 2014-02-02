package com.inklily.tennischarting;

import com.example.tennischarting.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
		SQLiteMatchStorage.closeGlobalInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
