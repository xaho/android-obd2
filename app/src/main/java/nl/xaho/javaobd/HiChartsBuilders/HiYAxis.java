package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIDateTimeLabelFormats;
import com.highsoft.highcharts.common.hichartsclasses.HIYAxis;

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
}
