package objects;

import java.util.Objects;

public class Word implements Comparable<Word> {
    private String text;
    private int id;
    private double frequency;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }


    public void increaseFrequency() {
        frequency++;
    }

    public void increaseFrequency(int n) {
        frequency += n;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.text);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Word{" + "text=" + text + ", id=" + id + ", frequency=" + frequency + '}';
    }

    @Override
    public int compareTo(Word other) {

        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this.id < other.id) {
            return BEFORE;
        } else {
            return AFTER;
        }

    }
}
