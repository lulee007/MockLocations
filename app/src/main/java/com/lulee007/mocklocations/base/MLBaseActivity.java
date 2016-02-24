package com.lulee007.mocklocations.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.lulee007.mocklocations.util.ActivitiesHelper;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * User: lulee007@live.com
 * Date: 2015-12-08
 * Time: 12:50
 */
public class MLBaseActivity extends AppCompatActivity {

    protected CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitiesHelper.instance().add(this);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setElevation(0);
        }

        mCompositeSubscription = new CompositeSubscription();

    }

    protected void setActionBarWithTitle(String title){
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
    }

    public void startActivity(Class target){
        Intent intent=new Intent(this,target);
        startActivity(intent);
    }

    public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    protected void addSubscription(Subscription subscription) {
        if (subscription != null) {
            mCompositeSubscription.add(subscription);
        }
    }

    private void unSubscribeAll() {
        mCompositeSubscription.unsubscribe();
        Logger.d("unSubscribe all");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unSubscribeAll();
        ActivitiesHelper.instance().remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
