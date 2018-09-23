package com.magicleap.camerachipmonk;

import android.app.Application;
import android.media.Image;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

class HttpResponseThread extends Thread {
    HostCallback host;

    Socket socket;
    Image image = null;
    Uri uri = null;
    String message = null;

    HttpResponseThread(HostCallback host, Socket socket) {
        this.host = host;
        this.socket = socket;
    }

    public void setImage(Image image) {
        this.image = image;
        this.uri = null;
        this.message = null;
    }

    public void setUri(Uri uri) {
        this.image = null;
        this.uri = uri;
        this.message = null;
    }

    public void setMessage(String message) {
        this.image = null;
        this.uri = null;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            if(image != null)
                dumpResponse(image, socket);
            else if (uri != null)
                dumpResponse(uri, socket);
            else if (message != null)
                dumpResponse(message, socket);
            else
                return;

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dumpResponse(String message, Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            String response = "<html><head></head>" +
                    "<body>" +
                    "<h1>" + message + "</h1>" +
                    "</body></html>";

            writer.print("HTTP/1.0 200" + "\r\n");
            writer.print("Content type: text/html" + "\r\n");
            writer.print("Content length: " + response.length() + "\r\n");
            writer.print("\r\n");
            writer.print(response + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dumpResponse(Uri uri, Socket socket) {
        try {
            ParcelFileDescriptor descriptor = host.getHostContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = descriptor.getFileDescriptor();
            FileInputStream stream = new FileInputStream(fileDescriptor);

            LinkedList<byte[]> listByteArray = new LinkedList<>();
            int total = 0;
            int available = stream.available();
            while(available > 0) {
                byte[] readBytes = new byte[available];
                int n = stream.read(readBytes);
                listByteArray.add(readBytes);
                total += n;
                available = stream.available();
            }

            OutputStream os = socket.getOutputStream();
            String s = "HTTP/1.0 200" + "\r\n" +
                    "Content type: image/jpeg" + "\r\n" +
                    "Content length: " + total + "\r\n" +
                    "\r\n";
            os.write(s.getBytes());
            for(byte[] bytes : listByteArray) {
                os.write(bytes);
            }
            os.write('\r');
            os.write('\n');
            os.flush();

        } catch ( NullPointerException | IOException e) {

        }
    }

    private void dumpResponse(Image image, final Socket socket) {
        try {

            ByteBuffer buffer = image.getPlanes()[0].getBuffer(); // this is the JPEG compressed data
            int total = buffer.remaining();
            byte[] bytes = new byte[total];
            buffer.get(bytes);
            image.close();

            OutputStream os = socket.getOutputStream();
            String s = "HTTP/1.0 200" + "\r\n" +
                    "Content type: image/jpeg" + "\r\n" +
                    "Content length: " + total + "\r\n" +
                    "\r\n";
            os.write(s.getBytes());
            os.write(bytes);
            os.flush();
            os.write('\r');
            os.write('\n');
            os.flush();
            os.close();

        } catch ( NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }
}