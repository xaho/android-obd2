package nl.xaho.javaobd;

import static android.app.Activity.RESULT_OK;

import static nl.xaho.javaobd.HiChartsBuilders.HiUtils.createDatetimeAxis;
import static nl.xaho.javaobd.HiChartsBuilders.HiUtils.getSeriesWithName;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
//import androidx.navigation.fragment.NavHostFragment;
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.protocol.DescribeProtocolCommand;
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
import nl.xaho.javaobd.HiChartsBuilders.HiYAxis;
import nl.xaho.javaobd.OBDCommands.InstantRPMCommand;
import nl.xaho.javaobd.OBDCommands.InstantSpeedCommand;
import nl.xaho.javaobd.OBDCommands.InstantThrottlePositionCommand;
import nl.xaho.javaobd.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String TAG = "obd";
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    private FragmentFirstBinding binding;
    private String deviceAddress;
    private Runnable pollOdbRunnable;
    private final Handler handler = new Handler();
    HIChartView chartView;
    HIChartView chartView2;
    TextView tv;
    Button btn;
    BluetoothService btService;
    boolean mBound = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
//        WebView.setWebContentsDebuggingEnabled(true);
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        setupHighcharts(binding.getRoot());
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv = view.findViewById(R.id.text_gear_ratio);
        btn = view.findViewById(R.id.button_first);

        binding.buttonFirst.setOnClickListener(x -> showDialogForBtDevice());
        binding.buttonFirst.setOnLongClickListener(x -> emulateConnection());
        Context context = this.requireContext();
        Intent intent = new Intent(context, BluetoothService.class);
        context.startForegroundService(intent);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to BluetoothService, cast the IBinder and get BluetoothService instance
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) service;
            btService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void showDialogForBtDevice() {
        try {
            Pair<List<String>, List<String>> btDevices = btService.GetBluetoothDevices(FirstFragment.this.requireActivity());
            // show dialog to select device
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.requireContext());
            List<String> deviceStrs = btDevices.first;
            final List<String> devices = btDevices.second;
            ArrayAdapter adapter = new ArrayAdapter(this.requireContext(), android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[deviceStrs.size()]));

            alertDialog
                    .setSingleChoiceItems(adapter, -1, (dialog, which) -> {
                        dialog.dismiss();
                        deviceAddress = devices.get(((AlertDialog) dialog).getListView().getCheckedItemPosition());
                        try {
                            connectToBtDeviceAddress(deviceAddress);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this.requireContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setTitle("Choose Bluetooth device")
                    .show();
        } catch (Exception e) {
        }
    }

    private void setupHighcharts(@NonNull View view) {
        chartView = view.findViewById(R.id.hc);
        chartView.plugins = new ArrayList<>(Arrays.asList("series-label"));

        // https://api.highcharts.com/class-reference/Highcharts.Time#dateFormat
        chartView.setOptions(
                new HiOptions()
                        .setChart(new HiChart().setZoomType("x").build())
                        .setTitle(new HiTitle().setText("OBD2 data").build())
                        .setXAxis(new ArrayList<>(Arrays.asList(createDatetimeAxis())))
                        .setYAxis(new ArrayList<>(Arrays.asList(
//                                new HiYAxis().setTitle("Voltage").setMin(0).build(),
                                new HiYAxis().setTitle("RPM").setMin(0).build(),
                                new HiYAxis().setTitle("KMh").setMin(0).setOpposite(true).build(),
                                new HiYAxis().setTitle("%").setMin(0).setMax(100).setOpposite(true).build()
                        )))
                        .setPlotOptions(
                                new HiPlotOptions().setSpline(
                                        new HiSpline().setMarker(
                                                new HiMarker().setEnabled(true).setRadius(0).build()).build()).build())
                        .setTooltip(new HiTooltip().setHeaderFormat("<b>{series.name}</b><br>").setPointFormat("{point.x:%H:%M:%S.%L}: {point.y:.2f}").build())
                        .setSeries(new ArrayList<>(Arrays.asList(
//                                new HiSpline().setName("Module voltage").build(),
                                new HiSpline().setName("Engine RPM").setYAxis(0).build(),
                                new HiSpline().setName("Speed").setYAxis(1).build(),
                                new HiSpline().setName("Throttle").setYAxis(2).build()
                        ))).build());

        HIGradient gradient = new HIGradient();
        LinkedList<HIStop> stops = new LinkedList<>(Arrays.asList(
                new HIStop(0, HIColor.initWithHexValue("FFF")),
                new HIStop(1, HIColor.initWithHexValue("333"))
        ));

        chartView2 = view.findViewById(R.id.hc2);
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
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean emulateConnection() {
        if (handler.hasCallbacks(pollOdbRunnable)) {
            handler.removeCallbacks(pollOdbRunnable);
            return true;
        }
        handler.postDelayed(pollOdbRunnable = () -> {
//            getSeriesWithName(chartView2, "Speed").setData(new ArrayList(Arrays.asList(new HiData().setX(System.currentTimeMillis()).setY(Math.random() * 100).build())));
//            getSeriesWithName(chartView, "Module voltage").addPoint(new HiData().setX(System.currentTimeMillis()).setY(Math.random() * 100).build());
            getSeriesWithName(chartView, "Engine RPM").addPoint(new HiData().setX(System.currentTimeMillis()).setY((int) (Math.random() * 100)).build());
            getSeriesWithName(chartView, "Speed").addPoint(new HiData().setX(System.currentTimeMillis()).setY((int) (Math.random() * 100)).build());
            handler.postDelayed(pollOdbRunnable, 350);
        }, 1000);
        return true; //consume event
    }

    final int REQUEST_ENABLE_BT = 0;
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK)
                showDialogForBtDevice();
            else
                Toast.makeText(this.requireContext(), "Cannot connect if Bluetooth is not enabled.", Toast.LENGTH_LONG).show();
        }
    }

    private void connectToBtDeviceAddress(String deviceAddress) throws Exception {
        Pair<InputStream, OutputStream> streams = btService.connectToBtDeviceAddress(deviceAddress);
        InputStream in = streams.first;
        OutputStream out = streams.second;

        btn.setText("Disconnect");
        setupOB2Configuration(in, out);
        setupPolling(in, out);
    }

    private void setupOB2Configuration(InputStream in, OutputStream out) throws IOException, InterruptedException {
        new EchoOffCommand().run(in, out);
        new HeadersOffCommand().run(in, out);
        new LineFeedOffCommand().run(in, out);
        new TimeoutCommand(100).run(in, out);
        DescribeProtocolCommand dpc = new DescribeProtocolCommand();
        dpc.run(in, out);
        Log.d(TAG, "Describe Protocol (6,7,8,9 or ISO_15765_4... can handle multiple reqeusts): " + dpc.getFormattedResult());
        new SelectProtocolCommand(ObdProtocols.AUTO).run(in, out);
    }

    interface PollCallback {
        void onResult(ArrayList<Number> values);
    }

    private void setupPolling(InputStream in, OutputStream out) {
        // list of commands to execute
        PollCallback cb = values -> {
//            Number voltage = values.get(0);
            Number rpm = values.get(1);
            Number speed = values.get(2);
            Number throttle = values.get(3);
//            getSeriesWithName(chartView, "Module voltage").addPoint(new HiData().setX(System.currentTimeMillis()).setY(voltage).build());
            getSeriesWithName(chartView, "Engine RPM").addPoint(new HiData().setX(System.currentTimeMillis()).setY(rpm).build());
            getSeriesWithName(chartView, "Speed").addPoint(new HiData().setX(System.currentTimeMillis()).setY(speed).build());
            getSeriesWithName(chartView2, "Speed").setData(new ArrayList(Arrays.asList(new HiData().setX(System.currentTimeMillis()).setY(speed).build())));
            getSeriesWithName(chartView, "Throttle").addPoint(new HiData().setX(System.currentTimeMillis()).setY(throttle).build());
        };
        executorService.execute(() -> handler.postDelayed(pollOdbRunnable = () -> {
            poll(in, out, cb);
            handler.postDelayed(pollOdbRunnable, 300);
        }, 0));
    }

    private void poll(InputStream in, OutputStream out, PollCallback cb) {
        // TODO: Packed data?
        // TODO: Multiple PID requests? https://stackoverflow.com/questions/21334147/send-multiple-obd-commands-together-and-get-response-simultaneously
        // TODO: resend last command using \r
        try {
            final long start = System.currentTimeMillis();
            // TODO: Unable to connect causes broken pipe
//            ModuleVoltageCommand mvc = new ModuleVoltageCommand();
//            mvc.run(in, out);
//            LogCommandDuration(mvc);
//            double voltage = mvc.getVoltage();

            InstantRPMCommand instantRPMCommand = new InstantRPMCommand();
            instantRPMCommand.run(in, out);
            LogCommandDuration(instantRPMCommand);
            int rpm = instantRPMCommand.getRPM();

            InstantSpeedCommand speedCommand = new InstantSpeedCommand();
            speedCommand.run(in, out);
            LogCommandDuration(speedCommand);
            int speed = speedCommand.getMetricSpeed();

            InstantThrottlePositionCommand throttlePositionCommand = new InstantThrottlePositionCommand();
            throttlePositionCommand.run(in, out);
            LogCommandDuration(throttlePositionCommand);
            float throttle = throttlePositionCommand.getPercentage();

//            IntakeManifoldPressureCommand impc = new IntakeManifoldPressureCommand();
//            impc.run(in, out);
//            LogCommandDuration(impc);
//            int kpa = impc.getMetricUnit();
//            Log.d(TAG, "Intake manifold pressure: " + impc.getFormattedResult());

//            InstantLoadCommand lc = new InstantLoadCommand();
//            lc.run(in, out);
//            LogCommandDuration(lc);
//            float load = lc.getPercentage();
//            Log.d(TAG, "Load: " + load);

            Log.d(TAG, String.format("%02f, %d, %d", throttle, rpm, speed));
            Log.d(TAG, "Total time for loop: " + (System.currentTimeMillis() - start));
            cb.onResult(new ArrayList(Arrays.asList(throttle, rpm, speed, throttle)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void LogCommandDuration(ObdCommand c) {
        Log.d(TAG, c.getName() + " took " + (c.getEnd() - c.getStart()) + " ms");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}