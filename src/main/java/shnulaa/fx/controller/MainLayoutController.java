package shnulaa.fx.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.nio.LocalNioServer;
import shnulaa.fx.nio.NioServerBase;

@SuppressWarnings("restriction")
public class MainLayoutController {

    private static Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    @FXML
    private TextField listenPort;

    @FXML
    private Label listenLab;

    @FXML
    private Button listen;

    @FXML
    private Button stop;

    @FXML
    private Button clear;

    @FXML
    private TextArea listenArea;

    private ExecutorService service;

    private NioServerBase base;

    @FXML
    private void initialize() {
        System.out.println("initialize");
        listenPort.setText("1234");
        listen.setDisable(false);
        stop.setDisable(true);
    }

    @FXML
    private void handleListen() {
        String portText = listenPort.getText();
        if (StringUtils.isEmpty(portText)) {
            showAlert(Constant.TITLE, "Invalid Port", Alert.AlertType.ERROR);
            return;
        }

        try {
            int port = Integer.valueOf(portText);
            if (port <= 0 || port > 65535) {
                showAlert(Constant.TITLE, "Port outOf range", Alert.AlertType.ERROR);
                return;
            }

            service = Executors.newSingleThreadExecutor();
            base = new LocalNioServer(listenArea, port);
            service.execute(base);

            listen.setDisable(true);
            stop.setDisable(false);

        } catch (Exception ex) {
            if (ex instanceof NumberFormatException) {
                showAlert(Constant.TITLE, "Port is not number..", Alert.AlertType.ERROR);
            } else {
                showAlert(Constant.TITLE, ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleStop() {
        try {
            if (base != null) {
                base.stop();
            }

            if (service != null) {
                service.shutdown();
            }
            listen.setDisable(false);
            stop.setDisable(true);

        } catch (Exception ex) {
            log.error("Exception occurred when handleStop", ex);
        } finally {
        }
    }

    @FXML
    private void handleClear() {
        if (listenArea != null) {
            listenArea.clear();
        }
    }

    /**
     * 
     * @param title
     * @param message
     * @param type
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(type.name());
        a.setResizable(false);
        a.setContentText(message);
        a.showAndWait();
    }

}
