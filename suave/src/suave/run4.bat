cd C:\laptop\owens\

set PATH=C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\Program Files\ATI Technologies\ATI Control Panel;C:\Program Files\MIT\Kerberos\bin;C:\Program Files\OpenAFS\Common;C:\Program Files\OpenAFS\Client\Program;C:\Program Files\Java\jdk1.6.0_04\bin;C:\Program Files\Subversion\bin;C:\apps\ant\bin;C:\Program Files\Vim\vim70;C:\OpenCV2.0\bin

REM set PATH=C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\Program Files\ATI Technologies\ATI Control Panel;C:\Program Files\MIT\Kerberos\bin;C:\Program Files\OpenAFS\Common;C:\Program Files\OpenAFS\Client\Program;C:\Program Files\Java\jdk1.6.0_04\bin;C:\Program Files\QuickTime\QTSystem\;C:\laptop\owens\j3d\lib\i386

REM Without debug skybox textures
"c:\Program Files\Java\jdk1.6.0_04\bin\java" -XX:+UseParallelGC -Xmx1024M -Djava.library.path=c:\laptop\owens\jogl-1.1.1-windows-i586\lib -cp .;c:\laptop\owens\jogl-1.1.1-windows-i586\lib\jogl.jar;c:\laptop\owens\jogl-1.1.1-windows-i586\lib\gluegen-rt.jar;C:\laptop\owens\jai\jai_codec.jar;C:\laptop\owens\jai\jai_core.jar;C:\laptop\owens\geotransform\geotransform.jar;C:\laptop\owens suave.Main --dem C:\laptop\owens\gascola_data\GascolaBareEarth1m.tif --texture C:\laptop\owens\gascola_data\gascola_mosaic_g1024_1024.jpg --sky C:\laptop\owens\generictextures\sky-texture-03_ogc_256_256.jpg --ground C:\laptop\owens\generictextures\earth_texture_3131.jpg --uavlog C:\laptop\owens\suave\gascola3.log --skyfront C:\laptop\owens\sky\sky12\frontax2.jpg --skyback C:\laptop\owens\sky\sky12\backax2.jpg --skyleft C:\laptop\owens\sky\sky12\leftax2.jpg --skyright C:\laptop\owens\sky\sky12\rightax2.jpg --skytop C:\laptop\owens\sky\sky12\topax2.jpg --uavmodel procerus_complete --meshstep 25 --vclogstartat 6000000 --vclog C:\laptop\owens\suave\telemetry_UAV5_2008_10_22_00.m --imagedir c:\suave_2008_10_22_30minutes_split


REM With debug skybox textures
REM "c:\Program Files\Java\jdk1.6.0_04\bin\java" -XX:+UseParallelGC -Xmx1024M -Djava.library.path=c:\laptop\owens\jogl-1.1.1-windows-i586\lib -cp .;c:\laptop\owens\jogl-1.1.1-windows-i586\lib\jogl.jar;c:\laptop\owens\jogl-1.1.1-windows-i586\lib\gluegen-rt.jar;C:\laptop\owens\jai\jai_codec.jar;C:\laptop\owens\jai\jai_core.jar;C:\laptop\owens\geotransform\geotransform.jar;C:\laptop\owens suave.Main --dem C:\laptop\owens\gascola_data\GascolaBareEarth1m.tif --texture C:\laptop\owens\gascola_data\gascola_mosaic_g1024_1024.jpg --sky C:\laptop\owens\generictextures\sky-texture-03_ogc_256_256.jpg --ground C:\laptop\owens\generictextures\earth_texture_3131.jpg --uavlog C:\laptop\owens\suave\gascola3.log --skyfront C:\laptop\owens\sky\sky12\frontax2debug.jpg --skyback C:\laptop\owens\sky\sky12\backax2debug.jpg --skyleft C:\laptop\owens\sky\sky12\leftax2debug.jpg --skyright C:\laptop\owens\sky\sky12\rightax2debug.jpg --skytop C:\laptop\owens\sky\sky12\topax2debug.jpg --uavmodel procerus_complete --meshstep 25 --vclogstartat 6000000 --vclog C:\laptop\owens\suave\telemetry_UAV5_2008_10_22_00.m --imagedir c:\suave_2008_10_22_30minutes_split


cd c:\laptop\owens\suave
