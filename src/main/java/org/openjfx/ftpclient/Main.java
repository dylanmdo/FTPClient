package org.openjfx.ftpclient;

import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe principale de l'application qui étend la classe Application de JavaFX.
 */
public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("File Transfer");

        // Charger le style d'interface utilisateur "Cupertino Light"
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        loginPage(primaryStage);


    }



    /**
     * Affiche la page de connexion dans la fenêtre principale.
     * @param primaryStage La fenêtre principale de l'application.
     * @throws IOException Si une erreur survient lors du chargement de la page de connexion.
     */
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
