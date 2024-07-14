package com.example.speakinghand;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

// Class that given an open BT Socket will
// Open, manage and close the data Stream from the Arduino BT device
public class ConnectedThread extends Thread {

    private static final String TAG = "FrugalLogs";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        // Input and Output streams members of the class
        // We won't use the Output stream of this project
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public String getValueRead() {
        return valueRead;
    }

    public void run() {
        StringBuilder buffer = new StringBuilder(); // Buffer to store incomplete readings

        byte[] bufferArray = new byte[1024];
        int bytes; // bytes returned from read()
        List<String> readings = new ArrayList<>(); // List to store individual readings

        try {
            while ((bytes = mmInStream.read(bufferArray)) != -1 && readings.size() < 30) {
                String data = new String(bufferArray, 0, bytes);
                buffer.append(data);

                int newlineIndex;
                while ((newlineIndex = buffer.indexOf("\n")) != -1) {
                    String reading = buffer.substring(0, newlineIndex).trim();

                    // Replace "nan" with 0
                    reading = reading.replaceAll("nan", "0");

                    readings.add(reading);
                    buffer.delete(0, newlineIndex + 1);

                    if (readings.size() == 30) {
                        break;
                    }
                }

                if (readings.size() == 30) {
                    break;
                }
            }

            // Process the first 30 readings (if collected)
            if (readings.size() > 0) {
                String accumulatedReadings = String.join("\n", readings);
                MainActivity3.handler.obtainMessage(MainActivity3.ERROR_READ, accumulatedReadings).sendToTarget();
            }

        } catch (IOException e) {
            Log.e(TAG, "Input stream was disconnected", e);
        }
    }

    public void cancel() {
    }
}