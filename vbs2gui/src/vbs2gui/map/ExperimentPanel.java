/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExperimentPanel.java
 *
 * Created on Jan 28, 2009, 4:41:30 PM
 */

package vbs2gui.map;

import vbs2gui.server.Vbs2Link;
import java.util.logging.Logger;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author pkv
 */
public class ExperimentPanel extends javax.swing.JPanel {
    private static final Logger logger = Logger.getLogger(ExperimentPanel.class.getName());

    public static final String ambulScript = "\"unit = this; null = this spawn { _exitStr = format [\"\"#ICON|AMBUL|%1|[-1,-1,-1]\"\", _this]; while {alive _this} do { sleep 0.250; pluginFunction [\"\"TcpBridge\"\", format [\"\"#ICON|AMBUL|%1|%2\"\", _this, position _this]]; }; pluginFunction [\"\"TcpBridge\"\", _exitStr]; };\"";
    public static final String hmmwvScript = "\"unit = this; null = this spawn { _exitStr = format [\"\"#ICON|HMMWV|%1|[-1,-1,-1]\"\", _this]; while {alive _this} do { sleep 0.250; pluginFunction [\"\"TcpBridge\"\", format [\"\"#ICON|HMMWV|%1|%2\"\", _this, position _this]]; }; pluginFunction [\"\"TcpBridge\"\", _exitStr]; };\"";
    public static final String tankScript = "\"unit = this; null = this spawn { _exitStr = format [\"\"#ICON|TANK|%1|[-1,-1,-1]\"\", _this]; while {alive _this} do { sleep 0.250; pluginFunction [\"\"TcpBridge\"\", format [\"\"#ICON|TANK|%1|%2\"\", _this, position _this]]; }; pluginFunction [\"\"TcpBridge\"\", _exitStr]; };\"";
    public static final String startLocation = "[3134.14,2662.55,0.00137901]";
    public static final String northLocation = "[2837.98,2913.27,0.00143814]";
    public static final String southLocation = "[2637.83,2459.89,0.00143337]";
    public static final String goalLocation = "[2596.89,2829.87,0.00143909]";

    public static final String origin = "[0.0, 0.0, 0.0]";

    public static final String group = "CONVOY";
    public static final String AMBULANCE = "M113";
    public static final String TANK = "M1Abrams";
    public static final String HMMWV = "HMMWV50";

    private Vbs2Link link = null;

    private int numAmbulances = 0;
    private int numHmmwvs = 0;
    private int numTanks = 0;
    private boolean isGoal = false;

    public void setLink(Vbs2Link l) {
        link = l;
        initScenario();
        new Thread(new Updater()).run();
    }

    public String doCommand(String command) {
        String res = link.evaluate(command);
        logger.info(command + " -> " + res);
        return res;
    }

    public void initScenario() {
        if (link == null) return;

        doCommand("hint \"VBS2GUI connected\"");
        doCommand(group + " = createGroup west");
    }

    public class Updater implements Runnable {
        public void run() {
            SpinnerModel spinModel;

            while (link.isConnected()) {
                spinModel = jSpinnerTank.getModel();
                if (spinModel instanceof SpinnerNumberModel) {
                    SpinnerNumberModel numModel = (SpinnerNumberModel)spinModel;
                    if (numTanks < numModel.getNumber().intValue()) {
                        addTank();
                    } else if (numTanks > numModel.getNumber().intValue()) {
                        deleteTank();
                    }
                }

                spinModel = jSpinnerHmmwv.getModel();
                if (spinModel instanceof SpinnerNumberModel) {
                    SpinnerNumberModel numModel = (SpinnerNumberModel)spinModel;
                    if (numHmmwvs < numModel.getNumber().intValue()) {
                        addHmmwv();
                    } else if (numHmmwvs > numModel.getNumber().intValue()) {
                        deleteHmmwv();
                    }
                }

                spinModel = jSpinnerAmbulance.getModel();
                if (spinModel instanceof SpinnerNumberModel) {
                    SpinnerNumberModel numModel = (SpinnerNumberModel)spinModel;
                    if (numAmbulances < numModel.getNumber().intValue()) {
                        addAmbulance();
                    } else if (numAmbulances > numModel.getNumber().intValue()) {
                        deleteAmbulance();
                    }
                }

                try {Thread.sleep(500);}
                catch (InterruptedException e) {}
            }
        }
    }

