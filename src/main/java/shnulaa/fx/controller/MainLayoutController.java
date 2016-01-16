package shnulaa.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MainLayoutController {

    @FXML
    private TextField textField;

    @FXML
    private Label label;

    @FXML
    private Button button;

    @FXML
    private void initialize() {
        System.out.println("initialize");
    }

    @FXML
    private void handleSubmit() {
        System.out.println("handleSubmit textField£º" + textField.getText());
    }

}
