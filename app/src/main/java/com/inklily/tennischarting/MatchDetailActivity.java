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
    private long mMatchId;
    private Match mMatch;
    private ListView mPointList;
    private ArrayAdapter<Point> mPointAdapter;
    private Button mSendButton;
    private boolean mReview;
    private List<String> mScores;

    @Override
    public void onStorageAvailable(MatchStorage storage) {
        try {
            mMatch = storage.retrieveMatch(mMatchId);
        } catch (MatchStorage.MatchStorageNotAvailableException e) {
            e.printStackTrace();
            return;
        }
        if (mMatch.sent)
            mSendButton.setText(getString(R.string.send_processing_sent));
        else
            mSendButton.setText(R.string.send_processing);

        makeScores();

        mPointAdapter = new ArrayAdapter<Point>(this, android.R.layout.simple_list_item_2, android.R.id.text1, mMatch.points) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView)v.findViewById(android.R.id.text1)).setText(mMatch.points.get(position).toString());
                ((TextView)v.findViewById(android.R.id.text2)).setText(mScores.get(position));
                return v;
            }
        };
        mPointList.setAdapter(mPointAdapter);
        updateTitle();
    }

    private void makeScores() {
        mScores = new ArrayList<String>();

        Score s = new Score(mMatch.sets(), mMatch.finalTb());
        for (Point p : mMatch.points) {
            s.score_point(p);
            mScores.add(s.toString());
        }
    }

    public void updateTitle() {
        setTitle(String.format("%s/%s - %s", mMatch.player1, mMatch.player2, mMatch.score()));
    }

    public void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (mReview) {
            super.onBackPressed();
        } else {
            toMainActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);

        mMatchId = getIntent().getLongExtra("match_id", 0);
        mReview = getIntent().getBooleanExtra("review", true);
        if (mReview)
            findViewById(R.id.send_later).setVisibility(View.GONE);
        else
            findViewById(R.id.continue_match).setVisibility(View.GONE);

        mPointList = (ListView)findViewById(R.id.point_list);
        mPointList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MatchDetailActivity.this);
                final Point p = mPointAdapter.getItem(position);
                final EditText text = new EditText(MatchDetailActivity.this);
                text.setText(p.toString());
                builder.setView(text);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        p.setPoint(text.getText().toString());
                        mMatch.rescore();
                        makeScores();
                        try {
                            mStorage.savePoint(mMatch, p);
                        } catch (MatchStorage.MatchStorageNotAvailableException e) {
                            e.printStackTrace();
                        }
                        mPointAdapter.notifyDataSetChanged();
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
                it.putExtra("match_id", mMatchId);
                startActivity(it);
            }
        });
        findViewById(R.id.send_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toMainActivity();
            }
        });

        mSendButton = (Button) findViewById(R.id.send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = mMatch.getSendIntent(MatchDetailActivity.this);
                if (intent == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MatchDetailActivity.this);
                    builder.setMessage("Something went wrong, mMatch not sent!");
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
