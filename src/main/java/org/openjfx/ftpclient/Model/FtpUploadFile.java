package org.openjfx.ftpclient.Model;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import org.apache.commons.net.ftp.FTP;
import org.openjfx.ftpclient.Controller.LoginController;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.openjfx.ftpclient.Controller.FileTransferController.getTimeline;
import static org.openjfx.ftpclient.Controller.LoginController.getLoginController;
import static org.openjfx.ftpclient.Model.ConnectionFtpClient.allConnectionFtp;

/**
 * Gère le téléchargement de fichiers et de répertoires vers un serveur FTP de manière asynchrone.
 */
public class FtpUploadFile {

    private UploadCallback uploadCallback;

    /**
     * Initialise un nouveau FtpUploadFile avec un pool d'exécution d'un seul thread.
     * Vous pouvez ajuster le nombre de threads selon vos besoins.
     */
    private final ExecutorService executorService;

    public FtpUploadFile() {
        // Crée un pool de threads pour gérer les téléchargements
        this.executorService = Executors.newFixedThreadPool(1); // Vous pouvez ajuster le nombre de threads selon vos besoins
    }

    public interface UploadCallback {
        void onActionComplete();
    }

    /**
     * Récupère le contrôleur de connexion FTP.
     *
     * @return Le contrôleur de connexion FTP initialisé correctement
     * @throws IOException En cas d'erreur lors de l'initialisation
     */


