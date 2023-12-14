package org.openjfx.ftpclient.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.openjfx.ftpclient.Model.ConnectionFtpClient;

public class SceneController {

    public Stage stage;
    private Scene scene;


    public void fileTransferVue(ActionEvent event, ConnectionFtpClient connectionFtpClient, String[] dataLogins) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vue/FileTransfer.fxml"));
        Parent root = loader.load();
        FileTransferController fileTransferController = loader.getController();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
        fileTransferController.initialize(connectionFtpClient,dataLogins);
    }



}
