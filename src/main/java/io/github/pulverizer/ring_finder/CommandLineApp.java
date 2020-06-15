package io.github.pulverizer.ring_finder;

import io.github.pulverizer.ring_finder.objects.System;
import io.github.pulverizer.ring_finder.utils.BeltType;
import io.github.pulverizer.ring_finder.utils.ReserveLevel;
import io.github.pulverizer.ring_finder.utils.SecurityLevel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Set;

public class CommandLineApp {
    private static final PrintStream OUTPUT = java.lang.System.out;
    private static final Scanner scan = new Scanner(java.lang.System.in);

    private static DataFilter dataFilter;

    public static void main(String[] args) {
        OUTPUT.println("***          Welcome to ED: Mining Assistant         ***");
        OUTPUT.println();

        dataFilter = new DataFilter();

        runMenu();

        OUTPUT.println();
        OUTPUT.println("***     Thank you for using ED: Mining Assistant     ***");
    }

    private static void printMenu() {

    }

    private static void runMenu() {
        printMenu();

        /*try {
            scan.nextInt();
            scan.nextLine();
        } catch (Exception e) {
            OUTPUT.println();
            OUTPUT.println("Invalid Input - Please try again.");
        }*/

        searchPopulatedSystemsOnly();
        selectBeltType();
        selectMinimumBeltReserveLevel();
        selectMinimumSizeDensityIndex();
        selectMinimumDensity();

        setShouldRefreshData();

        getAndPrintResults();
    }

    private static void searchPopulatedSystemsOnly() {
        boolean result = true;

        // Warn about file size, download time, RAM usage
        dataFilter.setShouldSearchOnlyPopulatedSystems(result);

        if (result) {
            selectMinimumSecurityLevel();
        }
    }

    private static void selectMinimumSecurityLevel() {
        dataFilter.setMinimumSecurityLevel(SecurityLevel.Medium);
    }

    private static void selectBeltType() {
        dataFilter.setBeltType(BeltType.Icy);
    }

    private static void selectMinimumBeltReserveLevel() {
        dataFilter.setMinimumReserveLevel(ReserveLevel.NULL);
    }

    private static void selectMinimumSizeDensityIndex() {
        dataFilter.setMinimumSizeDensityIndex(40000f);
    }

    private static void selectMinimumDensity() {
        dataFilter.setMinimumDensity(10.002f);
    }

    private static void setShouldRefreshData() {
        dataFilter.setShouldRefreshData(false);
    }

    private static void getAndPrintResults() {
        try {

            if (dataFilter.shouldRefreshData()) {
                OUTPUT.println("Downloading EDSM nightly dump...");
                dataFilter.downloadNightlyDump();
                OUTPUT.println("File downloaded.");

                OUTPUT.println("Attempting file decompression...");
                dataFilter.unzipFile();
                OUTPUT.println("File Decompressed.");
            }

            long fileLength = dataFilter.shouldSearchOnlyPopulatedSystems() ? DataFilter.POPULATED_FILE.length() : DataFilter.OTHER_FILE.length();
            OUTPUT.println("Loading JSON... (" + ((fileLength / 1024) / 1024) + " MB)");
            dataFilter.loadJSON();
            OUTPUT.println();
            OUTPUT.println("Total of " + dataFilter.getSystemCount() + " systems.");
            OUTPUT.println();
            OUTPUT.println(dataFilter.getData().size() + " systems matching filters.");
            OUTPUT.println();

            printResult(dataFilter.getData());

            OUTPUT.println();
            OUTPUT.println("Press ENTER to Exit");
            scan.nextLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResult(Set<System> data) {
        OUTPUT.println("Total of " + data.size() + " recommended systems.");

        data.forEach(system -> {
            OUTPUT.println();
            OUTPUT.println("====================");
            OUTPUT.println();
            OUTPUT.println(system.getName());
            OUTPUT.println("Security: " + system.getSecurity());
            OUTPUT.println("Superpower: " + system.getAllegiance());

            system.bodies().forEach(body -> {
                OUTPUT.println();
                OUTPUT.println(body.getName());
                OUTPUT.println("Reserve Level: " + body.getReserveLevel());
                OUTPUT.println("Distance from Arrival Point: " + body.getDistance() + " Ls");

                body.belts().forEach(belt -> {
                    OUTPUT.println();
                    OUTPUT.println(belt.getName());
                    // Mass * Density
                    OUTPUT.println("Size Density Index: " + String.format("%.2f", belt.getSizeDensityIndex()) + " Million");
                    OUTPUT.println("Density: " + String.format("%.5f", belt.getDensity()) + " Mt/Km^2");
                });
            });
        });
    }
}
