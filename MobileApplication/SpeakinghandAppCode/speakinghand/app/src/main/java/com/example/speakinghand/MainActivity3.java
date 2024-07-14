package com.example.speakinghand;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.speakinghand.ml.TfliteFinal21words;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import soup.neumorphism.NeumorphImageButton;

public class MainActivity3 extends AppCompatActivity {
    TextToSpeech tte;
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    public static Handler handler;
    public final static int ERROR_READ = 0;

    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TfliteFinal21words model;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bluetooth Connection");

        TextView btReadings = findViewById(R.id.btReadings);
        TextView btDevices = findViewById(R.id.btDevices);
        TextView result = findViewById(R.id.result);
        Button connectToDevice = findViewById(R.id.connectToDevice);
        Button searchDevices = findViewById(R.id.searchDevices);
        Button predictFragment = findViewById(R.id.predictFragment);
        Button clearValues = findViewById(R.id.refresh);
        NeumorphImageButton speakButton = findViewById(R.id.speakButton);

        speakButton.setOnClickListener(v -> {
            tte = new TextToSpeech(getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tte.setLanguage(Locale.US);
                    tte.setSpeechRate(1.0f);
                    tte.speak(result.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                }
            });
        });

        Log.d(TAG, "Begin Execution");

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == ERROR_READ) {
                    String arduinoMsg = msg.obj.toString();
                    btReadings.append(arduinoMsg);
                }
            }
        };

        clearValues.setOnClickListener(view -> {
            btReadings.setText("");
            result.setText("Result:");
        });

        Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();
            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");
                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if (connectedThread.getValueRead() != null) {
                    emitter.onNext(connectedThread.getValueRead());
                }
                connectedThread.cancel();
            }
            connectThread.cancel();
            emitter.onComplete();
        });

        connectToDevice.setOnClickListener(view -> {
            btReadings.setText("");
            if (arduinoBTModule != null) {
                new Thread(() -> connectToBTObservable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                btReadings::setText,
                                throwable -> Log.e(TAG, "Error reading from Bluetooth", throwable)
                        )).start();
            }
        });

        searchDevices.setOnClickListener(view -> {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothAdapter == null) {
                Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity3.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_BT);
                return;
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                StringBuilder devicesStringBuilder = new StringBuilder();
                for (BluetoothDevice device : pairedDevices) {
                    devicesStringBuilder.append(device.getName()).append("\n").append(device.getAddress()).append("\n\n");
                    if (device.getName().equals("HC-05")) {
                        arduinoBTModule = device;
                    }
                }
                btDevices.setText(devicesStringBuilder.toString());
                connectToDevice.setEnabled(arduinoBTModule != null);
            }
        });

        predictFragment.setOnClickListener(v -> {
            String data = btReadings.getText().toString();

            if (data.isEmpty()) {
                result.setText("No data to predict");
                return;
            }
            predictUsingModel(data, result);
        });
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("tflite_Final21Words.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void predictUsingModel(String data, TextView result) {
        try {
            MappedByteBuffer tfliteModel = loadModelFile(this);
            Interpreter.Options options = new Interpreter.Options();
            Interpreter interpreter = new Interpreter(tfliteModel, options);

            String[] dataRows = data.split(";");

            int numFeatures = 20;
            int numTimesteps = 30;

            List<List<Double>> numericalData = new ArrayList<>();

            for (String dataRow : dataRows) {
                String[] rowValues = dataRow.split(",");

                List<Double> rowData = new ArrayList<>();

                for (String value : rowValues) {
                    rowData.add(Double.parseDouble(value));
                }

                numericalData.add(rowData);
            }

            if (numericalData.size() < numTimesteps) {
                throw new IllegalArgumentException("Not enough timesteps. Expected at least " + numTimesteps +
                        ", got " + numericalData.size() + ".");
            }

            double[] means = {155.32813, 156.83272, 184.99436, -33.406864, 41.7419872, 26.8365373, 0.283098959, 0.264162277, 0.577512704, -0.320786267, -0.17822356, 0.264606489, 4.75680127, -0.362836851, 3.22691767, 186.593994, 296.326368, 428.521147, 407.810338, 291.020891};
            double[] stdDevs = {52.24348323, 44.74552135, 71.22302512, 24.28521161, 17.90686864, 19.04530714, 0.40190694, 0.37420368, 0.51490862, 0.3951466, 0.46546105, 0.46364466, 80.6295805, 62.86730305, 71.80844895, 21.91401089, 49.11814093, 59.19946979, 76.22642655, 57.73410232};

            double[][] normalizedData = new double[numericalData.size()][numFeatures];
            for (int i = 0; i < numericalData.size(); i++) {
                for (int j = 0; j < numFeatures; j++) {
                    normalizedData[i][j] = (numericalData.get(i).get(j) - means[j]) / stdDevs[j];
                }
            }

            float[][][][] reshapedData = new float[1][numTimesteps][numFeatures][1];
            for (int i = 0; i < numTimesteps; i++) {
                for (int j = 0; j < numFeatures; j++) {
                    reshapedData[0][i][j][0] = (float) normalizedData[i][j];
                }
            }

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 30, 20, 1}, DataType.FLOAT32);
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * numTimesteps * numFeatures * 4);
            inputBuffer.order(ByteOrder.nativeOrder());
            FloatBuffer floatBuffer = inputBuffer.asFloatBuffer();
            for (int i = 0; i < numTimesteps; i++) {
                for (int j = 0; j < numFeatures; j++) {
                    floatBuffer.put(reshapedData[0][i][j][0]);
                }
            }
            inputFeature0.loadBuffer(inputBuffer);

            TfliteFinal21words model = TfliteFinal21words.newInstance(this);
            TfliteFinal21words.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] predictions = outputFeature0.getFloatArray();
            int predictedClass = 0;
            float maxPrediction = predictions[0];
            for (int i = 1; i < predictions.length; i++) {
                if (predictions[i] > maxPrediction) {
                    maxPrediction = predictions[i];
                    predictedClass = i;
                }
            }

            String[] classLabels = {"Add", "Big", "Good", "Goodbye", "Hello", "Key", "No", "Small", "Stop", "Thank you", "Water",
                    "Lip", "Airplane", "After", "Absent", "Eat", "Again", "Boy", "Floor", "Plant", "Roof"};

            result.setText("Predicted Sign: " + classLabels[predictedClass]);

            model.close();
            interpreter.close();
        } catch (IOException e) {
            result.setText("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            result.setText("Error: " + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
