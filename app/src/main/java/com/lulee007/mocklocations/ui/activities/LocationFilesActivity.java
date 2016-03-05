package com.lulee007.mocklocations.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;

import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.adapter.LocationFileAdapter;
import com.lulee007.mocklocations.base.MLBaseActivity;
import com.lulee007.mocklocations.base.MLBaseAdapter;
import com.lulee007.mocklocations.model.LocationFile;
import com.lulee007.mocklocations.presenter.LocationFilesPresenter;
import com.lulee007.mocklocations.ui.views.ILocationFilesView;
import com.lulee007.mocklocations.util.DataStateViewHelper;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class LocationFilesActivity extends MLBaseActivity implements ILocationFilesView ,DataStateViewHelper.DataStateViewListener, MLBaseAdapter.ItemListener {

    public static final String BUNDLE_KEY_JSON_FILE = "JSON_FILE";
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.rv_files)
    UltimateRecyclerView rvFiles;
    private LocationFilesPresenter locationFilesPresenter;
    private LocationFileAdapter fileAdapter;
    private DataStateViewHelper mDataStateViewHelper;
    private String selectFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_files);
        ButterKnife.bind(this);

        locationFilesPresenter = new LocationFilesPresenter(this);
        locationFilesPresenter.init();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(LocationFilesActivity.BUNDLE_KEY_JSON_FILE, selectFile);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void refresh(List<LocationFile> entries) {

    }

    @Override
    public void refreshNoContent() {

    }

    @Override
    public void refreshError() {

    }

    @Override
    public void addMore(List<LocationFile> moreItems) {

    }

    @Override
    public void addNew(List<LocationFile> newItems) {
        fileAdapter.init(newItems);
        mDataStateViewHelper.setView(DataStateViewHelper.DateState.CONTENT);

    }

    @Override
    public void addNewError() {

    }

    @Override
    public void addMoreError() {

    }

    @Override
    public void noMore() {

    }

    @Override
    public void noData() {

    }

    @Override
    public void init() {
        setActionBarWithTitle(toolbar, "轨迹列表", true);


        rvFiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvFiles.setHasFixedSize(false);
        rvFiles.setItemAnimator(new DefaultItemAnimator());

        mDataStateViewHelper = new DataStateViewHelper(rvFiles);
        mDataStateViewHelper.setDataStateViewListener(this);
        mDataStateViewHelper.setView(DataStateViewHelper.DateState.LOADING);

        fileAdapter = new LocationFileAdapter();
        fileAdapter.setItemListener(this);
        rvFiles.setAdapter(fileAdapter);
//        rvFiles.setse

        locationFilesPresenter.loadNew();
    }

    @Override
    public void onErrorRetry() {

    }

    @Override
    public void onLoadMoreErrorRetry() {

    }

    @Override
    public void onNoDataButtonClick() {

    }

    @Override
    public void onItemClick(int pos,Object item) {
        fileAdapter.setSelected(pos);
        LocationFile locationFile=(LocationFile)item;
        selectFile=locationFile.getFilePath();
    }
}
