package shnulaa.fx.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.StringUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import shnulaa.fx.nio.LocalNioServer;
import shnulaa.fx.pool.TPools;

public class MainLayoutController {

    @FXML
    private TextField listenPort;

    @FXML
    private Label listenLab;

    @FXML
    private Button listen;

    @FXML
    private Button stop;

    @FXML
    private TextArea listenArea;

    private ExecutorService service;
//    private 

    // private TPools pool;

    @FXML
    private void initialize() {
        System.out.println("initialize");
        listen.setDisable(false);
        stop.setDisable(true);
    }

    @FXML
    private void handlelListen() {
        String portText = listenPort.getText();
        if (StringUtils.isEmpty(portText)) {
            showAlert("Nio demo", "Invalid Port", Alert.AlertType.ERROR);
            return;
        }

        try {
            int port = Integer.valueOf(portText);
            if (port < 0 || port > 65535) {
                showAlert("Nio demo", "Port outOf range", Alert.AlertType.ERROR);
                return;
            }

            ExecutorService service = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    final Thread t = new Thread();
                    t.setDaemon(true);
                    t.setName("Nio_server_Thread");
                    return t;
                }
            });

            final Runnable listenWorker = new LocalNioServer(listenArea, port);
            service.execute(listenWorker);
        } catch (Exception ex) {
            if (ex instanceof NumberFormatException) {
                showAlert("Nio demo", "Port is not number..", Alert.AlertType.ERROR);
            } else {
                showAlert("Nio demo", ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handlelStop() {

    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(type.name());
        a.setResizable(false);
        a.setContentText(message);
        a.showAndWait();
    }

}
