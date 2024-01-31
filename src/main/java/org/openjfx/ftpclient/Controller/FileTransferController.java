package org.openjfx.ftpclient.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.openjfx.ftpclient.Main;
import org.openjfx.ftpclient.Model.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.openjfx.ftpclient.Model.ConnectionFtpClient.allConnectionFtp;

public class FileTransferController implements FtpUploadFile.UploadCallback, FtpDeleteFile.DeleteCallback {


    @FXML
    Label ftpLinkBar;
    @FXML
    private VBox containerDocument;
    @FXML
    private Label ftpServer;

    @FXML
    private VBox containerDocChild;

    @FXML
    private MenuItem userId;

    @FXML
    private Pane popupDirDelete;

    @FXML
    private Label numDirDelete;

    @FXML
    private Label numFileDelete;

    public ConnectionFtpClient connectionFtpClient;

    private String server;
    private int port;
    private String id;
    private String password;


    private final Image imageFile = new Image(Objects.requireNonNull(getClass().getResource("/assets/file.png")).toExternalForm());
    private final Image imageDirectory = new Image(Objects.requireNonNull(getClass().getResource("/assets/directory.png")).toExternalForm());
    private final Image imageSymlink = new Image(Objects.requireNonNull(getClass().getResource("/assets/symlink.png")).toExternalForm());

    private String currentWorkingDirectory;
    private FTPFile[] files;

    DocumentItemController documentItemController;
    DocChildItemController docChildItemController;
    FtpDownloader ftpDownloader = new FtpDownloader();
    FtpDeleteFile ftpDeleteFile = new FtpDeleteFile();

    SelectableHbox selectableHbox = new SelectableHbox(this);

    @FXML
    private ProgressBar progressBarFtp;

    @FXML
    private HBox containerProgressBar;

    String[] dataLogins;

    FtpUploadFile ftpUploadFile = new FtpUploadFile();

    @FXML
    private Pane popupDownload;
    @FXML
    private Pane popupUpload;
    @FXML
    private Pane popupDelete;


    MenuItem menuItemDelete;
    MenuItem menuItemDownload;
    MenuItem menuItemRename;

    Main main = new Main();


    public FileTransferController() throws Exception {
    }


    /**
     * Initialise la fenêtre avec les données de connexion fournies.
     *
     * @param connectionFtpClient Objet représentant la connexion FTP.
     * @param dataLogins          Tableau de chaînes de caractères contenant les informations de connexion [server, port, id, password].
     * @throws Exception Si une erreur survient lors de l'initialisation.
     */
    @FXML
    public void initialize(ConnectionFtpClient connectionFtpClient, String[] dataLogins) throws Exception {
        this.connectionFtpClient = connectionFtpClient;
        files = connectionFtpClient.getFtpClient().listFiles();
        ftpServer.setText(connectionFtpClient.getServer());
        this.dataLogins = dataLogins;

        this.server = dataLogins[0];
        this.port = Integer.parseInt(dataLogins[1]);
        this.id = dataLogins[2];
        this.password = dataLogins[3];

        userId.setText(id);

        setFtpLink();

        updateUI();
    }


    /**
     * Met à jour le lien FTP affiché dans la barre de lien et configure les info-bulles associées.
     *
     * @throws IOException Si une erreur survient lors de la récupération du répertoire de travail courant du serveur FTP.
     */
    private void setFtpLink() throws IOException {

        currentWorkingDirectory = connectionFtpClient.getFtpClient().printWorkingDirectory();
        ftpLinkBar.setText(currentWorkingDirectory);

        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(1000));

