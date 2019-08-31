/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui;

import vbs2gui.SimpleUAVSim.UAV;
import vbs2gui.server.Vbs2Link;

/**
 *
 * @author nbb
 */
public class Vbs2Scripts {
    String response;

    public enum WPType {
        MOVE, LOITER
    };

    public static String[] addNewWPCmds(UAV[] uavs, int id, boolean fuel, double x, double y, double r, WPType type) {
        String[] cmds;
        if (uavs[id].getType() == UAV.Type.UAV || uavs[id].getType() == UAV.Type.UGV) {
            if(type == WPType.MOVE) {
                cmds = new String[]{
                            uavs[id].getGroupName() + " addWaypoint [[" + x + "," + y + ", 0], " + r + "]; "
                            + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointType \"MOVE\"; "
                            + uavs[id].getUavName() + " setFuel " + ((fuel) ? 1 : 0) + "; "};
            } else  if(type == WPType.LOITER) {
                cmds = new String[]{
                            uavs[id].getGroupName() + " addWaypoint [[" + x + "," + y + ", 0], " + r + "]; "
                            + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointType \"LOITER\"; "
                            + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointLoiterRadius " + r + "; "
                            + uavs[id].getUavName() + " setFuel " + ((fuel) ? 1 : 0) + "; "};
            } else {
                cmds = new String[]{"; "};
            }
        } else if (uavs[id].getType() == UAV.Type.ANIMAL) {
            cmds = new String[]{
                        uavs[id].getGroupName() + " addWaypoint [[" + x + "," + y + ", 0], 5]; "
                        + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointType \"MOVE\"; "
                        + uavs[id].getGroupName() + " setCurrentWaypoint [" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1]; "};
        } else {
            cmds = new String[]{"; "};
        }
        return cmds;
    }

    public static String[] createUAVCmds(UAV[] uavs, int id, String type, double x, double y, double z, double head) {
        // z is flying altitude for UAVs and max speed for UGVs
        // Heading is not used because VBS2 has no reliable method that I know
        //  of for changing the heading of a moving vehicle
        // Create UAVs individually because the commands are long enough we
        //  could plausibly exceed VBS2's maximum string length
        String[] cmds;
        if (uavs[id].getType() == UAV.Type.UAV) {
            cmds = new String[]{
                        uavs[id].getUavName() + " = createVehicle [\"" + type + "\", [" + x + ", " + y + ", " + z + "], [], 0, \"FLY\"]; "
                        + uavs[id].getUavName() + " flyInHeight " + z + "; "
//                        + uavs[id].getUavName() + " setpos [" + x + ", " + y + ", " + z + "]; "
            };
        } else if (uavs[id].getType() == UAV.Type.UGV) {
            cmds = new String[]{
                        "[] spawn { "
                        + uavs[id].getUavName() + "_vehicle" + " = createVehicle [\"" + type + "\", [" + x + ", " + y + ", " + z + "], [], 0, \"\"]; "
                        + "\"vbs2_iq_civ_dockworker_04\" createUnit [[" + x + ", " + y + ", 0], " + uavs[id].getGroupName() + ", \"" + uavs[id].getUavName() + " = this\"]; "
                        + uavs[id].getUavName() + " disableAI \"PATHPLAN\"; "
                        + "sleep 1; "
                        + uavs[id].getUavName() + " moveInDriver " + uavs[id].getUavName() + "_vehicle; "
                        + uavs[id].getUavName() + "_vehicle disableGeo [false, true, true, true]; "
                        + uavs[id].getUavName() + "_vehicle forceSpeed " + z + "; "
                        + "}; "
            };
        } else if (uavs[id].getType() == UAV.Type.ANIMAL) {
            cmds = new String[]{
                        "\"" + type + "\" createUnit [[" + x + ", " + y + ", 0], " + uavs[id].getGroupName() + ", \"" + uavs[id].getUavName() + " = this\"]; "
                        + uavs[id].getUavName() + " disableAI \"PATHPLAN\"; "
            };
        } else {
            cmds = new String[]{"; "};
        }
        return cmds;
    }

    public static String[] createGroupCmds(UAV[] uavs, int numAnimalsPerHerd) {
        // Create group, assign UAV to group later after a time delay.
        //  Afterwards we will add a new waypoint, otherwise, the UAV won't
        //  listen to our waypoint edit commands.
        String[] cmds = new String[]{"; "};
        for(int i = 0; i < uavs.length;) {
            if(uavs[i].getType() == UAV.Type.UGV) {
                cmds[0] += uavs[i].getGroupName() + " = createGroup civilian; ";
                i++;
            } else if(uavs[i].getType() == UAV.Type.ANIMAL) {
                cmds[0] += uavs[i].getGroupName() + " = createGroup civilian; ";
                i+= numAnimalsPerHerd;
            } else {
                i++;
            }
        }
        return cmds;
    }

