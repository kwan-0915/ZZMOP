package zzmop;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PatternClusterer implements Serializable {

    private static final long serialVersionUID = 3206151389510461337L;
    private EM EMClusterer = new EM();
    public Instances clusteringInstances;

    ClusterEvaluation eval = new ClusterEvaluation();
    DecimalFormat decimalFormatter;
    private DateFormat excelDateFormat;
    Object[] patternArray;

    static int bestShortCluster = -1;
    static int bestLongCluster = -1;

    static public double[] shortEfficiencies;
    static public double[] longEfficiencies;
    static public double[] efficiencyDifferences;
    static public double[] averageTPforLong;
    static public double[] averageSLforLong;
    static public double[] averageTPforShort;
    static public double[] averageSLforShort;
    static public int[] efficiencyDifferenceIndexes;
    static public int[] supports;

    public PatternClusterer(Collection<HSResult> patterns) {
        patternArray = patterns.toArray();
        clusteringInstances = InstanceFactory.createGenericPatternInstances(
                patterns.size(), ((HSResult) patternArray[0]).pointList.length);
        for (HSResult result : patterns) {
            Instance instance = result.generateGenericResultInstance();
            clusteringInstances.add(instance);
        }
        decimalFormatter = (DecimalFormat) NumberFormat
                .getNumberInstance(Locale.GERMAN);
        decimalFormatter.setMinimumFractionDigits(5);

        excelDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    }

    @Override
    public String toString() {
        String s = "";
        return s;
    }

    public void clusterInstances() throws Exception {

        EMClusterer.setNumClusters(39);
        EMClusterer.buildClusterer(clusteringInstances);
    }

    public int clusterInstance(Instance instance) {
        try {
            return EMClusterer.clusterInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void evaluateClusterer(boolean verbose, HashMap<HSResult, Date> evaluationDates) throws Exception {
        eval.setClusterer(EMClusterer);
        eval.evaluateClusterer(clusteringInstances);

        int patternLength = ((HSResult) patternArray[0]).pointList.length;
        double[] instanceClusters = eval.getClusterAssignments();

        if (verbose) {
            System.out.print("Date\t");

            for (int a = 0; a < patternLength; a++)
                System.out.print("P" + (a + 1) + "\t");

            System.out.print("HU\tL\tLU\tH\tLST\tLen\t");
            for (int a = 0; a < patternLength; a++)
                System.out.print("P" + (a + 1) + "S\t");

            System.out.println("QUALITY\tCLSTR");

            for (int i = 0; i < eval.getNumClusters(); i++) {

                for (int j = 0; j < clusteringInstances.size(); j++) {
                    if (instanceClusters[j] == i) {

                        HSResult result = (HSResult) patternArray[j];
                        Date patternDate = evaluationDates
                                .get(result);

                        System.out.print(excelDateFormat.format(patternDate)
                                + "\t");
                        for (double d : result.pointList)
                            System.out.print(decimalFormatter.format(d) + "\t");

                        System.out.print(decimalFormatter
                                .format(result.highuntil) + "\t");
                        System.out.print(decimalFormatter.format(result.low)
                                + "\t");
                        System.out.print(decimalFormatter
                                .format(result.lowuntil) + "\t");
                        System.out.print(decimalFormatter.format(result.high)
                                + "\t");
                        System.out.print(decimalFormatter.format(result.last)
                                + "\t");
                        System.out.print((result.left - result.right) + "\t");

                        double[] values = clusteringInstances.get(j)
                                .toDoubleArray();
                        System.out.print("0\t");
                        for (double d : values) {
                            System.out.print(decimalFormatter.format(d) + "\t");
                        }
                        System.out.println(i);
                    }
                }
            }
        } else {

            double bestShortEfficiency = 0;
            double bestLongEfficiency = 0;

            shortEfficiencies = new double[eval.getNumClusters()];
            longEfficiencies = new double[eval.getNumClusters()];
            efficiencyDifferences = new double[eval.getNumClusters()];
            averageTPforLong = new double[eval.getNumClusters()];
            averageSLforLong = new double[eval.getNumClusters()];
            averageTPforShort = new double[eval.getNumClusters()];
            averageSLforShort = new double[eval.getNumClusters()];
            efficiencyDifferenceIndexes = new int[eval.getNumClusters()];
            supports = new int[eval.getNumClusters()];

            for (int i = 0; i < eval.getNumClusters(); i++) {
                double hua = 0, la = 0, lua = 0, ha = 0, lsta = 0, lena = 0;

                double[] pXa = new double[patternLength];
                for (int a = 0; a < pXa.length; a++)
                    pXa[a] = 0;

                int count = 0;
                for (int j = 0; j < clusteringInstances.size(); j++) {
                    if (instanceClusters[j] == i) {
                        count++;
                        HSResult result = (HSResult) patternArray[j];

                        for (int a = 0; a < pXa.length; a++) {
                            pXa[a] += result.pointList[a];
                        }

                        hua += result.highuntil;
                        la += result.low;
                        lua += result.lowuntil;
                        ha += result.high;
                        lsta += result.last;
                        lena += (result.left - result.right);
                    }
                }

                double p7hu = hua - pXa[pXa.length - 1];
                if (p7hu <= 0)
                    p7hu = 0.0001;
                double p7l = pXa[pXa.length - 1] - la;
                double shortEfficiency = p7l / p7hu;

                double p7lu = pXa[pXa.length - 1] - lua;
                if (p7lu <= 0)
                    p7lu = 0.0001;
                double p7h = ha - pXa[pXa.length - 1];
                double longEfficiency = p7h / p7lu;

                shortEfficiencies[i] = shortEfficiency;
                longEfficiencies[i] = longEfficiency;
                efficiencyDifferences[i] = Math.abs(longEfficiency - shortEfficiency);
                supports[i] = count;

                averageSLforLong[i] = p7lu;
                averageTPforLong[i] = p7h;
                averageSLforShort[i] = p7hu;
                averageTPforShort[i] = p7l;

                if (shortEfficiency > bestShortEfficiency) {
                    bestShortCluster = i;
                    bestShortEfficiency = shortEfficiency;
                }

                if (longEfficiency > bestLongEfficiency) {
                    bestLongCluster = i;
                    bestLongEfficiency = longEfficiency;
                }


            }

            for (int i = 0; i < efficiencyDifferenceIndexes.length; i++) {
                int count = 0;
                for (int a = 0; a < efficiencyDifferences.length; a++) {
                    if (a == i)
                        continue;
                    if (efficiencyDifferences[a] > efficiencyDifferences[i])
                        count++;
                }
                efficiencyDifferenceIndexes[i] = count;
            }
        }

    }

    private static boolean findTopHSPattern(double[] sevenPoints) {
        boolean result = false;

        if (sevenPoints[5] > sevenPoints[3] || sevenPoints[1] > sevenPoints[3])
            return result;

        if (sevenPoints[6] > sevenPoints[5] || sevenPoints[4] > sevenPoints[5])
            return result;

        if (sevenPoints[0] > sevenPoints[1] || sevenPoints[2] > sevenPoints[1])
            return result;

        result = true;

        return result;
    }

    private boolean findBottomHSPattern(double[] sevenPoints) {
        boolean result = false;

        if (sevenPoints[6] < sevenPoints[5] || sevenPoints[4] < sevenPoints[5])
            return result;

        if (sevenPoints[0] < sevenPoints[1] || sevenPoints[2] < sevenPoints[1])
            return result;

        if (sevenPoints[5] < sevenPoints[3] || sevenPoints[1] < sevenPoints[3])
            return result;

        result = true;

        return result;
    }

    public void evaluatePredefinedPatternsClusterer(
            Collection<HSResult> predefinedPatterns) throws Exception {
        eval.setClusterer(EMClusterer);

        int patternLength = 7;

        Object[] predefinedPatternArray = predefinedPatterns.toArray();

        Instances predefinedClusteringInstances = InstanceFactory
                .createGenericPatternInstances(predefinedPatterns.size(),
                        patternLength);
        for (HSResult result : predefinedPatterns) {
            Instance instance = result.generateGenericResultInstance();
            predefinedClusteringInstances.add(instance);
        }

        eval.evaluateClusterer(predefinedClusteringInstances);

        double[] instanceClusters = eval.getClusterAssignments();

        for (int a = 0; a < patternLength; a++)
            System.out.print("P" + (a + 1) + "a\t");

        System.out
                .println("HUa\tLa\tLUa\tHa\tLSTa\tLena\tCLSTR\tSHORT_EFF\tLONG_EFF\tQUALITY\tSUPPORT");

        for (int i = 0; i < eval.getNumClusters(); i++) {
            double hua = 0, la = 0, lua = 0, ha = 0, lsta = 0, lena = 0, qualitya = 0;

            double[] pXa = new double[patternLength];
            for (int a = 0; a < pXa.length; a++)
                pXa[a] = 0;

            int count = 0;
            for (int j = 0; j < predefinedClusteringInstances.size(); j++) {
                if (instanceClusters[j] == i) {
                    count++;
                    HSResult result = (HSResult) predefinedPatternArray[j];

                    hua += result.highuntil;
                    la += result.low;
                    lua += result.lowuntil;
                    ha += result.high;
                    lsta += result.last;
                    lena += (result.left - result.right);
                    qualitya += result.patternBodyQuality * 10000;

                    for (int a = 0; a < pXa.length; a++) {
                        pXa[a] += result.pointList[a];
                    }
                }
            }

            if (count != 0) {
                for (int a = 0; a < pXa.length; a++) {
                    System.out.print(decimalFormatter.format((pXa[a] / count))
                            + "\t");
                }

                System.out.print(decimalFormatter.format((hua / count)) + "\t");
                System.out.print(decimalFormatter.format((la / count)) + "\t");
                System.out.print(decimalFormatter.format((lua / count)) + "\t");
                System.out.print(decimalFormatter.format((ha / count)) + "\t");
                System.out
                        .print(decimalFormatter.format((lsta / count)) + "\t");
                System.out
                        .print(decimalFormatter.format((lena / count)) + "\t");

                double p7hu = hua - pXa[pXa.length - 1];
                double p7l = pXa[pXa.length - 1] - la;
                double shortEfficiency = p7l / p7hu;

                double p7lu = pXa[pXa.length - 1] - lua;
                double p7h = ha - pXa[pXa.length - 1];
                double longEfficiency = p7h / p7lu;

                System.out.println(i + "\t"
                        + decimalFormatter.format(shortEfficiency) + "\t"
                        + decimalFormatter.format(longEfficiency) + "\t" + decimalFormatter.format(qualitya / count) + "\t"
                        + count);
            }
        }

    }

    public boolean isShortEfficient(int patternCluster) {
        if (efficiencyDifferenceIndexes[patternCluster] > 3)
            return false;
        if (shortEfficiencies[patternCluster] < 2)
            return false;
        if (shortEfficiencies[patternCluster] < (longEfficiencies[patternCluster]) + Online_Pattern_Mining_SVM_Strategy.allowedEfficiencyDifference)
            return false;
        if (supports[patternCluster] * eval.getNumClusters() < clusteringInstances.size() * Online_Pattern_Mining_SVM_Strategy.allowedMinimumSupport)
            return false;
        return true;
    }

    public boolean isLongEfficient(int patternCluster) {
        if (efficiencyDifferenceIndexes[patternCluster] > 3)
            return false;
        if (longEfficiencies[patternCluster] < 2)
            return false;
        if (longEfficiencies[patternCluster] < (shortEfficiencies[patternCluster]) + Online_Pattern_Mining_SVM_Strategy.allowedEfficiencyDifference)
            return false;
        if (supports[patternCluster] * eval.getNumClusters() < clusteringInstances.size() * Online_Pattern_Mining_SVM_Strategy.allowedMinimumSupport)
            return false;
        return true;
    }

    public static double determineLotSizeToBuy(int cluster) {
        return longEfficiencies[cluster] / shortEfficiencies[cluster];
    }

    public static double determineLotSizeToSell(int cluster) {
        return shortEfficiencies[cluster] / longEfficiencies[cluster];
    }

    public static double determineTPForLong(int cluster) {
        return averageTPforLong[cluster];
    }

    public static double determineTPForShort(int cluster) {
        return averageTPforShort[cluster];
    }

    public static double determineSLForLong(int cluster) {
        return averageSLforLong[cluster];
    }

    public static double determineSLForShort(int cluster) {
        return averageSLforShort[cluster];
    }
}
