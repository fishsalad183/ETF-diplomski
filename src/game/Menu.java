package game;

import game.HighScores.HighScore;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Menu implements EventHandler<Event> {

    private final StackPane root;
    private final VBox vbox;
    private final SubScene menuSubscene;
    private final Game game;

    public enum MenuKind {
        MAIN, PAUSE, OPTIONS, SCORES;
    }

    private MenuKind currentMenu = null;

    private static final int MAX_NAME_LENGTH = 20;
    private String playerName = "";

    private static final Image LOGO = new Image("resources/logo1 edit.png", Game.WINDOW_WIDTH / 1.7, Game.WINDOW_HEIGHT / 1.7, true, true); // or delegate certain parameters to ImageView?

    public Menu(Game game) {
        this.game = game;

        root = new StackPane();

        menuSubscene = new SubScene(root, Game.WINDOW_WIDTH, Game.WINDOW_HEIGHT);

        vbox = new VBox();
        vbox.getStylesheets().add("game/styles.css");
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPrefWidth(400);
        root.getChildren().add(vbox);

        importSettings();
    }

    private void handleKeyEvent(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE && e.getEventType() == KeyEvent.KEY_PRESSED) {
            if (currentMenu == MenuKind.PAUSE) {
                game.resume();
            } else if (currentMenu == MenuKind.SCORES || currentMenu == MenuKind.OPTIONS) {
                game.switchToMenu(MenuKind.MAIN);
            }
        } else if (e.getCode() == KeyCode.ENTER) {
            if (e.getEventType() == KeyEvent.KEY_PRESSED) {
                Node currentlyFocused = game.getGlobalScene().getFocusOwner();
                if (currentlyFocused instanceof Button) {
                    ((Button) currentlyFocused).arm();
                }
            } else if (e.getEventType() == KeyEvent.KEY_RELEASED) {
                Node currentlyFocused = game.getGlobalScene().getFocusOwner();
                if (currentlyFocused instanceof Button) {
                    if (((Button) currentlyFocused).isArmed()) {
                        ((Button) currentlyFocused).fire();
                        ((Button) currentlyFocused).disarm();
                    }
                } else if (currentlyFocused instanceof TextField) {
                    ObservableList<Node> vboxChildren = vbox.getChildren();
                    for (int i = vboxChildren.indexOf(currentlyFocused) + 1; i < vboxChildren.size(); i++) {
                        Node node = vboxChildren.get(i);
                        if (node.isFocusTraversable() && !node.isDisabled()) {
                            node.requestFocus();
                            break;
                        }
                    }
                }
            }
        } else if ((e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) && e.getEventType() == KeyEvent.KEY_PRESSED) {
            Node currentlyFocused = game.getGlobalScene().getFocusOwner();
            if (currentlyFocused == null) {
                for (Node node : vbox.getChildren()) {
                    if (node.isFocusTraversable() && !node.isDisabled()) {
                        node.requestFocus();
                        break;
                    }
                }
            }
        }
    }

    private void addMouseFocusHandlers(Pane pane) {
        for (Node node : pane.getChildren()) {
            if (node.isFocusTraversable()) {
                node.addEventHandler(MouseEvent.MOUSE_MOVED, (event) -> {
                    if (node.isHover() && !(node instanceof TextField) && !(game.getGlobalScene().getFocusOwner() instanceof TextField)) {
                        node.requestFocus();
                    }
                });
            }
        }
    }

    @Override
    public void handle(Event e) {
        if (e instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) e);
        }
    }

    public void setMenuKind(MenuKind menuKind) {
        vbox.getChildren().clear();
        switch (menuKind) {
            case MAIN: {
                currentMenu = MenuKind.MAIN;
                root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

                ImageView imageView = new ImageView(LOGO);

                TextField nameField = new TextField(playerName);    // note: JavaFX TextField is prone to bugs when undo is performed
                nameField.setAlignment(Pos.CENTER);
                nameField.setPromptText("enter player name");

                Button play = new Button("Play");
                play.setDisable(playerName.isEmpty());
                play.setOnAction(e -> {
                    game.reset();
                });

                UnaryOperator<TextFormatter.Change> lengthLimiter = c -> {
                    if (c.isContentChange()) {
                        if (c.getControlNewText().isEmpty()) {
                            play.setDisable(true);
                        } else {
                            play.setDisable(false);
                        }
                        if (c.getControlNewText().length() > MAX_NAME_LENGTH) {
                            return null;
                        }
                    }
                    return c;
                };
                nameField.setTextFormatter(new TextFormatter(lengthLimiter));
                nameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == false) {
                        playerName = nameField.getText();
                    }
                });
                nameField.setFocusTraversable(true);
                nameField.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {    // an event handler for switching focus up or down the menu when corresponding arrow keys are pressed
                    if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                        int indexChange = (event.getCode() == KeyCode.UP ? -1 : +1);    // UP, otherwise DOWN
                        int index = vbox.getChildren().indexOf(nameField) + indexChange;
                        while (index > 0 && index < vbox.getChildren().size() - 1) {
                            Node node = vbox.getChildren().get(index);
                            if (node.isFocusTraversable() && !node.isDisabled()) {
                                vbox.getChildren().get(index).requestFocus();
                                break;
                            }
                            index += indexChange;
                        }
                    }
                });
                nameField.addEventFilter(KeyEvent.KEY_PRESSED, (keyEvent) -> {
                    switch (keyEvent.getCode()) {
                        case UP:    // since there is nothing above nameField in this particular case
                            keyEvent.consume();
                    }
                });

                Button scores = new Button("High scores");
                scores.setOnAction(e -> game.switchToMenu(MenuKind.SCORES));

                Button options = new Button("Options");
                options.setOnAction(e -> game.switchToMenu(MenuKind.OPTIONS));

                Button exit = new Button("Exit");
                exit.setOnAction(e -> System.exit(0));

                vbox.getChildren().addAll(imageView, nameField, play, scores, options, exit);
            }
            break;
            case PAUSE: {
                currentMenu = MenuKind.PAUSE;
                root.setBackground(new Background(new BackgroundFill(new Color(.1, .1, .1, .6), CornerRadii.EMPTY, Insets.EMPTY)));

                Button resume = new Button("Resume");
                resume.setOnAction(e -> game.resume());

                Button reset = new Button("Reset");
                reset.setOnAction(e -> {
                    game.saveScoreIfNotAlreadySaved(false);
                    game.reset();
                });

                Button main = new Button("Main menu");
                main.setOnAction(e -> {
                    game.switchToMenu(MenuKind.MAIN);
                });

                Button exit = new Button("Exit");
                exit.setOnAction(e -> {
                    System.exit(0);
                });

                vbox.getChildren().addAll(resume, reset, main, exit);
            }
            break;
            case SCORES: {
                currentMenu = MenuKind.SCORES;
                root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

                Button back = new Button("Back");
                back.setOnAction(e -> game.switchToMenu(MenuKind.MAIN));

                vbox.getChildren().addAll(createHighScoreTable(), back);
            }
            break;
            case OPTIONS: {
                currentMenu = MenuKind.OPTIONS;
                root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

                HBox testModeHBox = new HBox();
                testModeHBox.getStyleClass().add("options-hbox");
                Label testModeText = new Label("Test mode");
                CheckBox testModeCheckBox = new CheckBox();
                testModeCheckBox.setSelected(game.isTestMode());
                testModeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    game.setTestMode(newValue);
                });
                testModeHBox.getChildren().addAll(testModeText, testModeCheckBox);

                SoundPlayer soundPlayer = game.getSoundPlayer();

                HBox musicVolumeHBox = new HBox();
                musicVolumeHBox.getStyleClass().add("options-hbox");
                Label musicVolumeText = new Label("Music volume");
                Slider musicVolumeSlider = new Slider(0., 1., soundPlayer.getMusicVolume());
                musicVolumeSlider.setBlockIncrement(0.01);
                Label musicVolumeValue = new Label(SoundPlayer.getVolumeString(musicVolumeSlider.getValue()));
                musicVolumeSlider.valueProperty().addListener((Observable observable) -> {
                    soundPlayer.setMusicVolume(musicVolumeSlider.getValue());
                });
                musicVolumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    musicVolumeValue.textProperty().setValue(SoundPlayer.getVolumeString(newValue.doubleValue()));
                });
                musicVolumeHBox.getChildren().addAll(musicVolumeText, musicVolumeSlider, musicVolumeValue);

                HBox effectsVolumeHBox = new HBox();
                effectsVolumeHBox.getStyleClass().add("options-hbox");
                Label effectsVolumeText = new Label("Effects volume");
                Slider effectsVolumeSlider = new Slider(0., 1., soundPlayer.getSoundEffectVolume());
                effectsVolumeSlider.setBlockIncrement(0.01);
                Label effectsVolumeValue = new Label(SoundPlayer.getVolumeString(effectsVolumeSlider.getValue()));
                effectsVolumeSlider.valueProperty().addListener((Observable observable) -> {
                    soundPlayer.setSoundEffectVolume(effectsVolumeSlider.getValue());
                });
                effectsVolumeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    effectsVolumeValue.textProperty().setValue(SoundPlayer.getVolumeString(newValue.doubleValue()));
                });
                effectsVolumeHBox.getChildren().addAll(effectsVolumeText, effectsVolumeSlider, effectsVolumeValue);

                Button back = new Button("Back");
                back.setOnAction(e -> {
                    exportSettings();
                    game.switchToMenu(MenuKind.MAIN);
                });

                vbox.getChildren().addAll(testModeHBox, musicVolumeHBox, effectsVolumeHBox, back);
            }
            break;
            default:
                currentMenu = null;
                break;
        }
        addMouseFocusHandlers(vbox);
    }

    public GridPane createHighScoreTable() {
        GridPane table = new GridPane();
        table.getStyleClass().add("scores-grid-pane");

        int rowIndex = 0;
        Label nameHeader = new Label("Name");
        nameHeader.getStyleClass().addAll("scores-label-header", "scores-label-name");
        table.add(nameHeader, 1, rowIndex);
        Label pointsHeader = new Label("Points");
        pointsHeader.getStyleClass().addAll("scores-label-header", "scores-label-points");
        table.add(pointsHeader, 2, rowIndex);

        for (HighScore hs : game.getHighScores().getScores()) {
            ++rowIndex;
            Label place = new Label("" + rowIndex);
            place.getStyleClass().add("scores-label-place");
            table.add(place, 0, rowIndex);
            Label name = new Label(hs.getName());
            name.getStyleClass().add("scores-label-name");
            table.add(name, 1, rowIndex);
            Label points = new Label(hs.getPoints() >= 0 ? "" + hs.getPoints() : "-");
            points.getStyleClass().add("scores-label-points");
            table.add(points, 2, rowIndex);
        }

        return table;
    }

    public SubScene getMenuSubscene() {
        return menuSubscene;
    }

    public String getPlayerName() {
        return playerName;
    }

    //================================================================================
    // Settings import and export
    //================================================================================
    static final String SETTINGS_FILENAME = "settings.dat";

    public static class Settings implements Serializable {

        private boolean testMode;
        private double soundEffectVolume;
        private double musicVolume;

        public Settings(boolean testMode, double soundEffectVolume, double musicVolume) {
            this.testMode = testMode;
            this.soundEffectVolume = soundEffectVolume;
            this.musicVolume = musicVolume;
        }

        public boolean isTestMode() {
            return testMode;
        }

        public void setTestMode(boolean testMode) {
            this.testMode = testMode;
        }

        public double getSoundEffectVolume() {
            return soundEffectVolume;
        }

        public void setSoundEffectVolume(double soundEffectVolume) {
            this.soundEffectVolume = soundEffectVolume;
        }

        public double getMusicVolume() {
            return musicVolume;
        }

        public void setMusicVolume(double musicVolume) {
            this.musicVolume = musicVolume;
        }

    }

    void importSettings() {
        ObjectInputStream inStream = null;
        try {
            inStream = new ObjectInputStream(new FileInputStream(SETTINGS_FILENAME));
            Settings settings = (Settings) inStream.readObject();
            game.setTestMode(settings.isTestMode());
            game.getSoundPlayer().setSoundEffectVolume(settings.getSoundEffectVolume());
            game.getSoundPlayer().setMusicVolume(settings.getMusicVolume());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HighScores.class.getName()).log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HighScores.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HighScores.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(HighScores.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void exportSettings() {
        ObjectOutputStream outStream = null;
        try {
            outStream = new ObjectOutputStream(new FileOutputStream(SETTINGS_FILENAME));
            outStream.writeObject(new Settings(game.isTestMode(), game.getSoundPlayer().getSoundEffectVolume(), game.getSoundPlayer().getMusicVolume()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HighScores.class.getName()).log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HighScores.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(HighScores.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
