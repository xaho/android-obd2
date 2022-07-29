package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIBackground;
import com.highsoft.highcharts.common.hichartsclasses.HIPane;

import java.util.ArrayList;

public class HiPane extends HighChartsBuilder<HIPane> {
    public HiPane setStartAngle(Number angle) {
        object.setStartAngle(angle);
        return this;
    }

    public HiPane setEndAngle(Number angle) {
        object.setEndAngle(angle);
        return this;
    }

    public HiPane setBackground(ArrayList<HIBackground> backgrounds) {
        object.setBackground(backgrounds);
        return this;
    }
}
