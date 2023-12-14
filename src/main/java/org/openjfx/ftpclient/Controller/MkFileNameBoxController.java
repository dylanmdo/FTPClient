package org.openjfx.ftpclient.Controller;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class MkFileNameBoxController extends MessageBoxController {

    @FXML
    private TextField mkFileNameTextField;

    @FXML
    private Button confirmButtom;


    public void initialize() {
        // Par défaut, le bouton "Confirmer" est désactivé
        confirmButtom.setDisable(true);

        // Ajoutez un écouteur sur le champ de texte pour activer/désactiver le bouton
        mkFileNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmButtom.setDisable(newValue.trim().isEmpty());
        });
    }

    public TextField getMkFileNameTextField() {
        return mkFileNameTextField;
    }


}
