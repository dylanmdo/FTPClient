package org.openjfx.ftpclient.Model;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.PrintWriter;
import java.util.ArrayList;


public class ConnectionFtpClient {
     private final String server;
     private final int port;
     private final String user;
    private final String password;
    private FTPClient ftpClient;

    public static ArrayList<ConnectionFtpClient> allConnectionFtp = new ArrayList<>();


    public String getServer() {
        return server;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }



    // constructor
    public ConnectionFtpClient(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }




    public void connectionLoginServer() throws Exception {
        ftpClient = new FTPClient();

        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));


        ftpClient.connect(server, port);
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
        } else {
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();

        }


    }


}


