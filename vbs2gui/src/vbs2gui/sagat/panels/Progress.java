/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.sagat.panels;

/**
 *
 * @author sha33
 */
import java.awt.*;
import javax.swing.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

public class Progress extends JFrame {

  static int iterations = 60;
  static FuelDialPlot dialplot_a;
  static JFreeChart chart_a;
  static DefaultValueDataset datainit_fuel;
  static ChartPanel cp_a;
  static TemperatureDialPlot dialplot_b;
  static JFreeChart chart_b;
  static DefaultValueDataset datainit_temperature;
  static ChartPanel cp_b;
  static BatteryDialPlot dialplot_c;
  static JFreeChart chart_c;
  static DefaultValueDataset datainit_battery;
  static ChartPanel cp_c;
  static GradientPaint gradientpaintThreat;
  static DialBackground fuelbackgroundThreat;
  static int fuel;
  static double[] battery = new double[iterations];
  static double[] temperature = new double[iterations];

  public Progress() {
    super("Progress");
  }

  public static void iterate() {
    for (int i = 0; i < iterations; i++) {
      datainit_fuel.setValue(fuel);
      dialplot_a.setDataset(datainit_fuel);

      datainit_temperature.setValue(temperature[i]);
      dialplot_b.setDataset(datainit_temperature);

      datainit_battery.setValue(battery[i]);
      dialplot_c.setDataset(datainit_battery);

      if (fuel < 20) {
        dialplot_a.setBackground(fuelbackgroundThreat);

      }
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      if (fuel >= 0) {
        fuel -= 1;
      }

    }
  }


  public static void batteryRandom() {
    double tmpNum = 0;
    double rand_1 = 0 + (int) (Math.random() * ((100 - 0) + 1));
    double rand_2 = 0 + (int) (Math.random() * ((100 - 0) + 1));
    double rand_3 = 0 + (int) (Math.random() * ((100 - 0) + 1));

    double diff_1 = (rand_1 - 0) / 20;
    double diff_2 = (rand_2 - rand_1) / 20;
    double diff_3 = (rand_3 - rand_2) / 20;

    for (int i = 0; i < battery.length; i++) {
      if (i <= 20) {
        tmpNum = tmpNum + diff_1;
        battery[i] = tmpNum;
      } else if (i > 20 && i <= 40) {
        tmpNum = tmpNum + diff_2;
        battery[i] = tmpNum;
      } else if (i > 40 && i <= 60) {
        tmpNum = tmpNum + diff_3;
        battery[i] = tmpNum;
      }
    }
  }

    public static void temperatureRandom() {
    double tmpNum = 100;
    double rand_1 = 100 + (int) (Math.random() * ((250 - 100) + 1));
    double rand_2 = 100 + (int) (Math.random() * ((250 - 100) + 1));
    double rand_3 = 100 + (int) (Math.random() * ((250 - 100) + 1));
    //System.out.println("temp: "+rand_1+","+rand_2+","+rand_3+",");

    double diff_1 = (rand_1 - 100) / 20;
    double diff_2 = (rand_2 - rand_1) / 20;
    double diff_3 = (rand_3 - rand_2) / 20;

    for (int i = 0; i < temperature.length; i++) {
      if (i <= 20) {
        tmpNum = tmpNum + diff_1;
        temperature[i] = tmpNum;
      } else if (i > 20 && i <= 40) {
        tmpNum = tmpNum + diff_2;
        temperature[i] = tmpNum;
      } else if (i > 40 && i <= 60) {
        tmpNum = tmpNum + diff_3;
        temperature[i] = tmpNum;
      }
    }
  }

  public static void main(String[] arguments) {

    JPanel controlPanel = new JPanel(new FlowLayout());
    // Initialize The thrat backgroung color.
    gradientpaintThreat = new GradientPaint(new Point(), Color.BLACK, new Point(), Color.RED);
    fuelbackgroundThreat = new DialBackground(gradientpaintThreat);
    fuelbackgroundThreat.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));

    fuel = 20 + (int) (Math.random() * ((240 - 20) + 1));
    // Get data for diagrams for 'a'
    dialplot_a = new FuelDialPlot();
    datainit_fuel = new DefaultValueDataset(fuel);
    dialplot_a.setDataset(0, datainit_fuel);
    chart_a = new JFreeChart(dialplot_a);
    cp_a = new ChartPanel(chart_a);
    cp_a.setPreferredSize(new Dimension(200, 200));

    // Get data for diagrams for 'b'
    dialplot_b = new TemperatureDialPlot();
    datainit_temperature = new DefaultValueDataset(0.0);
    dialplot_b.setDataset(0, datainit_temperature);
    chart_b = new JFreeChart(dialplot_b);
    cp_b = new ChartPanel(chart_b);
    cp_b.setPreferredSize(new Dimension(200, 200));

    // Get data for diagrams for 'c'
    dialplot_c = new BatteryDialPlot();
    datainit_battery = new DefaultValueDataset(40.0);
    dialplot_c.setDataset(0, datainit_battery);
    chart_c = new JFreeChart(dialplot_c);
    cp_c = new ChartPanel(chart_c);
    cp_c.setPreferredSize(new Dimension(200, 200));



    JFrame frame = new JFrame();
    //frame.setLayout(new BorderLayout());
//    frame.add(cp_a, BorderLayout.EAST);
//    frame.add(cp_b, BorderLayout.CENTER);
//    frame.add(cp_c, BorderLayout.WEST);

    controlPanel.add(cp_a);
    controlPanel.add(cp_b);
    controlPanel.add(cp_c);

    frame.add(controlPanel);

    //frame.add(start);
    frame.pack();
    frame.setVisible(true);
    //popUp();
    batteryRandom();
    temperatureRandom();
    iterate();


  }
}
