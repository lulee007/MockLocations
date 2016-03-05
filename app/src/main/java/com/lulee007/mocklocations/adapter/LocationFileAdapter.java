package com.lulee007.mocklocations.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.base.MLBaseAdapter;
import com.lulee007.mocklocations.model.LocationFile;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * User: lulee007@live.com
 * Date: 2016-03-03
 * Time: 15:18
 */
public class LocationFileAdapter extends MLBaseAdapter<LocationFile> {
    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new LocationViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.location_file_item,parent,false);
        return getViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (isItemViewHolder(position)) {
            LocationViewHolder viewHolder=(LocationViewHolder)holder;
            final LocationFile locationFile=getItem(position);
            viewHolder.fileName.setText(locationFile.getFileName());
            RxView.clicks(viewHolder.itemView)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            itemListener.onItemClick(position,locationFile);
                        }
                    });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    class LocationViewHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.tv_file_name)TextView fileName;

        public LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
