package ru.stacymay.wordsstartingwithlettergame;

public class Word {

    private String category;
    private String word;
    private boolean correct;

    public Word(String category, String word, boolean correct) {
        this.category = category;
        this.word = word;
        this.correct = correct;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}
