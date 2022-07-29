package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIData;

public class HiData extends HighChartsBuilder<HIData> {
    public HiData setX(Number number) {
        object.setX(number);
        return this;
    }

    public HiData setY(Number number) {
        object.setY(number);
        return this;
    }
}