    public static String[] createJoinCmds(UAV[] uavs, int numUAVs) {
        // Assign UAV to group. Later we will add a new waypoint, otherwise, the
        //  UAV won't listen to our waypoint edit commands.
        // Animals are created as units and join their group upon creation
        String[] cmds = new String[]{"; "};
        for (int i = 0; i < numUAVs; i++) {
            cmds[0] += "group" + i + " = (group " + uavs[i].getUavName() + "); ";
        }
        return cmds;
    }

    public static String[] createCamCmds(UAV[] uavs, int id, double zOffset, double fov) {
        // Camera object method
        String[] cmds = {
            "cam" + id + " = \"camera\" camCreate [0,0,0]; " +
            "cam" + id + " attachTo [" + uavs[id].getUavName() + ", [0, 0, " + zOffset + "]]; " +
//            "cam" + id + " attachTo [" + uavs[id].getUavName() + ", [0, 0, -0.4]]; " +
//            "cam" + id + " camSetAttachedLookDir [0, cos(30), -sin(30)]; " +
            "cam" + id + " camSetAttachedLookDir [0, cos(89), -sin(89)]; " +
            "cam" + id + " camSetFOV " + fov + "; " +
            "cam" + id + " camCommit 0; " };
        return cmds;
    }

    public static String[] createGlobalRefCmds(UAV[] uavs, int numUAVs) {
        // Create global references to UAVs on the server (so VBS2
        //  clients connected to the server can access the UAVs)
        // createUnit calls create global references
        // createVehicle calls do not, but the names can be passed via
        //  publicExec
        // camCreate calls are strictly local and must be performed on the
        //  machine that will use the camera
        String[] cmds = {"; "};
        for (int i = 0; i < numUAVs; i++) {
            cmds[0] = cmds[0] + "publicExec[\"true\", \"" + uavs[i].getUavName() + " = _this\", " + uavs[i].getUavName() + "]; ";
        }
        return cmds;
    }

    public static String[] createLoopCmds(UAV[] uavs, int numUAVs, int offset, boolean useGPS, boolean isMultiview) {
        String[] actualCamNames = new String[numUAVs];
        String[] actualUAVNames = new String[numUAVs];
        if(!isMultiview) {
            for(int i = 0; i < numUAVs; i++) {
                actualCamNames[i] = "cam" + (i + offset);
                actualUAVNames[i] = uavs[i + offset].getUavName();
            }
            String boxes = "";
            for(int i = 1; i <= 55; i++) {
                boxes += "box" + i + " = disp1 displayCtrl " + i + "; ";
            }
            // Commands are too long to string together into a single command
            String[] cmds;
            if(useGPS) {
                    cmds = new String[] {
                "hideUI true; " +
                    "diag = createdialog \"BoxPanel\"; " +
                    "disp1 = finddisplay 1; ",
                    boxes,
                    "dec10ToRgb = compile preprocessFile \"dec10ToRgb.sqf\"; " +
                    "dec15ToRgb = compile preprocessFile \"dec15ToRgb.sqf\"; " +
                    "gpsToRgb = compile preprocessFile \"gpsToRgb.sqf\"; " +
                    "updateFleetGps = compile preprocessfile \"updateFleetGps.sqf\"; " +
                    "updateUavGps = compile preprocessfile \"updateUavGps.sqf\"; " +
//                    "updateUAVText = compile preprocessfile \"updateUavText.sqf\"; " +
                    "sc = [[" + stringArrayToString(actualUAVNames, ", ") + "], [" + stringArrayToString(actualCamNames, ", ") + "], " + offset + "] spawn updateFleetGps; "
                };
            } else {
                    cmds = new String[] {
                    "hideUI true; " +
                    "diag = createdialog \"BoxPanel\"; " +
                    "disp1 = finddisplay 1; ",
                    boxes,
                    "dec10ToRgb = compile preprocessFile \"dec10ToRgb.sqf\"; " +
                    "dec15ToRgb = compile preprocessFile \"dec15ToRgb.sqf\"; " +
//                    "gpsToRgb = compile preprocessFile \"gpsToRgb.sqf\"; " +
                    "updateFleetLocal = compile preprocessfile \"updateFleetLocal.sqf\"; " +
                    "updateUavLocal = compile preprocessfile \"updateUavLocal.sqf\"; " +
//                    "updateUAVText = compile preprocessfile \"updateUavText.sqf\"; " +
                    "sc = [[" + stringArrayToString(actualUAVNames, ", ") + "], [" + stringArrayToString(actualCamNames, ", ") + "], " + offset + "] spawn updateFleetLocal; "
                };
            }
            return cmds;
        } else {
            for (int i = 0; i < numUAVs; i++) {
                actualCamNames[i] = "cam" + (i + offset);
            }
            String[] cmds = new String[]{
                "hideUI true; "
                + "hud = compile preprocessfile \"hud.sqf\"; "
                + "if (isNil \"tst_hud\") then { sc = [[" + stringArrayToString(actualCamNames, ",") + "]] spawn hud; } else { if (isNull tst_hud) then { main = [[" + stringArrayToString(actualCamNames, ",") + "]] spawn hud; } else { }; }; "};
            return cmds;
        }
    }

