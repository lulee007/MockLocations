package com.lulee007.mocklocations.util;

import android.content.Context;
import android.os.Environment;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.lulee007.mocklocations.util.coordtransform.CPoint;
import com.lulee007.mocklocations.util.coordtransform.CoordinateConversion;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * User: lulee007@live.com
 * Date: 2016-02-25
 * Time: 20:35
 * 分类文件 名称+时间+类型+ts.json 百度，离线缓存，在线缓存
 */
public class GpsJsonFileHelper {

    static final public String sRoot = Environment
            .getExternalStorageDirectory() + "/";
    static final public String sAppFolder = sRoot + "MockLocationData";


    private Context mContext;
    private SweetAlertDialog sweetAlertDialog;

    public GpsJsonFileHelper(Context mContext) {

        this.mContext = mContext;
    }

    public void saveJson(final List<LatLng> mPoints) {
        new MaterialDialog.Builder(mContext)
                .title("保存轨迹")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("请输入文件名", DateUtil.currentDateToStr(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, final CharSequence input) {
                        Logger.d(input.toString());
                        sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.PROGRESS_TYPE);
                        sweetAlertDialog.setTitleText("正在保存轨迹文件，请稍等..");
                        sweetAlertDialog.setCancelable(false);
                        sweetAlertDialog.show();
                        Observable.from(mPoints)
                                .map(new Func1<LatLng, CPoint>() {
                                    @Override
                                    public CPoint call(LatLng latLng) {
                                        return CoordinateConversion.bg2gps(latLng.longitude, latLng.latitude);
                                    }
                                })
                                .toList()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .doOnNext(new Action1<List<CPoint>>() {
                                    @Override
                                    public void call(List<CPoint> CPoints) {

                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        String fileName = String.format(sAppFolder + "/%s_%s_%l", input.toString(), "BD", new Date().getTime());
                                        Logger.d("save file:%s", fileName);
                                        File dataFolder = new File(sAppFolder);
                                        dataFolder.mkdirs();
                                        File jsonFile = new File(fileName);

                                        try {
                                            if (jsonFile.createNewFile()) {
                                                FileOutputStream os = null;
                                                os = new FileOutputStream(jsonFile);
                                                os.write(new Gson().toJson(mPoints).getBytes());
                                                os.flush();
                                                os.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                                .subscribe(
                                        new Action1<List<CPoint>>() {
                                            @Override
                                            public void call(List<CPoint> cPoints) {
                                                sweetAlertDialog
                                                        .setTitleText("保存成功")
                                                        .setConfirmText("确定")
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            }
                                        },
                                        new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                sweetAlertDialog
                                                        .setTitleText("保存失败")
                                                        .setConfirmText("确定")
                                                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                            }
                                        }
                                );
                    }
                })
                .positiveText("保存")
                .negativeText("取消")
                .show();
    }

    public List<CPoint> openJson() {
        return null;
    }
}
