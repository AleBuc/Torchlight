package alebuc.torchlight.controller;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.model.Topic;
import alebuc.torchlight.scene.ConsumerScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor
public class MainScreenController implements Initializable {
    private final KafkaEventConsumer consumer;
    private final ConsumerScene consumerScene;

    @FXML
    private Label topicsCount;

    @FXML
    private Label partitionsCount;

    @FXML
    private Button refreshButton;

    @FXML
    private CheckBox showHiddenTopicsCheckbox;

    @FXML
    private Accordion topicsAccordion;

    @FXML
    void refreshTopicList(ActionEvent event) {
        log.info("refresh list");
        List<Topic> topics;
        if (showHiddenTopicsCheckbox.isSelected()) {
            topics = consumer.getTopics();
            topics.sort(Comparator.comparing(Topic::getName, Comparator.naturalOrder()));
        } else {
            topics = consumer.getTopics().stream().filter(topic -> topic.getName().charAt(0) != '_').sorted(Comparator.comparing(Topic::getName, Comparator.naturalOrder())).toList();
        }
        int tCount = topics.size();
        int pCount = topics.stream().mapToInt(topic -> topic.getPartitions().size()).sum();
        topicsCount.setText(String.valueOf(tCount));
        partitionsCount.setText(String.valueOf(pCount));
        setTopicList(topics);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshTopicList(null);
    }

    private void setTopicList(List<Topic> topicList) {
        topicsAccordion.getPanes().removeAll(topicsAccordion.getPanes());
        for (Topic topic : topicList) {
            TitledPane titledPane = new TitledPane();
            titledPane.setAnimated(true);
            titledPane.setCollapsible(true);
            titledPane.setTooltip(new Tooltip(topic.getName()));
            titledPane.setText(topic.getName());
            titledPane.setContent(createTopicPaneContent(topic));
            topicsAccordion.getPanes().add(titledPane);
        }

    }

    private Node createTopicPaneContent(Topic topic) {
        URL resource = getClass().getClassLoader().getResource("topicPaneContent.fxml");
        TopicPaneContentController topicPaneContentController = new TopicPaneContentController(consumerScene);
        FXMLLoader paneContent = new FXMLLoader(resource);
        paneContent.setController(topicPaneContentController);
        Node node = null;
        try {
            node = paneContent.load();
            topicPaneContentController.setTopicName(topic.getName());
            topicPaneContentController.setEventsCount(topic.getEventCount());
            topicPaneContentController.setPartitionsCount(topic.getPartitionsCount());
        } catch (IOException e) {
            log.error("Error during pane creation.", e);
        }
        return node;
    }
}
