REM set PATH=%PATH%;C:\Program Files\Java\jdk1.6.0_02\bin
set PATH=C:\PROGRAM FILES\THINKPAD\UTILITIES;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\System32\Wbem;C:\Program Files\ATI Technologies\ATI Control Panel;C:\Program Files\PC-Doctor for Windows\services;C:\Program Files\Java\jdk1.6.0_02\bin;

cd c:\owens\suave
javac  -cp .;c:\owens\jogl-1.1.1-windows-i586\lib\jogl.jar;c:\owens\jogl-1.1.1-windows-i586\lib\gluegen-rt.jar;C:\owens\geotransform\geotransform.jar;C:\owens;C:\owens\objloader\OBJLoader.jar *.java
