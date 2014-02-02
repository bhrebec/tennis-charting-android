package com.inklily.tennischarting;

import com.example.tennischarting.R;
import com.inklily.tennischarting.MatchStorage.MatchStorageNotAvailableException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class MatchInfoActivity extends Activity implements OnClickListener {

	private AutoCompleteTextView player1_name;
	private AutoCompleteTextView player2_name;
	private SQLiteMatchStorage matchStorage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_match_info);
		
		player1_name = (AutoCompleteTextView) findViewById(R.id.match_p1_name);
		player2_name = (AutoCompleteTextView) findViewById(R.id.match_p2_name);
		String[] player_names = getResources().getStringArray(R.array.players_array);
		ArrayAdapter<String> player_name_adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, player_names);
		player1_name.setAdapter(player_name_adapter);
		player2_name.setAdapter(player_name_adapter);
		
		Button start = (Button) findViewById(R.id.match_start_charting);
		start.setOnClickListener(this);
		
		matchStorage = SQLiteMatchStorage.getGlobalInstance(this);
	}
	
	private void updateMatch(Match m) {
		int sets = ((RadioButton) findViewById(R.id.match_five_sets)).isChecked() ? 5 : 3;
		boolean final_tb = ((ToggleButton) findViewById(R.id.match_final_set_tb)).isChecked();
		boolean near = ((ToggleButton) findViewById(R.id.match_first_serve_near)).isChecked();
		
		m.nearServerFirst = near;
		m.setSets(sets);
		m.setFinalTb(final_tb);
		
		m.player1 = ((TextView) findViewById(R.id.match_p1_name)).getText().toString();
		m.player2 = ((TextView) findViewById(R.id.match_p2_name)).getText().toString();
        m.player1hand = ((RadioButton) findViewById(R.id.match_p1_right_handed)).isChecked() ? 'R' : 'L';
        m.player2hand = ((RadioButton) findViewById(R.id.match_p2_right_handed)).isChecked() ? 'R' : 'L';
        
        DatePicker datep = ((DatePicker) findViewById(R.id.match_date));
        m.date = String.format("%04d-%02d-%02d", datep.getYear(), datep.getMonth(), datep.getDayOfMonth());
        
        m.tournament = ((TextView) findViewById(R.id.match_tournament)).getText().toString();
        m.round = ((TextView) findViewById(R.id.match_round)).getText().toString();
        
        TimePicker timep = ((TimePicker) findViewById(R.id.match_time));
        m.time = String.format("%02d:%0d", timep.getCurrentHour(), timep.getCurrentMinute());
        m.court = ((TextView) findViewById(R.id.match_court)).getText().toString();
        m.surface =((TextView) findViewById(R.id.match_surface)).getText().toString();
        m.umpire =((TextView) findViewById(R.id.match_umpire)).getText().toString();
        m.charted_by =((TextView) findViewById(R.id.match_charted_by)).getText().toString();		
	}

	@Override
	public void onClick(View arg0) {
		Match m = new Match(3, true, true); // TODO: allow editing of existing match
		updateMatch(m);
		
		try {
			matchStorage.saveMatch(m);
			Intent it = new Intent(this, MatchChartActivity.class);
			it.putExtra("match_id", m.id);
			startActivity(it);
		} catch (MatchStorageNotAvailableException e) {
			e.printStackTrace(); // TODO: show error message
		}
	}

}
