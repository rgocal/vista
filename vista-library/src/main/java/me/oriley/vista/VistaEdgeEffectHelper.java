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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class VistaEdgeEffectHelper {

    @Nullable
    private static final Field EDGE_EFFECT_COMPAT_DELEGATE;

    static {
        EDGE_EFFECT_COMPAT_DELEGATE = getField(EdgeEffectCompat.class, "mEdgeEffect");
    }

    private static final String TAG = VistaEdgeEffectHelper.class.getSimpleName();
    private static final float DEFAULT_THICKNESS_SCALE = 0.5f;
    private static final float DEFAULT_EDGE_SCALE = 0.5f;

    public enum Side {
        LEFT, RIGHT, TOP, BOTTOM
    }

    @NonNull
    private final VistaEdgeEffectHost mHost;

    @NonNull
    private final HashMap<Side, VistaEdgeEffect> mEdges = new HashMap<>();

    private final float mThicknessScale;

    private final float mEdgeScale;

    @ColorInt
    private final int mInitialColor;

    private final boolean mDisableHotspot;


    public VistaEdgeEffectHelper(@NonNull VistaEdgeEffectHost customEdgeEffectHost,
                                 @NonNull Context context,
                                 @Nullable AttributeSet attrs) {
        mHost = customEdgeEffectHost;

        float thicknessScale = DEFAULT_THICKNESS_SCALE;
        float edgeScale = DEFAULT_EDGE_SCALE;
        boolean disableHotspot = false;

        boolean customColor = false;
        int initialColor = Color.WHITE;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VistaView);
            if (a.hasValue(R.styleable.VistaView_vistaColor)) {
                customColor = true;
                initialColor = a.getColor(R.styleable.VistaView_vistaColor, initialColor);
            }
            disableHotspot = a.getBoolean(R.styleable.VistaView_vistaDisableHotspot, false);
            thicknessScale = a.getFloat(R.styleable.VistaView_vistaThicknessScale, DEFAULT_THICKNESS_SCALE);
            edgeScale = a.getFloat(R.styleable.VistaView_vistaEdgeScale, DEFAULT_EDGE_SCALE);
            a.recycle();
        }

        // Couldn't get from attribute, try app compat accent color
        if (!customColor) {
            TypedValue typedValue = new TypedValue();
            TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
            initialColor = a.getColor(0, Color.WHITE);
            a.recycle();
        }

        mInitialColor = initialColor;
        mDisableHotspot = disableHotspot;
        mThicknessScale = thicknessScale;
        mEdgeScale = edgeScale;
    }


    public void refreshEdges(@NonNull Map<Side, Field> fields, boolean isCompat) {
        Context context = mHost.getContext();
        for (Map.Entry<Side, Field> entry : fields.entrySet()) {
            VistaEdgeEffect edgeEffect = mEdges.get(entry.getKey());
            if (edgeEffect == null) {
                edgeEffect = new VistaEdgeEffect(context);
                edgeEffect.updateValues(mInitialColor, mThicknessScale, mEdgeScale, mDisableHotspot);
            }

            if (replaceEdgeEffect(context, entry.getValue(), edgeEffect, isCompat)) {
                mEdges.put(entry.getKey(), edgeEffect);
            }
        }
    }

    @CheckResult
    private boolean replaceEdgeEffect(@NonNull Context context,
                                      @NonNull Field field,
                                      @NonNull VistaEdgeEffect edgeEffect,
                                      boolean isCompat) {
        if (isCompat) {
            return replaceEdgeEffectCompat(context, field, edgeEffect);
        } else {
            return replaceEdgeEffect(field, edgeEffect);
        }
    }

    @CheckResult
    private boolean replaceEdgeEffectCompat(@NonNull Context context,
                                            @NonNull Field field,
                                            @NonNull VistaEdgeEffect edgeEffect) {
        if (EDGE_EFFECT_COMPAT_DELEGATE == null) {
            Log.e(TAG, "Unable to find edge effect delegate field");
            return false;
        }

        try {
            EdgeEffectCompat edgeEffectCompat = new EdgeEffectCompat(context);
            EDGE_EFFECT_COMPAT_DELEGATE.set(edgeEffectCompat, edgeEffect);

            field.set(mHost, edgeEffectCompat);
            edgeEffectCompat.setSize(mHost.getMeasuredWidth(), mHost.getMeasuredHeight());
            Log.d(TAG, "Replaced edge effect " + field + " in " + mHost);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error replacing edge effect " + field + " in " + mHost);
            e.printStackTrace();
            return false;
        }
    }

    @CheckResult
    private boolean replaceEdgeEffect(@NonNull Field field,
                                      @NonNull VistaEdgeEffect edgeEffect) {
        try {
            field.set(mHost, edgeEffect);
            edgeEffect.setSize(mHost.getMeasuredWidth(), mHost.getMeasuredHeight());
            Log.d(TAG, "Replaced edge effect " + field + " in " + mHost);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error replacing edge effect " + field + " in " + mHost);
            e.printStackTrace();
            return false;
        }
    }

    public static void addEdgeEffectFieldIfFound(@NonNull Map<Side, Field> map,
                                                 @NonNull Class viewClass,
                                                 @NonNull Side side,
                                                 @NonNull String fieldName) {
        Field edge = getField(viewClass, fieldName);
        if (edge != null) {
            map.put(side, edge);
        }
    }

    @Nullable
    private static Field getField(@NonNull Class<?> clazz, @NonNull String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to get field " + name + " from class " + clazz.getCanonicalName(), e);
            return null;
        }
    }

    public void setEdgeEffectColors(@ColorInt int color) {
        for (Side side : Side.values()) {
            setEdgeEffectColor(side, color);
        }
    }

    public void setEdgeEffectColor(@NonNull Side side, @ColorInt int color) {
        VistaEdgeEffect edgeEffect = mEdges.get(side);
        if (edgeEffect != null) {
            edgeEffect.setColor(color);
        }
    }
}
