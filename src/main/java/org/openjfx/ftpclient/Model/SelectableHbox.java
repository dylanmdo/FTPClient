package org.openjfx.ftpclient.Model;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.apache.commons.net.ftp.FTPFile;
import org.openjfx.ftpclient.Controller.FileTransferController;

import java.io.IOException;

/**
 * Représente une boîte horizontale (HBox) sélectionnable utilisée dans le contexte du contrôleur FileTransferController.
 * Cette classe permet de créer une HBox avec une gestion d'événements de clic pour effectuer des actions en fonction des fichiers FTP associés.
 */
public class SelectableHbox {
    private HBox selectedHBox;
    private final FileTransferController fileTransferController;

    /**
     * Initialise une nouvelle instance de SelectableHbox.
     *
     * @param fileTransferController Le contrôleur FileTransferController associé à cette instance.
     */
    public SelectableHbox(FileTransferController fileTransferController) {
        this.fileTransferController = fileTransferController;
    }

    /**
     * Obtient une instance de FileTransferController en chargeant la vue associée.
     *
     * @return L'instance de FileTransferController.
     * @throws IOException En cas d'erreur lors du chargement de la vue FileTransfer.fxml.
     */
    private FileTransferController getFileTransferController() throws IOException {// Initialisez cette instance correctement
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vue/FileTransfer.fxml"));
        Parent root = loader.load();

        return loader.getController();
    }

    /**
     * Crée une HBox sélectionnable avec un gestionnaire d'événements de clic pour les fichiers FTP associés.
     *
     * @param hBox      La HBox à rendre sélectionnable.
     * @param ftpFiles  Le tableau des fichiers FTP associés à la HBox.
     * @param index     L'index du fichier FTP dans le tableau.
     */
    public void createSelectableHBox(HBox hBox, FTPFile[] ftpFiles, int index) {

        // Ajoute un gestionnaire d'événements au clic de la souris sur le HBox
        hBox.setOnMouseClicked(event -> {
            MouseButton button = event.getButton();

            if (button == MouseButton.PRIMARY && (event.getClickCount()) == 1) {

                if (ftpFiles[index].isFile()) {
                    handleHBoxClick(hBox);
                }
                handleHBoxClick(hBox);
            } else if (button == MouseButton.PRIMARY && (event.getClickCount()) == 2) {

                // Vérifie si le fichier est un répertoire
                if (ftpFiles[index].isDirectory()) {
                    // Enregistre le nom du document cliqué et l'ancien chemin du document actuel
                    try {
                        // Change le répertoire actuel et rafraîchit l'interface utilisateur
                        fileTransferController.changeAndRefreshDirectory(ftpFiles[index].getName());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                } else if (ftpFiles[index].isSymbolicLink()) {
                    String symlinkName = ftpFiles[index].getLink();

                    try {
                        getFileTransferController().connectionFtpClient.getFtpClient().changeWorkingDirectory(symlinkName);
                        getFileTransferController().updateFileListAndUI();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }


            } else if (button == MouseButton.SECONDARY && (event.getClickCount()) == 1) {
                handleHBoxClick(hBox);
            }
        });
    }


    /**
     * Gère l'événement de clic sur une HBox.
     *
     * @param hbox La HBox sur laquelle l'événement de clic a eu lieu.
     */
    private void handleHBoxClick(HBox hbox) {


        // Restaurer le fond et le style par défaut de la HBox précédemment sélectionnée
        if (selectedHBox != null) {
            Background defaultBackground = (Background) selectedHBox.getUserData();
            selectedHBox.setBackground(defaultBackground);
            selectedHBox.getChildren().forEach(child -> child.setStyle("-fx-text-fill: black; -fx-font-size: 16"));
        }
        // Enregistrer le fond par défaut si ce n'est pas déjà fait

        if (hbox.getUserData() == null) {
            hbox.setUserData(hbox.getBackground()); // Enregistre le background par défaut
        }

        // Appliquer le fond et le style pour la HBox sélectionnée
        hbox.setBackground(Background.fill(Color.web("#05a0d7")));
        hbox.getChildren().forEach(child -> child.setStyle("-fx-text-fill: white; -fx-font-size: 16"));
        selectedHBox = hbox;


    }
}
