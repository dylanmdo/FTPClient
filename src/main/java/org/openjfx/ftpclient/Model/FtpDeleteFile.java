package org.openjfx.ftpclient.Model;

import javafx.animation.Timeline;
import javafx.scene.Parent;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.openjfx.ftpclient.Controller.FileTransferController.getTimeline;
import static org.openjfx.ftpclient.Controller.LoginController.getLoginController;
import static org.openjfx.ftpclient.Model.ConnectionFtpClient.allConnectionFtp;

/**
 * Cette classe gère les opérations de suppression de fichiers et répertoires sur un serveur FTP de manière asynchrone.
 * Elle utilise un pool d'exécution pour gérer les tâches de suppression de manière efficace.
 */
public class FtpDeleteFile {

    private final ExecutorService executorService;
    private DeleteCallback deleteCallback;
    int dossier;
    int fichier;



    /**
     * Initialise un nouveau FtpDeleteFile avec un pool d'exécution d'un seul thread.
     * Vous pouvez ajuster le nombre de threads selon vos besoins.
     */
    public FtpDeleteFile() {
        this.executorService = Executors.newFixedThreadPool(1);
    }

    public interface DeleteCallback {
        void onActionComplete();

        void elementsDeleted(int fichier, int dossier);

        void menuItemEnable();
    }


    /**
     * Supprime de manière asynchrone un fichier sur le serveur FTP.
     *
     * @param fileToDelete Le chemin du fichier à supprimer
     * @param container    Le conteneur parent pour afficher la notification visuelle
     * @param callback     Le callback à exécuter après la suppression du fichier
     * @param dataLogins   Les informations d'identification pour la connexion FTP
     */
    public void deleteFileAsync(String fileToDelete, Parent container, DeleteCallback callback, String[] dataLogins) {
        this.deleteCallback = callback;
        executorService.execute(() -> {
            try {

                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer(dataLogins[0], Integer.parseInt(dataLogins[1]), dataLogins[2], dataLogins[3]);
                allConnectionFtp.add(connectionFtpClient);
                if (deleteFile(connectionFtpClient, fileToDelete)) {
                    if (deleteCallback != null) {
                        deleteCallback.onActionComplete();
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
     * Supprime de manière asynchrone un répertoire sur le serveur FTP.
     *
     * @param directoryToDelete Le chemin du répertoire à supprimer
     * @param container         Le conteneur parent pour afficher la notification visuelle
     * @param callback          Le callback à exécuter après la suppression du répertoire
     * @param dataLogins        Les informations d'identification pour la connexion FTP
     */
    public void  deleteDirectoryAsync(String directoryToDelete, Parent container, DeleteCallback callback, String[] dataLogins) {
        dossier = 1;
        fichier = 0;
        this.deleteCallback = callback;
        executorService.execute(() -> {
            try {
                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer(dataLogins[0], Integer.parseInt(dataLogins[1]), dataLogins[2], dataLogins[3]);
                allConnectionFtp.add(connectionFtpClient);

                if (deleteDirectory(connectionFtpClient, directoryToDelete, callback)) {
                    if (deleteCallback != null) {
                        deleteCallback.onActionComplete();
                    }
                    toastNotification(container).play();
                    allConnectionFtp.remove(connectionFtpClient);
                    connectionFtpClient.getFtpClient().disconnect();
                }
                deleteCallback.menuItemEnable();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });




    }

    /**
     * Supprime de manière synchrone un fichier sur le serveur FTP.
     *
     * @param connectionFtpClient La connexion FTP
     * @param fileToDelete        Le chemin du fichier à supprimer
     * @return True si la suppression réussit, sinon False
     */
    public boolean deleteFile(ConnectionFtpClient connectionFtpClient, String fileToDelete) throws IOException {
        try {
            connectionFtpClient.getFtpClient().deleteFile(fileToDelete);
            connectionFtpClient.getFtpClient().disconnect();
            return true;
        } catch (Throwable throwable) {
            connectionFtpClient.getFtpClient().disconnect();
            return false;
        }

    }

    /**
     * Supprime de manière synchrone un répertoire sur le serveur FTP.
     *
     * @param connectionFtpClient La connexion FTP
     * @param directoryToDelete   Le chemin du répertoire à supprimer
     * @return True si la suppression réussit, sinon False
     * @throws IOException En cas d'erreur lors de la suppression du répertoire
     */



    public boolean deleteDirectory(ConnectionFtpClient connectionFtpClient, String directoryToDelete, DeleteCallback callback) throws IOException {

        this.deleteCallback = callback;
        FTPFile[] subFiles = connectionFtpClient.getFtpClient().listFiles(directoryToDelete);

        if (subFiles != null) {
            for (FTPFile subFile : subFiles) {
                String fileName = subFile.getName();
                if (!(".".equals(fileName) || "..".equals(fileName))) {
                    String filePath = directoryToDelete + "/" + fileName; // Utilisation de '/' au lieu de File.separator car il s'agit d'un chemin FTP
                    if (subFile.isDirectory()) {
                        dossier++;
                        deleteDirectory(connectionFtpClient, filePath, callback); // Supprime récursivement les sous-répertoires
                    } else {
                        fichier++;
                        connectionFtpClient.getFtpClient().deleteFile(filePath); // Supprime les fichiers
                    }
                }
            }
            deleteCallback.elementsDeleted(fichier, dossier);
        }

        connectionFtpClient.getFtpClient().removeDirectory(directoryToDelete); // Supprime le répertoire principal

        return true;
    }



    //--------------------------------------------------------BYPASS----------------------------------------------------------------------------------------//

    /**
     * Supprime de manière asynchrone un fichier sur le serveur FTP en contournant la connexion actuelle.
     *
     * @param fileToDelete Le chemin du fichier à supprimer
     * @param container    Le conteneur parent pour afficher la notification visuelle
     * @param callback     Le callback à exécuter après la suppression du fichier
     */
    public void deleteFileAsync(String fileToDelete, Parent container, DeleteCallback callback) {
        this.deleteCallback = callback;
        executorService.execute(() -> {
            try {
                //BYPASS//
                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer();
                if (deleteFile(connectionFtpClient, fileToDelete)) {
                    if (deleteCallback != null) {
                        deleteCallback.onActionComplete();
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
     * Supprime de manière asynchrone un répertoire sur le serveur FTP en contournant la connexion actuelle.
     *
     * @param directoryToDelete Le chemin du répertoire à supprimer
     * @param container         Le conteneur parent pour afficher la notification visuelle
     * @param callback          Le callback à exécuter après la suppression du répertoire
     */
    public void deleteDirectoryAsync(String directoryToDelete, Parent container, DeleteCallback callback) {
        this.deleteCallback = callback;
        executorService.execute(() -> {
            try {
                //BYPASS//
                ConnectionFtpClient connectionFtpClient = getLoginController.loginToServer();
                if (deleteDirectory(connectionFtpClient, directoryToDelete, callback)) {
                    if (deleteCallback != null) {
                        deleteCallback.onActionComplete();
                    }
                    toastNotification(container).play();
                }
                connectionFtpClient.getFtpClient().disconnect();
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
}


