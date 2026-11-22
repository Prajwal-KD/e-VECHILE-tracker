package com.v2v.smartsystem.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * Manages in-cabin acoustic tones and haptic alerts when safety warnings are triggered.
 */
public class AudioAlertManager {

    private final Context context;
    private ToneGenerator toneGenerator;
    private final Vibrator vibrator;

    public AudioAlertManager(Context context) {
        this.context = context.getApplicationContext();
        try {
            this.toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        } catch (Exception e) {
            this.toneGenerator = null;
        }
        this.vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Plays a high-urgency acoustic beep and pulse vibration for critical threats (e.g. Emergency Brake Ahead).
     */
    public void playCriticalAlert() {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400);
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    long[] pattern = {0, 150, 100, 250, 100, 400};
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                } else {
                    vibrator.vibrate(500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays a single warning chime for caution events (e.g. turn conflict or road hazard).
     */
    public void playWarningAlert() {
        try {
            if (toneGenerator != null) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 250);
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
