package com.inklily.tennischarting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display and edit a Match.
 */
public class MatchDetailActivity extends Activity implements MatchStorage.OnStorageAvailableListener {
    private SQLiteMatchStorage mStorage;
    private long match_id;
    private Match match;
    private ListView point_list;
    private ArrayAdapter<Point> point_adapter;
    private Button sendButton;
    private boolean review;
    private List<String> mScores;

    @Override
    public void onStorageAvailable(MatchStorage storage) {
        try {
            match = storage.retrieveMatch(match_id);
        } catch (MatchStorage.MatchStorageNotAvailableException e) {
            e.printStackTrace();
            return;
        }
        if (match.sent)
            sendButton.setText(getString(R.string.send_processing_sent));
        else
            sendButton.setText(R.string.send_processing);

        makeScores();

        point_adapter = new ArrayAdapter<Point>(this, android.R.layout.simple_list_item_2, android.R.id.text1, match.points) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView)v.findViewById(android.R.id.text1)).setText(match.points.get(position).toString());
                ((TextView)v.findViewById(android.R.id.text2)).setText(mScores.get(position));
                return v;
            }
        };
        point_list.setAdapter(point_adapter);
        updateTitle();
    }

    private void makeScores() {
        mScores = new ArrayList<String>();

        Score s = new Score(match.sets(), match.finalTb());
        for (Point p : match.points) {
            s.score_point(p);
            mScores.add(s.toString());
        }
    }

    public void updateTitle() {
        setTitle(String.format("%s/%s - %s", match.player1, match.player2, match.score()));
    }

    public void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (review) {
            super.onBackPressed();
        } else {
            toMainActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);

        match_id = getIntent().getLongExtra("match_id", 0);
        review = getIntent().getBooleanExtra("review", true);
        if (review)
            findViewById(R.id.send_later).setVisibility(View.GONE);
        else
            findViewById(R.id.continue_match).setVisibility(View.GONE);

        point_list = (ListView)findViewById(R.id.point_list);
        point_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MatchDetailActivity.this);
                final Point p = point_adapter.getItem(position);
                final EditText text = new EditText(MatchDetailActivity.this);
                text.setText(p.toString());
                builder.setView(text);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        p.setPoint(text.getText().toString());
                        match.rescore();
                        makeScores();
                        try {
                            mStorage.savePoint(match, p);
                        } catch (MatchStorage.MatchStorageNotAvailableException e) {
                            e.printStackTrace();
                        }
                        point_adapter.notifyDataSetChanged();
                        updateTitle();
                    }
                });
                builder.show();

            }
        });
        findViewById(R.id.continue_match).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MatchDetailActivity.this, MatchChartActivity.class);
                it.putExtra("match_id", match_id);
                startActivity(it);
            }
        });
        findViewById(R.id.send_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toMainActivity();
            }
        });

        sendButton = (Button) findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = match.getSendIntent(MatchDetailActivity.this);
                if (intent == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MatchDetailActivity.this);
                    builder.setMessage("Something went wrong, match not sent!");
                    builder.show();
                } else {
                    toMainActivity();
                    startActivity(intent);
                }
            }
        });


        mStorage = SQLiteMatchStorage.getGlobalInstance(this.getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();

        mStorage.addOnStorageAvailableListener(this);
    }
}
