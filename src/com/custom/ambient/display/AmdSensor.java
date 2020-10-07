/*
 * Copyright (c) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.custom.ambient.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AmdSensor implements SensorEventListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "AmdSensor";

    private static final int BATCH_LATENCY_IN_MS = 100;
    private static final int MIN_PULSE_INTERVAL_MS = 2500;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Context mContext;

    private boolean mAmdGestureEnabled;

    private long mEntryTimestamp;
    private boolean mEnabled;

    private final ExecutorService mExecutorService;

    public AmdSensor(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensor = Utils.getSensor(mSensorManager, "qti.sensor.amd");
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long delta = SystemClock.elapsedRealtime() - mEntryTimestamp;

        if (delta < MIN_PULSE_INTERVAL_MS) {
            return;
        }

        mEntryTimestamp = SystemClock.elapsedRealtime();

        if (event.values[0] == 2) {
            Utils.launchDozePulse(mContext);
        }
    }

    private Future<?> submit(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* Empty */
    }

    protected void enable() {
        if (DEBUG) Log.d(TAG, "Enabling");
        submit(() -> {
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL, BATCH_LATENCY_IN_MS * 1000);
            mEntryTimestamp = SystemClock.elapsedRealtime();
        });
    }

    protected void disable() {
        if (DEBUG) Log.d(TAG, "Disabling");
        submit(() -> {
            mSensorManager.unregisterListener(this, mSensor);
        });
    }
}
