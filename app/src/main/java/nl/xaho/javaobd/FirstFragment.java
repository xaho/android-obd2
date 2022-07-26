package nl.xaho.javaobd;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

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
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.highsoft.highcharts.common.hichartsclasses.HIData;
import com.highsoft.highcharts.common.hichartsclasses.HIDateTimeLabelFormats;
import com.highsoft.highcharts.common.hichartsclasses.HIMarker;
import com.highsoft.highcharts.common.hichartsclasses.HIMonth;
import com.highsoft.highcharts.common.hichartsclasses.HIOptions;
import com.highsoft.highcharts.common.hichartsclasses.HIPlotOptions;
import com.highsoft.highcharts.common.hichartsclasses.HISpline;
import com.highsoft.highcharts.common.hichartsclasses.HITitle;
import com.highsoft.highcharts.common.hichartsclasses.HITooltip;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis;
import com.highsoft.highcharts.common.hichartsclasses.HIYear;
import com.highsoft.highcharts.core.HIChartView;

import nl.xaho.javaobd.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "obd";
    private FragmentFirstBinding binding;
    private String deviceAddress;
    private TextView tv;
    private Runnable pollOdbRunnable;
    private Runnable updateGraphRunnable;
    private final Handler handler = new Handler();
    private final Handler updateGraphHanlder = new Handler();
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
        tv = view.findViewById(R.id.textview_first);

        HIChartView chartView = (HIChartView) view.findViewById(R.id.hc);

        chartView.plugins = new ArrayList<>(Arrays.asList("series-label"));

        HIOptions options = new HIOptions();

        HITitle title = new HITitle();
        title.setText("OBD2 data");
        options.setTitle(title);

//        HISubtitle subtitle = new HISubtitle();
//        subtitle.setText("Irregular time data in Highcharts JS");
//        options.setSubtitle(subtitle);

        HIXAxis xAxis = new HIXAxis();
        xAxis.setType("datetime");
        xAxis.setDateTimeLabelFormats(new HIDateTimeLabelFormats());
        xAxis.getDateTimeLabelFormats().setMonth(new HIMonth());
        xAxis.getDateTimeLabelFormats().getMonth().setMain("%e. %b");
        xAxis.getDateTimeLabelFormats().setYear(new HIYear());
        xAxis.getDateTimeLabelFormats().getYear().setMain("%b");
        xAxis.setTitle(new HITitle());
        xAxis.getTitle().setText("Date");
        options.setXAxis(new ArrayList<HIXAxis>(){{add(xAxis);}});

        HIYAxis yAxis = new HIYAxis();
        yAxis.setTitle(new HITitle());
        yAxis.getTitle().setText("Voltage");
        yAxis.setMin(0);
        options.setYAxis(new ArrayList<HIYAxis>(){{add(yAxis);}});

        HITooltip tooltip = new HITooltip();
        tooltip.setHeaderFormat("<b>{series.name}</b><br>");
        // https://api.highcharts.com/class-reference/Highcharts.Time#dateFormat
        tooltip.setPointFormat("{point.x:%H:%M:%S}: {point.y:.2f}V");
        options.setTooltip(tooltip);

        HIPlotOptions plotOptions = new HIPlotOptions();
        plotOptions.setSpline(new HISpline());
        plotOptions.getSpline().setMarker(new HIMarker());
        plotOptions.getSpline().getMarker().setEnabled(true);
        options.setPlotOptions(plotOptions);

        HISpline series1 = new HISpline();
        series1.setName("Module voltage");
        Number[][] series1_data = new Number[][] {
        };
        series1.setData(new ArrayList<>(Arrays.asList(series1_data)));

        /*HISpline series2 = new HISpline();
        series2.setName("Winter 2013-2014");
        Number[][] series2_data = new Number[][] { { 26006400000L, 0 }, { 26956800000L, 0.4 }, { 28857600000L, 0.25 }, { 31536000000L, 1.66 }, { 32313600000L, 1.8 }, { 35769600000L, 1.76 }, { 38707200000L, 2.62 }, { 40867200000L, 2.41 }, { 41817600000L, 2.05 }, { 43027200000L, 1.7 }, { 43891200000L, 1.1 }, { 45360000000L, 0 } };
        series2.setData(new ArrayList<>(Arrays.asList(series2_data)));

        HISpline series3 = new HISpline();
        series3.setName("Winter 2014-2015");
        Number[][] series3_data = new Number[][] { { 28339200000L, 0 }, { 29289600000L, 0.25 }, { 30499200000L, 1.41 }, { 30931200000L, 1.64 }, { 31795200000L, 1.6 }, { 32918400000L, 2.55 }, { 33523200000L, 2.62 }, { 34473600000L, 2.5 }, { 35337600000L, 2.42 }, { 37065600000L, 2.74 }, { 37756800000L, 2.62 }, { 38620800000L, 2.6 }, { 39398400000L, 2.81 }, { 40262400000L, 2.63 }, { 41644800000L, 2.77 }, { 42249600000L, 2.68 }, { 42681600000L, 2.56 }, { 43113600000L, 2.39 }, { 43545600000L, 2.3 }, { 44928000000L, 2 }, { 45360000000L, 1.85 }, { 45792000000L, 1.49 }, { 46483200000L, 1.08 } };
        series3.setData(new ArrayList<>(Arrays.asList(series3_data)));*/

        options.setSeries(new ArrayList<>(Arrays.asList(series1/*, series2, series3*/)));

        chartView.setOptions(options);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBluetoothStuff();
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.S)
    public void doBluetoothStuff() {
        Log.println(Log.DEBUG, TAG, "dobtstuff");
        ArrayList<String> deviceStrs = new ArrayList<>();
        final ArrayList<String> devices = new ArrayList<>();

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
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.requireContext());

        final Context context = this.requireContext();
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                deviceAddress = devices.get(position);
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "should never happen");
                    return;
                }
                BluetoothSocket socket = null;
                InputStream in = null;
                OutputStream out = null;
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

                OutputStream finalOut = out;
                InputStream finalIn = in;
                handler.postDelayed(pollOdbRunnable = () -> {
                    poll(finalIn, finalOut);
                    handler.postDelayed(pollOdbRunnable, 1000);
                }, 1000);
            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }

    private void poll(InputStream in, OutputStream out) {
        try {
            new EchoOffCommand().run(in, out);
            new LineFeedOffCommand().run(in, out);
            new TimeoutCommand(100).run(in, out);
            new SelectProtocolCommand(ObdProtocols.AUTO).run(in, out);
            ModuleVoltageCommand mvc = new ModuleVoltageCommand();
            mvc.run(in, out);
            Log.d("odb", "Voltage" + mvc.getFormattedResult());
            HIData data = new HIData();
            data.setX(System.currentTimeMillis());
            data.setY(mvc.getVoltage());
            ((HIChartView) this.getView().findViewById(R.id.hc)).getOptions().getSeries().get(0).addPoint(data);
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