package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIPoint;

public class HiPoint extends HighChartsBuilder<HIPoint> {

    public HiPoint setY(Number y) {
        object.setY(y);
        return this;
    }
}
