# JvmDumper
Allows you to inspect/decompile/dump byte code loaded in JVM at runtime 

Attach to any java proccess and inspect any loaded class with three built-in modes: FernFlower decompiler, Asmifier (ByteCode), RawByteCode Mode

**To build it you have to install sa-jdi.jar and tools.jar to your local maven repo.
You can do it with commands:**
```
mvn install:install-file -DartifactId=sa-jdi -DgroupId=com.sun -Dversion=1.8 -Dfile="C:\Program Files\Java\jdk1.8.0_144\lib\sa-jdi.jar" -Dpackaging=jar
mvn install:install-file -DartifactId=tools -DgroupId=com.sun -Dversion=1.8 -Dfile="C:\Program Files\Java\jdk1.8.0_144\lib\tools.jar" -Dpackaging=jar
```
Replace *C:\Program Files\Java\jdk1.8.0_144* with your path to jdk.

**You can also download already built jar in release section - https://github.com/hyndor/JvmDumper/releases**

**It requires Java 8 to work correctly**

![Asmifier Image](https://i.imgur.com/BjoJWTc.png)

![FernFlower Image](https://i.imgur.com/ZoGYu11.png)

![RawByteCode Image](https://i.imgur.com/EAZ8rQu.png)
