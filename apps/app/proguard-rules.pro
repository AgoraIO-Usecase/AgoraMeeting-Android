
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class androidx.**{*;}
-keep class io.agora.meeting.**{*;}
