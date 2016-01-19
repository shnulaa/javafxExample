package shnulaa.fx.main;

import java.awt.AWTException;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class MainGui extends Application {
    private static Logger log = LoggerFactory.getLogger(MainGui.class);

    private Stage primaryStage;
    private Scene rootScene;
    private TrayIcon trayIcon;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Platform.setImplicitExit(false);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Server Configuration");

        try {
            // Load the root layout from the fxml file
            FXMLLoader mainLayoutLoader = new FXMLLoader(MainGui.class.getResource("/ui/MainLayout.fxml"));
            Pane rootLayout = mainLayoutLoader.load();

            rootScene = new Scene(rootLayout);
            primaryStage.setScene(rootScene);
            primaryStage.setResizable(false);

            addToTray();

            primaryStage.getIcons().add(new Image(MainGui.class.getResource("/image/icon.png").toString()));
            primaryStage.show();
        } catch (IOException e) {
            log.error("IOException occurred when load MainLayout.fxml..", e);
        }
    }

    private void addToTray() {
        // ensure awt is initialized
        java.awt.Toolkit.getDefaultToolkit();

        // make sure system tray is supported
        if (!java.awt.SystemTray.isSupported()) {
            log.warn("No system tray support!");
        }

        final java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
        try {

            java.awt.Image image = ImageIO.read(MainGui.class.getResource("/image/icon.png"));
            trayIcon = new TrayIcon(image);
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            primaryStage.show();
                        }
                    });
                }
            });

            java.awt.MenuItem openItem = new java.awt.MenuItem("Configuration");
            openItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            show();
                        }
                    });
                }
            });

            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // controller.closeServer();
                    Platform.exit();
                    tray.remove(trayIcon);
                }
            });

            PopupMenu popup = new PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            trayIcon.setToolTip("Not Connected");
            tray.add(trayIcon);
        } catch (IOException e) {
            log.error("IOException occurred when addToTray..", e);
        } catch (AWTException e) {
            log.error("AWTException occurred when addToTray..", e);
        }
    }

    public void show() {
        primaryStage.show();
    }

    public void hide() {
        primaryStage.hide();
    }

    public void setTooltip(String message) {
        if (trayIcon != null) {
            trayIcon.setToolTip(message);
        }
    }

    public void showNotification(String message) {
        trayIcon.displayMessage("Nio fx Demo", message, java.awt.TrayIcon.MessageType.INFO);
    }
}
