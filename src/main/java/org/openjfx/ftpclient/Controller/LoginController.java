package org.openjfx.ftpclient.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.apache.commons.net.ftp.FTPReply;
import org.openjfx.ftpclient.Model.ConnectionFtpClient;

public class LoginController {

    @FXML
    private Label errorLoginMessage;

    @FXML
    private TextField TextFieldLoginId;

    @FXML
    private PasswordField TextFieldLoginPassword;

    @FXML
    private TextField TextFieldLoginPort;

    @FXML
    private TextField TextFieldLoginServer;

    String server;
    int port;
    String id;
    String password;

    private ConnectionFtpClient connectionFtpClient;

    String[] dataLogins;


    @FXML
    private void submitConnection(ActionEvent event) throws Exception {


        SceneController sceneController = new SceneController();

        // Vérifie si l'un des champs de connexion est vide
        if (!TextFieldLoginServer.getText().isBlank() || !TextFieldLoginId.getText().isBlank() || !TextFieldLoginPassword.getText().isBlank() || !TextFieldLoginPort.getText().isBlank()) {


            try {
                // initailisation de la connexion
                loginToServer();
                String[] dataLogins = {server, String.valueOf(port), id, password};


                // retour des réponses   du serveur FTP
                String[] reponses = connectionFtpClient.getFtpClient().getReplyStrings();
                int reply = connectionFtpClient.getFtpClient().getReplyCode();


                // Si la réponse du serveur indique une connexion réussie, on Efface tout message d'erreur précédent du label et charge le vue du transfert de fichier
                if (FTPReply.isPositiveCompletion(reply)) {

                    //on charge le fichier fxml vers ...
                    errorLoginMessage.setText("");
                    sceneController.fileTransferVue(event, connectionFtpClient, dataLogins);


                } else {
                    // sinon on  Affiche les messages d'erreur du serveur
                    for (String reponse : reponses) {
                        errorLoginMessage.setText(reponse.substring(4));
                    }
                }


            } catch (Exception e) {


                // En cas d'erreur lors de la validation ou de la connexion on affiche le message d'erreur
                errorLoginMessage.setTextFill(Color.web("#e35c6a"));
                errorLoginMessage.setText(e.getMessage());
                System.out.println(e.getMessage());
            }

        } else {
            // Si tous les champs de connexion sont vides
            errorLoginMessage.setText("Remplissez les champs vides !");

            //Bypass connexion
            //loginToServer();
            //sceneController.fileTransferVue(event, connectionFtpClient, dataLogins);

        }


    }


    // Méthode pour valider et établir une connexion FTP
    public ConnectionFtpClient loginToServer() throws Exception {
        server = TextFieldLoginServer.getText().trim();
        // Vérifie si le champ de port est vide, si oui, utilise le port par défaut (21)
        if (TextFieldLoginPort.getText().isEmpty()) {
            TextFieldLoginPort.setText("21");
        }
        port = Integer.parseInt(TextFieldLoginPort.getText().trim());
        id = TextFieldLoginId.getText().trim();
        password = TextFieldLoginPassword.getText();

        //Bypass Login
        //connectionFtpClient = new ConnectionFtpClient("node161-eu.n0c.com", 21, "admin@devbraserogascon.go.yj.fr", "R@dica32000");
        connectionFtpClient = new ConnectionFtpClient("demo.wftpserver.com", 21, "demo", "demo");

        connectionFtpClient = new ConnectionFtpClient(server, port, id, password);
        connectionFtpClient.connectionLoginServer();

        return connectionFtpClient;

    }


    public ConnectionFtpClient loginToServer(String server, int port, String id, String password) throws Exception {

        connectionFtpClient = new ConnectionFtpClient(server, port, id, password);
        connectionFtpClient.connectionLoginServer();

        return connectionFtpClient;
    }
}
