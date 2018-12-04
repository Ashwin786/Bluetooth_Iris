package com.rk.bluetooth_send;

/**
 * Created by user1 on 30/11/18.
 */
public interface Connectivity {
    void connected();
    void disconnected();

    void message(String readMessage);
}
