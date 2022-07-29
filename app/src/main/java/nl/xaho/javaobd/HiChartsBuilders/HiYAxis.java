package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.hichartsclasses.HIDateTimeLabelFormats;
import com.highsoft.highcharts.common.hichartsclasses.HILabels;
import com.highsoft.highcharts.common.hichartsclasses.HIPlotBands;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis;

import java.util.ArrayList;

public class HiYAxis extends HighChartsBuilder<HIYAxis> {
    public HiYAxis setTitle(String titleText) {
        object.setTitle(new HiTitle().setText(titleText).build());
        return this;
    }

    public HiYAxis setMin(Number min) {
        object.setMin(min);
        return this;
    }

    public HiYAxis setMax(Number max) {
        object.setMax(max);
        return this;
    }

    public HiYAxis setOpposite(Boolean opposite) {
        object.setOpposite(opposite);
        return this;
    }

    public HiYAxis setType(String type) {
        object.setType(type);
        if ("datetime".equals(type)) {
            HIDateTimeLabelFormats labelFormats = new HIDateTimeLabelFormats();
            object.setDateTimeLabelFormats(labelFormats);
        }
        return this;
    }

    public HiYAxis setMinorTickWidth(Number width) {
        object.setMinorTickWidth(width);
        return this;
    }

    public HiYAxis setMinorTickLength(Number length) {
        object.setMinorTickLength(length);
        return this;
    }

    public HiYAxis setMinorTickPosition(String position) {
        object.setMinorTickPosition(position);
        return this;
    }

    public HiYAxis setMinorTickColor(HIColor color) {
        object.setMinorTickColor(color);
        return this;
    }

    public HiYAxis setTickPixelInterval(Number interval) {
        object.setTickPixelInterval(interval);
        return this;
    }

    public HiYAxis setTickWidth(Number width) {
        object.setTickWidth(width);
        return this;
    }

    public HiYAxis setTickPosition(String position) {
        object.setTickPosition(position);
        return this;
    }

    public HiYAxis setTickLength(Number length) {
        object.setTickLength(length);
        return this;
    }

    public HiYAxis setTickColor(HIColor color) {
        object.setTickColor(color);
        return this;
    }

    public HiYAxis setLabels(HILabels labels) {
        object.setLabels(labels);
        return this;
    }

    public HiYAxis setPlotBands(ArrayList<HIPlotBands> plotBands) {
        object.setPlotBands(plotBands);
        return this;
    }
}
