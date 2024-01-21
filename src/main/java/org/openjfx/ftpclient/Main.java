package org.openjfx.ftpclient;

import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    private Stage primaryStage;



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("File Transfer");

        // Charger le style d'interface utilisateur "Cupertino Light"
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        //Chargement de la page de connexion
        loginPage(primaryStage);


    }




    public void loginPage(Stage primaryStage) throws IOException {
        // Configurer la scène avec la vue chargée et afficher la fenêtre principale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Vue/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

    }




}
