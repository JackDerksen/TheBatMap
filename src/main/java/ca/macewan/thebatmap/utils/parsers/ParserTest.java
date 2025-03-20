package ca.macewan.thebatmap.utils.parsers;

import ca.macewan.thebatmap.utils.models.CrimeData;
import ca.macewan.thebatmap.utils.models.PropertyData;

import java.io.IOException;
import java.util.List;

/**
 * Simple test class to verify CSV parsers are working correctly
 */
public class ParserTest {
    public static void main(String[] args) {
        testPropertyParser();
        testCrimeParser();
    }

    private static void testPropertyParser() {
        System.out.println("=== Testing Property Data Parser ===");
        try {
            List<PropertyData> properties = ParseProperties.parsePropertyData();
            System.out.println("Successfully parsed " + properties.size() + " property records");

            // Print first 5 records (or fewer if less than 5 exist)
            int count = Math.min(properties.size(), 5);
            for (int i = 0; i < count; i++) {
                PropertyData property = properties.get(i);
                System.out.println("\nProperty #" + (i + 1));
                System.out.println("Account: " + property.getAccountNumber());
                System.out.println("Address: " + property.getAddress());
                System.out.println("Neighbourhood: " + property.getNeighbourhood());
                System.out.println("Value: $" + property.getAssessment());
                System.out.println("Location: " + property.getLocation());
            }
        } catch (IOException e) {
            System.err.println("Error testing property parser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testCrimeParser() {
        System.out.println("\n=== Testing Crime Data Parser ===");
        try {
            List<CrimeData> crimes = ParseCrime.parseCrimeData();
            System.out.println("Successfully parsed " + crimes.size() + " crime records");

            // Print first 5 records (or fewer if less than 5 exist)
            int count = Math.min(crimes.size(), 5);
            for (int i = 0; i < count; i++) {
                CrimeData crime = crimes.get(i);
                System.out.println("\nCrime #" + (i + 1));
                System.out.println("ID: " + crime.getObjectId());
                System.out.println("Type: " + crime.getOccurrenceTypeGroup());
                System.out.println("Category: " + crime.getOccurrenceCategory());
                System.out.println("Group: " + crime.getOccurrenceGroup());
                System.out.println("Location: " + crime.getIntersection());
                System.out.println("Date: " + crime.getDateReported());
                System.out.println("Coordinates: " + crime.getLocation());
            }
        } catch (IOException e) {
            System.err.println("Error testing crime parser: " + e.getMessage());
            e.printStackTrace();
        }
    }
}