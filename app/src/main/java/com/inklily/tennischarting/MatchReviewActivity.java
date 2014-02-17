package com.inklily.tennischarting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by mrdog on 2/15/14.
 */
public class MatchReviewActivity extends Activity implements MatchStorage.OnStorageAvailableListener {
    private SQLiteMatchStorage mStorage;

    @Override
    public void onStorageAvailable(MatchStorage storage) {
        ((ListView)findViewById(R.id.match_list)).setAdapter(storage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_review);

        ListView match_list = (ListView)findViewById(R.id.match_list);
        match_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(MatchReviewActivity.this, MatchDetailActivity.class);
                it.putExtra("match_id", id);
                startActivity(it);
            }
        });
        match_list.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MatchReviewActivity.this);
                builder.setMessage("Really delete this match?");
                builder.setNegativeButton("Not yet", null);
                builder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mStorage.deleteMatch(id);
                        } catch (MatchStorage.MatchStorageNotAvailableException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();
                return false;
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
