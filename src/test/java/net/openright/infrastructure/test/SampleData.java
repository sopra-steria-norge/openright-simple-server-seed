package net.openright.infrastructure.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleData {

    private static Random random = new Random();

    public static String sampleString(int numberOfWords) {
        List<String> words = new ArrayList<String>();
        for (int i = 0; i < numberOfWords; i++) {
            words.add(sampleWord());
        }
        return String.join(" ", words);
    }

    private static String sampleWord() {
        return random(new String[] { "foo", "bar", "baz", "qux", "quux", "quuuux" });
    }

    public static <T> T random(@SuppressWarnings("unchecked") T... alternatives) {
        return alternatives[random.nextInt(alternatives.length)];
    }

    public static double randomAmount() {
        return random.nextInt(10000) / 100.0;
    }

}
