-libraryjars <java.home>/lib/rt.jar
-libraryjars <java.home>/lib/jce.jar
-libraryjars <java.home>/lib/jsse.jar

-dontshrink
-dontoptimize
-dontusemixedcaseclassnames
-repackageclasses

-renamesourcefileattribute MyApplication
-keepattributes SourceFile,LineNumberTable,Signature

-keepclassmembers class RedLink_Menu_Item, 
	RedLink_Menu_Item$NDC,
	RedLink_Biometria$EnrolamientoRequest,
    RedLink_Biometria$Huella,
    RedLink_Ws_Geolocalizacion$RSParametrosPosicionRequest,
    RedLink_Ws_Geolocalizacion$CajeroCercano,
	StateTable_TypeLower_e$RSParametrosPosicionRequest,
	StateTable_TypeLower_e$CajeroCercano {
    <fields>;
    <init>();
}

-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class WallInit {
	public <methods>;
}
-keep public class Peripheral* {
	public <fields>;
	public <methods>;
}
-keep public class JamMask* {
	public <methods>;
}
-keep public class JamAnomaly* {
	public <methods>;
}
-keep public class JamConfigureService* {
	public <methods>;
}
-keep public class JamBuffer* {
	public <methods>;
}

-keep public class JamFile* {
	public <fields>;
	public <methods>;
}

-keep class * implements com.dtg.jsi.JamServiceCommunication{
    public <methods>;
}

-keep class * implements com.dtg.jam.mask.JamMaskScreen{
    public <methods>;
}