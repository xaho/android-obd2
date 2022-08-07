package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HISeries;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;
import com.highsoft.highcharts.core.HIChartView;

import java.util.ArrayList;

public class HiUtils {
    public static HIXAxis createDatetimeAxis() {
        return new HiXAxis()
                .setTitle("Date")
                .setType("datetime")
                .build();
    }

    public static HISeries getSeriesWithName(HIChartView chartview, String name) {
        ArrayList<HISeries> series = chartview.getOptions().getSeries();
        for (HISeries s : series) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }
}
