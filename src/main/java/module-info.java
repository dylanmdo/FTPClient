module org.openjfx.ftpclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.net;
    requires atlantafx.base;
    requires javafx.graphics;


    opens org.openjfx.ftpclient to javafx.fxml;
    opens org.openjfx.ftpclient.Controller;
    opens org.openjfx.ftpclient.Model;
    exports org.openjfx.ftpclient;
    exports org.openjfx.ftpclient.Controller;
    exports org.openjfx.ftpclient.Model;

}