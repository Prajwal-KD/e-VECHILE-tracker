package com.v2v.smartsystem.communication;

import com.v2v.smartsystem.model.V2VMessage;

/**
 * Interface defining V2V network transceivers.
 */
public interface IV2VCommunicator {
    void start();
    void stop();
    boolean isRunning();
    boolean broadcastMessage(V2VMessage message);
    void setOnMessageReceivedListener(OnMessageReceivedListener listener);
    String getChannelName();
}
