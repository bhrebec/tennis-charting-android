package com.inklily.tennischarting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mrdog on 2/15/14.
 */
public class MatchDetailActivity extends Activity implements MatchStorage.OnStorageAvailableListener {
    private SQLiteMatchStorage mStorage;
    private long match_id;
    private Match match;
    private ListView point_list;
    private ArrayAdapter<Point> point_adapter;
    private Button sendButton;
    private boolean review;

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

        point_adapter = new ArrayAdapter<Point>(this, android.R.layout.simple_list_item_1, match.points);
        point_list.setAdapter(point_adapter);
        updateTitle();
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
                Intent intent = new Intent(Intent.ACTION_SEND);

                try {
                    String name = String.format("%s v %s - %s - %s", match.player1, match.player2, match.date, match.tournament);
                    File path = new File(Environment.getExternalStorageDirectory().getPath(), "tennis-charting");
                    File file = new File(path, (name + ".csv").replaceAll("[^A-Za-z0-9 ]", ""));
                    path.mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(match.outputSpreadsheet().getBytes());
                    fos.close();

                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.submit_email_address)});
                    intent.putExtra(Intent.EXTRA_SUBJECT, String.format("[Tennis Chart App] %s vs. %s", match.player1, match.player2));
                    intent.putExtra(Intent.EXTRA_TEXT, name + "\n\nCharted by " + match.charted_by);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

                    toMainActivity();
                    startActivity(intent);
                } catch (IOException e) {
                    // TODO: error
                    e.printStackTrace();
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
