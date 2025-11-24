package alebuc.torchlight.controller;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.utils.ConsumerUtils;
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

    private final KafkaEventConsumer consumer;

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
        ConsumerUtils.createConsumer(getTopicName(), consumer);
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