    public void addAmbulance() {
        String name = "ambul" + (numAmbulances++);

        // Create vehicle
        doCommand(name + " = \"" + AMBULANCE + "\" createVehicle " + startLocation);
        doCommand(group + " addVehicle " + name);

        // Create crew
        doCommand("\"SoldierGCrew\" createUnit " +
                "[" + origin + ", " + group + ", " + ambulScript + "]");
        doCommand("unit assignAsDriver " + name);
        doCommand("unit moveInDriver " + name);

        // Maybe make this the group leader?
        if (numAmbulances == 1) {
            doCommand(group + " selectLeader unit");
        }
    }

    public void deleteAmbulance() {
        if (numAmbulances <= 0) return;
        String name = "ambul" + (--numAmbulances);
        doCommand("{unassignVehicle _x;" +
                " _x setPos " + origin + ";" +
                " _x setDamage 1.0;" +
                " deleteVehicle _x} forEach crew " + name);
        doCommand("deleteVehicle " + name);
    }

    public void addHmmwv() {
        String name = "hmmwv" + (numHmmwvs++);

        // Create vehicle
        doCommand(name + " = \"" + HMMWV + "\" createVehicle " + startLocation);
        doCommand(group + " addVehicle " + name);

        // Create crew
        doCommand("\"SoldierGCrew\" createUnit " +
                "[" + origin + ", " + group + ", " + hmmwvScript + "]");
        doCommand("unit assignAsDriver " + name);
        doCommand("unit moveInDriver " + name);
    }

    public void deleteHmmwv() {
        if (numHmmwvs <= 0) return;
        String name = "hmmwv" + (--numHmmwvs);
        doCommand("{unassignVehicle _x;" +
                " _x setPos " + origin + ";" +
                " _x setDamage 1.0;" +
                " deleteVehicle _x} forEach crew " + name);
        doCommand("deleteVehicle " + name);
    }

    public void addTank() {
        String name = "tank" + (numTanks++);

        // Create vehicle
        doCommand(name + " = \"" + TANK + "\" createVehicle " + startLocation);
        doCommand(group + " addVehicle " + name);

        // Create crew
        doCommand("\"SoldierGCrew\" createUnit " +
                "[" + origin + ", " + group + ", " + tankScript + "]");
        doCommand("unit assignAsDriver " + name);
        doCommand("unit moveInDriver " + name);
    }

    public void deleteTank() {
        if (numTanks <= 0) return;
        String name = "tank" + (--numTanks);
        doCommand("{unassignVehicle _x;" +
                " _x setPos " + origin + ";" +
                " _x setDamage 1.0;" +
                " deleteVehicle _x} forEach crew " + name);
        doCommand("deleteVehicle " + name);
    }

    public void goGoal(boolean isNorth) {
        doCommand("group2 = createGroup west");
        doCommand("{[_x] joinSilent group2} foreach (units " + group + ")");
        
        if (isNorth)
            doCommand("group2 addWaypoint [" + northLocation + ", 0]");
        else
            doCommand("group2 addWaypoint [" + southLocation + ", 0]");
        doCommand("[group2, 1] setWaypointType \"MOVE\"");
        doCommand("[group2, 1] setWaypointBehaviour \"SAFE\"");
        doCommand("[group2, 1] setWaypointFormation \"LINE\"");
        doCommand("[group2, 1] setWaypointSpeed \"LIMITED\"");


        doCommand("group2 addWaypoint [" + goalLocation + ", 0]");
        doCommand("[group2, 2] setWaypointType \"MOVE\"");
        doCommand("[group2, 2] setWaypointBehaviour \"SAFE\"");
        doCommand("[group2, 2] setWaypointFormation \"LINE\"");
        doCommand("[group2, 2] setWaypointSpeed \"LIMITED\"");
        
        doCommand(group + " = group2");
        doCommand("hint \"Executing mission...\"");
    }

