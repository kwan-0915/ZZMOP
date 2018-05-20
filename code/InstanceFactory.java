package zzmop;

import weka.core.Attribute;
import weka.core.Instances;

import java.util.ArrayList;

public class InstanceFactory {
    public static int bucketHigh = 50;
    public static int pipCeilingHigh = 250;

    public static int bucketHighUntil = 50;
    public static int pipCeilingHighUntil = 250;

    public static int bucketLow = 10;
    public static int pipCeilingLow = 50;

    public static int bucketLowUntil = 10;
    public static int pipCeilingLowUntil = 50;

    public static int bucketLast = 50;
    public static int pipCeilingLast = 250;

    static Instances LastInstances = null;
    static Instances LowInstances = null;
    static Instances HighUntilInstances = null;
    static Instances LowUntilInstances = null;
    static Instances HighInstances = null;
    static Instances GenericInstances = null;

    static ArrayList<String> LastList = null;
    static ArrayList<String> HighUntilList = null;
    static ArrayList<String> HighList = null;
    static ArrayList<String> LowList = null;
    static ArrayList<String> LowUntilList = null;

    static ArrayList<Attribute> LastAttributes = null;
    static ArrayList<Attribute> HighAttributes = null;
    static ArrayList<Attribute> HighUntilAttributes = null;
    static ArrayList<Attribute> LowAttributes = null;
    static ArrayList<Attribute> LowUntilAttributes = null;
    static ArrayList<Attribute> GenericAttributes = null;

    public static ArrayList<Attribute> attributes = new ArrayList<>();
    static ArrayList<String> list = new ArrayList<String>();
    public static Instances instances = new Instances("TI-SVM", attributes,
            1000);
    static ArrayList<String> gradientList = null;
    static ArrayList<Attribute> gradientAttributes = null;
    static Instances gradientInstances = null;

    static {
        list.add("B");
        list.add("S");
    }

    static {
        attributes.add(new Attribute("RSI9"));
        attributes.add(new Attribute("EMA13"));
        attributes.add(new Attribute("EMA48"));
        attributes.add(new Attribute("SMA13"));
        attributes.add(new Attribute("SMA48"));
        attributes.add(new Attribute("MACD"));
        attributes.add(new Attribute("STOCH"));
        attributes.add(new Attribute("SAR"));
        attributes.add(new Attribute("CCI"));
        attributes.add(new Attribute("EMA13EMA48"));
        attributes.add(new Attribute("SMA13SMA48"));
        attributes.add(new Attribute("ICHIMOKU"));
        attributes.add(new Attribute("DIRECTION"));
        attributes.add(new Attribute("HIGHLOWCLOSENESS"));
        attributes.add(new Attribute("Decision", list));
    }

    static {
        instances.setClass(new Attribute("DECISION"));
    }

    public static Instances createGradientInstances(int categoryGranularity,
                                                    int INSTANCE_COUNT) {
        if (gradientInstances == null) {
            gradientList = new ArrayList<String>();
            for (int i = 0; i < categoryGranularity; i++) {
                gradientList.add("BBBBBBBBBBBBBBBBBBBB".substring(0, i + 1));
                gradientList.add("SSSSSSSSSSSSSSSSSSSS".substring(0, i + 1));
            }

            gradientAttributes = new ArrayList<>();
            gradientAttributes.add(new Attribute("RSI9"));
            gradientAttributes.add(new Attribute("EMA13"));
            gradientAttributes.add(new Attribute("EMA48"));
            gradientAttributes.add(new Attribute("SMA13"));
            gradientAttributes.add(new Attribute("SMA48"));
            gradientAttributes.add(new Attribute("MACD"));
            gradientAttributes.add(new Attribute("STOCH"));
            gradientAttributes.add(new Attribute("SAR"));
            gradientAttributes.add(new Attribute("CCI"));
            gradientAttributes.add(new Attribute("EMA13EMA48"));
            gradientAttributes.add(new Attribute("SMA13SMA48"));
            gradientAttributes.add(new Attribute("ICHIMOKU"));
            gradientAttributes.add(new Attribute("DIRECTION"));
            gradientAttributes.add(new Attribute("HIGHLOWCLOSENESS"));

            gradientAttributes.add(new Attribute("Decision", gradientList));

            gradientInstances = new Instances("TI-SVM", gradientAttributes,
                    INSTANCE_COUNT);
            gradientInstances
                    .setClassIndex(gradientInstances.numAttributes() - 1);
        }
        return gradientInstances;

    }

    public static Instances createHighUntilPatternInstances(int INSTANCE_COUNT) {
        HighUntilList = new ArrayList<String>();
        HighUntilList.add(0 + "");
        for (int i = 1; i <= pipCeilingHighUntil / bucketHighUntil; i++) {
            HighUntilList.add(bucketHighUntil * i + "");
        }

        HighUntilAttributes = new ArrayList<>();

        HighUntilAttributes.add(new Attribute("2P"));
        HighUntilAttributes.add(new Attribute("3P"));
        HighUntilAttributes.add(new Attribute("4P"));
        HighUntilAttributes.add(new Attribute("5P"));
        HighUntilAttributes.add(new Attribute("6P"));
        HighUntilAttributes.add(new Attribute("7P"));
        HighUntilAttributes.add(new Attribute("LENGTH"));
        HighUntilAttributes.add(new Attribute("RESULT", HighUntilList));

        HighUntilInstances = new Instances("TI-SVM", HighUntilAttributes,
                INSTANCE_COUNT);
        HighUntilInstances
                .setClassIndex(HighUntilInstances.numAttributes() - 1);
        return HighUntilInstances;
    }


