/*
 * Copyright (C) 2016 Kane O'Riley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package me.oriley.vista;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;
import me.oriley.vista.VistaEdgeEffectHelper.Side;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unused")
public class VistaListView extends ListView implements VistaEdgeEffectHost {

    private static final Map<Side, Field> FIELD_MAP;

    static {
        Map<Side, Field> map = new HashMap<>();

        VistaEdgeEffectHelper.addEdgeEffectFieldIfFound(map, AbsListView.class, Side.TOP, "mEdgeGlowTop");
        VistaEdgeEffectHelper.addEdgeEffectFieldIfFound(map, AbsListView.class, Side.BOTTOM, "mEdgeGlowBottom");

        FIELD_MAP = Collections.unmodifiableMap(map);
    }

    @NonNull
    private final VistaEdgeEffectHelper mEdgeEffects;


    public VistaListView(Context context) {
        this(context, null);
    }

    public VistaListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VistaListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mEdgeEffects = new VistaEdgeEffectHelper(this, context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VistaListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mEdgeEffects = new VistaEdgeEffectHelper(this, context, attrs);
    }


    @Override
    public void setEdgeEffectColors(@ColorInt int color) {
        mEdgeEffects.setEdgeEffectColors(color);
    }

    @Override
    public void setEdgeEffectColor(@NonNull Side side, @ColorInt int color) {
        mEdgeEffects.setEdgeEffectColor(side, color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mEdgeEffects.refreshEdges(FIELD_MAP, false);
    }
}
