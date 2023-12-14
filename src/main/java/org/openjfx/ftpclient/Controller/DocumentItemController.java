package org.openjfx.ftpclient.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class DocumentItemController {

    @FXML
    public ImageView DocIcon;
    @FXML
    private Label DocName;
    @FXML
    private HBox ItemDoc;


    public void setDocName(String name) {
        DocName.setText(name);
    }

    public void setDocIcon(Image image) {
        DocIcon.setImage(image);
    }

    public HBox getItemDoc() {
        return ItemDoc;
    }


}






