/*
 * Copyright 2016, The Android Open Source Project
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

package com.example.androidthings.button;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Example of using Button driver for toggling a LED.
 * <p>
 * This activity initialize an InputDriver to emit key events when the button GPIO pin state change
 * and flip the state of the LED GPIO pin.
 * <p>
 * You need to connect an LED and a push button switch to pins specified in {@link BoardDefaults}
 * according to the schematic provided in the sample README.
 */
public class ButtonActivity extends Activity {
    private static final String TAG = ButtonActivity.class.getSimpleName();

    private Gpio mLedGpio;

    private String redLed = "BCM26";
    private String greenLed = "BCM5";
    private Gpio mLedGpio2;
    private ButtonInputDriver mButtonInputDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");

        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            Log.i(TAG, "Configuring GPIO pins");
            mLedGpio = pioService.openGpio(redLed);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mLedGpio2 = pioService.openGpio(greenLed);
            mLedGpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error configuring GPIO pins", e);
        }

        try {
            Log.i(TAG, "Registering button driver " + BoardDefaults.getGPIOForButton());
            // Initialize and register the InputDriver that will emit SPACE key events
            // on GPIO state changes.
            mButtonInputDriver = new ButtonInputDriver(
                    BoardDefaults.getGPIOForButton(),
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_SPACE);
            mButtonInputDriver.register();

        } catch (IOException e) {
            Log.e(TAG, "Error configuring GPIO pins", e);
        }

    }

    //when the button is pressed, the first led will flash the number of times equal to the hours passed from the day
    //the second ledd will flash the number of times equal to the minutes passed from this hour
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            setLed1Value(true);
            waitAWhile(200);
            setLed1Value(false);
            setLed2Value(true);
            waitAWhile(200);
            setLed2Value(false);



            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            setLed1Value(false);
            setLed2Value(false);
            return true;
        }
        return super.onKeyUp(keyCode,event);
    }


    /**
     * Update the value of the LED output.
     */
    private void setLed1Value(boolean value) {
        try {
            mLedGpio.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    private void setLed2Value(boolean value) {
        try {
            mLedGpio2.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop called.");
        if (mButtonInputDriver != null) {
            mButtonInputDriver.unregister();
            try {
                Log.d(TAG, "Unregistering button");
                mButtonInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally {
                mButtonInputDriver = null;
            }
        }

        if (mLedGpio != null) {
            try {
                Log.d(TAG, "Unregistering LED.");
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED GPIO", e);
            } finally {
                mLedGpio = null;
            }
        }
        super.onStop();
    }


    public void waitAWhile(int miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
