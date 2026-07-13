/*
 * Esta clase actúa como el lanzador independiente y oficial del sistema.
 * Es un patrón obligatorio en JavaFX 11+ (conocido como Fat-JAR workaround)
 * que requiere que la clase que contiene el método main() principal NO herede
 * de Application. Desde aquí delegamos el arranque de forma segura.
 */
package com.controlador;

public class Launcher {

    public static void main(String[] args) {
        // Delegamos la ejecución a nuestra clase nativa de JavaFX
        MainApp.main(args);
    }
}