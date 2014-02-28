package com.inklily.tennischarting;

import com.inklily.tennischarting.MatchStorage.MatchStorageNotAvailableException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

/**
 * Activity to create a match or edit an existing match's properties.
 */
public class MatchInfoActivity extends Activity implements OnClickListener, MatchStorage.OnStorageAvailableListener {

	private SQLiteMatchStorage matchStorage;

    private final static int[] PERSIST_ID = {
            R.id.match_tournament,
            R.id.match_charted_by,
            R.id.match_surface,
    };
    private int mMatchId;
    private Match mMatch;
    private TextView mPlayer1Name;
    private TextView mPlayer2Name;

    private void addAutocorrect(int id, ArrayAdapter<String> adapter) {
        AutoCompleteTextView v = (AutoCompleteTextView) findViewById(id);
        v.setAdapter(adapter);
    }

    private void addAutocorrect(int id, int array) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(array));
        addAutocorrect(id, adapter);
    }

    private void saveValues() {
        Resources res = getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = prefs.edit();
        for (int id : PERSIST_ID) {
            TextView v = (TextView) findViewById(id);
            e.putString("match_info_" + res.getResourceEntryName(id), v.getText().toString());
        }
        e.commit();
    }
    private void loadValues() {
        Resources res = getResources();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (int id : PERSIST_ID) {
            TextView v = (TextView) findViewById(id);
            v.setText(prefs.getString("match_info_" + res.getResourceEntryName(id), ""));
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Intent it = getIntent();
        matchStorage = SQLiteMatchStorage.getGlobalInstance(this);

		setContentView(R.layout.activity_match_info);

		String[] player_names = getResources().getStringArray(R.array.players_array);
		ArrayAdapter<String> player_name_adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, player_names);
        addAutocorrect(R.id.match_p1_name, player_name_adapter);
        addAutocorrect(R.id.match_p2_name, player_name_adapter);
        addAutocorrect(R.id.match_round, R.array.round_array);
        addAutocorrect(R.id.match_umpire, R.array.umpire_array);
        addAutocorrect(R.id.match_court, R.array.court_array);
        addAutocorrect(R.id.match_tournament, R.array.tournament_array);
        addAutocorrect(R.id.match_surface, R.array.surface_array);

        mPlayer1Name = (TextView) findViewById(R.id.match_p1_name);
        mPlayer2Name = (TextView) findViewById(R.id.match_p2_name);

        Button start = (Button) findViewById(R.id.match_start_charting);
        start.setOnClickListener(this);

        if (it.hasExtra("match_id")) {
            mMatchId = it.getIntExtra("match_id", -1);
            matchStorage.addOnStorageAvailableListener(this);
            start.setText(getString(R.string.save));
        } else {
            mMatch = null;
            loadValues();
            start.setText(getString(R.string.start_charting));
        }
	}

    private void fromMatch(Match m) {
        if (m.sets() == 3)
            ((RadioButton) findViewById(R.id.match_three_sets)).setChecked(true);
        else
            ((RadioButton) findViewById(R.id.match_five_sets)).setChecked(true);
        ((ToggleButton) findViewById(R.id.match_final_set_tb)).setChecked(m.finalTb());
        ((ToggleButton) findViewById(R.id.match_first_serve_near)).setChecked(m.nearServerFirst);

        mPlayer1Name.setText(m.player1);
        mPlayer2Name.setText(m.player2);
        if (m.player1hand == 'R')
            ((RadioButton) findViewById(R.id.match_p1_right_handed)).setChecked(true);
        else
            ((RadioButton) findViewById(R.id.match_p1_left_handed)).setChecked(true);
        if (m.player2hand == 'R')
            ((RadioButton) findViewById(R.id.match_p2_right_handed)).setChecked(true);
        else
            ((RadioButton) findViewById(R.id.match_p2_left_handed)).setChecked(true);

        if (m.gender == 'W')
            ((RadioButton) findViewById(R.id.match_women)).setChecked(true);
        else
            ((RadioButton) findViewById(R.id.match_men)).setChecked(true);

        DatePicker datep = ((DatePicker) findViewById(R.id.match_date));
        datep.updateDate(Integer.parseInt(m.date.substring(0, 4)),
                Integer.parseInt(m.date.substring(4, 6)),
                Integer.parseInt(m.date.substring(6, 8)));

        ((TextView) findViewById(R.id.match_tournament)).setText(m.tournament);
        ((TextView) findViewById(R.id.match_round)).setText(m.round);

        TimePicker timep = ((TimePicker) findViewById(R.id.match_time));
        timep.setCurrentHour(Integer.parseInt(m.time.substring(0, 2)));
        timep.setCurrentMinute(Integer.parseInt(m.time.substring(3, 5)));

        ((TextView) findViewById(R.id.match_court)).setText(m.court);
        ((TextView) findViewById(R.id.match_surface)).setText(m.surface);
        ((TextView) findViewById(R.id.match_umpire)).setText(m.umpire);
        ((TextView) findViewById(R.id.match_charted_by)).setText(m.charted_by);
    }

	private void updateMatch(Match m) {
		int sets = ((RadioButton) findViewById(R.id.match_five_sets)).isChecked() ? 5 : 3;
		boolean final_tb = ((ToggleButton) findViewById(R.id.match_final_set_tb)).isChecked();
		boolean near = ((ToggleButton) findViewById(R.id.match_first_serve_near)).isChecked();
		
		m.nearServerFirst = near;
		m.setSets(sets);
		m.setFinalTb(final_tb);
		
		m.player1 = mPlayer1Name.getText().toString();
		m.player2 = mPlayer2Name.getText().toString();
        m.player1hand = ((RadioButton) findViewById(R.id.match_p1_right_handed)).isChecked() ? 'R' : 'L';
        m.player2hand = ((RadioButton) findViewById(R.id.match_p2_right_handed)).isChecked() ? 'R' : 'L';
        m.gender = ((RadioButton) findViewById(R.id.match_women)).isChecked() ? 'W' : 'M';

        DatePicker datep = ((DatePicker) findViewById(R.id.match_date));
        m.date = String.format("%04d%02d%02d", datep.getYear(), datep.getMonth(), datep.getDayOfMonth());
        
        m.tournament = ((TextView) findViewById(R.id.match_tournament)).getText().toString();
        m.round = ((TextView) findViewById(R.id.match_round)).getText().toString();
        
        TimePicker timep = ((TimePicker) findViewById(R.id.match_time));
        m.time = String.format("%02d:%02d", timep.getCurrentHour(), timep.getCurrentMinute());
        m.court = ((TextView) findViewById(R.id.match_court)).getText().toString();
        m.surface =((TextView) findViewById(R.id.match_surface)).getText().toString();
        m.umpire =((TextView) findViewById(R.id.match_umpire)).getText().toString();
        m.charted_by =((TextView) findViewById(R.id.match_charted_by)).getText().toString();		
	}

	@Override
	public void onClick(View arg0) {
        if (mPlayer1Name.getText().length() == 0 && mPlayer2Name.getText().length() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setMessage(getString(R.string.player_name_warning));
            builder.show();
            return;
        }

        Match m;
        if (mMatch == null)
		    m = new Match(3, true, true); // TODO: allow editing of existing match
        else
            m = mMatch;

		updateMatch(m);
        saveValues();
        try {
            matchStorage.saveMatch(m);

            if (mMatch == null) {
                Intent it = new Intent(this, MatchChartActivity.class);
                it.putExtra("match_id", m.id);
                startActivity(it);
            } else {
                onBackPressed();
            }
        } catch (MatchStorageNotAvailableException e) {
            e.printStackTrace(); // TODO: show error message
        }
	}

    @Override
    public void onStorageAvailable(MatchStorage storage) {
        if (mMatchId != -1) {
            try {
                mMatch = matchStorage.retrieveMatch(mMatchId);
                fromMatch(mMatch);
            } catch (MatchStorageNotAvailableException e) {
                // TODO: bug da user
                e.printStackTrace();
            }
        }
    }
}
