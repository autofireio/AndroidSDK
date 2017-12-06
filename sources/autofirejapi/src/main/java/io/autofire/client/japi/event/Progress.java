package io.autofire.client.japi.event;

public class Progress extends GameEvent {
    public static final String PROGRESS_NAME = "PROGRESS";
    public static final String PROGRESS_LEVEL_NAME = "level";

    private String level;
    private int score;

    public Progress(String level, int score) {
        super(PROGRESS_NAME);
        this.level = level;
        this.score = score;
        withPredefinedFeature(PROGRESS_LEVEL_NAME, level);
        withPredefinedFeature("score", score);
    }

    public String getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }
}
