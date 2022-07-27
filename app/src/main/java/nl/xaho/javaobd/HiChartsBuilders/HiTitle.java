package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HITitle;

public class HiTitle extends HighChartsBuilder<HITitle> {
    public HiTitle setText(String text) {
        object.setText(text);
        return this;
    }
}
