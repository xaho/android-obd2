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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.HeadersOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.HIGradient;
import com.highsoft.highcharts.common.HIStop;
import com.highsoft.highcharts.common.hichartsclasses.HIBackground;
import com.highsoft.highcharts.common.hichartsclasses.HIPoint;
import com.highsoft.highcharts.common.hichartsclasses.HISeries;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;
import com.highsoft.highcharts.core.HIChartView;

import nl.xaho.javaobd.HiChartsBuilders.HiBackground;
import nl.xaho.javaobd.HiChartsBuilders.HiChart;
import nl.xaho.javaobd.HiChartsBuilders.HiData;
import nl.xaho.javaobd.HiChartsBuilders.HiGauge;
import nl.xaho.javaobd.HiChartsBuilders.HiLabels;
import nl.xaho.javaobd.HiChartsBuilders.HiMarker;
import nl.xaho.javaobd.HiChartsBuilders.HiOptions;
import nl.xaho.javaobd.HiChartsBuilders.HiPane;
import nl.xaho.javaobd.HiChartsBuilders.HiPlotBands;
import nl.xaho.javaobd.HiChartsBuilders.HiPlotOptions;
import nl.xaho.javaobd.HiChartsBuilders.HiSpline;
import nl.xaho.javaobd.HiChartsBuilders.HiTitle;
import nl.xaho.javaobd.HiChartsBuilders.HiTooltip;
import nl.xaho.javaobd.HiChartsBuilders.HiXAxis;
import nl.xaho.javaobd.HiChartsBuilders.HiYAxis;
import nl.xaho.javaobd.OBDCommands.InstantCommand;
import nl.xaho.javaobd.OBDCommands.InstantRPMCommand;
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
                        .setTooltip(new HiTooltip().setHeaderFormat("<b>{series.name}</b><br>").setPointFormat("{point.x:%H:%M:%S.%L}: {point.y:.2f}").build())
                        .setSeries(new ArrayList<>(Arrays.asList(
//                                new HiSpline().setName("Module voltage").build(),
                                new HiSpline().setName("Engine RPM").setYAxis(1).build()
                        ))).build());

        HIGradient gradient = new HIGradient();
        LinkedList<HIStop> stops = new LinkedList<>(Arrays.asList(
                new HIStop(0, HIColor.initWithHexValue("FFF")),
                new HIStop(1, HIColor.initWithHexValue("333"))
        ));

        HIChartView chartView2 = view.findViewById(R.id.hc2);
        chartView2.setOptions(new HiOptions()
                .setChart(new HiChart().setType("gauge").setPlotBorderWidth(0).build())
                .setTitle("Speedometer")
                .setPane(new HiPane()
                        .setStartAngle(-150)
                        .setEndAngle(150)
                        .setBackground(new ArrayList<>(Arrays.asList(
                                new HiBackground().setBackgroundColor(HIColor.initWithLinearGradient(gradient, stops)).setBorderWidth(0).setOuterRadius("109%").build(),
                                new HiBackground().setBackgroundColor(HIColor.initWithLinearGradient(gradient, stops)).setBorderWidth(1).setOuterRadius("107%").build(),
                                new HIBackground(),
                                new HiBackground().setBackgroundColor("DDD").setBorderWidth(0).setOuterRadius("105%").setInnerRadius("103%").build()
                        ))).build())
                .setYAxis(new ArrayList<>(Collections.singletonList(new HiYAxis()
                        .setMin(0)
                        .setMax(200)
                        .setMinorTickWidth(1)
                        .setMinorTickLength(10)
                        .setMinorTickPosition("inside")
                        .setMinorTickColor(HIColor.initWithHexValue("666"))
                        .setTickPixelInterval(30)
                        .setTickWidth(2)
                        .setTickPosition("inside")
                        .setTickLength(10)
                        .setTickColor(HIColor.initWithHexValue("666"))
                        .setLabels(new HiLabels().setStep(2).build())
                        .setTitle("km/h")
                        .setPlotBands(new ArrayList<>(Arrays.asList(
                                new HiPlotBands()
                                        .setFrom(0)
                                        .setTo(120)
                                        .setColor("55BF3B").build(),
                                new HiPlotBands()
                                        .setFrom(120)
                                        .setTo(160)
                                        .setColor("DDDF0D").build(),
                                new HiPlotBands()
                                        .setFrom(160)
                                        .setTo(200)
                                        .setColor("DF5353").build()
                        ))).build())))
                .setSeries(new ArrayList<>(Collections.singletonList(
                        new HiGauge()
                                .setName("Speed")
                                .setTooltip(new HiTooltip().setValueSuffix(" km/h").build())
                                .setData(new ArrayList<>(Collections.singletonList(0)))
                                .build())))
                .build());

        binding.buttonFirst.setOnClickListener(x -> checkBluetooth());
        binding.buttonFirst.setOnLongClickListener(x -> emulateConnection());
    }

    private boolean emulateConnection() {
        handler.postDelayed(pollOdbRunnable = () -> {
            getSeriesWithName("Speed").setData(new ArrayList(Arrays.asList(new HiData().setX(System.currentTimeMillis()).setY(Math.random() * 100).build())));
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

    final int REQUEST_ENABLE_BT = 0;
    @RequiresApi(api = Build.VERSION_CODES.S)
    protected void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                selectBluetoothDevice();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void checkBluetooth() {
        // TODO: turn BT on if off
        BluetoothManager bluetoothManager = getSystemService(this.requireContext(), BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            selectBluetoothDevice();
        }
    }

    final int REQUEST_CONNECT_BT = 1;
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void selectBluetoothDevice() {
        // Check permissions
        Log.println(Log.DEBUG, TAG, "dobtstuff");
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CONNECT_BT);
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

                    try {
                        if (!setupBluetoothConnection(device)) return;
                        setupOB2Configuration(in, out);
                        setupPolling(in, out);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .setTitle("Choose Bluetooth device")
                .show();
    }

    private boolean setupBluetoothConnection(BluetoothDevice device) throws IOException {
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
            throw e;
        }
        try {
            assert socket != null;
            socket.connect();
        } catch (IOException e) {
            Log.d(TAG, "Failed to connect");
            // TODO: Show toast
            throw e;
        }
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG, "Failed to get IO stream");
            throw e;
        }
        return true;
    }

    private void setupOB2Configuration(InputStream in, OutputStream out) throws IOException, InterruptedException {
        new EchoOffCommand().run(in, out);
        new HeadersOffCommand().run(in, out);
        new LineFeedOffCommand().run(in, out);
        new TimeoutCommand(100).run(in, out);
        // TODO do not use auto
        new SelectProtocolCommand(ObdProtocols.AUTO).run(in, out);
    }

    private void setupPolling(InputStream in, OutputStream out) {
        handler.postDelayed(pollOdbRunnable = () -> {
            poll(in, out);
            handler.postDelayed(pollOdbRunnable, 0);
        }, 0);
    }

    private HISeries getSeriesWithName(String name) {
        ArrayList<HISeries> series = ((HIChartView) this.getView().findViewById(R.id.hc)).getOptions().getSeries();
        for (HISeries s : series) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        series = ((HIChartView) this.getView().findViewById(R.id.hc2)).getOptions().getSeries();
        for (HISeries s : series) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    private void poll(InputStream in, OutputStream out) {
        // TODO: Packed data?
        // TODO: Multiple PID requests? https://stackoverflow.com/questions/21334147/send-multiple-obd-commands-together-and-get-response-simultaneously
        // TODO: instant response on first data: https://stackoverflow.com/questions/21334147/send-multiple-obd-commands-together-and-get-response-simultaneously
        // TODO: resend last command using \r
        try {
            final long start = System.currentTimeMillis();
            // TODO: Unable to connect causes broken pipe

            InstantRPMCommand instantRPMCommand = new InstantRPMCommand();
            instantRPMCommand.run(in, out);
            LogCommandDuration(instantRPMCommand);
            getSeriesWithName("Engine RPM").addPoint(new HiData().setX(System.currentTimeMillis()).setY(instantRPMCommand.getRPM()).build());

            SpeedCommand speedCommand = new SpeedCommand();
            speedCommand.run(in, out);
            LogCommandDuration(speedCommand);
            getSeriesWithName("Speed").setData(new ArrayList(Arrays.asList(new HiData().setX(System.currentTimeMillis()).setY(speedCommand.getMetricSpeed()).build())));

            Log.d(TAG, "Total time for loop: " + String.valueOf(System.currentTimeMillis()-start));
//        } catch (UnableToConnectException e) {
//            Toast.makeText(this.requireContext(), "Failed to connect to OBD2 scanner", Toast.LENGTH_SHORT).show();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void LogCommandDuration(ObdCommand c) {
        Log.d(TAG, c.getName() + " took " + (c.getEnd()-c.getStart()) + " ms");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}