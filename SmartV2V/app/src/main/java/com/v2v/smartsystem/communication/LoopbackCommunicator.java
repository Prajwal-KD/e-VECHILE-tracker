package com.v2v.smartsystem.communication;

import android.os.Handler;
import android.os.Looper;

import com.v2v.smartsystem.model.V2VMessage;

/**
 * In-memory simulation communicator for single-device test bench demonstrations.
 */
public class LoopbackCommunicator implements IV2VCommunicator {

    private boolean isRunning = false;
    private OnMessageReceivedListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void start() {
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean broadcastMessage(final V2VMessage message) {
        if (!isRunning || listener == null || message == null) return false;
        mainHandler.post(() -> listener.onMessageReceived(message, "127.0.0.1"));
        return true;
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public String getChannelName() {
        return "In-Memory Simulation Channel";
    }

    /**
     * Injects a synthetic remote vehicle message into the pipeline for testing.
     */
    public void injectSimulatedMessage(final V2VMessage message) {
        if (listener != null && message != null) {
            mainHandler.post(() -> listener.onMessageReceived(message, "192.168.43.102"));
        }
    }
}
