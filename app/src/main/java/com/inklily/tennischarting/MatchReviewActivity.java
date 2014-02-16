package com.inklily.tennischarting;

import android.app.Activity;
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
                startActivity(it);
            }
        });

        mStorage = SQLiteMatchStorage.getGlobalInstance(this.getApplication());
        mStorage.addOnStorageAvailableListener(this);
    }

}
