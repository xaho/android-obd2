package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.hichartsclasses.HIPlotBands;
import com.highsoft.highcharts.common.hichartsclasses.HISeries;

public class HiPlotBands extends HighChartsBuilder<HIPlotBands> {
    public HiPlotBands setFrom(Number from) {
        object.setFrom(from);
        return this;
    }

    public HiPlotBands setTo(Number to) {
        object.setTo(to);
        return this;
    }

    public HiPlotBands setColor(HIColor color) {
        object.setColor(color);
        return this;
    }

    public HiPlotBands setColor(String color) {
        return this.setColor(HIColor.initWithHexValue(color));
    }
}
