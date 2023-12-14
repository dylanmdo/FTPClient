package org.openjfx.ftpclient.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MessageBoxController {

    private boolean isConfirm = false;

    private final Stage dialogStage = new Stage();

    public Stage getDialogStage() {
        return dialogStage;
    }

    @FXML
    private void messageBoxCancel(ActionEvent event) {
        dialogStage.close();
    }

    @FXML
    private void messageBoxConfirm(ActionEvent event) {
        isConfirm = true;
        dialogStage.close();
    }


    public boolean isConfirmClicked() {
        return isConfirm;
    }

}
