package com.v2v.smartsystem.hardware;

/**
 * Abstraction layer for vehicular hardware interfaces (Bluetooth SPP, USB Serial, Wi-Fi Socket, Simulator).
 */
public interface IHardwareBridge {
    void connect(String targetAddressOrId);
    void disconnect();
    boolean isConnected();
    boolean sendCommand(String command);
    void setListener(HardwareStatusListener listener);
    String getTransportName();
}
