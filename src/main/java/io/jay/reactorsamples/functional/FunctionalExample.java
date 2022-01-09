package io.jay.reactorsamples.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionalExample {
    public static void main(String[] args) {
        var names = List.of("alex", "ben", "chloe", "adam", "adam");
        var filteredNames = namesLongerThan(names, 3);
        System.out.println(filteredNames);

    }

    private static List<String> namesLongerThan(List<String> names, int i) {
        return names.stream()
                .filter(n -> n.length() > i)
                .distinct()
                .collect(Collectors.toList());
    }
}
