package org.openjfx.ftpclient.Model;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.openjfx.ftpclient.Controller.LoginController;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.openjfx.ftpclient.Controller.FileTransferController.getTimeline;
import static org.openjfx.ftpclient.Controller.LoginController.getLoginController;
import static org.openjfx.ftpclient.Model.ConnectionFtpClient.allConnectionFtp;

/**
 * Cette classe gère le téléchargement de fichiers et de répertoires depuis un serveur FTP de manière asynchrone.
 * Elle utilise un pool de threads pour gérer efficacement les tâches de téléchargement.
 */
public class FtpDownloader {
    /**
     * Initialise un nouveau FtpDownloader avec un pool d'exécution d'un seul thread.
     * Vous pouvez ajuster le nombre de threads selon vos besoins.
     */
    private final ExecutorService executorService;

    public FtpDownloader() {
        // Crée un pool de threads pour gérer les téléchargements
        this.executorService = Executors.newFixedThreadPool(1); // Vous pouvez ajuster le nombre de threads selon vos besoins
    }




    /**
     * Télécharge de manière asynchrone un répertoire depuis le serveur FTP.
     *
     * @param remoteDir    Le répertoire distant à télécharger
     * @param localDirPath Le chemin local où le répertoire qui sera téléchargé
     * @param progressBar  La barre de progression pour afficher l'avancement du téléchargement
     * @param container    Le conteneur parent pour afficher la notification visuelle
     * @param dataLogins   Les informations d'identification pour la connexion FTP
     */
    public void downloadDirectoryAsync(String remoteDir, String localDirPath, ProgressBar progressBar, Parent container, String[] dataLogins) throws Exception {
        executorService.execute(() -> {
            try {
                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer(dataLogins[0], Integer.parseInt(dataLogins[1]), dataLogins[2], dataLogins[3]);
                allConnectionFtp.add(connectionFtpClient);
                // Appel de la méthode downloadDirectory
                if (downloadDirectory(connectionFtpClient, remoteDir, localDirPath, progressBar)) {
                    toastNotification(container).play();
                    allConnectionFtp.remove(connectionFtpClient);
                }
                connectionFtpClient.getFtpClient().disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);

            }


        });

    }

    /**
     * Télécharge de manière asynchrone un fichier depuis le serveur FTP.
     *
     * @param remoteFile  Le fichier distant à télécharger
     * @param localFile   Le chemin local où le fichier qui sera téléchargé
     * @param progressBar La barre de progression pour afficher l'avancement du téléchargement
     * @param container   Le conteneur parent pour afficher la notification visuelle
     * @param dataLogins  Les informations d'identification pour la connexion FTP
     */
    public void downloadFileAsync(String remoteFile, String localFile, ProgressBar progressBar, Parent container, String[] dataLogins) throws Exception {

        executorService.execute(() -> {
            try {
                // Appel de la méthode downloadFile
                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer(dataLogins[0], Integer.parseInt(dataLogins[1]), dataLogins[2], dataLogins[3]);
                allConnectionFtp.add(connectionFtpClient);
                if (downloadFile(connectionFtpClient, remoteFile, localFile, progressBar)) {
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
     * Affiche une notification visuelle.
     *
     * @param container Le conteneur pour la notification
     * @return La timeline pour la notification
     */
    private Timeline toastNotification(Parent container) {
        return getTimeline(container);
    }


    /**
     * Télécharge récursivement un répertoire depuis le serveur FTP.
     *
     * @param connectionFtpClient La connexion FTP
     * @param remoteDirPath       Le répertoire distant à télécharger
     * @param localDirPath        Le chemin local où le répertoire sera téléchargé
     * @param progressBar         La barre de progression pour afficher l'avancement du téléchargement
     * @return True si le téléchargement réussit, sinon False
     * @throws Exception En cas d'erreur lors du téléchargement
     */
    public boolean downloadDirectory(ConnectionFtpClient connectionFtpClient, String remoteDirPath, String localDirPath, ProgressBar progressBar) throws Exception {
        connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
        FTPFile[] subFiles = connectionFtpClient.getFtpClient().listFiles(remoteDirPath);
        int totalFiles = subFiles.length;
        int downloadedFiles = 0;


        File downloadDir = new File(localDirPath);
        if (!downloadDir.exists() && downloadDir.mkdirs() ) {
            for (FTPFile subFile : subFiles) {

                String fileName = subFile.getName();
                if (!(".".equals(fileName) || "..".equals(fileName))) {
                    String remoteFilePath = remoteDirPath + "/" + fileName;
                    String localFilePath = localDirPath + File.separator + fileName;

                    if (subFile.isFile()) {
                        if (downloadFile(connectionFtpClient, remoteFilePath, localFilePath)) { // Télécharge les fichiers
                            downloadedFiles++;
                        }
                    } else if (subFile.isDirectory()) {

                        if (downloadDirectory(connectionFtpClient, remoteFilePath, localFilePath, progressBar)) { // Télécharge récursivement les sous-répertoires
                            downloadedFiles++;
                        }

                    }


                    double progress = (double) downloadedFiles / totalFiles;
                    Platform.runLater(() -> progressBar.setProgress(progress));

                }
            }
        }

        Platform.runLater(() -> progressBar.setProgress(1.0));


        return true;
    }


    /**
     * Télécharge un fichier depuis le serveur FTP.
     *
     * @param connectionFtpClient La connexion FTP
     * @param remoteFilePath      Le fichier distant à télécharger
     * @param localFilePath       Le chemin local où le fichier sera téléchargé.
     * @param progressBar         La barre de progression pour afficher l'avancement du téléchargement
     * @return True si le téléchargement réussit, sinon False
     * @throws Exception En cas d'erreur lors du téléchargement
     */
    public boolean downloadFile(ConnectionFtpClient connectionFtpClient, String remoteFilePath, String localFilePath, ProgressBar progressBar) throws Exception {
        connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
        FTPFile[] ftpFiles = new FTPFile[]{connectionFtpClient.getFtpClient().mlistFile(remoteFilePath)};

        FTPFile ftpFile = ftpFiles[0];
        long fileSize = ftpFile.getSize();

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFilePath))) {
            InputStream inputStream = connectionFtpClient.getFtpClient().retrieveFileStream(remoteFilePath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // Calculez la progression et mettez à jour la ProgressBar
                double progress = (double) totalBytesRead / fileSize;
                Platform.runLater(() -> progressBar.setProgress(progress));
            }

            inputStream.close();
            outputStream.close();

            Platform.runLater(() -> progressBar.setProgress(1.0));


            return true;
        }
    }


    //Surcharge de methode downloadFile() pour ne pas prendre en compte la progress Bar dans la recursive de downloadDirectory()
    /**
     * Télécharge un fichier depuis un serveur FTP distant vers un emplacement local.
     *
     * @param connectionFtpClient L'objet ConnectionFtpClient contenant les informations de connexion.
     * @param remoteFilePath      Le chemin du fichier distant sur le serveur FTP.
     * @param localFilePath       Le chemin de destination local pour enregistrer le fichier téléchargé.
     * @return true si le téléchargement réussit, false sinon.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de l'opération de téléchargement.
     */
    private boolean downloadFile(ConnectionFtpClient connectionFtpClient, String remoteFilePath, String localFilePath) throws IOException {
        connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFilePath))) {
            if (connectionFtpClient.getFtpClient().retrieveFile(remoteFilePath, outputStream)) {
                return true;
            } else {

                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //--------------------------------------------------------BYPASS----------------------------------------------------------------------------------------//

    /**
     * Télécharge de manière asynchrone un fichier depuis le serveur FTP en contournant la connexion actuelle.
     *
     * @param remoteFilePath Le fichier distant à télécharger
     * @param localFilePath  Le chemin local où le fichier sera téléchargé
     * @param progressBar    La barre de progression pour afficher l'avancement du téléchargement
     * @param container      Le conteneur parent pour afficher la notification visuelle
     */
    public ExecutorService downloadFileAsync(String remoteFilePath, String localFilePath, ProgressBar progressBar, Parent container) {

        executorService.execute(() -> {
            try {
                //ByPass//
                if (downloadFile(getLoginController.loginToServer(), remoteFilePath, localFilePath, progressBar)) {
                    toastNotification(container).play();
                    getLoginController.loginToServer().getFtpClient().disconnect();

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });


        return executorService;


    }

    /**
     * Télécharge de manière asynchrone un répertoire depuis le serveur FTP en contournant la connexion actuelle.
     *
     * @param remoteDirPath Le répertoire distant à télécharger
     * @param localDirPath  Le chemin local où le répertoire sera téléchargé
     * @param progressBar   La barre de progression pour afficher l'avancement du téléchargement
     * @param container     Le conteneur parent pour afficher la notification visuelle
     */
    public ExecutorService downloadDirectoryAsync(String remoteDirPath, String localDirPath, ProgressBar progressBar, Parent container) throws Exception {
        executorService.execute(() -> {
            try {

                if (downloadDirectory(getLoginController.loginToServer(), remoteDirPath, localDirPath, progressBar)) {
                    toastNotification(container).play();
                    getLoginController.loginToServer().getFtpClient().disconnect();

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }


        });

        return executorService;

    }


}
