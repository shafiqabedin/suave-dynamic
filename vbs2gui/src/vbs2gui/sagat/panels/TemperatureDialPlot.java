/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.sagat.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

/**
 *
 * @author sha33
 */
public class TemperatureDialPlot extends org.jfree.chart.plot.dial.DialPlot {

  public TemperatureDialPlot() {
    initComponents();
  }

  protected void initComponents() {
    this.setView(0.0D, 0.0D, 1.0D, 1.0D);
    //dialplot.setDataset(0, data1);
    StandardDialFrame standarddialframe = new StandardDialFrame();
    standarddialframe.setBackgroundPaint(Color.MAGENTA);
    standarddialframe.setForegroundPaint(Color.darkGray);
    this.setDialFrame(standarddialframe);
    GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(0, 0, 0), new Point(), new Color(0, 0, 0));
    DialBackground dialbackground = new DialBackground(gradientpaint);
    dialbackground.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
    this.setBackground(dialbackground);
    DialTextAnnotation dialtextannotation = new DialTextAnnotation("TEMPERATURE");
    dialtextannotation.setFont(new Font("Dialog", 1, 14));
    dialtextannotation.setPaint(Color.WHITE);
    dialtextannotation.setRadius(0.69999999999999996D);
    this.addLayer(dialtextannotation);
    DialValueIndicator dialvalueindicator = new DialValueIndicator(0);
    dialvalueindicator.setFont(new Font("Dialog", 0, 10));
    dialvalueindicator.setOutlinePaint(Color.darkGray);
    dialvalueindicator.setRadius(0.59999999999999998D);
    dialvalueindicator.setAngle(-103D);
    this.addLayer(dialvalueindicator);
    StandardDialScale standarddialscale = new StandardDialScale(100.0D, 250.0D, -120D, -300D, 50.0D, 4);
    standarddialscale.setTickLabelPaint(Color.WHITE);
    standarddialscale.setMajorTickPaint(Color.WHITE);
    standarddialscale.setMinorTickPaint(Color.WHITE);
    standarddialscale.setTickRadius(0.88D);
    standarddialscale.setTickLabelOffset(0.14999999999999999D);
    standarddialscale.setTickLabelFont(new Font("Dialog", 0, 18));
    this.addScale(0, standarddialscale);
    StandardDialScale standarddialscale1 = new StandardDialScale(100.0D, 250.0D, -120D, -300D, 50.0D, 4);
    standarddialscale1.setTickLabelPaint(Color.BLACK);
    standarddialscale1.setTickRadius(0.5D);
    standarddialscale1.setTickLabelOffset(0.14999999999999999D);
    standarddialscale1.setTickLabelFont(new Font("Dialog", 0, 10));
    standarddialscale1.setMajorTickPaint(Color.BLACK);
    standarddialscale1.setMinorTickPaint(Color.BLACK);
    this.addScale(1, standarddialscale1);
    this.mapDatasetToScale(1, 1);
    StandardDialRange standarddialrange1 = new StandardDialRange(100D, 175D, Color.BLUE);
    StandardDialRange standarddialrange2 = new StandardDialRange(176D, 250D, Color.RED);
    standarddialrange1.setScaleIndex(1);
    standarddialrange1.setInnerRadius(0.58999999999999997D);
    standarddialrange1.setOuterRadius(0.58999999999999997D);
    this.addLayer(standarddialrange1);

    standarddialrange2.setScaleIndex(1);
    standarddialrange2.setInnerRadius(0.58999999999999997D);
    standarddialrange2.setOuterRadius(0.58999999999999997D);
    this.addLayer(standarddialrange2);


    org.jfree.chart.plot.dial.DialPointer.Pin pin = new org.jfree.chart.plot.dial.DialPointer.Pin(1);
    pin.setRadius(0.55000000000000004D);
    this.addPointer(pin);
    org.jfree.chart.plot.dial.DialPointer.Pointer pointer = new org.jfree.chart.plot.dial.DialPointer.Pointer(0);
    pointer.setFillPaint(Color.WHITE);
    this.addPointer(pointer);
    DialCap dialcap = new DialCap();
    dialcap.setRadius(0.10000000000000001D);
    this.setCap(dialcap);
  }
}