    /**
     * Télécharge de manière asynchrone une liste de fichiers vers un serveur FTP.
     *
     * @param filesDragDrop         La liste des fichiers à télécharger via DragAndDrop.
     * @param currentRemoteFilePath Le chemin distant où les fichiers seront téléversés.
     * @param progressBar           La barre de progression pour afficher l'avancement.
     * @param container             Le conteneur parent pour afficher les notifications.
     * @param callback              Le rappel appelé lorsque le téléchargement est terminé.
     * @param dataLogins            Les informations de connexion au serveur FTP.
     */
    public void uploadFileAsync(List<File> filesDragDrop, String currentRemoteFilePath, ProgressBar progressBar, Parent container, UploadCallback callback, String[] dataLogins) {

        this.uploadCallback = callback;

        executorService.execute(() -> {

            try {


                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer(dataLogins[0], Integer.parseInt(dataLogins[1]), dataLogins[2], dataLogins[3]);
                allConnectionFtp.add(connectionFtpClient);
                if (uploadFile(connectionFtpClient, filesDragDrop, currentRemoteFilePath, progressBar)) {

                    if (uploadCallback != null) {
                        uploadCallback.onActionComplete();
                    }
                    toastNotification(container).play();

                    allConnectionFtp.remove(connectionFtpClient);
                    connectionFtpClient.getFtpClient().disconnect();

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        });


    }

    /**
     * Télécharge de manière asynchrone un répertoire vers un serveur FTP.
     *
     * @param filesDragDrop         La liste des fichiers du répertoire à télécharger via DragAndDrop.
     * @param currentRemoteFilePath Le chemin distant où les fichiers seront téléversés.
     * @param progressBar           La barre de progression pour afficher l'avancement.
     * @param container             Le conteneur parent pour afficher les notifications.
     * @param callback              Le rappel appelé lorsque le téléchargement est terminé.
     * @param dataLogins            Les informations de connexion au serveur FTP.
     */
    public void uploadDirectoryAsync(List<File> filesDragDrop, String currentRemoteFilePath, ProgressBar progressBar, Parent container, UploadCallback callback, String[] dataLogins) {
        this.uploadCallback = callback;

        executorService.execute(() -> {

            try {
                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer(dataLogins[0], Integer.parseInt(dataLogins[1]), dataLogins[2], dataLogins[3]);
                allConnectionFtp.add(connectionFtpClient);
                if (uploadDirectory(connectionFtpClient, filesDragDrop, currentRemoteFilePath, progressBar)) {
                    if (uploadCallback != null) {
                        uploadCallback.onActionComplete();
                    }
                    toastNotification(container).play();
                    allConnectionFtp.remove(connectionFtpClient);
                    connectionFtpClient.getFtpClient().disconnect();

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        });


    }

    /**
     * Génère une notification sous forme de timeline.
     *
     * @param container Le conteneur parent pour afficher les notifications.
     * @return La timeline pour la notification.
     */
    private Timeline toastNotification(Parent container) {
        return getTimeline(container);
    }

    /**
     * Télécharge un fichier vers un serveur FTP.
     *
     * @param connectionFtpClient   Le client FTP connecté.
     * @param filesDragDrop         La liste des fichiers à télécharger via DragAndDrop.
     * @param currentRemoteFilePath Le chemin distant où les fichiers seront téléchargés.
     * @param progressBar           La barre de progression pour afficher l'avancement.
     * @return Vrai si le téléchargement réussit, faux sinon.
     * @throws IOException En cas d'erreur d'entrée/sortie pendant le téléchargement.
     */
    public boolean uploadFile(ConnectionFtpClient connectionFtpClient, List<File> filesDragDrop, String currentRemoteFilePath, ProgressBar progressBar) throws IOException {
        connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);

        for (File file : filesDragDrop) {
            String remoteFilePath = currentRemoteFilePath + "/" + file.getName();
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = connectionFtpClient.getFtpClient().storeFileStream(remoteFilePath);


            byte[] bytesIn = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, bytesRead);
                totalBytesRead += bytesRead;

                // Calculez la progression et mettez à jour la ProgressBar
                double progress = (double) totalBytesRead / file.length();
                Platform.runLater(() -> progressBar.setProgress(progress));
            }

            inputStream.close();
            outputStream.close();

            // Fermez le flux de transfert
            connectionFtpClient.getFtpClient().completePendingCommand();
        }

        return true;
    }

    /**
     * Télécharge un répertoire vers un serveur FTP.
     *
     * @param connectionFtpClient   Le client FTP connecté.
     * @param filesDragDrop         La liste des fichiers du répertoire à télécharger.
     * @param currentRemoteFilePath Le chemin distant où les fichiers seront téléchargés.
     * @param progressBar           La barre de progression pour afficher l'avancement.
     * @return Vrai si le téléchargement réussit, faux sinon.
     * @throws IOException En cas d'erreur d'entrée/sortie pendant le téléchargement.
     */
    public boolean uploadDirectory(ConnectionFtpClient connectionFtpClient, List<File> filesDragDrop, String currentRemoteFilePath, ProgressBar progressBar) throws IOException {
        uploadCallback.onActionComplete();
        int totalFiles = 0;
        int downloadedFiles = 0;
        for (File file : filesDragDrop) {
            if (file.isDirectory()) {
                String remoteDirectoryPath = currentRemoteFilePath + "/" + file.getName();
                connectionFtpClient.getFtpClient().makeDirectory(remoteDirectoryPath);
                File[] subFiles = file.listFiles();

                if (subFiles != null) {
                    totalFiles = totalFiles + subFiles.length;
                    connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
                    for (File subFile : subFiles) {

                        String fileName = subFile.getName();
                        String remoteFilePath = remoteDirectoryPath + "/" + fileName;
                        String localFilePath = subFile.getAbsolutePath();  // Mettez à jour ce chemin local

                        if (subFile.isFile()) {
                            if (uploadFile(connectionFtpClient, remoteFilePath, localFilePath)) { //appel de la surcharge uploadFile()
                                // Fichier téléchargé avec succès
                                downloadedFiles++;
                            }
                        } else if (subFile.isDirectory()) {
                            if (uploadDirectory(connectionFtpClient, Collections.singletonList(subFile), remoteDirectoryPath, progressBar)) {
                                // Répertoire téléchargé avec succès
                                downloadedFiles++;
                            }
                        }

                        double progress = (double) downloadedFiles / totalFiles;
                        Platform.runLater(() -> progressBar.setProgress(progress));

                    }
                }


            }

        }
        Platform.runLater(() -> progressBar.setProgress(1.0));

        return true;
    }


    //Surcharge de la methode uploadFile() pour la recursive uploadDirectory()
    public boolean uploadFile(ConnectionFtpClient connectionFtpClient, String remoteFilePath, String localFilePath) throws IOException {
        File localFile = new File(localFilePath);
        InputStream inputStream = new FileInputStream(localFile);

        try {
            connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
            return connectionFtpClient.getFtpClient().storeFile(remoteFilePath, inputStream);
        } catch (Exception e) {
            inputStream.close();
            connectionFtpClient.getFtpClient().completePendingCommand();

        }

        // Vérifiez si outputStream est null
        return true;

    }


    //--------------------------------------------------------BYPASS----------------------------------------------------------------------------------------//

    /**
     * Télécharge de manière asynchrone une liste de fichiers vers un chemin distant sur un serveur FTP en contournant la connexion actuelle.
     *
     * @param filesDragDrop         La liste des fichiers à télécharger.
     * @param currentRemoteFilePath Le chemin distant où les fichiers seront téléchargés.
     * @param progressBar           La barre de progression pour afficher l'avancement du téléchargement.
     * @param container             Le conteneur parent pour afficher les notifications.
     * @param callback              Le rappel appelé lorsque le téléchargement est terminé.
     */
    public void uploadFileAsync(List<File> filesDragDrop, String currentRemoteFilePath, ProgressBar progressBar, Parent container, UploadCallback callback) {

        this.uploadCallback = callback;

        executorService.execute(() -> {

            try {

                //ByPass//
                if (uploadFile(getLoginController.loginToServer(), filesDragDrop, currentRemoteFilePath, progressBar)) {

                    if (uploadCallback != null) {
                        uploadCallback.onActionComplete();
                    }
                    toastNotification(container).play();

                    getLoginController.loginToServer().getFtpClient().disconnect();

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        });


    }

    /**
     * Télécharge de manière asynchrone un répertoire vers un chemin distant sur un serveur FTP en contournant la connexion actuelle.
     *
     * @param filesDragDrop         La liste des fichiers du répertoire à télécharger.
     * @param currentRemoteFilePath Le chemin distant où les fichiers seront téléchargés.
     * @param progressBar           La barre de progression pour afficher l'avancement du téléchargement.
     * @param container             Le conteneur parent pour afficher les notifications.
     * @param callback              Le rappel appelé lorsque le téléchargement est terminé.
     */
    public void uploadDirectoryAsync(List<File> filesDragDrop, String currentRemoteFilePath, ProgressBar progressBar, Parent container, UploadCallback callback) {
        this.uploadCallback = callback;

        executorService.execute(() -> {

            try {
                //ByPass//

                if (uploadDirectory(getLoginController.loginToServer(), filesDragDrop, currentRemoteFilePath, progressBar)) {
                    if (uploadCallback != null) {
                        uploadCallback.onActionComplete();
                    }
                    toastNotification(container).play();
                    getLoginController.loginToServer().getFtpClient().disconnect();

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        });


    }


}