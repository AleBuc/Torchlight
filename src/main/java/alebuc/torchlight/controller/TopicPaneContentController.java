package alebuc.torchlight.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.awt.event.MouseEvent;

public class TopicPaneContentController extends HBox {

    @FXML
    private Label eventsCount;

    @FXML
    private Label partitionsCount;

    @FXML
    private Button consumeButton;

    @FXML
    private Button produceButton;

    @FXML
    void consumeTopic(MouseEvent event) {

    }

    public void setEventsCount(long eventsCount) {
        this.eventsCount.setText(String.valueOf(eventsCount));
    }

    public void setPartitionsCount(int partitionsCount) {
        this.partitionsCount.setText(String.valueOf(partitionsCount));
    }

    //todo add consumer button to open consumer window
}
