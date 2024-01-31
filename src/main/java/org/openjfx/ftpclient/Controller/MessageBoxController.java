package org.openjfx.ftpclient.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Contrôleur pour la boîte de dialogue d'un message.
 */
public class MessageBoxController {

    private boolean isConfirm = false; // Indique si le bouton de confirmation a été cliqué

    private final Stage dialogStage = new Stage(); // Stage de la boîte de dialogue

    /**
     * Retourne le stage de la boîte de dialogue.
     *
     * @return Le stage de la boîte de dialogue.
     */
    public Stage getDialogStage() {
        return dialogStage;
    }

    /**
     * Méthode appelée lors du clic sur le bouton d'annulation.
     *
     * @param event L'événement associé au clic sur le bouton d'annulation.
     */
    @FXML
    private void messageBoxCancel(ActionEvent event) {
        dialogStage.close(); // Ferme la boîte de dialogue
    }

    /**
     * Méthode appelée lors du clic sur le bouton de confirmation.
     *
     * @param event L'événement associé au clic sur le bouton de confirmation.
     */
    @FXML
    private void messageBoxConfirm(ActionEvent event) {
        isConfirm = true; // Marque que le bouton de confirmation a été cliqué
        dialogStage.close(); // Ferme la boîte de dialogue
    }

    /**
     * Indique si le bouton de confirmation a été cliqué.
     *
     * @return true si le bouton de confirmation a été cliqué, sinon false.
     */
    public boolean isConfirmClicked() {
        return isConfirm;
    }

}

