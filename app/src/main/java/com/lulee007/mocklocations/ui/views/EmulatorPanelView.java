package com.lulee007.mocklocations.ui.views;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lulee007.mocklocations.R;

import butterknife.ButterKnife;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 10:50
 */
public class EmulatorPanelView {
    private View emulatorPanelView;

    public EmulatorPanelView(@NonNull ViewGroup root) {
        emulatorPanelView= LayoutInflater.from(root.getContext()).inflate(R.layout.emulator_panel,root,false);
        ButterKnife.bind(this,emulatorPanelView);
    }

    public View getEmulatorPanelView() {
        return emulatorPanelView;
    }
}
