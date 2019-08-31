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
import java.util.logging.Logger;
import javax.swing.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

///////////////////////////////////////////////////////// DrawingArea
public class DialPanel extends JPanel {
    Logger logger = Logger.getLogger(DialPanel.class.getName());
    
    private final static int DIAL_PREFERRED_WIDTH = 130;
    private final static int DIAL_PREFERRED_HEIGHT = 130;
    int iterations = 60;
    FuelDialPlot dialplot_a;
    JFreeChart chart_a;
    DefaultValueDataset datainit_fuel;
    ChartPanel cp_a;
    TemperatureDialPlot dialplot_b;
    JFreeChart chart_b;
    DefaultValueDataset datainit_temperature;
    ChartPanel cp_b;
    BatteryDialPlot dialplot_c;
    JFreeChart chart_c;
    DefaultValueDataset datainit_battery;
    ChartPanel cp_c;
    GradientPaint gradientpaintThreat;
    DialBackground fuelbackgroundThreat;
    int fuel;
    double[] battery = new double[iterations];
    double[] temperature = new double[iterations];
    double[] DIAL_VALUES = new double[3];
    private String uavName;

    public JPanel makeUavDialSubpanel(String title, Color color) {
        JPanel uav1 = new JPanel(new BorderLayout());
        uav1.setBackground(color);
//        uav1.setBorder(new BevelBorder(BevelBorder.LOWERED));
        JLabel uav1Label = new JLabel(title);
        uav1Label.setBackground(color);
        uav1Label.setForeground(Color.WHITE);
        uav1Label.setHorizontalAlignment(uav1Label.CENTER);
        JPanel uav1Dials = new DialPanel();
//        uav1Dials.setBackground(color);
        uav1.add(uav1Label, BorderLayout.NORTH);
        uav1.add(uav1Dials, BorderLayout.SOUTH);
        return uav1;
    }

    public DialPanel() {

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
        cp_a.setPreferredSize(new Dimension(DIAL_PREFERRED_WIDTH, DIAL_PREFERRED_HEIGHT));

        // Get data for diagrams for 'b'
        dialplot_b = new TemperatureDialPlot();
        datainit_temperature = new DefaultValueDataset(0.0);
        dialplot_b.setDataset(0, datainit_temperature);
        chart_b = new JFreeChart(dialplot_b);
        cp_b = new ChartPanel(chart_b);
        cp_b.setPreferredSize(new Dimension(DIAL_PREFERRED_WIDTH, DIAL_PREFERRED_HEIGHT));

        // Get data for diagrams for 'c'
        dialplot_c = new BatteryDialPlot();
        datainit_battery = new DefaultValueDataset(40.0);
        dialplot_c.setDataset(0, datainit_battery);
        chart_c = new JFreeChart(dialplot_c);
        cp_c = new ChartPanel(chart_c);
        cp_c.setPreferredSize(new Dimension(DIAL_PREFERRED_WIDTH, DIAL_PREFERRED_HEIGHT));

        add(cp_a);
        add(cp_b);
        add(cp_c);

        batteryRandom();
        temperatureRandom();
    }

    public void setAndRecordBackground(Color color) {
        setBackground(color);
        //System.out.println("BG SET TO " + color.getRed() + " " + color.getGreen() + " " + color.getBlue());

        new Thread() {

            @Override
            public void run() {
                for (int i = 0; i < iterations; i++) {
                    logger.info("Dial Panel Value of " + uavName + " are: Fuel [" + fuel + "] | Battery [" + battery[i] + "] Temperature [" + temperature[i] + "]");

                    DIAL_VALUES[0] = fuel;
                    DIAL_VALUES[1] = battery[i];
                    DIAL_VALUES[2] = temperature[i];
                    datainit_fuel.setValue(fuel);
                    dialplot_a.setDataset(datainit_fuel);

                    datainit_temperature.setValue(temperature[i]);
                    dialplot_b.setDataset(datainit_temperature);

                    datainit_battery.setValue(battery[i]);
                    dialplot_c.setDataset(datainit_battery);

                    if (fuel < 20) {
                        dialplot_a.setBackground(fuelbackgroundThreat);

                    }
                    if (fuel >= 0) {
                        fuel -= 1;
                    }
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }.start();

        validate();
    }

    public String getOwner() {
        return uavName;
    }

    public void setOwner(String name) {
        this.uavName = name;
    }

    public void batteryRandom() {

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

    public void temperatureRandom() {
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

    public double[] getDialPanelValues() {
        return DIAL_VALUES;
    }
}
