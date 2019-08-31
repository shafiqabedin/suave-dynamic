#!/bin/bash
EXPR="FEED_11_WARMUP"
FOLDER="/Users/sha33/NetBeansProjects/vbs2gui/"
VBS2GUICP="../../build/classes:../../lib/jcommon-1.0.16.jar:../../lib/jfreechart-1.0.13.jar:../../lib/Machinetta.jar"
JARFLAGS="-Xmx4096M"
JAVA="java"
ARGS="--exp ${EXPR} --folder ${FOLDER}" 

echo "Initializing suave with args $ARGS." 
echo "Command line is " ${JAVA} ${JARFLAGS} -classpath ${VBS2GUICP} vbs2gui.SimpleUAVSim.Main ${ARGS}
${JAVA} ${JARFLAGS} -classpath ${VBS2GUICP} vbs2gui.SimpleUAVSim.Main ${ARGS}

