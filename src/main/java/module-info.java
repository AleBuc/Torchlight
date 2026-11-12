open module alebuc.torchlight {
    requires static lombok;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    // Spring logs through Commons Logging, a named module that its automatic modules cannot require themselves.
    requires org.apache.commons.logging;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires kafka.clients;
    requires javafx.graphics;
    requires java.desktop;
    requires org.slf4j;
    requires tools.jackson.databind;
    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;

    exports alebuc.torchlight.component;
}