    public static String[] createTrackingCmds(UAV[] uavs, int numUAVs, int numUGVs, int numAnimals, double updateRate) {
        String[] cmds = new String[numUAVs + numUGVs + numAnimals];
        for (int i = 0; i < numUAVs; i++) {
            cmds[i] = "trackUAV" + uavs[i].getUavID() + " = [] spawn { _exitStr = format [\"#ICON|PLANE" + uavs[i].getUavID() + "|" + uavs[i].getUavID() + "|[-1,-1,-1]|-1\"]; while {alive " + uavs[i].getUavName() + "} do { sleep " + updateRate + "; pluginFunction [\"TcpBridge\", format [\"#ICON|PLANE" + uavs[i].getUavID() + "|" + uavs[i].getUavID() + "|%1|%2\", position " + uavs[i].getUavName() + ", getDir " + uavs[i].getUavName() + "]]; }; pluginFunction [\"TcpBridge\", _exitStr]; }; ";
            cmds[i] += "gpsTrackUAV" + uavs[i].getUavID() + " = [] spawn { _exitStr = format [\"#GPS|PLANE" + uavs[i].getUavID() + "|" + uavs[i].getUavID() + "|[-1,-1,-1]|-1\"]; while {alive " + uavs[i].getUavName() + "} do { sleep " + updateRate + "; pluginFunction [\"TcpBridge\", format [\"#GPS|PLANE" + uavs[i].getUavID() + "|" + uavs[i].getUavID() + "|%1|%2\", [postoCoord [position " + uavs[i].getUavName() + ", 'll', 10] select 0, postoCoord [position " + uavs[i].getUavName() + ", 'll', 10] select 1, position " + uavs[i].getUavName() + " select 2], getDir " + uavs[i].getUavName() + "]]; }; pluginFunction [\"TcpBridge\", _exitStr]; }; ";
        }
        for (int i = numUAVs; i < numUAVs + numUGVs; i++) {
            cmds[i] = "trackUAV" + uavs[i].getUavID() + " = [] spawn { _exitStr = format [\"#ICON|UGV|" + uavs[i].getUavID() + "_vehicle|[-1,-1,-1]|-1\"]; while {alive " + uavs[i].getUavName() + "} do { sleep " + updateRate + "; pluginFunction [\"TcpBridge\", format [\"#ICON|UGV|" + uavs[i].getUavID() + "_vehicle|%1|%2\", position " + uavs[i].getUavName() + ", getDir " + uavs[i].getUavName() + "]]; }; pluginFunction [\"TcpBridge\", _exitStr]; }; ";
            cmds[i] += "gpsTrackUAV" + uavs[i].getUavID() + " = [] spawn { _exitStr = format [\"#GPS|UGV|" + uavs[i].getUavID() + "_vehicle|[-1,-1,-1]|-1\"]; while {alive " + uavs[i].getUavName() + "} do { sleep " + updateRate + "; pluginFunction [\"TcpBridge\", format [\"#GPS|UGV|" + uavs[i].getUavID() + "_vehicle|%1|%2\", [postoCoord [position " + uavs[i].getUavName() + ", 'll', 10] select 0, postoCoord [position " + uavs[i].getUavName() + ", 'll', 10] select 1, position " + uavs[i].getUavName() + " select 2], getDir " + uavs[i].getUavName() + "]]; }; pluginFunction [\"TcpBridge\", _exitStr]; }; ";
        }
        for (int i = numUAVs + numUGVs; i < numUAVs + numUGVs + numAnimals; i++) {
            cmds[i] = "trackUAV" + uavs[i].getUavID() + " = [] spawn { _exitStr = format [\"#ICON|GOAT|" + uavs[i].getUavID() + "|[-1,-1,-1]|-1\"]; while {alive " + uavs[i].getUavName() + "} do { sleep " + updateRate + "; pluginFunction [\"TcpBridge\", format [\"#ICON|GOAT|" + uavs[i].getUavID() + "|%1|%2\", position " + uavs[i].getUavName() + ", getDir " + uavs[i].getUavName() + "]]; }; pluginFunction [\"TcpBridge\", _exitStr]; }; ";
            cmds[i] += "gpsTrackUAV" + uavs[i].getUavID() + " = [] spawn { _exitStr = format [\"#GPS|GOAT|" + uavs[i].getUavID() + "|[-1,-1,-1]|-1\"]; while {alive " + uavs[i].getUavName() + "} do { sleep " + updateRate + "; pluginFunction [\"TcpBridge\", format [\"#GPS|GOAT|" + uavs[i].getUavID() + "|%1|%2\", [postoCoord [position " + uavs[i].getUavName() + ", 'll', 10] select 0, postoCoord [position " + uavs[i].getUavName() + ", 'll', 10] select 1, position " + uavs[i].getUavName() + " select 2], getDir " + uavs[i].getUavName() + "]]; }; pluginFunction [\"TcpBridge\", _exitStr]; }; ";
        }
        return cmds;
    }

