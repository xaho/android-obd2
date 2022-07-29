package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.HIColor;
import com.highsoft.highcharts.common.hichartsclasses.HIBackground;

public class HiBackground extends HighChartsBuilder<HIBackground> {
    public HiBackground setBackgroundColor(HIColor color) {
        object.setBackgroundColor(color);
        return this;
    }

    public HiBackground setBackgroundColor(String color) {
        return this.setBackgroundColor(HIColor.initWithHexValue(color));
    }

    public HiBackground setBorderWidth(Number width) {
        object.setBorderWidth(width);
        return this;
    }

    public HiBackground setOuterRadius(String radius) {
        object.setOuterRadius(radius);
        return this;
    }

    public HiBackground setInnerRadius(String radius) {
        object.setInnerRadius(radius);
        return this;
    }
}
