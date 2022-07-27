package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIDateTimeLabelFormats;
import com.highsoft.highcharts.common.hichartsclasses.HIXAxis;

public class HiXAxis extends HighChartsBuilder<HIXAxis> {
    public HiXAxis setTitle(String titleText) {
        object.setTitle(new HiTitle().setText(titleText).build());
        return this;
    }

    public HiXAxis setMin(Number min) {
        object.setMin(min);
        return this;
    }

    public HiXAxis setMax(Number max) {
        object.setMax(max);
        return this;
    }

    public HiXAxis setType(String type) {
        object.setType(type);
        if ("datetime".equals(type)) {
            HIDateTimeLabelFormats labelFormats = new HIDateTimeLabelFormats();
            object.setDateTimeLabelFormats(labelFormats);
        }
        return this;
    }
}