    public static void createVisibility(Vbs2Link link) {
        String[] cmds = new String[1];
        // Set time to high noon and weather conditions to high visibility
        double time = Double.parseDouble(link.evaluate("daytime;"));
        cmds[0] = "skipTime " + (12 - time + 24)%24 + "; " +
                "0 setFog 0; " +
                "0 setOvercast 0.1; " +
                "showCinemaBorder false; ";
        for(String cmd : cmds) {
            link.evaluate(cmd);
        }
    }

    public static void deleteScripts(Vbs2Link link) {
        int numOldAnimals, numOldUAVs, numOldUGVs;
        try {
            numOldAnimals = Integer.parseInt(link.evaluate("numAnimals; "));
        } catch (NumberFormatException nfe) {
            numOldAnimals = 0;
        }
        try {
            numOldUAVs = Integer.parseInt(link.evaluate("numUAVs; "));
        } catch (NumberFormatException nfe) {
            numOldUAVs = 0;
        }
        try {
            numOldUGVs = Integer.parseInt(link.evaluate("numUGVs; "));
        } catch (NumberFormatException nfe) {
            numOldUGVs = 0;
        }
        String[] cmds = new String[]{""};
        // Terminate map's asset tracking scripts
        for (int i = 0; i < numOldUAVs + numOldUGVs + numOldAnimals; i++) {
            cmds[0] += "terminate trackUAV" + i + "; terminate gpsTrackUAV" + i + "; ";
        }
        cmds[0] += "pauseSimulation false; ";
        for(String cmd : cmds) {
            link.evaluate(cmd);
        }
    }

    public static void deleteLoops(Vbs2Link link) {
        // Terminate UAV MUXing script and close telemetry pixel boxes
        String cmd = "terminate sc; closeDialog 1; ";
        link.evaluate(cmd);
    }

    public static void deleteAssets(Vbs2Link link) {
        int numOldAnimals, numOldHerds, numOldUAVs, numOldUGVs;
        // link.evaluate("") causes indefinite waiting for response
        String[] cmds = new String[]{"; ", "; ", "; ", "; "};
        try {
            numOldAnimals = Integer.parseInt(link.evaluate("numAnimals; "));
        } catch (NumberFormatException nfe) {
            numOldAnimals = 0;
        }
        try {
            numOldHerds = Integer.parseInt(link.evaluate("numHerds; "));
        } catch (NumberFormatException nfe) {
            numOldHerds = 0;
        }
        try {
            numOldUAVs = Integer.parseInt(link.evaluate("numUAVs; "));
        } catch (NumberFormatException nfe) {
            numOldUAVs = 0;
        }
        try {
            numOldUGVs = Integer.parseInt(link.evaluate("numUGVs; "));
        } catch (NumberFormatException nfe) {
            numOldUGVs = 0;
        }
        // Destroy groups
        for (int i = 0; i < numOldUAVs + numOldUGVs + numOldAnimals; i++) {
            cmds[0] += "[uav" + i + "] join grpnull; ";
        }
        for (int i = 0; i < numOldUAVs + numOldUGVs + numOldHerds; i++) {
            cmds[1] += "deleteGroup group" + i + "; ";
        }
        // Destroy cameras
        for (int i = 0; i < numOldUAVs; i++) {
            // Camera object method
            cmds[2] += "camDestroy cam" + i + "; ";
        }
        // Destroy units
        for (int i = numOldUAVs; i < numOldUAVs + numOldUGVs; i++) {
            cmds[3] += "deleteVehicle uav" + i + "_vehicle; ";
        }
        for (int i = 0; i < numOldUAVs + numOldUGVs + numOldAnimals; i++) {
            cmds[3] += "deleteVehicle uav" + i + "; ";
        }
        for(String cmd : cmds) {
            link.evaluate(cmd);
        }
    }

