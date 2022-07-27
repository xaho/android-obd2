package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIMarker;

public class HiMarker extends HighChartsBuilder<HIMarker> {
    public HiMarker setEnabled(Boolean enabled) {
        object.setEnabled(enabled);
        return this;
    }
}
