package com.gornostaev.recognize;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.gornostaev.recognize.label_list.ContainerImageLabels;
import com.gornostaev.recognize.label_list.LabelScoreAdapter;

public class LabelScoreActivity extends AppCompatActivity {
    private ContainerImageLabels containerImageLabels;
    private final String TAG = "LabelScoreActivity";
    private RecyclerView recyclerView;
    private LabelScoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_score);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle input = getIntent().getExtras();
        if (input != null) {
            containerImageLabels = (ContainerImageLabels) input
                    .getParcelable("image_labels");
        } else {
            Log.d(TAG, "null input");
        }

        recyclerView = (RecyclerView) findViewById(R.id.info_list);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new LabelScoreAdapter(containerImageLabels);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        //завершаем активность, чтобы не перегружать бэкстек
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //завершаем активность, чтобы не перегружать бэкстек
                finish();
                return (true);
            default:
                return (super.onOptionsItemSelected(item));
        }
    }

}
