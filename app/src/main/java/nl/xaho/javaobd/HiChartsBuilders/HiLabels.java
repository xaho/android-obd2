package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HILabels;

public class HiLabels extends HighChartsBuilder<HILabels> {

    public HiLabels setStep(Number step) {
        object.setStep(step);
        return this;
    }
}
