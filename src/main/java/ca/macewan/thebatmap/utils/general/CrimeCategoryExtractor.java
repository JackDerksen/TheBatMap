package ca.macewan.thebatmap.utils.general;

import ca.macewan.thebatmap.utils.models.CrimeData;
import ca.macewan.thebatmap.utils.parsers.ParseCrime;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to extract and analyze unique crime categorizations from the dataset.
 * We can delete this later, I just used it to get all the crime types for grouping purposes
 */
public class CrimeCategoryExtractor {

    /**
     * Extracts all unique occurrence categories, groups, and types from the crime dataset
     * and prints them in a structured format
     */
    public static void extractUniqueCategories() {
        try {
            // Load crime data
            List<CrimeData> crimes = ParseCrime.parseCrimeData();
            System.out.println("Loaded " + crimes.size() + " crime records");

            // Extract unique values
            Set<String> categories = new TreeSet<>();
            Set<String> groups = new TreeSet<>();
            Set<String> types = new TreeSet<>();

            // Category to groups mapping
            Map<String, Set<String>> categoryToGroups = new TreeMap<>();
            // Group to types mapping
            Map<String, Set<String>> groupToTypes = new TreeMap<>();

            // Extract all unique values and build mappings
            for (CrimeData crime : crimes) {
                String category = crime.getOccurrenceCategory();
                String group = crime.getOccurrenceGroup();
                String type = crime.getOccurrenceTypeGroup();

                if (category != null) {
                    categories.add(category);

                    // Create mappings
                    if (group != null) {
                        categoryToGroups.computeIfAbsent(category, k -> new TreeSet<>()).add(group);
                        groups.add(group);

                        if (type != null) {
                            groupToTypes.computeIfAbsent(group, k -> new TreeSet<>()).add(type);
                            types.add(type);
                        }
                    }
                }
            }

            // Print results in a structured format
            System.out.println("\n=== CRIME CATEGORIES HIERARCHY ===\n");

            System.out.println("TOTAL UNIQUE CATEGORIES: " + categories.size());
            System.out.println("TOTAL UNIQUE GROUPS: " + groups.size());
            System.out.println("TOTAL UNIQUE TYPES: " + types.size());

            System.out.println("\n=== CATEGORIES ===");
            for (String category : categories) {
                System.out.println("- " + category);
            }

            System.out.println("\n=== CATEGORY TO GROUPS MAPPING ===");
            for (Map.Entry<String, Set<String>> entry : categoryToGroups.entrySet()) {
                System.out.println("\nCATEGORY: " + entry.getKey());
                for (String group : entry.getValue()) {
                    System.out.println("  └─ " + group);
                }
            }

            System.out.println("\n=== GROUP TO TYPES MAPPING ===");
            for (Map.Entry<String, Set<String>> entry : groupToTypes.entrySet()) {
                System.out.println("\nGROUP: " + entry.getKey());
                for (String type : entry.getValue()) {
                    System.out.println("  └─ " + type);
                }
            }

            // Print full hierarchy
            System.out.println("\n=== FULL HIERARCHY ===");
            for (String category : categoryToGroups.keySet()) {
                System.out.println("\nCATEGORY: " + category);

                Set<String> groupsForCategory = categoryToGroups.get(category);
                for (String group : groupsForCategory) {
                    System.out.println("  └─ GROUP: " + group);

                    Set<String> typesForGroup = groupToTypes.getOrDefault(group, Collections.emptySet());
                    for (String type : typesForGroup) {
                        System.out.println("      └─ TYPE: " + type);
                    }
                }
            }

            // Print record counts by category
            System.out.println("\n=== RECORD COUNTS BY CATEGORY ===");
            Map<String, Long> categoryCounts = crimes.stream()
                    .filter(c -> c.getOccurrenceCategory() != null)
                    .collect(Collectors.groupingBy(CrimeData::getOccurrenceCategory, Collectors.counting()));

            categoryCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));

        } catch (IOException e) {
            System.err.println("Error extracting crime categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        extractUniqueCategories();
    }
}