package org.openjfx.ftpclient.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.apache.commons.net.ftp.FTPReply;
import org.openjfx.ftpclient.Model.ConnectionFtpClient;

import java.io.IOException;

/**
 * Contrôleur pour la vue de connexion (FXML) permettant de gérer la connexion au serveur FTP.
 */
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
    public static LoginController getLoginController;

    /**
     * Méthode appelée lors de la soumission du formulaire de connexion.
     *
     * @param event ActionEvent déclenché par l'utilisateur.
     * @throws Exception Si une erreur se produit lors de la connexion.
     */
    @FXML
    private void submitConnection(ActionEvent event) throws Exception {
        getLoginController();

        SceneController sceneController = new SceneController();

        if (checkInput()) {

            try {

                loginToServer();
                processServerResponse(event, sceneController);


            } catch (Exception e) {


                // En cas d'erreur lors de la validation ou de la connexion on affiche le message d'erreur
                errorLoginMessage.setTextFill(Color.web("#e35c6a"));
                errorLoginMessage.setText(e.getMessage());
                System.out.println(e.getMessage());
            }

        }


    }


    /**
     * Traite la réponse du serveur après une tentative de connexion.
     *
     * @param event           ActionEvent déclenché par l'utilisateur.
     * @param sceneController Contrôleur de scène pour la gestion des vues.
     * @throws Exception Si une erreur se produit lors de la récupération de la réponse du serveur FTP.
     */
    private void processServerResponse(ActionEvent event, SceneController sceneController) throws Exception {
        String[] dataLogins = {server, String.valueOf(port), id, password};


        // retour des réponses du serveur FTP
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
    }

    /**
     * Vérifie si les champs de saisie pour le serveur, l'identifiant, le mot de passe et le port sont remplis.
     * Affiche un message d'erreur si des champs sont vides.
     *
     * @return true si tous les champs sont remplis, sinon false.
     */
    private boolean checkInput() {
        if (!TextFieldLoginServer.getText().isBlank() || !TextFieldLoginId.getText().isBlank() || !TextFieldLoginPassword.getText().isBlank() || !TextFieldLoginPort.getText().isBlank()) {

            return true;
        } else {
            errorLoginMessage.setText("Remplissez les champs vides !");
            return false;
        }
    }


    /**
     * Établit une connexion au serveur FTP en utilisant les informations saisies par l'utilisateur dans les champs correspondants.
     *
     * @return Une instance de ConnectionFtpClient représentant la connexion établie.
     * @throws Exception Si une erreur se produit lors de la connexion.
     */
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
        //connectionFtpClient = new ConnectionFtpClient("demo.wftpserver.com", 21, "demo", "demo");

        connectionFtpClient = new ConnectionFtpClient(server, port, id, password);
        connectionFtpClient.connectionLoginServer();

        return connectionFtpClient;

    }

    /**
     * Établit une connexion au serveur FTP en utilisant les informations fournies.
     *
     * @param server   Le nom ou l'adresse IP du serveur FTP.
     * @param port     Le numéro de port du serveur FTP.
     * @param id       L'identifiant de connexion FTP.
     * @param password Le mot de passe de connexion FTP.
     * @return Une instance de ConnectionFtpClient représentant la connexion établie.
     * @throws Exception Si une erreur se produit lors de la connexion.
     */
    public ConnectionFtpClient loginToServer(String server, int port, String id, String password) throws Exception {

        connectionFtpClient = new ConnectionFtpClient(server, port, id, password);
        connectionFtpClient.connectionLoginServer();

        return connectionFtpClient;
    }


    /**
     * Initialise l'instance du contrôleur de la vue de connexion en chargeant le fichier FXML.
     *
     * @throws IOException Si une erreur survient lors du chargement du fichier FXML.
     */
    private void getLoginController() throws IOException {// Initialisez cette instance correctement
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vue/Login.fxml"));
        Parent root = loader.load();

        getLoginController = loader.getController();

    }


}
