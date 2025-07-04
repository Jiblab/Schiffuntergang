package org.example.schiffuntergang.components;

import javafx.application.Platform;
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

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Gamefield extends GridPane {
    private final List<Ships> placedShip = new ArrayList<>();
    private final int lang;
    private final int breit;
    private final boolean enemy;
    private final Cell[][] cells;
    private int usedCells = 0;
    private HelloController control;
    private EnemyPlayer en;
    private MultiplayerLogic lo;

    private boolean multiplayer = false;


    public Gamefield(boolean enemy, HelloController controler, int h, int b) {
        lang = h;
        breit = b;
        cells = new Cell[h][b];
        this.enemy = enemy;
        this.control = controler;
        setupBackground();

        for (int i = 0; i < h; i++) {

            for (int j = 0; j < b; j++) {
                Cell c = new Cell(j, i, this, 30, 30, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(new Color(0.1, 0.3, 0.8, 0.6));

                final int x = i;
                final int y = j;
                // Hier ein OnClickListener setzen, um jeden Klick abzufangen :P
                // Ihr könnt hier dann mehrere Fälle einbauen wie rechtsklick zum Löschen etc...
                c.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !enemy) {

                        if (getUsedCells() + control.getLength() <= maxShipsC()) { //+ control.getlength damit das auf auf neue schiffe prüft
                            Ships ship = new Ships(control.getLength(), control.getLength());
                            if (placeShip(ship, y, x, control.getDirection())) {
                                increaseCells(ship.getLength());
                                control.updateRemainingCellsDisplay();

                            }
                        } else {
                            System.out.println(maxShipsC());
                            System.out.println("Maximale Anzahl an schiffen erreicht");
                        }


                    } else if (event.getButton() == MouseButton.PRIMARY && enemy && control.getReady()) {
                        // shoot((int) c.getX(), (int) c.getY());
                        shoot(c.x, c.y);
                    }
                });

                add(c, j, i);
            }
        }
    }


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
                Cell c = new Cell(j, i, this, 30, 30, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(new Color(0.1, 0.3, 0.8, 0.6));
                final int x = i;
                final int y = j;
                // Hier ein OnClickListener setzen, um jeden Klick abzufangen :P
                // Ihr könnt hier dann mehrere Fälle einbauen wie rechtsklick zum Löschen etc...
                c.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !enemy) {

                        if (getUsedCells() <= maxShipsC()) {
                            Ships ship = new Ships(control.getLength(), control.getLength());
                            if (placeShip(ship, y, x, control.getDirection())) {
                                increaseCells(ship.getLength());

                            }
                        } else {
                            System.out.println(maxShipsC());
                            System.out.println("Maximale Anzahl an schiffen erreicht");
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Limit erreicht");
                            alert.setHeaderText("Maximale Anzahl an Schiffen platziert.");
                            alert.setContentText("Sie können keine weiteren Schiffe hinzufügen.");
                            alert.show();
                        }


                    } else if (event.getButton() == MouseButton.PRIMARY && enemy && control.getReady()) {
                        shoot(y, x); //hier geswapped
                    }
                });

                add(c, j, i);
            }
        }
    }

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
                Cell c = new Cell(j, i, this, 30, 30, controler);
                cells[i][j] = c;

                c.setStroke(Color.BLACK);
                c.setFill(new Color(0.1, 0.3, 0.8, 0.6));
                final int x = i;
                final int y = j;
                // Hier ein OnClickListener setzen, um jeden Klick abzufangen :P
                // Ihr könnt hier dann mehrere Fälle einbauen wie rechtsklick zum Löschen etc...
                c.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !enemy) {

                        int len = control.getLength();
                        System.out.println(len);

                        boolean isServer = controler.getHost(); // Der Server ist NICHT im Client-Modus

                        if (isServer) {
                            // --- SERVER-LOGIK: Prüft gegen das Gesamtlimit an Zellen ---
                            if (getUsedCells() + len <= maxShipsC()) {
                                Ships ship = new Ships(len, len);
                                // Wichtig: Parameter an placeShip sind (startX, startY), also (Spalte, Reihe) -> (y, x)
                                if (placeShip(ship, y, x, control.getDirection())) {
                                    increaseCells(len);
                                    control.updateRemainingCellsDisplay(); // Label für verbleibende Punkte aktualisieren
                                }
                            } else {
                                // Server hat das Limit erreicht
                                System.out.println("Limit an Bau-Punkten erreicht!");
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING);
                                    alert.setTitle("Limit erreicht");
                                    alert.setHeaderText("Maximale Anzahl an Schiffen platziert.");
                                    alert.setContentText("Sie können keine weiteren Schiffe hinzufügen.");
                                    alert.showAndWait();
                                });
                            }
                        } else {
                            // --- CLIENT-LOGIK: Prüft gegen die vom Server gesendeten Regeln ---

                            if (control.canClientPlaceShip(len)) {
                                Ships ship = new Ships(len, len);
                                // Wichtig: Parameter an placeShip sind (startX, startY), also (Spalte, Reihe) -> (y, x)
                                if (placeShip(ship, y, x, control.getDirection())) {
                                    increaseCells(len); // Zählt trotzdem die Zellen für Konsistenz
                                    // Ruft die spezielle Update-Methode für den Client auf
                                    control.clientPlacedShip(len);
                                }
                            } else {
                                // Client hat das Limit für diese Schiffslänge erreicht
                                System.out.println("Von Schiffslänge " + len + " können keine mehr platziert werden.");
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Limit für diese Länge erreicht");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Sie haben bereits die maximale Anzahl an Schiffen der Länge " + len + " platziert.");
                                    alert.showAndWait();
                                });
                            }
                        }

                        // ---- LOGIK FÜR DAS GEGNER-FELD (`enemy`) ----
                    } else if (event.getButton() == MouseButton.PRIMARY && this.enemy) {
                        // Diese Logik ist nur im Multiplayer relevant
                        if (lo != null && control.getReady() && lo.getTurn()) {
                            // Wir schießen auf die angeklickte Koordinate (Spalte y, Reihe x)
                            lo.setX(y); // lo.setX erwartet die Spalten-Koordinate
                            lo.setY(x); // lo.setY erwartet die Reihen-Koordinate
                            System.out.println("Schuss wird vorbereitet auf: Spalte " + y + ", Reihe " + x);
                            try {
                                lo.startShoot();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });

                add(c, j, i);
            }
        }
    }

    private void setupBackground() {
        try {
            // Load the image from resources. Make sure the path is correct!
            Image backgroundImage = new Image(getClass().getResource("/images/Boardbg.png").toExternalForm());

            BackgroundImage bgImage = new BackgroundImage(backgroundImage,
                    BackgroundRepeat.NO_REPEAT, // Don't tile the image
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,  // Center the image
                    new BackgroundSize(100, 100, true, true, false, true) // Stretch to cover the whole pane
            );

            this.setBackground(new Background(bgImage));
        } catch (Exception e) {
            System.err.println("Failed to load background image for Gamefield.");
            // e.printStackTrace(); // Uncomment for debugging
        }
    }

    public static Gamefield fromData(GamefieldData data, HelloController controller, MultiplayerLogic logic) {
        Gamefield board;
        if (logic != null) {
            board = new Gamefield(data.isEnemy(), controller, data.getHeight(), data.getWidth(), logic);
        } else {
            // Adjust this if you have a separate constructor for single-player AI
            System.out.println("height: " + data.getHeight() + " width: " + data.getWidth());
            board = new Gamefield(data.isEnemy(), controller, data.getHeight(), data.getWidth());
        }

        for (SerializableShip shipData : data.getShips()) {
            Ships ship = new Ships(shipData.getLength(), shipData.getHealth());
            board.placeShip(ship, shipData.getStartX(), shipData.getStartY(), shipData.isVertical());
            board.increaseCells(ship.getLength()); // Ensure usedCells count is correct
        }

        // Re-apply all the shots
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

        return board;
    }

    public boolean getStatus() {
        return enemy;
    }

    public Cell getCell(int x, int y) {
        // Add boundary checks to prevent crashes!
        if (x >= 0 && x < breit && y >= 0 && y < lang) {
            return cells[y][x];
        }
        return null;
    }

    public boolean isEnemy() {
        return enemy;
    }

    public double maxShipsC() {
        return (double) lang * (double) breit * 0.3;
    }

    public void addShip(Ships ship) {
        placedShip.add(ship);
    }

    public int getUsedCells() {
        return usedCells;
    }

    public void increaseCells(int laenge) {
        usedCells += laenge;
    }

    public void shoot(int x, int y) {

        Cell c = getCell(x, y);
        if (c == null) return; // Sicherheitsprüfung

        // Fall 1: Feld wurde bereits beschossen
        if (c.isShot()) {
            System.out.println("Bereits beschossen");
            // Nur eine Nachricht anzeigen, wenn der Spieler selbst auf ein bereits beschossenes Feld klickt
            if (this.enemy && control != null) {
                control.showNotification("Dieses Feld wurde bereits beschossen!", "info");
            }
            return;
        }

        c.setShot(true);
        Ships s = c.getShip();

        // Fall 2: Ein Schiff wurde getroffen (s ist nicht null)
        if (s != null) {
            s.hit();
            c.setFill(Color.RED);
            // SoundEffect.play("hit.wav");

            if (s.getHealth() == 0) {
                // Fall 2a: Schiff ist versenkt
                if (control != null) control.showNotification("Schiff versenkt!", "sunk");
                deleteShip();
            } else {
                // Fall 2b: Normaler Treffer
                if (control != null) control.showNotification("Treffer!", "hit");
            }
        }
        // Fall 3: Fehlschuss (s ist null)
        else {
            c.setFill(Color.BLACK);
            // SoundEffect.play("miss.wav");
            if (control != null) control.showNotification("Verfehlt!", "miss");
        }

        // Fall 4: Gegner ist im Einzelspielermodus am Zug (nach Treffer ODER Fehlschuss)
        if (this.enemy && !multiplayer) {
            System.out.println("Einzelspieler: Gegner ist am Zug.");
            en.revenge();
        }
    }

    public boolean placeShip(Ships ship, int startX, int startY, boolean vertical) {
        int length = ship.getLength();

        // --- PHASE 1: Alle Zellen prüfen (Rand und Kollision) ---
        for (int i = 0; i < length; i++) {
            // Korrekte, klare Berechnung der Koordinaten
            int reihenIndex = vertical ? startY + i : startY;  // Y-Koordinate
            int spaltenIndex = vertical ? startX : startX + i; // X-Koordinate

            Cell cellToCheck = getCell(spaltenIndex, reihenIndex);

            if (cellToCheck == null || cellToCheck.getShip() != null) {
                System.out.println("Fehler: Platzierung nicht möglich bei (Reihe " + reihenIndex + ", Spalte " + spaltenIndex + ").");
                return false;
            }
        }

        // --- PHASE 2: Schiff platzieren (logisch und visuell) ---
        // Dieser Code wird nur erreicht, wenn alle Prüfungen erfolgreich waren.
        for (int i = 0; i < length; i++) {
            int reihenIndex = vertical ? startY + i : startY;
            int spaltenIndex = vertical ? startX : startX + i;

            Cell cellToPlaceOn = getCell(spaltenIndex, reihenIndex);
            cellToPlaceOn.setShip(ship);
        }
        if (!enemy) { // Nur Bilder auf dem Spieler-Feld anzeigen
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
                    // Wichtig: Verhindert, dass das Bild gestreckt wird, falls die Proportionen nicht exakt stimmen.
                    shipView.setPreserveRatio(false);

                    int colSpan = 1;
                    int rowSpan = 1;

                    // 2. Setze die korrekten Dimensionen für das Layout
                    if (vertical) {
                        // Ein vertikales Schiff ist 30px breit und 'length * 30' hoch
                        shipView.setFitWidth(30.0);
                        shipView.setFitHeight(length * 30.0);
                        rowSpan = length;

                    } else {
                        // Ein horizontales Schiff ist 'length * 30' breit und 30px hoch
                        shipView.setFitWidth(length * 30.0);
                        shipView.setFitHeight(30.0);
                        colSpan = length;

                    }
                    final int fcolSpan = colSpan;
                    final int frowSpan = rowSpan;
                    ship.setShipImageView(shipView);
                    Platform.runLater(() -> {
                        // Diese Zeile verursacht den Fehler, wenn sie nicht im runLater-Block ist.
                        this.add(shipView, startX, startY, fcolSpan, frowSpan);
                    });
                } catch (Exception e) {
                    System.err.println("Fehler beim Laden des Schiff-Bildes: " + imagePath);
                    e.printStackTrace();
                }
            }
        }
        ship.setDirection(vertical);
        addShip(ship);
        System.out.println("Schiff platziert: Start(Reihe " + startX + ", Spalte " + startY + "), " + (vertical ? "vertikal" : "horizontal"));
        return true;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }

    public int getLang() {
        return lang;
    }

    public int getBreit() {
        return breit;
    }

    private boolean turn;

    public void deleteShip() {
        Iterator<Ships> iterator = placedShip.listIterator();
        while (iterator.hasNext()) {
            Ships schiff = iterator.next();
            if (!schiff.isAlive()) {
                iterator.remove();
            }
        }

        if (placedShip.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Spiel beendet");
                alert.setHeaderText(null);
                if (enemy) {
                    alert.setContentText("Du gewinnst");
                } else {
                    alert.setContentText("Gegner gewinnt");
                }

                alert.showAndWait();
            });
        }
    }

    public void setTurn(boolean t) {
        turn = t;
    }

    public void setLogic(MultiplayerLogic l) {
        lo = l;
    }

    public int[] getShipLengths() {
        int[] lengths = new int[placedShip.size()];
        for (int i = 0; i < placedShip.size(); i++) {
            lengths[i] = placedShip.get(i).getLength();
        }
        return lengths;
    }

    public double getMusicVolume() {
        return BackgroundMusic.getInstance().getVolume();
    }

    public boolean isMusicEnabled() {
        return BackgroundMusic.getInstance().isPlaying();
    }

    public List<Ships> getShips() {
        return placedShip;
    }

    public void increaseUsedCells(int length) {
        this.usedCells += length;
    }

    public static Gamefield fromGameState(GameState state, boolean isEnemy) {
        // Dummy-Controller, da dieser im Konstruktor benötigt wird
        HelloController dummyController = new HelloController();

        // Gamefield erzeugen (ohne AI, da Player)
        Gamefield board = new Gamefield(isEnemy, dummyController,
                state.getPlayerBoardData().getHeight(), state.getPlayerBoardData().getWidth());

        // Schiffe rekonstruieren
        for (SerializableShip s : state.getPlayerBoardData().getShips()) {
            Ships ship = new Ships(s.getLength(), s.getHealth());
            if (board.placeShip(ship, s.getStartX(), s.getStartY(), s.isVertical())) {
                board.addShip(ship);
                board.increaseUsedCells(ship.getLength());
            }
        }

        // Getroffene Zellen wiederherstellen
        for (Position p : state.getPlayerBoardData().getShotPositions()) {
            Cell cell = board.getCell(p.getX(), p.getY());
            cell.setShot(true);
            cell.setFill(cell.getShip() != null
                    ? Color.RED
                    : Color.BLACK);
        }

        return board;
    }

    public HelloController getControl() {
        return control;
    }

    public void clearShips() {
        placedShip.clear();
        // ggf. auch das Spielfeld zurücksetzen, falls nötig
    }

    public void setEnemy(EnemyPlayer e) {
        en = e;
    }

    public boolean hasShip() {
        System.out.println(placedShip.isEmpty());
        return !placedShip.isEmpty();
    }

    public GamefieldData toData() {
        GamefieldData data = new GamefieldData();
        data.setWidth(this.breit);
        data.setHeight(this.lang);
        data.setEnemy(this.enemy);


        List<SerializableShip> shipDataList = new ArrayList<>();
        Set<Ships> processedShips = new HashSet<>(); // To avoid adding a ship multiple times

        for (int y = 0; y < breit; y++) {
            for (int x = 0; x < lang; x++) {

                Cell cell = getCell(x, y);
                Ships ship = cell.getShip();

                if (ship != null && !processedShips.contains(ship)) {
                    processedShips.add(ship); // Mark as processed
                    boolean isVertical = false;
                    // = (y + 1 < lang && getCell(x, y + 1).getShip() == ship) || y+1 >= breit || x+1 >= lang
                    if (y + 1 >= breit) {
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


        // --- Populate Shot Positions ---
        List<Position> shotPositions = new ArrayList<>();
        for (int y = 0; y < breit; y++) {
            for (int x = 0; x < lang; x++) {
                if (getCell(x, y).isShot()) {
                    shotPositions.add(new Position(x, y));
                }
            }
        }
        data.setShotPositions(shotPositions);

        return data;
    }

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

    public void setController(HelloController controller) {
        this.control = controller;
    }

    public int getShipCount() {
        return placedShip.size();
    }

    public void redrawAllCells() {
        // Dieser Aufruf stellt sicher, dass der gesamte Block auf dem
        // JavaFX Application Thread ausgeführt wird.
        Platform.runLater(() -> {
            System.out.println("DEBUG: Redraw für " + (isEnemy() ? "Gegner" : "Spieler") + "-Feld wird ausgeführt.");
            for (int y = 0; y < lang; y++) { // Reihen
                for (int x = 0; x < breit; x++) { // Spalten
                    Cell cell = getCell(x, y); // Holt cells[y][x]
                    if (cell == null) continue;

                    // Logik, um zu entscheiden, welche Farbe die Zelle haben soll
                    if (cell.isShot()) {
                        // Getroffene Felder sind immer rot oder schwarz
                        cell.setFill(cell.getShip() != null ? Color.RED : Color.BLACK);
                    } else if (cell.getShip() != null && !isEnemy()) {
                        // Nicht-getroffene, EIGENE Schiffe sind weiß
                        cell.setFill(Color.WHITE);
                        cell.setStroke(Color.GREEN);
                    } else {
                        // Leere Felder oder unentdeckte Gegner-Felder sind blau
                        cell.setFill(new Color(0.1, 0.3, 0.8, 0.6));
                        cell.setStroke(Color.BLACK);
                    }
                }
            }
        });
    }

}

