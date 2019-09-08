/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.leinardi.android.speeddial.FabWithLabelView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class ThemedSpeedDialView extends SpeedDialView {

    public ThemedSpeedDialView(Context context) {
        super(context);

        init();
    }

    public ThemedSpeedDialView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ThemedSpeedDialView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        Context context = getContext();
        int backgroundColor = ViewUtils.getColorFromAttrRes(R.attr.colorFabSurface, 0, context);
        setMainFabClosedBackgroundColor(backgroundColor);
        setMainFabOpenedBackgroundColor(backgroundColor);
        int imageTintColor = ViewUtils.getColorFromAttrRes(R.attr.colorOnFabSurface, 0, context);
        getMainFab().setSupportImageTintList(ColorStateList.valueOf(imageTintColor));
    }

    @Override
    public void addActionItem(SpeedDialActionItem actionItem, int position, boolean animate) {
        Context context = getContext();
        int fabImageTintColor = ViewUtils.getColorFromAttrRes(R.attr.colorOnMiniFabSurface, 0,
                context);
        int fabBackgroundColor = ViewUtils.getColorFromAttrRes(R.attr.colorMiniFabSurface, 0,
                context);
        int labelColor = ViewUtils.getColorFromAttrRes(android.R.attr.textColorSecondary, 0,
                context);
        // Label view doesn't have enought elevation (only 1dp) for elevation overlay to work well.
        int labelBackgroundColor = ViewUtils.getColorFromAttrRes(R.attr.colorBackgroundFloating, 0,
                context);
        actionItem = new SpeedDialActionItem.Builder(actionItem.getId(),
                // Should not be a resource, pass null to fail fast.
                actionItem.getFabImageDrawable(null))
                .setLabel(actionItem.getLabel())
                .setFabImageTintColor(fabImageTintColor)
                .setFabBackgroundColor(fabBackgroundColor)
                .setLabelColor(labelColor)
                .setLabelBackgroundColor(labelBackgroundColor)
                .setLabelClickable(actionItem.isLabelClickable())
                .setTheme(actionItem.getTheme())
                .create();
        super.addActionItem(actionItem, position, animate);
    }

    @Override
    public void addView(@NonNull View child, int index) {
        super.addView(child, index);

        // https://github.com/leinardi/FloatingActionButtonSpeedDial/pull/127
        if (child instanceof FabWithLabelView) {
            int fabImageTintColor = ViewUtils.getColorFromAttrRes(R.attr.colorOnMiniFabSurface, 0,
                    getContext());
            FabWithLabelView fabWithLabelView = (FabWithLabelView) child;
            fabWithLabelView.getFab().setSupportImageTintList(ColorStateList.valueOf(
                    fabImageTintColor));
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = ((Bundle) super.onSaveInstanceState()).getParcelable("superState");
        SavedState savedState = new SavedState(superState);
        savedState.open = isOpen();
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        if (savedState.open) {
            toggle(false);
        }
    }

    private static class SavedState extends BaseSavedState {

        public boolean open;

        public SavedState(Parcelable superState) {
            super(superState);
        }


        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }
                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        public SavedState(Parcel in) {
            super(in);

            open = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeByte(open ? (byte) 1 : (byte) 0);
        }
    }
}
