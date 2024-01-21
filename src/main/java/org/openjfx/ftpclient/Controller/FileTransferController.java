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
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import javafx.util.Duration;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.openjfx.ftpclient.Main;
import org.openjfx.ftpclient.Model.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.openjfx.ftpclient.Model.ConnectionFtpClient.allConnectionFtp;

public class FileTransferController implements FtpUploadFile.UploadCallback {


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

    public ConnectionFtpClient connectionFtpClient;


    private String server;
    private int port;
    private String id;
    private String password;


    private final Image imageFile = new Image(getClass().getResource("/assets/file.png").toExternalForm());
    private final Image imageDirectory = new Image(getClass().getResource("/assets/directory.png").toExternalForm());
    private final Image imageSymlink = new Image(getClass().getResource("/assets/symlink.png").toExternalForm());

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

    Main main = new Main();



    public FileTransferController() throws Exception {
    }


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


    public void setParentDocument(FTPFile[] files) throws IOException {

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
                    addDocumentItemToUI(hBox, name, imageDirectory, documentItemController);
                }
                if (currentFile.isSymbolicLink()) {
                    addDocumentItemToUI(hBox, name, imageSymlink, documentItemController);
                }
            }
        }
    }




    // Méthode pour ajouter Les dossiers Parent à l'interface utilisateur (Partie : Parent Document)
    private void addDocumentItemToUI(HBox hBox, String name, Image image, DocumentItemController documentItemController) {
        documentItemController.setDocName(name);
        documentItemController.setDocIcon(image);
        containerDocument.getChildren().add(hBox);
    }

    private void setChildDocument(FTPFile[] files) throws IOException {
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
                        addChildDocumentItemToUI(hBox, name, dateFormat, imageDirectory, docChildItemController, date);
                    } else if (currentFile.isFile()) {
                        addChildDocumentItemToUI(hBox, name, dateFormat, imageFile, docChildItemController, date, size);
                    } else if (currentFile.isSymbolicLink()) {
                        addChildDocumentItemToUI(hBox, name, dateFormat, imageSymlink, docChildItemController, date, size);
                    }
                }
            }
        }
    }


    //Surcharge de la methode addChildDocumentItemToUI()
    private void addChildDocumentItemToUI(HBox hBox, String name, DateFormat dateFormat, Image image, DocChildItemController docChildItemController, Date date) {
        docChildItemController.setDocChildItemName(name);
        docChildItemController.setDocChildItemDate(dateFormat.format(date));
        docChildItemController.setDocChildItemIcon(image);
        containerDocChild.getChildren().add(hBox);
    }

    // Méthode pour ajouter des éléments des répertoires Parent à l'interface utilisateur (Partie : Child Document)
    private void addChildDocumentItemToUI(HBox hBox, String name, DateFormat dateFormat, Image image, DocChildItemController docChildItemController, Date date, long size) {
        docChildItemController.setDocChildItemName(name);
        docChildItemController.setDocChildItemDate(dateFormat.format(date));
        docChildItemController.setDocChildItemIcon(image);
        docChildItemController.setDocChildItemSize(formatBytes(size));
        containerDocChild.getChildren().add(hBox);
    }


    public void changeAndRefreshDirectory(String name) throws IOException {
        // Changer le répertoire de travail
        String originalDirectoryPath = "/";
        String newPath = currentWorkingDirectory + originalDirectoryPath + name;

        if (!connectionFtpClient.getFtpClient().printWorkingDirectory().equals(newPath)) {
            if (!connectionFtpClient.getFtpClient().changeWorkingDirectory(newPath)) {
                connectionFtpClient.getFtpClient().changeWorkingDirectory(originalDirectoryPath + name);
            }
        }

        // Mettre à jour le lien FTP
        setFtpLink();

        // Mettre à jour la liste des fichiers
        updateFileListAndUI();

    }

    public void updateFileListAndUI() throws IOException {
        // Mettre à jour la liste des fichiers
        files = connectionFtpClient.getFtpClient().listFiles();
        updateUI();
    }


    private void updateUI() throws IOException {
        containerDocChild.getChildren().clear();
        containerDocument.getChildren().clear();

        setChildDocument(files);

        List<FTPFile> arrayDirectory = new ArrayList<>();

        for (FTPFile file : files) {
            if (file.isDirectory()) {
                arrayDirectory.add(file);
            }
        }
        FTPFile[] ftpDirectory = arrayDirectory.toArray(new FTPFile[0]);

        setParentDocument(ftpDirectory);


    }


    @FXML
    private void ftpRefresh() throws Exception {

        if (connectionFtpClient.getFtpClient().isConnected()){
            connectionFtpClient.getFtpClient().logout();
            connectionFtpClient.getFtpClient().disconnect();
        }
        connectionFtpClient.getFtpClient().connect(server, port);
        connectionFtpClient.getFtpClient().login(id, password);
        connectionFtpClient.getFtpClient().enterLocalPassiveMode();
        connectionFtpClient.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);

    }

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

    @FXML
    private void ftpReturn(MouseEvent event) throws IOException {
        MouseButton button = event.getButton();

        if (button == MouseButton.PRIMARY && (event.getClickCount()) == 1) {
            changeAndRefreshDirectory(String.valueOf(connectionFtpClient.getFtpClient().changeToParentDirectory()));
        }


    }

    boolean isEnter = false;

    @FXML
    private Pane popupDownload;
    @FXML
    private Pane popupUpload;
    @FXML
    private Pane popupDelete;


    private void contextMenu(HBox hBox, FTPFile[] files, int iterator) {

        ContextMenu contextMenu = new ContextMenu();


        MenuItem menuItemDelete = new MenuItem("Supprimer");
        MenuItem menuItemRename = new MenuItem("Renommer");
        MenuItem menuItemDownload = new MenuItem("Télécharger");
        contextMenu.getItems().addAll(menuItemRename, menuItemDelete, menuItemDownload);

        menuItemDelete.setOnAction(event -> handleDeleteAction(files, iterator));
        menuItemRename.setOnAction(event -> handleRenameAction(hBox, files, iterator));
        menuItemDownload.setOnAction(event -> handleDownloadAction(files, iterator));


        hBox.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                contextMenu.show(hBox, event.getScreenX(), event.getScreenY());
            }
        });

    }


    private void handleDownloadAction(FTPFile[] files, int iterator) {


        try {

            DirectoryChooser directoryChooser = new DirectoryChooser();

            File selectedDirectory = directoryChooser.showDialog(containerDocument.getScene().getWindow());

            if (files[iterator].isFile()) {
                if (selectedDirectory != null) {

                    String remoteFile;
                    if (currentWorkingDirectory.equals("/")) {
                        remoteFile = currentWorkingDirectory + files[iterator].getName();
                    } else {
                        remoteFile = currentWorkingDirectory + "/" + files[iterator].getName();
                    }
                    //Emplacement ou le fichier va etre téléchargé
                    String downloadFile = selectedDirectory.getAbsolutePath() + "/" + files[iterator].getName();

                    String fileName = null;


                    for (File file : Objects.requireNonNull(selectedDirectory.listFiles())) {
                        if (file.getAbsolutePath().equals(downloadFile)) {
                            fileName = file.getAbsolutePath();
                            System.out.println(fileName);
                            break;
                        }

                    }

                    if (!downloadFile.equals(fileName)) {

                        progressBarFtp.setProgress(0.0);
                        ftpDownloader.downloadFileAsync(remoteFile, downloadFile, progressBarFtp, popupDownload, dataLogins);

                        //-----BYPASS---//
                        //ftpDownloader.downloadFileAsync(remoteFile, downloadFile, progressBarFtp, popupDownload);


                    } else {
                        showDialogueBox("/Vue/MessageBoxFileExist.fxml");
                        //ftpDownloader.deleteCloseConnection(dataLogins);
                    }


                }
            } else if (files[iterator].isDirectory()) {
                String fileName = null;

                if (selectedDirectory != null) {
                    String remoteDir;
                    if (currentWorkingDirectory.equals("/")) {
                        remoteDir = currentWorkingDirectory + files[iterator].getName();
                    } else {
                        remoteDir = currentWorkingDirectory + "/" + files[iterator].getName();
                    }

                    String downloadDirPath = selectedDirectory.getAbsolutePath() + "/" + files[iterator].getName();

                    for (File file : Objects.requireNonNull(selectedDirectory.listFiles())) {
                        if (file.getAbsolutePath().equals(downloadDirPath)) {
                            fileName = file.getAbsolutePath();
                            System.out.println(fileName);
                            break;
                        }

                    }


                    if (!downloadDirPath.equals(fileName)) {

                        progressBarFtp.setProgress(0.0);

                        ftpDownloader.downloadDirectoryAsync(remoteDir, downloadDirPath, progressBarFtp, popupDownload, dataLogins);

                        //BYPASS//
                        //ftpDownloader.downloadDirectoryAsync(remoteDir, downloadDirPath, progressBarFtp, popupDownload).shutdown();


                    } else {
                        showDialogueBox("/Vue/MessageBoxFileExist.fxml");

                    }


                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


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

    private void handleDeleteAction(FTPFile[] files, int iterator) {
        MessageBoxController messageBoxController;
        String fileToDelete = currentWorkingDirectory + "/" + files[iterator].getName();
        String directoryToDelete = currentWorkingDirectory + "/" + files[iterator].getName();
        try {
            messageBoxController = showDialogueBox("/Vue/MessageBox.fxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (messageBoxController.isConfirmClicked()) {

            if (files[iterator].isFile()) {
                ftpDeleteFile.deleteFileAsync(fileToDelete, popupDelete, this, dataLogins);
                //----BYPASS-------//
                //ftpDeleteFile.deleteFileAsync(fileToDelete, popupDelete,this);


            } else if (files[iterator].isDirectory() || files[iterator].isSymbolicLink()) {
                ftpDeleteFile.deleteDirectoryAsync(directoryToDelete, popupDelete, this, dataLogins);
                //----BYPASS-------//
                //ftpDeleteFile.deleteDirectoryAsync(directoryToDelete, popupDelete,this);

            }

            try {
                updateFileListAndUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }


    private void renameAndCheckDuplicateAndUpdate(HBox hBox, FTPFile[] files, int iterator, Label label, TextField renameTextField) {
        String input = renameTextField.getText();
        boolean inputMatchesFileName = false;
        String fileName = null;

        // Vérifier si l'input correspond au nom d'un fichier existant (et pas à l'ancien nom)
        for (FTPFile file : files) {
            fileName = file.getName();
            if (input.equals(fileName) && !input.equals(label.getText())) {
                inputMatchesFileName = true;
                break;
            }
        }

        if (!input.equals(fileName) && !input.isBlank()) {
            // Valider et renommer le fichier si nécessaire
            if (validateInput(input)) {
                String oldFile = currentWorkingDirectory + "/" + files[iterator].getName();
                String newFile = currentWorkingDirectory + "/" + input;
                try {
                    connectionFtpClient.getFtpClient().rename(oldFile, newFile);
                    updateFileListAndUI();
                } catch (IOException e) {
                    // Gérer l'exception d'entrée/sortie ici (par exemple, enregistrer dans un fichier de journal)
                    e.printStackTrace();
                }
            }
        } else if (inputMatchesFileName) {
            Platform.runLater(() -> {
                try {
                    showDialogueBox("/Vue/MessageBoxNameExist.fxml");
                    label.setText(files[iterator].getName());
                    hBox.getChildren().set(1, label);
                } catch (IOException e) {
                    // Gérer l'exception d'entrée/sortie ici (par exemple, enregistrer dans un fichier de journal)
                    e.printStackTrace();
                }
            });
        } else {
            label.setText(files[iterator].getName());
            hBox.getChildren().set(1, label);
        }
    }


    private boolean validateInput(String input) {
        return !input.trim().isEmpty();
    }

    @FXML
    private void ftpMakeFile(MouseEvent event) throws IOException {
        MouseButton button = event.getButton();

        MkFileNameBoxController mkFileNameBoxController = showDialogueBox("/Vue/MkFileNameBox.fxml");

        String input = mkFileNameBoxController.getMkFileNameTextField().getText();
        String remoteFilePath = currentWorkingDirectory + "/" + input;
        String fileName = null;

        if (button == MouseButton.PRIMARY && (event.getClickCount()) == 1) {

            boolean inputMatchesFileName = false; // Variable pour vérifier si l'input correspond à fileName

            for (FTPFile file : files) {

                fileName = file.getName();
                if (input.equals(fileName)) {
                    inputMatchesFileName = true;
                    break;
                }

            }

            if (!input.equals(fileName) && !input.isBlank()) {
                if (mkFileNameBoxController.isConfirmClicked()) {
                    connectionFtpClient.getFtpClient().storeFile(remoteFilePath, new ByteArrayInputStream(new byte[0]));
                    updateFileListAndUI();
                }
            } else if (inputMatchesFileName) {
                showDialogueBox("/Vue/MessageBoxNameExist.fxml");
            }


        }

    }

    @FXML
    private void ftpMkdir(MouseEvent event) throws IOException {
        MouseButton button = event.getButton();
        MkFileNameBoxController mkFileNameBoxController = showDialogueBox("/Vue/MkFileNameBox.fxml");
        String input = mkFileNameBoxController.getMkFileNameTextField().getText();
        String directoryName = null;
        if (button == MouseButton.PRIMARY && (event.getClickCount()) == 1) {

            boolean inputMatchesFileName = false; // Variable pour vérifier si l'input correspond à fileName

            for (FTPFile file : files) {

                directoryName = file.getName();
                if (input.equals(directoryName)) {
                    inputMatchesFileName = true;
                    break;
                }
            }

            if (!input.equals(directoryName) && !input.isBlank()) {
                if (mkFileNameBoxController.isConfirmClicked()) {
                    connectionFtpClient.getFtpClient().makeDirectory(input);
                    updateFileListAndUI();
                }
            } else if (inputMatchesFileName) {
                showDialogueBox("/Vue/MessageBoxNameExist.fxml");
            }

        }


    }

    FtpUploadFile ftpUploadFile = new FtpUploadFile();


    @FXML
    void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    void handleDragDrop(DragEvent event) throws IOException {
        List<File> filesDragDrop = event.getDragboard().getFiles();
        List<File> filesToUpload = new ArrayList<>(filesDragDrop); // Créez une copie de la liste d'origine

        String remoteFile = currentWorkingDirectory;
        FTPFile[] existingFiles = connectionFtpClient.getFtpClient().listFiles(remoteFile);

        for (File file : filesDragDrop) {
            for (FTPFile fileExisting : existingFiles) {
                if (fileExisting.getName().equals(file.getName())) {
                    showDialogueBox("/Vue/MessageBoxNameExist.fxml");
                    filesToUpload.remove(file); // Supprime l'élément en question de filesToUpload
                    break;
                }
            }
        }

        List<File> filesDagDropToUpload = new ArrayList<>();
        List<File> DirDagDropToUpload = new ArrayList<>();

        for (File file : filesToUpload) {
            if (file.isFile()) {
                filesDagDropToUpload.add(file);
            } else if (file.isDirectory()) {
                DirDagDropToUpload.add(file);
            }
        }


        try {
            if (!filesDagDropToUpload.isEmpty()) {
                //ftpUploadFile.uploadFileAsync(filesDagDropToUpload, currentWorkingDirectory, progressBarFtp, popupUpload, this);
                ftpUploadFile.uploadFileAsync(filesDagDropToUpload, currentWorkingDirectory, progressBarFtp, popupUpload, this, dataLogins);
            }

            if (!DirDagDropToUpload.isEmpty()) {
                //ftpUploadFile.uploadDirectoryAsync(DirDagDropToUpload, currentWorkingDirectory, progressBarFtp, popupUpload, this);
                ftpUploadFile.uploadDirectoryAsync(DirDagDropToUpload, currentWorkingDirectory, progressBarFtp, popupUpload, this, dataLogins);
            }
        } catch (Exception e) {
            ////AJOUTER MESSAGE ERREUR
            System.out.println(e.getMessage());
        }


    }

    @FXML
    void ftpClose(ActionEvent event) throws IOException {
        closeAllConnectionTtp();

        System.exit(0);
    }


    @FXML
    void ftpLogout(ActionEvent event) throws IOException {
        Stage primaryStage = new Stage();
        Stage currentStage = (Stage) containerDocument.getScene().getWindow();

        closeAllConnectionTtp();

        currentStage.close();
        main.loginPage(primaryStage);
    }

    public void closeAllConnectionTtp() throws IOException {
        //Fermeture de la connexion du thread principal
        connectionFtpClient.getFtpClient().logout();
        connectionFtpClient.getFtpClient().disconnect();

        //Fermeture de la connexion du thread asynchrone
        if (!allConnectionFtp.isEmpty()){
            for (ConnectionFtpClient ftpClient : allConnectionFtp) {
                ftpClient.getFtpClient().logout();
                ftpClient.getFtpClient().disconnect();
            }
        }
    }




    @Override
    public void onUploadComplete() {
        // Mettez ici le code que vous voulez exécuter après la fin de l'upload
        Platform.runLater(() -> {
            try {
                updateFileListAndUI();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


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









