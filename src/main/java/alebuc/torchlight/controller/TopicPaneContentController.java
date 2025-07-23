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

    /**
     * Sets event count in the pane. Disable the "Consume" button if no event to read.
     *
     * @param eventsCount event count from the topic
     */
    public void setEventsCount(long eventsCount) {
        this.eventsCount.setText(String.valueOf(eventsCount));
        if (eventsCount == 0L) {
            consumeButton.setDisable(true);
        }
    }

    /**
     * Sets partition count in the pane.
     *
     * @param partitionsCount partition count from the topic
     */
    public void setPartitionsCount(int partitionsCount) {
        this.partitionsCount.setText(String.valueOf(partitionsCount));
    }

}
