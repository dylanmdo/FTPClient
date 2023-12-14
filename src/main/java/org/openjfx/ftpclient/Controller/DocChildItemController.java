package org.openjfx.ftpclient.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class DocChildItemController {

    @FXML
    private Label DocChildItemSize;

    @FXML
    private Label DocChildItemDate;

    @FXML
    private ImageView DocChildItemIcon;

    @FXML
    private Label DocChildItemName;

    @FXML
    private HBox containerDocChildItem;



    public HBox getContainerDocChildItem() {
        return containerDocChildItem;
    }

    public void setDocChildItemDate(String date) {
        DocChildItemDate.setText(date);
    }

    public void setDocChildItemIcon(Image image) {
        DocChildItemIcon.setImage(image);
    }

    public void setDocChildItemName(String name) {
        DocChildItemName.setText(name);
    }

    public void setDocChildItemSize(String size) {
        DocChildItemSize.setText(size);
    }

}
