package com.mazhangjing.lab.sound;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * CaptureDemo 是一个独立的 JavaFx 程序，此程序用于提供对于声音输入流的"实时"检测。
 * 当捕获到声音时，GUI 会更新数字以反映此变化。
 * @apiNote 2019-03-11 撰写并验证了此类
 */
public class CaptureDemo extends Application {

    private Text number = new Text("Not Ready");
    private Button start = new Button("Start");
    private Button end = new Button("End");
    private SimpleDoubleProperty volume = new SimpleDoubleProperty(4.0);
    private SimpleBooleanProperty disableVolume = new SimpleBooleanProperty(false);

    private long id = 0;

    private FileWriter fw;
    private FileWriter fw2;
    {
        number.setFont(Font.font(70));
        number.setFill(Color.RED);
        number.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double value = Double.parseDouble(newValue);
                if (disableVolume.get()) {
                    if (Math.abs(value) >= volume.get()) {
                        id += 1;
                        String data = id + " " + System.currentTimeMillis() + " " + value + "\n";
                        fw.write(data);
                        fw.flush();
                        fw2.write(data);
                        fw2.flush();
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }
    private Capture capture;

    public CaptureDemo() {
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Scene getScene(Parent parent) {
        return new Scene(parent, 600, 400);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sound Capture - A Part of Psy4J Project");
        primaryStage.setScene(getScene(drawParent()));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            if (capture != null) {
                try {
                    capture.stop();
                } catch (Exception ignore) {}
            }
            System.exit(0);
        });
    }

    private Parent drawParent() {
        BorderPane pane = new BorderPane();
        Text header = new Text("声音检测程序");
        header.setFont(Font.font(50));
        Text info = new Text("请连接好麦克风，并调节音量与增益，点击 Start 后开始说话，观察数值的变化。");
        Text note = new Text("当声音绝对值大于此分贝数后，记录到 result.txt 中");
        TextField vol = new TextField();
        vol.setText(volume.getValue().toString());
        vol.textProperty().addListener((observable, oldValue, newValue) -> {
            volume.set(Double.parseDouble(newValue));
        });
        vol.disableProperty().bindBidirectional(disableVolume);
        HBox logBox = new HBox();
        logBox.getChildren().addAll(vol, note);
        logBox.setSpacing(10);
        logBox.setAlignment(Pos.CENTER);
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
        box.setPadding(new Insets(20,0,0,0));
        box.getChildren().addAll(header, info, logBox);
        pane.setTop(box);
        Button about = new Button("About");
        end.setDisable(true);
        HBox btnBox = new HBox();
        btnBox.setPadding(new Insets(0,0,20,0));
        btnBox.getChildren().addAll(start,end,about);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setSpacing(15);
        pane.setBottom(btnBox);
        pane.setCenter(number);

        about.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("关于此程序");
            alert.setHeaderText("声音变化检测程序");
            alert.setContentText("此程序用于声音检测，安装好麦克风并调整音量、增益后，点击 Start 开始检测。" +
                    "当检测到声音后，屏幕上的数值会变化。\n" + "\nPowered By JVM, Design By Corkine Ma," +
                    " Use Java Sound and OpenJFX API. \nVisit http://www.mazhangjing.com to know more.");
            alert.showAndWait();
        });

        start.setOnAction(event -> {
            if (capture == null) capture = new Capture();
            Task task = capture;
            task.valueProperty().addListener(e -> {
                Double value = (Double) task.getValue();
                if (value != null) {
                    if (value == 0.0) {
                        doStart();
                    } else if (value == 1.0) {
                        doReStart();
                    }
                }
            });
            task.exceptionProperty().addListener(e -> {
                System.out.println(task.getException().getMessage());
                try { capture.stop(); } catch (Exception ignore) {}
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText("在启动过程中发生错误");
                String err = "在处理过程中发生错误：";
                if (task.getException() != null && task.getException().getMessage() != null){
                    StringWriter stringWriter = new StringWriter();
                    task.getException().printStackTrace(new PrintWriter(stringWriter));
                    err += stringWriter.toString();
                }
                alert.setContentText(err);
                alert.showAndWait().ifPresent(r -> {
                    capture = null;
                    doReStart();
                });
            });
            number.textProperty().bind(task.messageProperty());
            new Thread(task).start();
        });

        end.setOnAction(e -> {
            try {
                capture.stop();
                capture = null;
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText("在终止过程中发生错误");
                String err = "在处理过程中发生错误：";
                if (ex.getMessage() != null){
                    StringWriter stringWriter = new StringWriter();
                    ex.printStackTrace(new PrintWriter(stringWriter));
                    err += stringWriter.toString();
                }
                alert.setContentText(err);
                alert.showAndWait().ifPresent(r -> {
                    capture = null;
                    doReStart();
                });
            }
        });
        return pane;
    }

    private void doReStart() {
        disableVolume.set(false);
        start.setDisable(false);
        end.setDisable(true);
        number.setFill(Color.RED);
        number.textProperty().unbind();
        number.setText("Not Ready");
        try { fw.close(); fw2.close(); } catch (Exception ignore) { }
    }

    private void doStart() {
        disableVolume.set(true);
        start.setDisable(true);
        end.setDisable(false);
        number.setFill(Color.GREEN);
        try {
            fw = new FileWriter(new File("result_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")) + ".txt"));
            fw2 = new FileWriter(new File("temp_result.txt"));
        } catch (Exception ignore) { }
    }
}
