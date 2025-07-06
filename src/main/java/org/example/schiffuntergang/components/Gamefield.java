package org.example.schiffuntergang.components;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.schiffuntergang.EnemyPlayer;
import org.example.schiffuntergang.HelloController;
import org.example.schiffuntergang.Multiplayer.MultiplayerLogic;
import org.example.schiffuntergang.filemanagement.GamefieldData;
import org.example.schiffuntergang.sounds.*;
import org.example.schiffuntergang.filemanagement.GameState;
import org.example.schiffuntergang.filemanagement.SerializableShip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;

import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Repräsentiert ein komplettes Spielfeld (sowohl für den Spieler als auch für den Gegner).
 * Diese Klasse erweitert {@link GridPane} und verwaltet ein 2D-Array von {@link Cell}-Objekten.
 * Sie ist verantwortlich für die Platzierung von Schiffen, die Auswertung von Schüssen
 * und die visuelle Darstellung des Spielfelds.
 */
public class Gamefield extends GridPane {
    private final List<Ships> placedShip = new ArrayList<>();
    /** Die Höhe des Spielfelds (Anzahl der Reihen). */
    private final int lang;
    /** Die Breite des Spielfelds (Anzahl der Spalten). */
    private final int breit;
    /** Gibt an, ob dies das Spielfeld des Gegners ist. */
    private final boolean enemy;
    /** Das 2D-Array, das alle Zellen des Spielfelds speichert. */
    private final Cell[][] cells;
    /** Zählt die Anzahl der von Schiffen belegten Zellen. */
    private int usedCells = 0;
    /** Referenz zum Haupt-Controller für UI-Updates. */
    private HelloController control;
    /** Referenz zur KI-Logik im Einzelspieler-Modus. */
    private EnemyPlayer en;
    /** Referenz zur Netzwerk-Logik im Multiplayer-Modus. */
    private MultiplayerLogic lo;
    private boolean multiplayer = false;
    private boolean turn;