        ftpLinkBar.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tooltip.setText(currentWorkingDirectory);
                Tooltip.install(ftpLinkBar, tooltip);
            }
        });

        ftpServer.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tooltip.setText(connectionFtpClient.getServer());
                Tooltip.install(ftpServer, tooltip);
            }
        });
    }

    /**
     * Affiche la liste des répertoires dans l'interface utilisateur.
     *
     * @param files Un tableau de fichiers FTP à afficher dans l'interface utilisateur.
     * @throws IOException Si une erreur survient lors du chargement de l'interface utilisateur du document.
     */
    public void directoryList(FTPFile[] files) throws IOException {

        for (int i = 0; i < files.length; i++) {
            FTPFile currentFile = files[i];
            String name = currentFile.getName();

            // Exclure les dossiers "." et ".."
            if (!name.equals(".") && !name.equals("..")) {
                HBox containerItemDoc;
                System.out.println(name);
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/Vue/DocumentItem.fxml"));
                HBox hBox = fxmlLoader.load();
                documentItemController = fxmlLoader.getController();

                containerItemDoc = documentItemController.getItemDoc();

                selectableHbox.createSelectableHBox(containerItemDoc, files, i);
                contextMenu(hBox, files, i);

                if (currentFile.isDirectory()) {
                    addDirectoryItemToUI(hBox, name, imageDirectory, documentItemController);
                }
                if (currentFile.isSymbolicLink()) {
                    addDirectoryItemToUI(hBox, name, imageSymlink, documentItemController);
                }
            }
        }
    }


    /**
     * Ajoute les items répertoire à l'interface utilisateur.
     *
     * @param hBox                   Le conteneur HBox représentant un item répertoire.
     * @param name                   Le nom du répertoire.
     * @param image                  L'image représentant le répertoire.
     * @param documentItemController Le contrôleur de l'élément de document.
     */
    private void addDirectoryItemToUI(HBox hBox, String name, Image image, DocumentItemController documentItemController) {
        documentItemController.setDocName(name);
        documentItemController.setDocIcon(image);
        containerDocument.getChildren().add(hBox);
    }

    /**
     * Définit les sous-répertoires et Ajoute les item sous-répertoires dans l'interface utilisateur.
     *
     * @param files Un tableau de fichiers FTP représentant les sous-répertoires.
     * @throws IOException Si une erreur survient lors du chargement de l'interface utilisateur du sous-répertoire.
     */
    private void setChildDirectory(FTPFile[] files) throws IOException {
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                FTPFile currentFile = files[i];
                String name = currentFile.getName();

                // Exclure les dossiers "." et ".."
                if (!name.equals(".") && !name.equals("..")) {
                    HBox containerDocChildItem;
                    FXMLLoader fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("/Vue/DocChildItem.fxml"));
                    HBox hBox = fxmlLoader.load();
                    docChildItemController = fxmlLoader.getController();

                    containerDocChildItem = docChildItemController.getContainerDocChildItem();
                    selectableHbox.createSelectableHBox(containerDocChildItem, files, i);

                    DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy  HH:mm");
                    Date date = new Date(currentFile.getTimestamp().getTimeInMillis());


                    long size = currentFile.getSize();

                    contextMenu(hBox, files, i);

                    if (currentFile.isDirectory()) {
                        addChildDirectoryItemToUI(hBox, name, dateFormat, imageDirectory, docChildItemController, date);
                    } else if (currentFile.isFile()) {
                        addChildFileItemToUI(hBox, name, dateFormat, imageFile, docChildItemController, date, size);
                    } else if (currentFile.isSymbolicLink()) {
                        addChildFileItemToUI(hBox, name, dateFormat, imageSymlink, docChildItemController, date, size);
                    }
                }
            }
        }
    }


    /**
     * Ajoute un élément représentant un répertoire dans l'interface utilisateur.
     *
     * @param hBox                   Le conteneur HBox représentant l'élément de répertoire.
     * @param name                   Le nom du répertoire.
     * @param dateFormat             Le format de date à utiliser pour afficher la date de modification.
     * @param image                  L'image représentant le répertoire.
     * @param docChildItemController Le contrôleur de l'élément de répertoire.
     * @param date                   La date de modification du répertoire.
     */
    private void addChildDirectoryItemToUI(HBox hBox, String name, DateFormat dateFormat, Image image, DocChildItemController docChildItemController, Date date) {
        docChildItemController.setDocChildItemName(name);
        docChildItemController.setDocChildItemDate(dateFormat.format(date));
        docChildItemController.setDocChildItemIcon(image);
        containerDocChild.getChildren().add(hBox);
    }

    /**
     * Ajoute un élément représentant un fichier dans l'interface utilisateur.
     *
     * @param hBox                   Le conteneur HBox représentant l'élément de fichier.
     * @param name                   Le nom du fichier.
     * @param dateFormat             Le format de date à utiliser pour afficher la date de modification.
     * @param image                  L'image représentant le fichier.
     * @param docChildItemController Le contrôleur de l'élément de fichier.
     * @param date                   La date de modification du fichier.
     * @param size                   La taille du fichier.
     */
    private void addChildFileItemToUI(HBox hBox, String name, DateFormat dateFormat, Image image, DocChildItemController docChildItemController, Date date, long size) {
        docChildItemController.setDocChildItemName(name);
        docChildItemController.setDocChildItemDate(dateFormat.format(date));
        docChildItemController.setDocChildItemIcon(image);
        docChildItemController.setDocChildItemSize(formatBytes(size));
        containerDocChild.getChildren().add(hBox);
    }


    /**
     * Change le répertoire de travail et actualise la vue.
     *
     * @param name Le nom du répertoire à changer.
     * @throws IOException Si une erreur survient lors du changement de répertoire ou de l'actualisation de la vue.
     */
    public void changeAndRefreshDirectory(String name) throws IOException {
        // Changer le répertoire de travail
        String originalDirectoryPath = "/";
        String newPath = currentWorkingDirectory + originalDirectoryPath + name;

        if (!connectionFtpClient.getFtpClient().printWorkingDirectory().equals(newPath)) {
            if (!connectionFtpClient.getFtpClient().changeWorkingDirectory(newPath)) {
                connectionFtpClient.getFtpClient().changeWorkingDirectory(originalDirectoryPath + name);
            }
        }
        setFtpLink();
        updateFileListAndUI();

    }

    /**
     * Met à jour la liste des fichiers à partir du serveur FTP et actualise l'interface utilisateur.
     *
     * @throws IOException Si une erreur survient lors de la mise à jour de la liste des fichiers ou de l'actualisation de l'interface utilisateur.
     */
    public void updateFileListAndUI() throws IOException {
        // Mettre à jour la liste des fichiers
        files = connectionFtpClient.getFtpClient().listFiles();
        updateUI();
    }

    /**
     * Actualise l'interface utilisateur en supprimant les éléments existants et en mettant à jour la liste des répertoires.
     *
     * @throws IOException Si une erreur survient lors de l'actualisation de l'interface utilisateur.
     */
    private void updateUI() throws IOException {
        containerDocChild.getChildren().clear();
        containerDocument.getChildren().clear();

        setChildDirectory(files);

        List<FTPFile> arrayDirectory = new ArrayList<>();

        for (FTPFile file : files) {
            if (file.isDirectory()) {
                arrayDirectory.add(file);
            }
        }
        FTPFile[] ftpDirectory = arrayDirectory.toArray(new FTPFile[0]);

        directoryList(ftpDirectory);


    }


    /**
     * Actualise la connexion FTP et rafraîchit l'interface utilisateur.
     *
     * @throws Exception Si une erreur survient lors de la déconnexion, de la reconnexion ou de l'actualisation de la liste des fichiers et de l'interface utilisateur.
     */
    @FXML
    private void ftpRefresh() throws Exception {

        connectionFtpClient.getFtpClient().logout();
        connectionFtpClient.getFtpClient().disconnect();
        connectionFtpClient.getFtpClient().connect(server, port);
        connectionFtpClient.getFtpClient().login(id, password);
        connectionFtpClient.getFtpClient().enterLocalPassiveMode();
        connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
        updateFileListAndUI();
        setFtpLink();


    }


    /**
     * Formatte la taille en octets en une représentation lisible.
     *
     * @param bytes La taille en octets.
     * @return Une chaîne représentant la taille formatée.
     */
    public String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " octets";
        } else if (bytes < 1024 * 1024) {
            double kilobytes = (double) bytes / 1024.0;
            return String.format("%.2f Ko", kilobytes);
        } else if (bytes < 1024 * 1024 * 1024) {
            double megabytes = (double) bytes / (1024.0 * 1024.0);
            return String.format("%.2f Mo", megabytes);
        } else {
            double gigabytes = (double) bytes / (1024.0 * 1024.0 * 1024.0);
            return String.format("%.2f Go", gigabytes);
        }
    }


    /**
     * Gère le retour en arrière dans la hiérarchie des répertoires FTP lors d'un clic de souris.
     *
     * @param event L'événement de souris associé au clic.
     * @throws IOException Si une erreur survient lors du changement de répertoire ou de l'actualisation de l'interface utilisateur.
     */
    @FXML
    private void ftpReturn(MouseEvent event) throws IOException {
        MouseButton button = event.getButton();

        if (button == MouseButton.PRIMARY && (event.getClickCount()) == 1) {
            changeAndRefreshDirectory(String.valueOf(connectionFtpClient.getFtpClient().changeToParentDirectory()));
        }


    }


    /**
     * Gère le menu contextuel lors du clic droit sur un élément de l'interface utilisateur.
     *
     * @param hBox     Le conteneur HBox associé à l'élément.
     * @param files    Un tableau de fichiers FTP.
     * @param iterator L'indice de l'élément dans le tableau.
     */
    private void contextMenu(HBox hBox, FTPFile[] files, int iterator) {

        ContextMenu contextMenu = new ContextMenu();


        menuItemDelete = new MenuItem("Supprimer");
        menuItemDownload = new MenuItem("Télécharger");
        menuItemRename = new MenuItem("Renommer");

        contextMenu.getItems().addAll(menuItemRename, menuItemDelete, menuItemDownload);

        menuItemDelete.setDisable(handleAction);

        menuItemDelete.setOnAction(event -> handleDeleteAction(files, iterator));
        menuItemRename.setOnAction(event -> handleRenameAction(hBox, files, iterator));
        menuItemDownload.setOnAction(event -> handleDownloadAction(files, iterator));


        hBox.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(hBox, event.getScreenX(), event.getScreenY());
            }
        });

    }

    /**
     * Gère l'action de téléchargement d'un fichier ou d'un répertoire depuis le serveur FTP.
     *
     * @param files    Un tableau de fichiers FTP.
     * @param iterator L'indice de l'élément dans le tableau.
     */
    private void handleDownloadAction(FTPFile[] files, int iterator) {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(containerDocument.getScene().getWindow());

            if (selectedDirectory == null) {
                return;
            }


            String fileName = files[iterator].getName();
            String remotePath = currentWorkingDirectory.equals("/") ? currentWorkingDirectory + fileName : currentWorkingDirectory + "/" + fileName;
            String localPath = selectedDirectory.getAbsolutePath() + "/" + fileName;

            for (File file : Objects.requireNonNull(selectedDirectory.listFiles())) {
                if (file.getAbsolutePath().equals(localPath)) {
                    showDialogueBox("/Vue/MessageBoxFileExist.fxml");
                    return;
                }
            }

            progressBarFtp.setProgress(0.0);

            if (files[iterator].isFile()) {
                ftpDownloader.downloadFileAsync(remotePath, localPath, progressBarFtp, popupDownload, dataLogins);
            } else if (files[iterator].isDirectory()) {

                ftpDownloader.downloadDirectoryAsync(remotePath, localPath, progressBarFtp, popupDownload, dataLogins);
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    boolean isEnter = false;

    private void handleRenameAction(HBox hBox, FTPFile[] files, int iterator) {
        Label label = (Label) hBox.getChildren().get(1);
        TextField renameTextField = new TextField();
        renameTextField.setText(label.getText());
        renameTextField.selectRange(0, renameTextField.getText().length());
        hBox.getChildren().set(1, renameTextField);
        renameTextField.setPrefWidth(label.getPrefWidth());
        renameTextField.requestFocus();

        String fileName = null;

        // Vérifier si l'input correspond au nom d'un fichier existant (et pas à l'ancien nom)
        for (FTPFile file : files) {
            fileName = file.getName();
            if (renameTextField.getText().equals(file.getName())) {
                break;
            }
        }


        String finalFileName = fileName;
        renameTextField.setOnAction(textFieldValidate -> {
            isEnter = true;
            if (!renameTextField.getText().equals(finalFileName)) {
                renameAndCheckDuplicateAndUpdate(hBox, files, iterator, label, renameTextField);

            } else {
                label.setText(files[iterator].getName());
                hBox.getChildren().set(1, label);
            }

        });

        renameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                if (!isEnter && !renameTextField.getText().equals(finalFileName)) {
                    renameAndCheckDuplicateAndUpdate(hBox, files, iterator, label, renameTextField);
                } else {
                    label.setText(files[iterator].getName());
                    hBox.getChildren().set(1, label);
                }
                isEnter = false;
            } else {
                label.setText(files[iterator].getName());
                hBox.getChildren().set(1, label);
                isEnter = true;
            }


        });
    }

    private boolean handleAction = false;

    /**
     * Gère l'action de suppression d'un fichier ou d'un répertoire depuis le serveur FTP.
     *
     * @param files    Un tableau de fichiers FTP.
     * @param iterator L'indice de l'élément dans le tableau.
     */
    private void handleDeleteAction(FTPFile[] files, int iterator) {
        String fileToDelete = currentWorkingDirectory + "/" + files[iterator].getName();
        MessageBoxController messageBoxController;

        try {
            messageBoxController = showDialogueBox("/Vue/MessageBox.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (messageBoxController.isConfirmClicked()) {
            if (files[iterator].isFile()) {
                ftpDeleteFile.deleteFileAsync(fileToDelete, popupDelete, this, dataLogins);
            } else if (files[iterator].isDirectory() || files[iterator].isSymbolicLink()) {
                handleAction = true;
                ftpDeleteFile.deleteDirectoryAsync(fileToDelete, popupDelete, this, dataLogins);
            }

            try {
                updateFileListAndUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        handleAction = false;
    }


    /**
     * Renomme l'élément sélectionné, vérifie s'il existe déjà un élément avec le même nom,
     * et met à jour l'interface utilisateur en conséquence.
     *
     * @param hBox            Le conteneur HBox associé à l'élément.
     * @param files           Un tableau de fichiers FTP.
     * @param iterator        L'indice de l'élément dans le tableau.
     * @param label           Le label de l'élément.
     * @param renameTextField Le champ de texte contenant le nouveau nom.
     */
    private void renameAndCheckDuplicateAndUpdate(HBox hBox, FTPFile[] files, int iterator, Label label, TextField renameTextField) {
        String input = renameTextField.getText();

        if (input.isBlank()) {
            label.setText(files[iterator].getName());
            hBox.getChildren().set(1, label);
            return;
        }

        for (FTPFile file : files) {
            String fileName = file.getName();
            if (input.equals(fileName) && !input.equals(label.getText())) {
                Platform.runLater(() -> {
                    try {
                        showDialogueBox("/Vue/MessageBoxNameExist.fxml");
                        label.setText(files[iterator].getName());
                        hBox.getChildren().set(1, label);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }
        }

        if (validateInput(input)) {
            String oldFile = currentWorkingDirectory + "/" + files[iterator].getName();
            String newFile = currentWorkingDirectory + "/" + input;
            try {
                connectionFtpClient.getFtpClient().rename(oldFile, newFile);
                updateFileListAndUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Valide l'entrée utilisateur en vérifiant si elle n'est pas vide après suppression des espaces inutiles.
     *
     * @param input L'entrée utilisateur à valider.
     * @return true si l'entrée est valide, sinon false.
     */
    private boolean validateInput(String input) {
        return !input.trim().isEmpty();
    }

    /**
     * Gère l'action de création d'un nouveau fichier sur le serveur FTP.
     *
     * @param event L'événement de souris associé à l'action.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    @FXML
    private void ftpMakeFile(MouseEvent event) throws IOException {
        handleFtpAction(event, ActionType.MAKE_FILE);
    }

    /**
     * Gère l'action de création d'un nouveau répertoire sur le serveur FTP.
     *
     * @param event L'événement de souris associé à l'action.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    @FXML
    private void ftpMkdir(MouseEvent event) throws IOException {
        handleFtpAction(event, ActionType.MAKE_DIRECTORY);
    }


    /**
     * Gère l'action de création d'un nouveau fichier ou d'un répertoire sur le serveur FTP en fonction du type d'action spécifié.
     *
     * @param event      L'événement de souris associé à l'action.
     * @param actionType Le type d'action à effectuer (MAKE_FILE pour créer un fichier, MAKE_DIRECTORY pour créer un répertoire).
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    @FXML
    private void handleFtpAction(MouseEvent event, ActionType actionType) throws IOException {
        MouseButton button = event.getButton();

        if (button != MouseButton.PRIMARY || event.getClickCount() != 1) {
            return;
        }

        MkFileNameBoxController mkFileNameBoxController = showDialogueBox("/Vue/MkFileNameBox.fxml");
        String input = mkFileNameBoxController.getMkFileNameTextField().getText();

        if (input.isBlank()) {
            return;
        }

        String remotePath = currentWorkingDirectory + "/" + input;

        for (FTPFile file : files) {
            if (input.equals(file.getName())) {
                showDialogueBox("/Vue/MessageBoxNameExist.fxml");
                return;
            }
        }

        if (mkFileNameBoxController.isConfirmClicked()) {
            if (actionType == ActionType.MAKE_FILE) {
                connectionFtpClient.getFtpClient().storeFile(remotePath, new ByteArrayInputStream(new byte[0]));
            } else if (actionType == ActionType.MAKE_DIRECTORY) {
                connectionFtpClient.getFtpClient().makeDirectory(input);
            }
            updateFileListAndUI();
        }
    }

    // Enum pour représenter les actions FTP possibles
    /**
     * Représente les différentes actions disponibles pour les opérations FTP.
     * MAKE_FILE : Action pour créer un nouveau fichier.
     * MAKE_DIRECTORY : Action pour créer un nouveau répertoire.
     */
    private enum ActionType {
        MAKE_FILE,
        MAKE_DIRECTORY
    }

    /**
     * Gère l'événement de survol lors du glisser-déposer des fichiers.
     * Cette méthode accepte tous les modes de transfert si des fichiers sont glissés.
     *
     * @param event L'événement de survol associé.
     */
    @FXML
    void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }


    /**
     * Gère l'événement de glisser-déposer des fichiers pour les téléverser sur le serveur FTP.
     * Cette méthode vérifie les fichiers à téléverser, évite les doublons et commence le téléversement.
     *
     * @param event L'événement de glisser-déposer associé.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    @FXML
    void handleDragDrop(DragEvent event) throws IOException {
        if (!event.getDragboard().hasFiles()) {
            return;
        }

        List<File> filesToUpload = new ArrayList<>(event.getDragboard().getFiles());
        String remoteFile = currentWorkingDirectory;
        FTPFile[] existingFiles = connectionFtpClient.getFtpClient().listFiles(remoteFile);

        for (File file : new ArrayList<>(filesToUpload)) {
            for (FTPFile fileExisting : existingFiles) {
                if (fileExisting.getName().equals(file.getName())) {
                    showDialogueBox("/Vue/MessageBoxNameExist.fxml");
                    filesToUpload.remove(file);
                    break;
                }
            }
        }

        List<File> filesToUploadDagDrop = new ArrayList<>();
        List<File> directoriesToUploadDagDrop = new ArrayList<>();

        for (File file : filesToUpload) {
            if (file.isFile()) {
                filesToUploadDagDrop.add(file);
            } else if (file.isDirectory()) {
                directoriesToUploadDagDrop.add(file);
            }
        }

        if (!filesToUploadDagDrop.isEmpty()) {

            ftpUploadFile.uploadFileAsync(filesToUploadDagDrop, currentWorkingDirectory, progressBarFtp, popupUpload, this, dataLogins);
        }

        if (!directoriesToUploadDagDrop.isEmpty()) {

            ftpUploadFile.uploadDirectoryAsync(directoriesToUploadDagDrop, currentWorkingDirectory, progressBarFtp, popupUpload, this, dataLogins);
        }

    }

    /**
     * Gère l'action de fermeture de l'application FTP.
     *
     * @param event L'événement associé à la fermeture de l'application.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    @FXML
    void ftpClose(ActionEvent event) throws IOException {
        System.exit(0);
    }


    /**
     * Gère l'action de déconnexion de l'utilisateur FTP.
     * Cette méthode ferme toutes les connexions FTP ouvertes et redirige l'utilisateur vers la page de connexion.
     *
     * @param event L'événement associé à la déconnexion de l'utilisateur.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de la redirection vers la page de connexion.
     */
    @FXML
    void ftpLogout(ActionEvent event) throws IOException {
        closeAllConnectionTtp();
        Stage currentStage = (Stage) containerDocument.getScene().getWindow();
        main.loginPage(currentStage);
    }




    /**
     * Ferme toutes les connexions FTP ouvertes.
     * Cette méthode boucle sur toutes les connexions FTP et les ferme en utilisant la méthode {@link #closeFtpClient(ConnectionFtpClient)}.
     *
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de la déconnexion des clients FTP.
     */
    public void closeAllConnectionTtp() throws IOException {
        closeFtpClient(connectionFtpClient);

        for (ConnectionFtpClient ftpClient : allConnectionFtp) {
            closeFtpClient(ftpClient);
        }
    }

    /**
     * Ferme le client FTP spécifié.
     * Cette méthode déconnecte le client FTP en appelant les méthodes logout() et disconnect().
     *
     * @param ftpClient Le client FTP à fermer.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de la déconnexion du client FTP.
     */
    private void closeFtpClient(ConnectionFtpClient ftpClient) throws IOException {
        if (ftpClient != null) {
            ftpClient.getFtpClient().logout();
            ftpClient.getFtpClient().disconnect();
        }
    }


    /**
     * Appelé lorsque l'action est terminée.
     * Cette méthode met à jour la liste des fichiers et l'interface utilisateur sur le thread de l'interface graphique.
     */
    @Override
    public void onActionComplete() {
        Platform.runLater(() -> {

            try {
                updateFileListAndUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }

    /**
     * Appelé lorsque des éléments sont supprimés.
     * Cette méthode met à jour les compteurs de fichiers et de dossiers supprimés, puis déclenche une animation de popup sur le thread de l'interface graphique.
     *
     * @param fichier Le nombre de fichiers supprimés.
     * @param dossier Le nombre de dossiers supprimés.
     */
    @Override
    public void elementsDeleted(int fichier, int dossier) {
        Platform.runLater(() -> {

                numFileDelete.setText(String.valueOf(fichier));
                numDirDelete.setText(String.valueOf(dossier));
                getTimeline(popupDirDelete).play();

        });
    }

    /**
     * Active les éléments du menu.
     * Cette méthode active les options de suppression, de téléchargement et de renommage dans le menu sur le thread de l'interface graphique.
     */
    @Override
    public void menuItemEnable() {
        Platform.runLater(() -> {
            menuItemDelete.setDisable(false);
            menuItemDownload.setDisable(false);
            menuItemRename.setDisable(false);
        });
    }


    /**
     * Affiche une boîte de dialogue à partir d'un fichier FXML spécifié.
     * Cette méthode charge le fichier FXML, configure le contrôleur, puis affiche la boîte de dialogue modale.
     *
     * @param fxmlFile Le chemin vers le fichier FXML de la boîte de dialogue.
     * @param <T> Le type du contrôleur de la boîte de dialogue.
     * @return Le contrôleur de la boîte de dialogue.
     * @throws IOException Si une erreur d'entrée-sortie se produit lors du chargement du fichier FXML.
     */
    private <T> T showDialogueBox(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        T controller = loader.getController();


        Stage stage = null;
        if (controller instanceof MessageBoxController messageBoxController) {
            stage = messageBoxController.getDialogStage();
        }

        if (stage != null) {
            stage.initOwner(containerDocument.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.setResizable(false);

            stage.showAndWait();

        }


        return controller;
    }

    /**
     * Crée et retourne une timeline qui rend visible le conteneur spécifié pendant 5 secondes, puis le rend invisible.
     *
     * @param container Le conteneur à rendre visible pendant un certain temps.
     * @return Une timeline qui contrôle la visibilité du conteneur.
     */
    public static Timeline getTimeline(Parent container) {
        container.setVisible(true);
        Duration delay = Duration.seconds(5);
        KeyFrame keyFrame = new KeyFrame(delay, event1 -> {
            container.setVisible(false);

        });
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(1);


        return timeline;
    }


}









