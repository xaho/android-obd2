package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIOptions;
import com.highsoft.highcharts.common.hichartsclasses.HISeries;

import java.util.ArrayList;

public class HiOptions extends HighChartsBuilder<HIOptions> {
    public HiOptions setSeries(ArrayList<HISeries> series) {
        object.setSeries(series);
        return this;
    }
}
