package io.jay.reactorsamples.imperative;

import java.util.ArrayList;
import java.util.List;

public class ImperativeExample {

    public static void main(String[] args) {
        var names = List.of("alex", "ben", "chloe", "adam", "adam");
        var filteredNames = namesLongerThan(names, 3);
        System.out.println(filteredNames);
    }

    private static List<String> namesLongerThan(List<String> names, int i) {
        var out = new ArrayList<String>();
        for (String n : names) {
            if (n.length() > i && !out.contains(n)) {
                out.add(n);
            }
        }
        return out;
    }
}
