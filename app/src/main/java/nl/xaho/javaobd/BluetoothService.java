package nl.xaho.javaobd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {
    final String TAG = "BluetoothService";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private final IBinder binder = new BluetoothBinder();
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("ContentText")
//                .setSmallIcon(R.drawable.ic_stat_name)
//                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        return START_REDELIVER_INTENT;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void enableBluetooth(Activity activity) {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device does not have Bluetooth, unable to connect.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    final int REQUEST_ENABLE_BT = 0;
    final int REQUEST_CONNECT_BT = 1;
    @RequiresApi(api = Build.VERSION_CODES.S)
    public Pair<List<String>, List<String>> GetBluetoothDevices(Activity activity) throws Exception {
        enableBluetooth(activity);

        ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CONNECT_BT);
            throw new Exception("Permission denied");
        }
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
//                Boolean autoConnect = true;
//                if (autoConnect && device.getName().equals("OBDII")) {
//                    deviceAddress = device.getAddress();
//                    connectToBtDeviceAddress();
//                    return;
//                }
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }
        return new Pair<>(deviceStrs, devices);
    }

    public Pair<InputStream, OutputStream> connectToBtDeviceAddress(String deviceAddress) throws Exception {
        BluetoothAdapter btAdapter1 = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter1.getRemoteDevice(deviceAddress);
        return setupBluetoothConnection(device);
    }

    @SuppressLint("MissingPermission")
    private Pair<InputStream, OutputStream> setupBluetoothConnection(BluetoothDevice device) throws Exception {
        // TODO: Spinner
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        // set up bluetooth serial connection
        socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        socket.connect();
        in = socket.getInputStream();
        out = socket.getOutputStream();
        return new Pair<>(in, out);
    }

    public class BluetoothBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
