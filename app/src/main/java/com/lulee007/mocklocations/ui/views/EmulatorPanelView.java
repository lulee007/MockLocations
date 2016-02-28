package com.lulee007.mocklocations.ui.views;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.util.RxBus;
import com.lulee007.mocklocations.util.coordtransform.CPoint;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 10:50
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
        state = EmulatorPanelState.NONE;
        emulatorStateChanged(state);
    }


    public void cancelEmulate() {

    }

    @OnClick({R.id.btn_open_gps_file, R.id.btn_start_emulator, R.id.btn_pause_emulator, R.id.btn_holding_emulator, R.id.btn_continue_emulator, R.id.btn_stop_emulator})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open_gps_file:
                RxBus.getDefault().send(new EmulatorPanelEvent(null, state = EmulatorPanelState.OPEN_FILE));
                emulatorStateChanged(state);

                break;
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
            RxBus.getDefault().send(new EmulatorPanelEvent(null, state));

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
            case OPEN_FILE:
                btnStartEmulator.setEnabled(true);
                break;
            case START:
                btnPauseEmulator.setEnabled(true);
                btnHoldingEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case PAUSE:
                btnContinueEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case HOLDING:
                btnContinueEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case CONTINUE:
                btnPauseEmulator.setEnabled(true);
                btnHoldingEmulator.setEnabled(true);
                btnStopEmulator.setEnabled(true);
                break;
            case STOP:
                btnStartEmulator.setEnabled(true);
                btnOpenGpsFile.setEnabled(true);
                break;
            default:
                btnOpenGpsFile.setEnabled(true);
                break;
        }

    }

    public View getEmulatorPanelView() {
        return emulatorPanelView;
    }

    public boolean isInEmulateMode() {
        return state != EmulatorPanelState.OPEN_FILE;
    }


    public class EmulatorPanelEvent {
        private List<CPoint> data;

        public EmulatorPanelState getState() {
            return state;
        }

        private EmulatorPanelState state;

        public EmulatorPanelEvent(List<CPoint> data, EmulatorPanelState state) {
            this.data = data;

            this.state = state;
        }

        public List<CPoint> getData() {
            return data;
        }
    }
}
