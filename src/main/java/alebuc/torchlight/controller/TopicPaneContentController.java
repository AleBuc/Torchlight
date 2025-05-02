package alebuc.torchlight.controller;

import alebuc.torchlight.scene.ConsumerScene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@RequiredArgsConstructor
public class TopicPaneContentController extends HBox {

    private final ConsumerScene consumerScene;

    @Setter
    @Getter
    private String topicName;

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
        consumerScene.createConsumer(getTopicName());
    }

    public void setEventsCount(long eventsCount) {
        this.eventsCount.setText(String.valueOf(eventsCount));
    }

    public void setPartitionsCount(int partitionsCount) {
        this.partitionsCount.setText(String.valueOf(partitionsCount));
    }

}
