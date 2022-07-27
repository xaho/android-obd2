package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIMarker;
import com.highsoft.highcharts.common.hichartsclasses.HISpline;

public class HiSpline extends HighChartsBuilder<HISpline> {
    public HiSpline setName(String name) {
        object.setName(name);
        return this;
    }

    public HiSpline setYAxis(int i) {
        object.setYAxis(i);
        return this;
    }

    public HiSpline setMarker(HIMarker marker) {
        object.setMarker(marker);
        return this;
    }
}
