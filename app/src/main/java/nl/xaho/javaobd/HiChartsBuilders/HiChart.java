package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIChart;

public class HiChart extends HighChartsBuilder<HIChart> {

    public HiChart setType(String type) {
        object.setType(type);
        return this;
    }

    public HiChart setPlotBorderWidth(Number width) {
        object.setPlotBorderWidth(width);
        return this;
    }

    public HiChart setZoomType(String zoomType) {
        object.setZoomType(zoomType);
        return this;
    }
}
