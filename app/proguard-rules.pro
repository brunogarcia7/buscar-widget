# ===== Regras de ProGuard/R8 do Buscar Widget =====

# Componentes declarados no Manifest são instanciados por nome pelo sistema.
# Mantê-los evita que o R8 os remova/renomeie e quebre o widget.
-keep class com.example.buscarwidget.BuscarWidgetProvider { *; }
-keep class com.example.buscarwidget.SearchTrampolineActivity { *; }
-keep class com.example.buscarwidget.AppPickerActivity { *; }

# WidgetStyle é persistido por NOME (enum.name) e lido com valueOf().
# Ofuscar os nomes do enum quebraria a leitura das preferências salvas.
-keepclassmembers enum com.example.buscarwidget.WidgetStyle {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames class com.example.buscarwidget.WidgetStyle

# AppWidgetProvider é um BroadcastReceiver; preserve o construtor padrão.
-keepclassmembers class * extends android.appwidget.AppWidgetProvider {
    public <init>();
}
