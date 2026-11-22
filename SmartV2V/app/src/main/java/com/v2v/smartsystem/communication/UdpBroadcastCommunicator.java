package com.v2v.smartsystem.communication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.v2v.smartsystem.model.V2VMessage;
import com.v2v.smartsystem.utils.NetworkUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Real Wi-Fi / Hotspot peer-to-peer UDP broadcast transceiver.
 * Transmits and receives V2V Basic Safety Messages directly between nearby Android devices without servers.
 */
public class UdpBroadcastCommunicator implements IV2VCommunicator {

    public static final int V2V_UDP_PORT = 8888;
    private static final int BUFFER_SIZE = 2048;

    private final Context context;
    private DatagramSocket socket;
    private Thread receiverThread;
    private volatile boolean isRunning = false;
    private OnMessageReceivedListener listener;
    private final Handler mainHandler;

    public UdpBroadcastCommunicator(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;

        NetworkUtils.acquireMulticastLock(context);

        receiverThread = new Thread(() -> {
            try {
                socket = new DatagramSocket(V2V_UDP_PORT);
                socket.setBroadcast(true);
                socket.setReuseAddress(true);

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (isRunning && !socket.isClosed()) {
                    try {
                        socket.receive(packet);
                        String json = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                        final String sourceIp = packet.getAddress().getHostAddress();

                        final V2VMessage message = V2VMessage.fromJson(json);
                        if (message != null && listener != null) {
                            mainHandler.post(() -> listener.onMessageReceived(message, sourceIp));
                        }
                    } catch (Exception e) {
                        if (!isRunning) break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });
        receiverThread.start();
    }

    @Override
    public synchronized void stop() {
        isRunning = false;
        NetworkUtils.releaseMulticastLock();

        if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
        }

        if (receiverThread != null) {
            receiverThread.interrupt();
            receiverThread = null;
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean broadcastMessage(final V2VMessage message) {
        if (message == null) return false;

        new Thread(() -> {
            try {
                String payload = message.toJson();
                byte[] data = payload.getBytes(StandardCharsets.UTF_8);

                InetAddress broadcastAddr = NetworkUtils.getBroadcastAddress();
                if (broadcastAddr == null) {
                    broadcastAddr = InetAddress.getByName("255.255.255.255");
                }

                DatagramSocket sendSocket = new DatagramSocket();
                sendSocket.setBroadcast(true);

                DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddr, V2V_UDP_PORT);
                sendSocket.send(packet);
                sendSocket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return true;
    }

    @Override
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public String getChannelName() {
        return "Direct UDP Wi-Fi Broadcast (Port 8888)";
    }
}
