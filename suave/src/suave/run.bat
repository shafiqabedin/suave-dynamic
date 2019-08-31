REM set PATH=%PATH%;C:\Program Files\Java\jdk1.6.0_02\bin
set PATH=C:\PROGRAM FILES\THINKPAD\UTILITIES;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\Program Files\ATI Technologies\ATI Control Panel;C:\Program Files\PC-Doctor for Windows\services;c:\unx\usr\local\wbin\;C:\Program Files\Java\jdk1.6.0_02\bin;C:\laptop\owens\j3d\lib\i386

cd c:\owens\
java -Xmx1024M -Djava.library.path=c:\owens\jogl-1.1.1-windows-i586\lib -cp .;c:\owens\jogl-1.1.1-windows-i586\lib\jogl.jar;c:\owens\jogl-1.1.1-windows-i586\lib\gluegen-rt.jar;c:\owens\jai\jai_core.jar;c:\owens\jai\jai_codec.jar;C:\owens\geotransform\geotransform.jar;C:\owens;C:\owens\objloader\OBJLoader.jar suave.Main --dem C:\owens\gascola_data\GascolaBareEarth1m.tif --texture C:\owens\gascola_data\gascola_mosaic_g1024_1024.jpg --sky C:\owens\generictextures\sky-texture-03_ogc_256_256.jpg --ground C:\owens\generictextures\earth_texture_3131.jpg --uavlog c:\owens\uavlogs\uavmap_youngwoo_10_09.log --skyfront C:\owens\sky\sky12\frontax2.jpg --skyback C:\owens\sky\sky12\backax2.jpg --skyleft C:\owens\sky\sky12\leftax2.jpg --skyright C:\owens\sky\sky12\rightax2.jpg --skytop C:\owens\sky\sky12\topax2.jpg --uavmodel procerus_complete


cd c:\owens\suave
