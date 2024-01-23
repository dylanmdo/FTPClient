package org.openjfx.ftpclient.Model;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * La classe ConnectionFtpClient représente une connexion FTP à un serveur.
 * Elle utilise la bibliothèque Apache Commons Net pour la communication FTP.
 */
public class ConnectionFtpClient {

    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private FTPClient ftpClient;

    /**
     * Liste statique contenant toutes les instances de ConnectionFtpClient créées.
     */
    public static ArrayList<ConnectionFtpClient> allConnectionFtp = new ArrayList<>();

    /**
     * Constructeur de la classe ConnectionFtpClient.
     *
     * @param server   Le nom du serveur FTP.
     * @param port     Le numéro de port du serveur FTP.
     * @param user     Le nom d'utilisateur pour la connexion FTP.
     * @param password Le mot de passe pour la connexion FTP.
     */
    public ConnectionFtpClient(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    /**
     * Renvoie le nom du serveur FTP.
     *
     * @return Le nom du serveur FTP.
     */
    public String getServer() {
        return server;
    }

    /**
     * Renvoie l'objet FTPClient associé à la connexion.
     *
     * @return L'objet FTPClient.
     */
    public FTPClient getFtpClient() {
        return ftpClient;
    }

    /**
     * Établit la connexion et effectue l'authentification sur le serveur FTP.
     *
     * @throws Exception Si une erreur survient lors de la connexion ou de l'authentification.
     */
    public void connectionLoginServer() throws Exception {
        ftpClient = new FTPClient();

        // Ajoute un écouteur pour imprimer les commandes FTP dans la console.
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftpClient.connect(server, port);
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            // Déconnecte si la réponse n'est pas positive.
            ftpClient.disconnect();
        } else {
            // Effectue l'authentification et configure le mode passif.
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
        }
    }
}
