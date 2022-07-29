package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIOptions;
import com.highsoft.highcharts.common.hichartsclasses.HIPlotOptions;
import com.highsoft.highcharts.common.hichartsclasses.HISeries;
import com.highsoft.highcharts.common.hichartsclasses.HITitle;
import com.highsoft.highcharts.common.hichartsclasses.HITooltip;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis;

import java.util.ArrayList;

public class HiOptions extends HighChartsBuilder<HIOptions> {
    public HiOptions setSeries(ArrayList<HISeries> series) {
        object.setSeries(series);
        return this;
    }

    public HiOptions setPlotOptions(HIPlotOptions options) {
        object.setPlotOptions(options);
        return this;
    }

    public HiOptions setTooltip(HITooltip tooltip) {
        object.setTooltip(tooltip);
        return this;
    }

    public HiOptions setTitle(HITitle title) {
        object.setTitle(title);
        return this;
    }

    public HiOptions setXAxis(ArrayList<HIXAxis> axes) {
        object.setXAxis(axes);
        return this;
    }

    public HiOptions setYAxis(ArrayList<HIYAxis> axes) {
        object.setYAxis(axes);
        return this;
    }
}
