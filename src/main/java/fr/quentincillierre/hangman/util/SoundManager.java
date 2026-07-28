package fr.quentincillierre.hangman.util;

import java.net.URL;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioClip;

public final class SoundManager {

    private static final String CLICK_SOUND_PATH = "/music/click1.mp3";
    private static final Set<Node> ATTACHED_NODES = Collections.newSetFromMap(new IdentityHashMap<>());
    private static AudioClip clickClip;

    private SoundManager() {
    }

    public static void attachClickSound(Node node) {
        if (ATTACHED_NODES.contains(node)) {
            return;
        }
        ATTACHED_NODES.add(node);

        if (node instanceof Control || node instanceof Hyperlink) {
    node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> playClick());
}
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                attachClickSound(child);
            }
        }
    }

    public static synchronized void playClick() {
        URL resource = SoundManager.class.getResource(CLICK_SOUND_PATH);
        if (resource == null) {
            System.err.println("Click sound resource not found: " + CLICK_SOUND_PATH);
            return;
        }

        if (clickClip == null) {
            clickClip = new AudioClip(resource.toExternalForm());
            clickClip.setVolume(0.25);
        }

        clickClip.play();
    }
}
