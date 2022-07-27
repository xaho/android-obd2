package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HITooltip;

public class HiTooltip extends HighChartsBuilder<HITooltip> {
    public HiTooltip setHeaderFormat(String format) {
        object.setHeaderFormat(format);
        return this;
    }

    public HiTooltip setPointFormat(String format) {
        object.setPointFormat(format);
        return this;
    }
}
