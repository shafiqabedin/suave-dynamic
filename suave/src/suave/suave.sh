#!/bin/bash 
FOLDER="/home/usar/Programming/vbs2gui/"
SUAVECP="../../dist/suave.jar"
JARFLAGS="-Djava.library.path="../../lib" -XX:+UseParallelGC -Xmx8000M" 
JAVA="java" 
ARGS="--addsadials --texture ../../warminster/warminster-small-geotag-4096.jpg --worldfile  ../../warminster/warminster-small-4096.jfw --dem ../../warminster/map_2011_01_29_072758-LLA-RES_25.lla --sky ../../generictextures/sky-texture-03_ogc_256_256.jpg --ground ../../generictextures/earth_texture_3131.jpg  --skyfront ../../sky/sky12/frontax2.jpg --skyback ../../sky/sky12/backax2.jpg --skyleft ../../sky/sky12/leftax2.jpg --skyright ../../sky/sky12/rightax2.jpg --skytop ../../sky/sky12/topax2.jpg --meshstep 1  --exp SUAVE1 --folder ${FOLDER}" 

export TIMESTAMP=`/bin/date "+%Y_%m_%d_%H_%M_%S"`

echo "Initializing suave with args $ARGS." 
echo "Command line is " ${JAVA} ${JARFLAGS} -classpath ${SUAVECP} suave.Main  ${ARGS} 
echo Logging to suave_${TIMESTAMP}.log
${JAVA} ${JARFLAGS} -classpath ${SUAVECP} suave.Main  ${ARGS}