    public void goHome() {
        doCommand("group2 = createGroup west");
        doCommand("{[_x] joinSilent group2} foreach (units " + group + ")");

        doCommand("group2 addWaypoint [" + startLocation + ", 0]");
        doCommand("[group2, 1] setWaypointType \"MOVE\"");
        doCommand("[group2, 1] setWaypointBehaviour \"SAFE\"");
        doCommand("[group2, 1] setWaypointFormation \"LINE\"");
        doCommand("[group2, 1] setWaypointSpeed \"LIMITED\"");

        doCommand(group + " = group2");
        doCommand("hint \"Aborting mission...\"");
    }

    /** Creates new form ExperimentPanel */
    public ExperimentPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupRoute = new javax.swing.ButtonGroup();
        jLabelAllocate = new javax.swing.JLabel();
        jSpinnerTank = new javax.swing.JSpinner();
        jSpinnerHmmwv = new javax.swing.JSpinner();
        jSpinnerAmbulance = new javax.swing.JSpinner();
        jLabelTank = new javax.swing.JLabel();
        jLabelHmmwv = new javax.swing.JLabel();
        jLabelAmbulance = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelCommand = new javax.swing.JLabel();
        jToggleButtonExecute = new javax.swing.JToggleButton();
        jRadioButtonNorth = new javax.swing.JRadioButton();
        jRadioButtonSouth = new javax.swing.JRadioButton();

        jLabelAllocate.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabelAllocate.setText("Allocate Convoy Units");

        jSpinnerTank.setModel(new javax.swing.SpinnerNumberModel(0, 0, 5, 1));

        jSpinnerHmmwv.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10, 1));

        jSpinnerAmbulance.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));

        jLabelTank.setText("Tanks:");

        jLabelHmmwv.setText("HMMWVs:");

        jLabelAmbulance.setText("Ambulances:");

        jLabelCommand.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabelCommand.setText("Command Convoy");

        jToggleButtonExecute.setText("Execute Mission");
        jToggleButtonExecute.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jToggleButtonExecuteStateChanged(evt);
            }
        });

        buttonGroupRoute.add(jRadioButtonNorth);
        jRadioButtonNorth.setSelected(true);
        jRadioButtonNorth.setText("North Route");

        buttonGroupRoute.add(jRadioButtonSouth);
        jRadioButtonSouth.setText("South Route");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonSouth, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jRadioButtonNorth, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelAllocate)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .addComponent(jLabelCommand)
                    .addComponent(jToggleButtonExecute, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelTank)
                            .addComponent(jLabelHmmwv)
                            .addComponent(jLabelAmbulance))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jSpinnerAmbulance)
                            .addComponent(jSpinnerHmmwv)
                            .addComponent(jSpinnerTank, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelAllocate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerTank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTank))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerHmmwv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelHmmwv))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerAmbulance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelAmbulance))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelCommand)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonNorth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonSouth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jToggleButtonExecute)
                .addContainerGap(106, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButtonExecuteStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButtonExecuteStateChanged
        // If clicked, add waypoints to group
        if (jToggleButtonExecute.isSelected() && isGoal == false) {
            jRadioButtonNorth.setEnabled(false);
            jRadioButtonSouth.setEnabled(false);
            goGoal(jRadioButtonNorth.isSelected());
            isGoal = true;
        } else if (!jToggleButtonExecute.isSelected() && isGoal == true) {
            jRadioButtonNorth.setEnabled(true);
            jRadioButtonSouth.setEnabled(true);
            goHome();
            isGoal = false;
        }
    }//GEN-LAST:event_jToggleButtonExecuteStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupRoute;
    private javax.swing.JLabel jLabelAllocate;
    private javax.swing.JLabel jLabelAmbulance;
    private javax.swing.JLabel jLabelCommand;
    private javax.swing.JLabel jLabelHmmwv;
    private javax.swing.JLabel jLabelTank;
    private javax.swing.JRadioButton jRadioButtonNorth;
    private javax.swing.JRadioButton jRadioButtonSouth;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner jSpinnerAmbulance;
    private javax.swing.JSpinner jSpinnerHmmwv;
    private javax.swing.JSpinner jSpinnerTank;
    private javax.swing.JToggleButton jToggleButtonExecute;
    // End of variables declaration//GEN-END:variables

}
