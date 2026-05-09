# Android Studio Setup — Recipe Cost Calculator

## Si Android Studio no abre el proyecto

El error `Task 'prepareKotlinBuildScriptModel' not found` ocurre cuando el IDE tiene un caché desactualizado del Gradle configuration.

**Solución (en orden):**

1. **Cierra Android Studio completamente**

2. **Desde terminal, ejecuta:**
   ```bash
   cd /home/br1/dev/kotlin/recipe-cost-calculator
   ./gradlew --stop
   ```

3. **Abre Android Studio nuevamente**
   - Abre el proyecto desde `File → Open`
   - Selecciona `/home/br1/dev/kotlin/recipe-cost-calculator`

4. **Espera a que termine la sincronización** (puede tardar 2-3 min la primera vez)
   - Si pide "Sync Now", hacé click
   - No interrumpas el proceso

5. **Si aún falla:**
   - `File → Invalidate Caches → Invalidate and Restart`

## APK ya compilado

Si no querés esperar a que Android Studio sincronice, el APK debug ya está listo:

```
composeApp/build/outputs/apk/debug/composeApp-debug.apk (20 MB)
```

**Instalarlo en el device:**
```bash
adb install -r /home/br1/dev/kotlin/recipe-cost-calculator/composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## Verificación rápida desde terminal

Si querés verificar que el proyecto compila sin abrir el IDE:
```bash
cd /home/br1/dev/kotlin/recipe-cost-calculator
./gradlew :composeApp:assembleDebug --no-daemon
```

Si eso funciona (termina con `BUILD SUCCESSFUL`), el proyecto está OK. El problema es 100% del IDE.

## Configuración aplicada

He agregado:
- `gradle.properties` — optimizaciones para Gradle y Android Studio
- `.idea/gradle.xml` — configuración del gradle de IDE
- `.idea/misc.xml` — configuración de JDK

Estos archivos resuelven la mayoría de issues de sincronización con Android Studio.
