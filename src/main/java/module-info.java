open module alebuc.torchlight {
    requires static lombok;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires kafka.clients;
    requires javafx.graphics;
    requires java.desktop;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;

    exports alebuc.torchlight.component;
}