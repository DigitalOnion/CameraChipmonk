package com.magicleap.camerachipmonk;

import android.media.Image;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class HttpServerThread extends Thread {
    public static final String UNKNOWN_ADDRESS = "Unknown IP Address";
    public static final int HttpServerPORT = 8888;

    private ServerSocket httpServerSocket;
    private Uri uri = null;
    private String message = null;

    private HostCallback hostCallback = null;

    public void setHostCallback(HostCallback hostCallback) {
        this.hostCallback = hostCallback;
    }

    public void setUri(Uri uri) {
        this.message = null;
        this.uri = uri;
    }

    public void setMessage(String message) {
        this.message = message;
        this.uri = null;
    }

    public void close() {
        if (httpServerSocket != null) {
            try {
                httpServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces
                    = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress
                        = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        String s = inetAddress.getHostAddress().toUpperCase();
                        if(s.matches(".*[:].*")) {
                            s = "[" + s + "]";
                        }
                        return s + ":" + HttpServerPORT;
                    }
                }
            }
        } catch (SocketException e) {
            return UNKNOWN_ADDRESS;
        }
        return UNKNOWN_ADDRESS;
    }

    private Socket currentSocket = null;

    @Override
    public void run() {
        currentSocket = null;
        try {
            httpServerSocket = new ServerSocket(HttpServerPORT);
            hostCallback.onIpAddressKnown(getIpAddress());

            while(true){
                currentSocket = httpServerSocket.accept();

                BufferedReader is = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
                String httpRequest = is.readLine();

                String msgLog = "Request of " + httpRequest
                        + " from " + currentSocket.getInetAddress().toString();
                hostCallback.logHttpEvent(msgLog);

                hostCallback.onHttpRequestReceived(currentSocket);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateHttpResponse(Image image) {
        HttpResponseThread httpResponseThread = null;

        if(currentSocket!=null && image != null) {
            httpResponseThread =
                    new HttpResponseThread(
                            hostCallback,
                            currentSocket);
            httpResponseThread.setImage(image);
            httpResponseThread.start();
        }
    }

    public void generateHttpResponse(String message) {
        HttpResponseThread httpResponseThread = null;

        if(currentSocket!=null && message != null) {
            httpResponseThread =
                    new HttpResponseThread(
                            hostCallback,
                            currentSocket);
            httpResponseThread.setMessage(message);
            httpResponseThread.start();
        }
    }

    public void generateHttpResponse(Uri uri) {
        HttpResponseThread httpResponseThread = null;

        if(currentSocket!=null && uri != null) {
            httpResponseThread =
                    new HttpResponseThread(
                            hostCallback,
                            currentSocket);
            httpResponseThread.setUri(uri);
            httpResponseThread.start();
        }
    }

}
