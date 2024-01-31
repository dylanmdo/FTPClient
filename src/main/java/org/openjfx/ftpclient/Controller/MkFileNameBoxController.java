package org.openjfx.ftpclient.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Contrôleur pour la boîte de dialogue permettant de saisir un nom de fichier.
 * Cette classe étend la classe MessageBoxController.
 */
public class MkFileNameBoxController extends MessageBoxController {

    @FXML
    private TextField mkFileNameTextField;

    @FXML
    private Button confirmButtom;

    /**
     * Initialise le contrôleur. Désactive le bouton de confirmation par défaut
     * et ajoute un écouteur sur le champ de texte pour activer/désactiver le bouton en fonction du contenu.
     */
    public void initialize() {
        confirmButtom.setDisable(true); // Par défaut, le bouton "Confirmer" est désactivé
        mkFileNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmButtom.setDisable(newValue.trim().isEmpty());
        }); // Ajoute un écouteur sur le champ de texte pour activer/désactiver le bouton
    }

    /**
     * Récupère le champ de texte pour le nom de fichier.
     * @return Le champ de texte pour le nom de fichier.
     */
    public TextField getMkFileNameTextField() {
        return mkFileNameTextField;
    }
}
