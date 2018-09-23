package com.magicleap.camerachipmonk;

import android.content.ContentResolver;

import java.net.Socket;

public interface HostCallback {
    public void logHttpEvent(String httpEvent);
    public void onIpAddressKnown(String ipAddress);
    public void onHttpRequestReceived(Socket socket);
    public ContentResolver getHostContentResolver();
}
