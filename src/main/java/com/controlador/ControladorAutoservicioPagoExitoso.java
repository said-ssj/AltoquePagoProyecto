package com.controlador;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class ControladorAutoservicioPagoExitoso {

    public void PantallaPausada (){

        PauseTransition pausa = new PauseTransition(Duration.seconds(3));

        pausa.setOnFinished(evento -> {
            /*
            try {
                Stage stageActual = (Stage) .getScene().getWindow();
                stageActual.close();

                Parent root = FXMLLoader.load(getClass().getResource("/com/vista/AutoservicioCheckoutDividida-view.fxml"));
                Stage nuevoStage = new Stage();
                nuevoStage.setScene(new Scene(root));
                nuevoStage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
             */
        });

        pausa.play();
    }
}
