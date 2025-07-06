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
    private boolean turn;

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
                            if (placeShip(ship, y, x, control.getDirection())) {
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
                          /*  Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Limit erreicht");
                            alert.setHeaderText("Maximale Anzahl an Schiffen platziert.");
                            alert.setContentText("Sie können keine weiteren Schiffe hinzufügen.");
                            alert.show();*/
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
                               /* Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.WARNING);
                                    alert.setTitle("Limit erreicht");
                                    alert.setHeaderText("Maximale Anzahl an Schiffen platziert.");
                                    alert.setContentText("Sie können keine weiteren Schiffe hinzufügen.");
                                    alert.showAndWait();
                                });*/
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
                                /*Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Limit für diese Länge erreicht");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Sie haben bereits die maximale Anzahl an Schiffen der Länge " + len + " platziert.");
                                    alert.showAndWait();
                                });*/
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

    public boolean getStatus() {
        return enemy;
    }
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
    public Cell getCell(int x, int y) {
        if (x >= 0 && x < breit && y >= 0 && y < lang) {
            return cells[x][y];
        }
        return null;
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
          /*  Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                if (enemy) {
                    alert.setContentText("You Won! Congrats :)");
                } else {
                    alert.setContentText("Uh-oh, you lost :(");
                }

                alert.showAndWait();
            });*/
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
    public boolean placeShip(Ships ship, int startX, int startY, boolean vertical) {
        int length = ship.getLength();

        for (int i = 0; i < length; i++) {
            int reihenIndex = vertical ? startY + i : startY;
            int spaltenIndex = vertical ? startX : startX + i;

            Cell cellToCheck = getCell(spaltenIndex, reihenIndex);

            if (cellToCheck == null || cellToCheck.getShip() != null) {
                System.out.println("[Gamefield] Fehler: Platzierung nicht möglich bei (Reihe " + reihenIndex + ", Spalte " + spaltenIndex + ").");
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
    public HelloController getControl() {
        return control;
    }
    public void clearShips() {
        placedShip.clear();
    }
    public void setEnemy(EnemyPlayer e) {
        en = e;
    }
    public boolean hasShip() {
        System.out.println("[Gamefield] "+placedShip.isEmpty());
        return !placedShip.isEmpty();
    }
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