    public static Instances createHighPatternInstances(int INSTANCE_COUNT) {
        HighList = new ArrayList<String>();
        HighList.add(0 + "");
        for (int i = 1; i <= pipCeilingHigh / bucketHigh; i++) {
            HighList.add(bucketHigh * i + "");
        }

        HighAttributes = new ArrayList<>();

        HighAttributes.add(new Attribute("2P"));
        HighAttributes.add(new Attribute("3P"));
        HighAttributes.add(new Attribute("4P"));
        HighAttributes.add(new Attribute("5P"));
        HighAttributes.add(new Attribute("6P"));
        HighAttributes.add(new Attribute("7P"));
        HighAttributes.add(new Attribute("LENGTH"));
        HighAttributes.add(new Attribute("RESULT", HighList));

        HighInstances = new Instances("TI-SVM", HighAttributes,
                INSTANCE_COUNT);
        HighInstances
                .setClassIndex(HighInstances.numAttributes() - 1);
        return HighInstances;
    }

    public static Instances createLowPatternInstances(int INSTANCE_COUNT) {
        LowList = new ArrayList<String>();
        LowList.add(0 + "");
        for (int i = 1; i <= pipCeilingLow / bucketLow; i++) {
            LowList.add(bucketLow * i + "");
        }

        LowAttributes = new ArrayList<>();

        LowAttributes.add(new Attribute("2P"));
        LowAttributes.add(new Attribute("3P"));
        LowAttributes.add(new Attribute("4P"));
        LowAttributes.add(new Attribute("5P"));
        LowAttributes.add(new Attribute("6P"));
        LowAttributes.add(new Attribute("7P"));
        LowAttributes.add(new Attribute("LENGTH"));
        LowAttributes.add(new Attribute("RESULT", LowList));

        LowInstances = new Instances("TI-SVM", LowAttributes, INSTANCE_COUNT);
        LowInstances.setClassIndex(LowInstances.numAttributes() - 1);
        return LowInstances;
    }

    public static Instances createLowUntilPatternInstances(int INSTANCE_COUNT) {
        LowUntilList = new ArrayList<String>();
        LowUntilList.add(0 + "");
        for (int i = 1; i <= pipCeilingLowUntil / bucketLowUntil; i++) {
            LowUntilList.add(bucketLowUntil * i + "");
        }

        LowUntilAttributes = new ArrayList<>();

        LowUntilAttributes.add(new Attribute("2P"));
        LowUntilAttributes.add(new Attribute("3P"));
        LowUntilAttributes.add(new Attribute("4P"));
        LowUntilAttributes.add(new Attribute("5P"));
        LowUntilAttributes.add(new Attribute("6P"));
        LowUntilAttributes.add(new Attribute("7P"));
        LowUntilAttributes.add(new Attribute("LENGTH"));
        LowUntilAttributes.add(new Attribute("RESULT", LowUntilList));

        LowUntilInstances = new Instances("TI-SVM", LowUntilAttributes, INSTANCE_COUNT);
        LowUntilInstances.setClassIndex(LowUntilInstances.numAttributes() - 1);
        return LowUntilInstances;
    }

    public static Instances createLastPatternInstances(int INSTANCE_COUNT) {
        LastList = new ArrayList<String>();
        LastList.add(0 + "");
        for (int i = 1; i <= pipCeilingLast / bucketLast; i++) {
            LastList.add(bucketLast * i + "");
            LastList.add(-1 * bucketLast * i + "");
        }

        LastAttributes = new ArrayList<>();

        LastAttributes.add(new Attribute("2P"));
        LastAttributes.add(new Attribute("3P"));
        LastAttributes.add(new Attribute("4P"));
        LastAttributes.add(new Attribute("5P"));
        LastAttributes.add(new Attribute("6P"));
        LastAttributes.add(new Attribute("7P"));
        LastAttributes.add(new Attribute("LENGTH"));
        LastAttributes.add(new Attribute("RESULT", LastList));

        LastInstances = new Instances("TI-SVM", LastAttributes, INSTANCE_COUNT);
        LastInstances.setClassIndex(LastInstances.numAttributes() - 1);
        return LastInstances;
    }


    public static Instances createGenericPatternInstances(int INSTANCE_COUNT, int PCount) {
        GenericAttributes = new ArrayList<>();

        for (int i = 2; i <= PCount; i++) {
            GenericAttributes.add(new Attribute(i + "P"));
        }

        GenericAttributes.add(new Attribute("QUALITY"));
        GenericInstances = new Instances("TI-SVM", GenericAttributes,
                INSTANCE_COUNT);
        return GenericInstances;
    }
}
