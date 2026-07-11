package com.controlador;

import com.sun.tools.javac.Main;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}
 class MainLauncher {
    public static void main(String[] args) throws Exception {
        // Reemplaza "Main" con el nombre exacto de tu clase que extiende Application
        Main.main(args);
    }
}