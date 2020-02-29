package game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HighScores {

    public static final int SCORES_TO_SAVE = 10;

    static final String HIGHSCORES_FILENAME = "scores.dat";

    private ArrayList<HighScore> highScores = null;
    
    private static HighScores instance = null;
    
    public static HighScores getInstance() {
        if (instance == null) {
            instance = new HighScores();
        }
        return instance;
    }

    private HighScores() {
        importScores();
        if (highScores == null) {
            highScores = new ArrayList<>(SCORES_TO_SAVE);
            for (int i = 0; i < SCORES_TO_SAVE; i++) {
                highScores.add(new HighScore("-", -1, false));
            }
        }
    }

    public void add(String name, int score, boolean gameWon) {
        HighScore newHighScore = new HighScore(name, score, gameWon);
        highScores.add(newHighScore);
        highScores.sort(Collections.reverseOrder((hs1, hs2) -> {
            int comparison = hs1.getPoints() - hs2.getPoints();
            if (comparison == 0) {
                if (hs1.isGameWon() && !hs2.isGameWon()) {
                    return +1;
                } else if (!hs1.isGameWon() && hs2.isGameWon()) {
                    return -1;
                } else {    // In case of same game outcome, the newly added high score has an advantage over the existing ones.
                    if (hs1 == newHighScore) {
                        return +1;
                    } else if (hs2 == newHighScore) {
                        return -1;
                    }
                    return comparison;  // 0
                }
            } else {
                return comparison;
            }
        }));
        if (highScores.size() > SCORES_TO_SAVE) {
            highScores.remove(highScores.size() - 1);
        }
        exportScores();
    }

    public List<HighScore> getScores() {
        return Collections.unmodifiableList(highScores);
    }

    public static class HighScore implements Serializable {

        private String name;
        private int points;
        private boolean gameWon;

        public HighScore(String name, int score, boolean gameWon) {
            this.name = name;
            this.points = score;
            this.gameWon = gameWon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public boolean isGameWon() {
            return gameWon;
        }

        public void setGameWon(boolean gameWon) {
            this.gameWon = gameWon;
        }

    }

    void importScores() {
        ObjectInputStream inStream = null;
        try {
            inStream = new ObjectInputStream(new FileInputStream(HIGHSCORES_FILENAME));
            highScores = (ArrayList<HighScore>) inStream.readObject();
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

    void exportScores() {
        ObjectOutputStream outStream = null;
        try {
            outStream = new ObjectOutputStream(new FileOutputStream(HIGHSCORES_FILENAME));
            outStream.writeObject(highScores);
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
