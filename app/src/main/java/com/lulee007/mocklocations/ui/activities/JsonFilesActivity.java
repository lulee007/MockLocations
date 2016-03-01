package com.lulee007.mocklocations.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;

import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.base.MLBaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class JsonFilesActivity extends MLBaseActivity {

    public static final String BUNDLE_KEY_JSON_FILE = "JSON_FILE";
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.app_bar)
    AppBarLayout appBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_files);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setTitle("轨迹列表");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(JsonFilesActivity.BUNDLE_KEY_JSON_FILE, "/storage/emulated/0/MockLocationData/1.json");
        setResult(RESULT_OK, intent);
        finish();
    }
}
