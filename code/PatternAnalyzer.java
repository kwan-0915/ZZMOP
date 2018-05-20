package zzmop;

import jforex.devicelocal.Constants;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

public class PatternAnalyzer {

    public static PatternClusterer clusterer;
    static String fileKey = "zz-quality-7";
    static String patternsToTestFileKey = "zz-quality-7";

    static HashMap<Date, HSResult> patternsToTest;
    static HashMap<Date, HSResult> storedPatterns;
    static HashMap<HSResult, Date> storedDates;
    static HashMap<HSResult, Date> storedTestDates;

    public static void createClassifiers() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(Constants.jforex_home
                    + "\\objects\\" + fileKey + "-patterns.java"));

            ObjectInputStream oos = new ObjectInputStream(fis);
            Object sp = oos.readObject();
            oos.close();

            storedPatterns = (HashMap<Date, HSResult>) sp;

            storedDates = new HashMap<>();

            Set<Entry<Date, HSResult>> patternEntrySet = storedPatterns
                    .entrySet();
            for (Entry<Date, HSResult> entry : patternEntrySet) {
                Date date = (Date) entry.getKey();
                HSResult result = (HSResult) entry.getValue();
                storedDates.put(result, date);
            }

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.setMinimumFractionDigits(5);
            Collection<HSResult> results = storedPatterns.values();

            clusterer = new PatternClusterer(results);
            clusterer.clusterInstances();

            storeClustererToFile();
            clusterer.evaluateClusterer(true, storedDates);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static void storeClustererToFile() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(
                    Constants.jforex_home + "\\objects\\" + fileKey
                            + "-clusterer.java"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(clusterer);
            oos.close();
            fos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void loadClusterer() {

        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(Constants.jforex_home
                    + "\\objects\\" + fileKey + "-clusterer.java"));
            ObjectInputStream oos = new ObjectInputStream(fis);
            Object sp = oos.readObject();
            oos.close();
            clusterer = (PatternClusterer) sp;
            InstanceFactory.createGenericPatternInstances(
                    clusterer.clusteringInstances.size(), Integer
                            .parseInt(fileKey.substring(fileKey.length() - 1,
                                    fileKey.length())));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void loadPatternsToTestAgainstClusterer() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(Constants.jforex_home
                    + "\\objects\\" + patternsToTestFileKey + "-patterns.java"));

            ObjectInputStream oos = new ObjectInputStream(fis);
            Object sp = oos.readObject();
            oos.close();

            patternsToTest = (HashMap<Date, HSResult>) sp;

            storedTestDates = new HashMap<>();

            Set<Entry<Date, HSResult>> patternEntrySet = patternsToTest
                    .entrySet();
            for (Entry<Date, HSResult> entry : patternEntrySet) {
                Date date = (Date) entry.getKey();
                HSResult result = (HSResult) entry.getValue();
                storedTestDates.put(result, date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        createClassifiers();

        loadClusterer();
        try {
            clusterer.evaluateClusterer(false, storedDates);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("---------------------------------------------------");

        loadPatternsToTestAgainstClusterer();
        try {
            clusterer.evaluatePredefinedPatternsClusterer(patternsToTest
                    .values());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
