package com.lulee007.mocklocations.presenter;

import android.support.annotation.NonNull;

import com.lulee007.mocklocations.base.MLBasePresenter;
import com.lulee007.mocklocations.model.LocationFile;
import com.lulee007.mocklocations.ui.views.ILocationFilesView;
import com.lulee007.mocklocations.util.GpsJsonFileHelper;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * User: lulee007@live.com
 * Date: 2016-03-02
 * Time: 21:56
 */
public class LocationFilesPresenter extends MLBasePresenter {

    private ILocationFilesView jsonFilesView;

    public LocationFilesPresenter(ILocationFilesView jsonFilesView){

        this.jsonFilesView = jsonFilesView;
    }

    @Override
    protected void onLoadMoreComplete(List items) {

    }

    @Override
    protected HashMap<String, String> buildRequestParams(String where, int skip) {
        return null;
    }

    @NonNull
    @Override
    protected HashMap<String, String> buildRequestParams(String where) {
        return null;
    }

    @Override
    public void loadNew() {
        String filesFolder = GpsJsonFileHelper.sAppFolder;
        Subscription loadFileSubs=Observable.just(filesFolder)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<File>>() {
                    @Override
                    public Observable<File> call(String s) {
                        File folder = new File(s);
                        File[] jsonFiles = folder.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".json");
                            }
                        });
                        return Observable.from(jsonFiles);
                    }
                })
                .map(new Func1<File,  LocationFile>() {
                    @Override
                    public LocationFile call(File s) {
                        LocationFile locationFile = new LocationFile();
                        locationFile.setFilePath(s.getAbsolutePath());
                        locationFile.setFileName(s.getName());
                        return locationFile;
                    }
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<List<LocationFile>>() {
                            @Override
                            public void call(List<LocationFile> locationFiles) {
                                Logger.d("location file count:%d",locationFiles.size());
                                jsonFilesView.addNew(locationFiles);
                            // TODO handle files
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                //TODO handle error
                                jsonFilesView.addNewError();
                            }
                        }
                );
        addSubscription(loadFileSubs);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void loadMore() {

    }

    public void init() {
        jsonFilesView.init();
    }
}