    /**
     * Konstruktor für ein Standard-Spielfeld (typischerweise Einzelspieler).
     * Initialisiert das Gitter, die Zellen und die Klick-Listener.
     *
     * @param enemy      True, wenn es das Feld des Gegners ist, sonst false.
     * @param controler  Referenz zum {@link HelloController} für Interaktionen.
     * @param h          Höhe des Spielfelds (Anzahl Reihen).
     * @param b          Breite des Spielfelds (Anzahl Spalten).
     */
    public Gamefield(boolean enemy, HelloController controler, int h, int b) {
        lang = h;
        breit = b;
        cells = new Cell[h][b];
        this.enemy = enemy;
        this.control = controler;
        setupBackground();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < b; j++) {
                Cell c = new Cell(i, j, this, 30, 30, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(new Color(0.1, 0.3, 0.8, 0.6));

                final int x = i;
                final int y = j;
                c.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !enemy) {

                        if (getUsedCells() + control.getLength() <= maxShipsC()) { //+ control.getlength damit das auf auf neue schiffe prüft
                            Ships ship = new Ships(control.getLength(), control.getLength());
                            if (placeShip(ship, x, y, control.getDirection())) {
                                increaseCells(ship.getLength());
                                control.updateRemainingCellsDisplay();

                            }
                        } else {
                            System.out.println(maxShipsC());
                            System.out.println("Maximale Anzahl an schiffen erreicht");
                            control.showNotification("No Buildpoints left!", "error");
                        }

                    } else if (event.getButton() == MouseButton.PRIMARY && enemy && control.getReady()) {
                        shoot(x, y);
                    }
                });
                add(c, i, j);
            }
        }
    }

    /**
     * Konstruktor für ein Spielfeld im Einzelspieler-Modus mit einer KI als Gegner.
     *
     * @param enemy      True, wenn es das Feld des Gegners ist.
     * @param controler  Referenz zum {@link HelloController}.
     * @param h          Höhe des Spielfelds.
     * @param b          Breite des Spielfelds.
     * @param e          Referenz zur Instanz des {@link EnemyPlayer}.
     */
    public Gamefield(boolean enemy, HelloController controler, int h, int b, EnemyPlayer e) {
        en = e;
        lang = h;
        breit = b;
        cells = new Cell[h][b];
        this.enemy = enemy;
        this.control = controler;
        setupBackground();
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < b; j++) {
                Cell c = new Cell(i, j, this, 30, 30, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(new Color(0.1, 0.3, 0.8, 0.6));
                final int x = i;
                final int y = j;

                c.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !enemy) {

                        if (getUsedCells() <= maxShipsC()) {
                            Ships ship = new Ships(control.getLength(), control.getLength());
                            if (placeShip(ship, x, y, control.getDirection())) {
                                increaseCells(ship.getLength());
                            }
                        } else {
                            System.out.println(maxShipsC());
                            System.out.println("[Gamefield] Maximale Anzahl an schiffen erreicht");
                            control.showNotification("No Buildpoints left!", "error");
                        }
                    }
                    else if (event.getButton() == MouseButton.PRIMARY && enemy && control.getReady()) {
                        shoot(x, y);
                    }
                });
                add(c, i, j);
            }
        }
    }

    /**
     * Konstruktor für ein Spielfeld im Multiplayer-Modus.
     *
     * @param enemy      True, wenn es das Feld des Gegners ist.
     * @param controler  Referenz zum {@link HelloController}.
     * @param h          Höhe des Spielfelds.
     * @param b          Breite des Spielfelds.
     * @param l          Referenz zur Instanz der {@link MultiplayerLogic}.
     */
    public Gamefield(boolean enemy, HelloController controler, int h, int b, MultiplayerLogic l) {
        lang = h;
        breit = b;
        multiplayer = true;
        cells = new Cell[h][b];
        this.enemy = enemy;
        this.control = controler;
        this.lo = l;
        setupBackground();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < b; j++) {
                Cell c = new Cell(i, j, this, 30, 30, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(new Color(0.1, 0.3, 0.8, 0.6));
                final int x = i;
                final int y = j;
                c.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !enemy) {

                        int len = control.getLength();
                        System.out.println(len);

                        boolean isServer = controler.getHost();

                        if (isServer) {
                            if (getUsedCells() + len <= maxShipsC()) {
                                Ships ship = new Ships(len, len);

                                if (placeShip(ship, x, y, control.getDirection())) {
                                    increaseCells(len);
                                    control.updateRemainingCellsDisplay();
                                }
                            } else {
                                System.out.println("[Gamefield] Limit an Bau-Punkten erreicht!");
                                control.showNotification("No Buildpoints left!", "error");
                            }
                        } else {

                            if (control.canClientPlaceShip(len)) {
                                Ships ship = new Ships(len, len);

                                if (placeShip(ship, x, y, control.getDirection())) {
                                    increaseCells(len); // Zählt trotzdem die Zellen für Konsistenz
                                    control.clientPlacedShip(len);
                                }
                            } else {
                                System.out.println("[Gamefield] Von Schiffslänge " + len + " können keine mehr platziert werden.");
                                control.showNotification("No more of this ship length available ", "error");
                            }
                        }

                    } else if (event.getButton() == MouseButton.PRIMARY && this.enemy) {

                        if (lo != null && control.getReady() && lo.getTurn()) {
                            lo.setX(x);
                            lo.setY(y);
                            System.out.println("[Gamefield] Schuss wird vorbereitet auf: X: " + x + ", Y: " + y);
                            try {
                                lo.startShoot();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else {
                            control.showNotification("Wait for your turn!", "error");
                        }
                    }
                });
                add(c, i, j);
            }
        }
    }

    /**
     * Erstellt ein Spielfeld aus gespeicherten Daten (Deserialisierung).
     * Nützlich für das Laden eines Spielstands.
     *
     * @param data      Das {@link GamefieldData}-Objekt mit den gespeicherten Informationen.
     * @param controller Die Referenz zum Haupt-Controller.
     * @param logic     Die Referenz zur Multiplayer-Logik (kann null sein für Einzelspieler).
     * @return Ein vollständig rekonstruiertes {@code Gamefield}-Objekt.
     */
    public static Gamefield fromData(GamefieldData data, HelloController controller, MultiplayerLogic logic) {
        Gamefield board;
        if (logic != null) {
            board = new Gamefield(data.isEnemy(), controller, data.getHeight(), data.getWidth(), logic);
        } else {

            System.out.println("[Gamefield] height: " + data.getHeight() + " width: " + data.getWidth());
            board = new Gamefield(data.isEnemy(), controller, data.getHeight(), data.getWidth());
        }

        for (SerializableShip shipData : data.getShips()) {
            Ships ship = new Ships(shipData.getLength(), shipData.getHealth());
            board.placeShip(ship, shipData.getStartX(), shipData.getStartY(), shipData.isVertical());
            board.increaseCells(ship.getLength());
        }

        for (Position pos : data.getShotPositions()) {
            Cell cell = board.getCell(pos.getX(), pos.getY());
            if (cell != null) {
                cell.setShot(true);
                if (cell.getShip() != null) {
                    cell.setFill(Color.RED);
                } else {
                    cell.setFill(Color.BLACK);
                }
            }
        }

        //Sonderkondition für Multiplayer
        for (Position pos : data.getHitVariables()) {
            Cell cell = board.getCell(pos.getX(), pos.getY());
            if (cell != null) {
                cell.setFill(Color.RED);
            }
        }
        return board;
    }

    /**
     * Erstellt ein Spielfeld aus einem allgemeinen Spielzustand.
     * Ähnlich wie {@link #fromData}, aber für einen anderen Datencontainer.
     *
     * @param state     Der {@link GameState}, aus dem geladen wird.
     * @param isEnemy   Gibt an, ob das zu erstellende Feld ein Gegnerfeld ist.
     * @return Ein rekonstruiertes {@code Gamefield}-Objekt.
     */
    public static Gamefield fromGameState(GameState state, boolean isEnemy) {
        HelloController dummyController = new HelloController();
        Gamefield board = new Gamefield(isEnemy, dummyController,
                state.getPlayerBoardData().getHeight(), state.getPlayerBoardData().getWidth());
        for (SerializableShip s : state.getPlayerBoardData().getShips()) {
            Ships ship = new Ships(s.getLength(), s.getHealth());
            if (board.placeShip(ship, s.getStartX(), s.getStartY(), s.isVertical())) {
                board.addShip(ship);
                board.increaseUsedCells(ship.getLength());
            }
        }

        for (Position p : state.getPlayerBoardData().getShotPositions()) {
            Cell cell = board.getCell(p.getX(), p.getY());
            cell.setShot(true);
            cell.setFill(cell.getShip() != null
                    ? Color.RED
                    : Color.BLACK);
        }
        return board;
    }

    /**
     * Gibt zurück, ob dies ein Gegnerfeld ist.
     *
     * @return True, wenn es ein Gegnerfeld ist, sonst false.
     * @deprecated Besser {@link #isEnemy()} verwenden für klarere Benennung.
     */
    public boolean getStatus() {
        return enemy;
    }

    /**
     * Konfiguriert das Hintergrundbild des Spielfelds.
     */
    private void setupBackground() {
        try {
            Image backgroundImage = new Image(getClass().getResource("/images/Boardbg.png").toExternalForm());

            BackgroundImage bgImage = new BackgroundImage(backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true)
            );

            this.setBackground(new Background(bgImage));
        } catch (Exception e) {
            System.err.println("[Gamefield] Failed to load background image for Gamefield.");
        }
    }

    /**
     * Prüft, ob dies das Spielfeld des Gegners ist.
     *
     * @return true, wenn es sich um das Gegnerfeld handelt, andernfalls false.
     */
    public boolean isEnemy() {
        return enemy;
    }

    /**
     * Berechnet die maximal erlaubte Anzahl an Zellen, die von Schiffen belegt werden dürfen.
     * Aktuell sind dies 30% der Gesamtfläche.
     *
     * @return Die maximal erlaubte Anzahl an belegten Zellen als double.
     */
    public double maxShipsC() {
        return (double) lang * (double) breit * 0.3;
    }

    /**
     * Fügt ein Schiff zur internen Liste der platzierten Schiffe hinzu.
     *
     * @param ship Das hinzuzufügende {@link Ships}-Objekt.
     */
    public void addShip(Ships ship) {
        placedShip.add(ship);
    }

    /**
     * Gibt die aktuelle Anzahl der von Schiffen belegten Zellen zurück.
     *
     * @return Die Anzahl der belegten Zellen.
     */
    public int getUsedCells() {
        return usedCells;
    }

    /**
     * Erhöht den Zähler für belegte Zellen.
     *
     * @param laenge Die Länge des hinzugefügten Schiffs, um die der Zähler erhöht wird.
     */
    public void increaseCells(int laenge) {
        usedCells += laenge;
    }

    /**
     * Führt einen Schuss auf die angegebene Koordinate aus.
     * Aktualisiert den Zustand der Zelle und des getroffenen Schiffs.
     * Löst Benachrichtigungen und ggf. den Zug des Gegners aus.
     *
     * @param x Die X-Koordinate (Spalte) des Schusses.
     * @param y Die Y-Koordinate (Reihe) des Schusses.
     */
    public void shoot(int x, int y) {
        Cell c = getCell(x, y);
        if (c == null) return; // Sicherheitsprüfung

        if (c.isShot()) {
            System.out.println("[Gamefield] Bereits beschossen");
            if (this.enemy && control != null) {
                control.showNotification("Already shot that one, try again!", "info");
            }
            return;
        }

        c.setShot(true);
        Ships s = c.getShip();

        if (s != null) {
            s.hit();
            c.setFill(Color.RED);
            // SoundEffect.play("hit.wav");

            if (s.getHealth() == 0) {
                if (control != null) control.showNotification("Yay, ship is down!", "sunk");
                deleteShip();
            } else {
                if (control != null) control.showNotification("Successful hit!", "hit");
            }
        }
        else {
            c.setFill(Color.BLACK);
            // SoundEffect.play("miss.wav");
            if (control != null) control.showNotification("Oops, you missed!", "miss");
        }

        if (this.enemy && !multiplayer) {
            System.out.println("[Gamefield] Einzelspieler: Gegner ist am Zug.");
            en.revenge();
        }
    }

    /**
     * Gibt die Zelle an der spezifischen Koordinate zurück.
     * ACHTUNG: Die Implementierung ist inkonsistent. Sie erwartet x als Reihenindex
     * und y als Spaltenindex, was unüblich ist. (x-Wert wird gegen Höhe/lang, y-Wert gegen Breite/breit geprüft)
     *
     * @param x Der Index der Reihe (Höhe).
     * @param y Der Index der Spalte (Breite).
     * @return Das {@link Cell}-Objekt an der Position oder null, wenn die Koordinaten ungültig sind.
     */
    public Cell getCell(int x, int y) {
        if (x >= 0 && x < lang && y >= 0 && y < breit) {
            return cells[x][y];
        }
        return null;
    }

    /**
     * Prüft, ob eine Koordinate innerhalb der Spielfeldgrenzen liegt.
     *
     * @param x Die X-Koordinate.
     * @param y Die Y-Koordinate.
     * @return true, wenn die Koordinate gültig ist, andernfalls false.
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }

    /**
     * Gibt die Höhe (Anzahl der Reihen) des Spielfelds zurück.
     *
     * @return Die Höhe des Spielfelds.
     */
    public int getLang() {
        return lang;
    }

    /**
     * Gibt die Breite (Anzahl der Spalten) des Spielfelds zurück.
     *
     * @return Die Breite des Spielfelds.
     */
    public int getBreit() {
        return breit;
    }

    /**
     * Entfernt versenkte Schiffe aus der Liste der platzierten Schiffe.
     * Prüft nach dem Entfernen, ob das Spiel beendet ist (keine Schiffe mehr vorhanden).
     */
    public void deleteShip() {
        Iterator<Ships> iterator = placedShip.listIterator();
        while (iterator.hasNext()) {
            Ships schiff = iterator.next();
            if (!schiff.isAlive()) {
                iterator.remove();
            }
        }

        if (placedShip.isEmpty()) {
            if (control != null) {
                control.showGameOverScreen(this.enemy);
            }
        }
    }

    /**
     * Setzt den Zugstatus.
     * @param t true, wenn dieser Spieler am Zug ist, sonst false.
     */
    public void setTurn(boolean t) {
        turn = t;
    }

    /**
     * Setzt die Referenz zur Multiplayer-Logik.
     *
     * @param l Die Instanz der {@link MultiplayerLogic}.
     */
    public void setLogic(MultiplayerLogic l) {
        lo = l;
    }

    /**
     * Sammelt die Längen aller auf dem Feld platzierten Schiffe.
     *
     * @return Ein int-Array mit den Längen der Schiffe.
     */
    public int[] getShipLengths() {
        int[] lengths = new int[placedShip.size()];
        for (int i = 0; i < placedShip.size(); i++) {
            lengths[i] = placedShip.get(i).getLength();
        }
        return lengths;
    }

    /**
     * Gibt die aktuelle Lautstärke der Hintergrundmusik zurück.
     * @return Die Lautstärke als double-Wert.
     */
    public double getMusicVolume() {
        return BackgroundMusic.getInstance().getVolume();
    }

    /**
     * Prüft, ob die Hintergrundmusik gerade abgespielt wird.
     * @return true, wenn die Musik aktiv ist, sonst false.
     */
    public boolean isMusicEnabled() {
        return BackgroundMusic.getInstance().isPlaying();
    }

    /**
     * Gibt die Liste aller platzierten Schiffe zurück.
     *
     * @return Eine Liste von {@link Ships}-Objekten.
     */
    public List<Ships> getShips() {
        return placedShip;
    }

    /**
     * Erhöht den Zähler für belegte Zellen.
     * Diese Methode ist identisch mit {@link #increaseCells(int)}.
     *
     * @param length Die Länge des hinzugefügten Schiffs.
     */
    public void increaseUsedCells(int length) {
        this.usedCells += length;
    }

    /**
     * Platziert ein Schiff auf dem Spielfeld an der angegebenen Startposition.
     * Prüft vor der Platzierung auf Kollisionen und ob die Position innerhalb der Grenzen liegt.
     * Stellt das Schiff auch visuell dar.
     *
     * @param ship     Das zu platzierende {@link Ships}-Objekt.
     * @param startX   Die Start-Spalte (X-Koordinate) für die Platzierung.
     * @param startY   Die Start-Reihe (Y-Koordinate) für die Platzierung.
     * @param vertical True für eine vertikale Ausrichtung, false für eine horizontale.
     * @return True, wenn die Platzierung erfolgreich war, sonst false.
     */
    public boolean placeShip(Ships ship, int startX, int startY, boolean vertical) {
        int length = ship.getLength();

        for (int i = 0; i < length; i++) {
            int reihenIndex = vertical ? startY + i : startY;
            int spaltenIndex = vertical ? startX : startX + i;

            if(reihenIndex >= breit || reihenIndex < 0) {
                System.out.println("[Gamefield] Fehler: Platzierung breit bei (Reihe " + reihenIndex + ", Spalte " + spaltenIndex + "). geht outofbounds");
                return false;
            }

            if(spaltenIndex >= lang || spaltenIndex < 0) {
                System.out.println("[Gamefield] Fehler: Platzierung lang bei (Reihe " + reihenIndex + ", Spalte " + spaltenIndex + "). geht outofbounds");
                return false;
            }

            Cell ce = getCell(spaltenIndex, reihenIndex);
            if(ce.getShip() != null){
                System.out.println("[Gamefield] Fehler: Platzierung bei (Reihe " + reihenIndex + ", Spalte " + spaltenIndex + ")geht nix, weil da Schiff ist.");
                return false;
            }

        }

        for (int i = 0; i < length; i++) {
            int reihenIndex = vertical ? startY + i : startY;
            int spaltenIndex = vertical ? startX : startX + i;

            Cell cellToPlaceOn = getCell(spaltenIndex, reihenIndex);
            cellToPlaceOn.setShip(ship);
        }
        if (!enemy) {
            String imagePath = "";

            if (vertical) {
                switch (length) {
                    case 2:
                        imagePath = "/images/shipverticallength2.png";
                        break;
                    case 3:
                        imagePath = "/images/shipverticallength3.png";
                        break;
                    case 4:
                        imagePath = "/images/shipverticallength4.png";
                        break;
                    case 5:
                        imagePath = "/images/shipverticallength5.png";
                        break;
                }
            } else {
                switch (length) {
                    case 2:
                        imagePath = "/images/shiplength2.png";
                        break;
                    case 3:
                        imagePath = "/images/shiplength3.png";
                        break;
                    case 4:
                        imagePath = "/images/shiplength4.png";
                        break;
                    case 5:
                        imagePath = "/images/shiplength5.png";
                        break;
                }
            }
            if (!imagePath.isEmpty()) {
                try {
                    Image shipImage = new Image(getClass().getResource(imagePath).toExternalForm());
                    ImageView shipView = new ImageView(shipImage);

                    shipView.setMouseTransparent(true);
                    shipView.setPreserveRatio(false);

                    int colSpan = 1;
                    int rowSpan = 1;

                    if (vertical) {
                        shipView.setFitWidth(30.0);
                        shipView.setFitHeight(length * 30.0);
                        rowSpan = length;

                    } else {
                        shipView.setFitWidth(length * 30.0);
                        shipView.setFitHeight(30.0);
                        colSpan = length;

                    }
                    final int fcolSpan = colSpan;
                    final int frowSpan = rowSpan;
                    ship.setShipImageView(shipView);
                    Platform.runLater(() -> {
                        this.add(shipView, startX, startY, fcolSpan, frowSpan);
                    });
                } catch (Exception e) {
                    System.err.println("[Gamefield] Fehler beim Laden des Schiff-Bildes: " + imagePath);
                    e.printStackTrace();
                }
            }
        }
        ship.setDirection(vertical);
        addShip(ship);
        System.out.println("[Gamefield] Schiff platziert: Start(Reihe " + startX + ", Spalte " + startY + "), " + (vertical ? "vertikal" : "horizontal"));
        return true;
    }

    /**
     * Gibt die Referenz zum Haupt-Controller zurück.
     * @return der {@link HelloController}.
     */
    public HelloController getControl() {
        return control;
    }

    /**
     * Entfernt alle Schiffe aus der internen Liste.
     */
    public void clearShips() {
        placedShip.clear();
    }

    /**
     * Setzt die Referenz zum KI-Gegner.
     * @param e der {@link EnemyPlayer}.
     */
    public void setEnemy(EnemyPlayer e) {
        en = e;
    }

    /**
     * Prüft, ob sich Schiffe auf dem Spielfeld befinden.
     * @return true, wenn mindestens ein Schiff platziert ist.
     */
    public boolean hasShip() {
        System.out.println("[Gamefield] "+placedShip.isEmpty());
        return !placedShip.isEmpty();
    }

    /**
     * Konvertiert den aktuellen Zustand des Spielfelds in ein serialisierbares Datenobjekt.
     * Nützlich für das Speichern des Spiels.
     *
     * @return Ein {@link GamefieldData}-Objekt, das den Zustand dieses Feldes repräsentiert.
     */
    public GamefieldData toData() {
        GamefieldData data = new GamefieldData();
        data.setWidth(this.breit);
        data.setHeight(this.lang);
        data.setEnemy(this.enemy);

        List<SerializableShip> shipDataList = new ArrayList<>();
        Set<Ships> processedShips = new HashSet<>();

        for (int y = 0; y < breit; y++) {
            for (int x = 0; x < lang; x++) {
                Cell cell = getCell(x, y);
                Ships ship = cell.getShip();

                if (ship != null && !processedShips.contains(ship)) {
                    processedShips.add(ship);
                    boolean isVertical = false;
                    if (y + 1 >= breit) {
                        //kann niemals vertikal sein
                    }else{
                        if (getCell(x, y + 1).getShip() == ship) {
                            isVertical = true;
                        }
                    }

                    SerializableShip serializableShip = new SerializableShip();
                    serializableShip.setLength(ship.getLength());
                    serializableShip.setHealth(ship.getHealth());
                    serializableShip.setStartX(x);
                    serializableShip.setStartY(y);
                    serializableShip.setVertical(isVertical);
                    shipDataList.add(serializableShip);
                }
            }
        }
        data.setShips(shipDataList);

        List<Position> shotPositions = new ArrayList<>();
        for (int y = 0; y < breit; y++) {
            for (int x = 0; x < lang; x++) {
                if (getCell(x, y).isShot()) {
                    shotPositions.add(new Position(x, y));
                }
            }
        }
        data.setShotPositions(shotPositions);

        List<Position> hitVariables = new ArrayList<>();
        for (int y = 0; y < breit; y++) {
            for (int x = 0; x < lang; x++) {
                if(getCell(x,y).isShot() && getCell(x,y).getShipHit()){
                    hitVariables.add(new Position(x, y));
                }
            }
        }
        data.setHitVariables(hitVariables);
        return data;
    }

    /**
     * Private Hilfsmethode, um die Startzelle eines Schiffes zu finden.
     *
     * @param ship Das Schiff, dessen Startzelle gesucht wird.
     * @return Die {@link Cell}, die den Anfang des Schiffs markiert, oder null.
     */
    private Cell findShipStartCell(Ships ship) {
        for (int y = 0; y < lang; y++) {
            for (int x = 0; x < breit; x++) {
                if (getCell(x, y).getShip() == ship) {
                    return getCell(x, y);
                }
            }
        }
        return null; //Sollte nie passieren, wenn das Schiff auf dem Feld ist
    }

    /**
     * Setzt die Referenz zum Haupt-Controller.
     *
     * @param controller Der {@link HelloController}.
     */
    public void setController(HelloController controller) {
        this.control = controller;
    }

    /**
     * Gibt die Anzahl der noch nicht versenkten Schiffe zurück.
     *
     * @return Die aktuelle Anzahl der Schiffe auf dem Feld.
     */
    public int getShipCount() {
        return placedShip.size();
    }

    /**
     * Zeichnet alle Zellen des Spielfelds neu basierend auf ihrem aktuellen Zustand.
     * Diese Methode wird im JavaFX Application Thread ausgeführt, um Thread-Sicherheit zu gewährleisten.
     */
    public void redrawAllCells() {
        Platform.runLater(() -> {
            System.out.println("[Gamefield] DEBUG: Redraw für " + (isEnemy() ? "Gegner" : "Spieler") + "-Feld wird ausgeführt.");
            for (int y = 0; y < lang; y++) {
                for (int x = 0; x < breit; x++) {
                    Cell cell = getCell(x, y);
                    if (cell == null) continue;

                    if (cell.isShot()) {
                        cell.setFill(cell.getShip() != null ? Color.RED : Color.BLACK);
                    } else if (cell.getShip() != null && !isEnemy()) {
                        cell.setFill(Color.WHITE);
                        cell.setStroke(Color.GREEN);
                    } else {
                        cell.setFill(new Color(0.1, 0.3, 0.8, 0.6));
                        cell.setStroke(Color.BLACK);
                    }
                }
            }
        });
    }
}