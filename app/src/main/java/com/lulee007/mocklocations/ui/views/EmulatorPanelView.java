package com.lulee007.mocklocations.ui.views;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;
import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.util.RxBus;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 10:50
 * GPS 位置模拟控制面板，打开文件，开始模拟，暂停
 * <p>通过RxBus.getDefault().send() 将状态发送到 {@link EmulatorPanelEvent}到 {@link com.lulee007.mocklocations.util.MockLocationHelper}
 */
public class EmulatorPanelView {

    public enum EmulatorPanelState {
        OPEN_FILE,
        START,
        PAUSE,
        HOLDING,
        CONTINUE,
        STOP,
        NONE
    }

    @Bind(R.id.btn_open_gps_file)
    Button btnOpenGpsFile;
    @Bind(R.id.btn_start_emulator)
    Button btnStartEmulator;
    @Bind(R.id.btn_pause_emulator)
    Button btnPauseEmulator;
    @Bind(R.id.btn_holding_emulator)
    Button btnHoldingEmulator;
    @Bind(R.id.btn_continue_emulator)
    Button btnContinueEmulator;
    @Bind(R.id.btn_stop_emulator)
    Button btnStopEmulator;
    private View emulatorPanelView;

    private EmulatorPanelState state;

    public EmulatorPanelView(@NonNull ViewGroup root) {
        emulatorPanelView = LayoutInflater.from(root.getContext()).inflate(R.layout.emulator_panel, root, false);
        ButterKnife.bind(this, emulatorPanelView);
        init();
    }

    private void init() {
        state = EmulatorPanelState.NONE;
        emulatorStateChanged(state);
        RxView.clicks(btnOpenGpsFile)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        RxBus.getDefault().send(new EmulatorPanelEvent(EmulatorPanelState.OPEN_FILE));
                    }
                });
    }


    public void cancelEmulate() {
        btnStopEmulator.performClick();
    }

    public void onFileOpened(){
        emulatorStateChanged(state = EmulatorPanelState.OPEN_FILE);
    }

    @OnClick({R.id.btn_open_gps_file, R.id.btn_start_emulator, R.id.btn_pause_emulator, R.id.btn_holding_emulator, R.id.btn_continue_emulator, R.id.btn_stop_emulator})
    public void onClick(View view) {
        switch (view.getId()) {
            //  btn_open_gps_file use RxView.clicks()
            case R.id.btn_start_emulator:
                emulatorStateChanged(state = EmulatorPanelState.START);
                break;
            case R.id.btn_pause_emulator:
                emulatorStateChanged(state = EmulatorPanelState.PAUSE);
                break;
            case R.id.btn_holding_emulator:
                emulatorStateChanged(state = EmulatorPanelState.HOLDING);
                break;
            case R.id.btn_continue_emulator:
                emulatorStateChanged(state = EmulatorPanelState.CONTINUE);
                break;
            case R.id.btn_stop_emulator:
                emulatorStateChanged(state = EmulatorPanelState.STOP);
                break;
        }
        if (state != EmulatorPanelState.OPEN_FILE && state != EmulatorPanelState.NONE)
            RxBus.getDefault().send(new EmulatorPanelEvent(state));

    }

    /**
     * 响应toolbar的控件状态
     *
     * @param state 0 打开文件；1开始；2暂停；3等待；4继续；5停止;默认 禁用所有 除了 打开文件
     */
    private void emulatorStateChanged(EmulatorPanelState state) {
        btnStartEmulator.setEnabled(false);
        btnContinueEmulator.setEnabled(false);
        btnHoldingEmulator.setEnabled(false);
        btnOpenGpsFile.setEnabled(false);
        btnPauseEmulator.setEnabled(false);
        btnStopEmulator.setEnabled(false);
        switch (state) {
            case OPEN_FILE://打开了文件，可以开始进行模拟
                btnStartEmulator.setEnabled(true);
                break;
            case START://开始模拟，
                btnPauseEmulator.setEnabled(true);
                btnHoldingEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case PAUSE://暂停模拟，不发送新位置
                btnContinueEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case HOLDING://暂停模拟，持续发送最后一个位置
                btnContinueEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case CONTINUE://继续进行模拟位置发送
                btnPauseEmulator.setEnabled(true);
                btnHoldingEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case STOP://停止位置模拟
                btnStartEmulator.setEnabled(true);
                btnOpenGpsFile.setEnabled(true);
                break;
            default://默认打开文件可用
                btnOpenGpsFile.setEnabled(true);
                break;
        }

    }

    public View getEmulatorPanelView() {
        return emulatorPanelView;
    }

    public boolean isInEmulateMode() {
        return state != EmulatorPanelState.OPEN_FILE
                && state != EmulatorPanelState.NONE
                && state != EmulatorPanelState.STOP;
    }

    public class EmulatorPanelEvent {

        public EmulatorPanelState getState() {
            return state;
        }

        private EmulatorPanelState state;

        public EmulatorPanelEvent(EmulatorPanelState state) {
            this.state = state;
        }

    }
}
