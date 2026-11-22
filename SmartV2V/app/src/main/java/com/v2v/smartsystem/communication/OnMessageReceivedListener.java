package com.v2v.smartsystem.communication;

import com.v2v.smartsystem.model.V2VMessage;

/**
 * Callback for received V2V network packets.
 */
public interface OnMessageReceivedListener {
    void onMessageReceived(V2VMessage message, String sourceIp);
}
