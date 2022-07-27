package nl.xaho.javaobd.HiChartsBuilders;

import com.highsoft.highcharts.common.hichartsclasses.HIPlotOptions;
import com.highsoft.highcharts.common.hichartsclasses.HISpline;

public class HiPlotOptions extends HighChartsBuilder<HIPlotOptions> {
    public HiPlotOptions setSpline(HISpline spline) {
        object.setSpline(spline);
        return this;
    }
//    HIPlotOptions plotOptions = new HIPlotOptions();
//        plotOptions.setSpline(new HISpline());
//        plotOptions.getSpline().setMarker(new HIMarker());
//        plotOptions.getSpline().getMarker().setEnabled(true);

}
