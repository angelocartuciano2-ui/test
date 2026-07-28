module fr.quentincillierre.hangman {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.media;
    requires transitive javafx.base;

    exports fr.quentincillierre.hangman.application;
    opens fr.quentincillierre.hangman.application to javafx.fxml;

    exports fr.quentincillierre.hangman;

    exports fr.quentincillierre.hangman.controller;
    opens fr.quentincillierre.hangman.controller to javafx.fxml;

    exports fr.quentincillierre.hangman.model;
}