    public static void deleteWaypoints(Vbs2Link link) {
        // http://community.bistudio.com/wiki/deleteWaypoint
        // To delete waypoints, first set their position to the unit's current
        //  position. Wait, then delete the waypoint. The path of the unit is
        //  calculated by the waypoints present at start, and the unit will
        //  continue according to the original waypoints even if you delete them
        //  by using this command.

        int numOldGoats, numOldUAVs, numWPs, commandSleep = 1000;
        String response;
        try {
            numOldGoats = Integer.parseInt(link.evaluate("numAnimals; "));
        } catch (NumberFormatException nfe) {
            numOldGoats = 0;
        }
        try {
            numOldUAVs = Integer.parseInt(link.evaluate("numUAVs; "));
        } catch (NumberFormatException nfe) {
            numOldUAVs = 0;
        }
        for (int id = 0; id < numOldUAVs + numOldGoats; id++) {
            try {
                response = link.evaluate("nWaypoints group" + id + "; ");
                System.out.println("Response: " + response);
                numWPs = Integer.parseInt(response);
            } catch (NumberFormatException nfe) {
                // We didn't have any groups so nWaypoints returned 'scalar' and our
                //  parseInt failed. So we don't have any waypoints to delete.
                numWPs = 1;
            }
            int watchDog = 0;
            while (numWPs > 1 && watchDog < 3) {
                // Set the waypoint to 0 and its position to where the UAV is so it
                //  completes the waypoint
                response = link.evaluate("group" + id + " setCurrentWaypoint [group" + id + ", 1]; ");
                System.out.println("Response: " + response);
                response = link.evaluate("[group" + id + ", waypointCurrent group" + id + "] setWPPos getPos uav" + id + "; ");
                System.out.println("Response: " + response);
                // Wait so it has time to recalculate and realize it has finished the waypoint
                try {
                    Thread.sleep(commandSleep);
                } catch (InterruptedException e) {
                }
                // Delete the waypoint!!!
                response = link.evaluate("deleteWaypoint [group" + id + ", waypointCurrent group" + id + "]; ");
                System.out.println("Response: " + response);
                try {
                    Thread.sleep(commandSleep);
                } catch (InterruptedException e) {
                }
                try {
                    response = link.evaluate("nWaypoints group" + id + "; ");
                    System.out.println("Response: " + response);
                    numWPs = Integer.parseInt(response);
                } catch (NumberFormatException nfe) {
                    numWPs = 1;
                }
                watchDog++;
            }
            if(numWPs > 1 && watchDog >= 3) {
                System.out.println("WATCHDOG DELETEWAYPOINT COUNTER ACTIVATED");
            }
        }
    }

    public static String[] modifyLastWPCmds(UAV[] uavs, int id, boolean fuel, double x, double y) {
        String[] cmds;
        if (uavs[id].getType() == UAV.Type.UAV) {
            cmds = new String[]{
                        "[" + uavs[id].getGroupName() + ", nWaypoints " + uavs[id].getGroupName() + " - 1] setWPPos [" + x + "," + y + ", 0]; "
                        + uavs[id].getGroupName() + " setCurrentWaypoint [" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1]; "
                        + uavs[id].getUavName() + " setFuel " + ((fuel) ? 1 : 0) + "; "};
        } else if (uavs[id].getType() == UAV.Type.ANIMAL) {
            cmds = new String[]{
                        "[" + uavs[id].getGroupName() + ", nWaypoints " + uavs[id].getGroupName() + " - 1] setWPPos [" + x + "," + y + ", 0]; "
                        + uavs[id].getGroupName() + " setCurrentWaypoint [" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1]; "};
        } else {
            cmds = new String[]{"; "};
        }
        return cmds;
    }

    public static String stringArrayToString(String[] array, String delimiter) {
        String ret = "";
        for (int i = 0; i < array.length - 1; i++) {
            ret += array[i] + delimiter;
        }
        ret += array[array.length - 1];
        return ret;
    }
}
