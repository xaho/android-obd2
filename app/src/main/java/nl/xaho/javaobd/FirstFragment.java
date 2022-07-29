package nl.xaho.javaobd;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.webkit.WebView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
//import androidx.navigation.fragment.NavHostFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.highsoft.highcharts.common.hichartsclasses.HISeries;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;
import com.highsoft.highcharts.core.HIChartView;

import nl.xaho.javaobd.HiChartsBuilders.HiData;
import nl.xaho.javaobd.HiChartsBuilders.HiMarker;
import nl.xaho.javaobd.HiChartsBuilders.HiOptions;
import nl.xaho.javaobd.HiChartsBuilders.HiPlotOptions;
import nl.xaho.javaobd.HiChartsBuilders.HiSpline;
import nl.xaho.javaobd.HiChartsBuilders.HiTitle;
import nl.xaho.javaobd.HiChartsBuilders.HiTooltip;
import nl.xaho.javaobd.HiChartsBuilders.HiXAxis;
import nl.xaho.javaobd.HiChartsBuilders.HiYAxis;
import nl.xaho.javaobd.OBDCommands.GearCommand;
import nl.xaho.javaobd.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String TAG = "obd";
    private FragmentFirstBinding binding;
    private String deviceAddress;
    private Runnable pollOdbRunnable;
    private final Handler handler = new Handler();
    private InputStream in;
    private OutputStream out;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
//        WebView.setWebContentsDebuggingEnabled(true);
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HIChartView chartView = view.findViewById(R.id.hc);
        chartView.plugins = new ArrayList<>(Arrays.asList("series-label"));

        // https://api.highcharts.com/class-reference/Highcharts.Time#dateFormat
        chartView.setOptions(
                new HiOptions()
                        .setTitle(new HiTitle().setText("OBD2 data").build())
                        .setXAxis(new ArrayList<>(Arrays.asList(createDatetimeAxis())))
                        .setYAxis(new ArrayList<>(Arrays.asList(
                                new HiYAxis().setTitle("Voltage").setMin(0).build(),
                                new HiYAxis().setTitle("RPM").setMin(0).setOpposite(true).build()
                        )))
                        .setPlotOptions(
                                new HiPlotOptions().setSpline(
                                        new HiSpline().setMarker(
                                                new HiMarker().setEnabled(true).setRadius(2).build()).build()).build())
                        .setTooltip(new HiTooltip().setHeaderFormat("<b>{series.name}</b><br>").setPointFormat("{point.x:%H:%M:%S}: {point.y:.2f}").build())
                        .setSeries(new ArrayList<>(Arrays.asList(
                                new HiSpline().setName("Module voltage").build(),
                                new HiSpline().setName("Engine RPM").setYAxis(1).build()
                        ))).build());

        binding.buttonFirst.setOnClickListener(x -> selectBluetoothDevice());
        binding.buttonFirst.setOnLongClickListener(x -> emulateConnection());
    }

    private boolean emulateConnection() {
        handler.postDelayed(pollOdbRunnable = () -> {
            getSeriesWithName("Module voltage").addPoint(new HiData().setX(System.currentTimeMillis()).setY(Math.random()*100).build());
            getSeriesWithName("Engine RPM").addPoint(new HiData().setX(System.currentTimeMillis()).setY(Math.random()*100).build());
            handler.postDelayed(pollOdbRunnable, 1000);
        }, 1000);
        return true; //consume event
    }

    @NonNull
    private HIXAxis createDatetimeAxis() {
        return new HiXAxis()
                .setTitle("Date")
                .setType("datetime")
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void selectBluetoothDevice() {
        // Check permissions
        Log.println(Log.DEBUG, TAG, "dobtstuff");
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // get bluetooth devices
        ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        // show dialog to select device
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.requireContext());

        ArrayAdapter adapter = new ArrayAdapter(this.requireContext(), android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog
                .setSingleChoiceItems(adapter, -1, (dialog, which) -> {
                    // handle selection -> connect to device
                    dialog.dismiss();

                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    deviceAddress = devices.get(position);
                    BluetoothAdapter btAdapter1 = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = btAdapter1.getRemoteDevice(deviceAddress);

                    if (!setupBluetoothConnection(device)) return;
                    setupOB2Configuration(in, out);
                    setupPolling(in, out);
                })
                .setTitle("Choose Bluetooth device")
                .show();
    }

    private boolean setupBluetoothConnection(BluetoothDevice device) {
        // TODO: Spinner
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        if (ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "should never happen");
            return false;
        }
        // set up bluetooth serial connection
        BluetoothSocket socket = null;
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.d(TAG, "Failed to create RfCommSocket");
            e.printStackTrace();
        }
        try {
            assert socket != null;
            socket.connect();
        } catch (IOException e) {
            Log.d(TAG, "Failed to connect");
            e.printStackTrace();
        }
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG, "Failed to get IO stream");
            e.printStackTrace();
        }
        return true;
    }

    private void setupOB2Configuration(InputStream in, OutputStream out) {
        try {
            new EchoOffCommand().run(in, out);
            new LineFeedOffCommand().run(in, out);
            new TimeoutCommand(100).run(in, out);
            new SelectProtocolCommand(ObdProtocols.AUTO).run(in, out);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupPolling(InputStream in, OutputStream out) {
        handler.postDelayed(pollOdbRunnable = () -> {
            poll(in, out);
            handler.postDelayed(pollOdbRunnable, 1000);
        }, 1000);
    }
    
    private HISeries getSeriesWithName(String name) {
        ArrayList<HISeries> series = ((HIChartView) this.getView().findViewById(R.id.hc)).getOptions().getSeries();
        for (HISeries s : series) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    private void poll(InputStream in, OutputStream out) {
        try {
            ModuleVoltageCommand mvc = new ModuleVoltageCommand();
            mvc.run(in, out);
            getSeriesWithName("Module voltage").addPoint(new HiData().setX(System.currentTimeMillis()).setY(mvc.getVoltage()).build());

            RPMCommand rpmCommand = new RPMCommand();
            rpmCommand.run(in, out);
            getSeriesWithName("Engine RPM").addPoint(new HiData().setX(System.currentTimeMillis()).setY(rpmCommand.getRPM()).build());

            GearCommand gearCommand = new GearCommand();
            gearCommand.run(in, out);
            Log.d(TAG, gearCommand.getCalculatedResult());
//        } catch (UnableToConnectException e) {
//            Toast.makeText(this.requireContext(), "Failed to connect to OBD2 scanner", Toast.LENGTH_SHORT).show();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}