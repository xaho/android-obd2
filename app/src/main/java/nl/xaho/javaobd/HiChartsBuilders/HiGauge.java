package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIGauge;
import com.highsoft.highcharts.common.hichartsclasses.HIPoint;
import com.highsoft.highcharts.common.hichartsclasses.HITooltip;

import java.util.ArrayList;

public class HiGauge extends HighChartsBuilder<HIGauge> {
    public HiGauge setName(String name) {
        object.setName(name);
        return this;
    }

    public HiGauge setTooltip(HITooltip tooltip) {
        object.setTooltip(tooltip);
        return this;
    }

    public HiGauge setData(ArrayList<Number> data) {
        object.setData(data);
        return this;
    }

    public HiGauge setPoint(HIPoint point) {
        object.setPoint(point);
        return this;
    }
}
