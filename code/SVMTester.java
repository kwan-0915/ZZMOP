package zzmop;

import jforex.devicelocal.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class SVMTester {

    public static HSPatternClassifier classifier_odd_hs;
    public static HSPatternClassifier classifier_even_hs;

    public static HSPatternClassifier classifier_odd_inverse_hs;
    public static HSPatternClassifier classifier_even_inverse_hs;


    static String svm_optimization_mode = "INVERSE_HS";


    static long fileKey = 23454001577582l;

    public static void createClassifiers() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(Constants.jforex_home + "\\objects\\" + fileKey + "-patterns.java"));

            ObjectInputStream oos = new ObjectInputStream(fis);
            Object sp = oos.readObject();
            oos.close();

            HashMap<Date, HSResult> storedPatterns = (HashMap<Date, HSResult>) sp;

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.setMinimumFractionDigits(5);
            Collection<HSResult> results = storedPatterns.values();
            Set<Date> dates = storedPatterns.keySet();

            Collection<HSResult> resultsToUse_odd = new ArrayList<>(results);
            Collection<HSResult> resultsToUse_even = new ArrayList<>(results);

            for (Date date : dates) {
                if (date.getDay() % 2 == 0)
                    resultsToUse_odd.remove(storedPatterns.get(date));

                else
                    resultsToUse_even.remove(storedPatterns.get(date));
            }

            classifier_odd_hs = new HSPatternClassifier(resultsToUse_odd);
            classifier_odd_hs.trainClassifier();

            classifier_even_hs = new HSPatternClassifier(resultsToUse_even);
            classifier_even_hs.trainClassifier();

        } catch (Exception e) {

        }

        try {
            fis = new FileInputStream(new File(Constants.jforex_home + "\\objects\\" + fileKey + "-patterns-inverse.java"));

            ObjectInputStream oos = new ObjectInputStream(fis);
            Object sp = oos.readObject();
            oos.close();

            HashMap<Date, HSResult> storedPatterns = (HashMap<Date, HSResult>) sp;

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.setMinimumFractionDigits(5);
            Collection<HSResult> results = storedPatterns.values();
            Set<Date> dates = storedPatterns.keySet();

            Collection<HSResult> resultsToUse_odd = new ArrayList<>(results);
            Collection<HSResult> resultsToUse_even = new ArrayList<>(results);

            for (Date date : dates) {
                if (date.getDay() % 2 == 0)
                    resultsToUse_odd.remove(storedPatterns.get(date));

                else
                    resultsToUse_even.remove(storedPatterns.get(date));
            }

            classifier_odd_inverse_hs = new HSPatternClassifier(resultsToUse_odd);
            classifier_odd_inverse_hs.trainClassifier();

            classifier_even_inverse_hs = new HSPatternClassifier(resultsToUse_even);
            classifier_even_inverse_hs.trainClassifier();

        } catch (Exception e) {

        }

    }

    public static void main(String[] args) {
        FileInputStream fis;
        try {
            if (svm_optimization_mode.equals("HS"))
                fis = new FileInputStream(new File(Constants.jforex_home + "\\objects\\" + fileKey + "-patterns.java"));
            else
                fis = new FileInputStream(new File(Constants.jforex_home + "\\objects\\" + fileKey + "-patterns-inverse.java"));

            ObjectInputStream oos = new ObjectInputStream(fis);
            Object sp = oos.readObject();
            oos.close();

            @SuppressWarnings("unchecked")
            HashMap<Date, HSResult> storedPatterns = (HashMap<Date, HSResult>) sp;

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
            DecimalFormat formatter = (DecimalFormat) nf;
            formatter.setMinimumFractionDigits(5);
            Collection<HSResult> results = storedPatterns.values();
            Set<Date> dates = storedPatterns.keySet();

            int[] bucket_l = new int[]{10, 20, 50};
            int[] pipCeiling_l = new int[]{50, 100, 200};

            for (int bucket_last : bucket_l)
                for (int pipCeiling_last : pipCeiling_l)
                    for (int bucket_low : bucket_l)
                        for (int pipCeiling_low : pipCeiling_l)
                            for (int bucket : bucket_l)
                                for (int pipCeiling : pipCeiling_l) {
                                    if (pipCeiling > bucket && pipCeiling % bucket == 0) {
                                        InstanceFactory.bucketHighUntil = bucket;
                                        InstanceFactory.pipCeilingHighUntil = pipCeiling;
                                        HSResult.bucketHighUntil = bucket;
                                        HSResult.pipCeilingHighUntil = pipCeiling;
                                    }

                                    if (pipCeiling_low > bucket_low && pipCeiling_low % bucket_low == 0) {
                                        InstanceFactory.bucketLow = bucket_low;
                                        InstanceFactory.pipCeilingLow = pipCeiling_low;
                                        HSResult.bucketLow = bucket_low;
                                        HSResult.pipCeilingLow = pipCeiling_low;
                                    }

                                    if (pipCeiling_last > bucket_last && pipCeiling_last % bucket_last == 0) {
                                        InstanceFactory.bucketLast = bucket_last;
                                        InstanceFactory.pipCeilingLast = pipCeiling_last;
                                        HSResult.bucketLast = bucket_last;
                                        HSResult.pipCeilingLast = pipCeiling_last;
                                    }

                                    Collection<HSResult> resultsToUse_odd = new ArrayList<>(results);
                                    Collection<HSResult> resultsToUse_even = new ArrayList<>(results);

                                    for (Date date : dates) {
                                        if (date.getDay() % 2 == 0)
                                            resultsToUse_odd.remove(storedPatterns.get(date));

                                        else
                                            resultsToUse_even.remove(storedPatterns.get(date));
                                    }

                                    double[] coef0_l = new double[]{0};
                                    double[] cost_l = new double[]{1};
                                    double[] cacheSize_l = new double[]{40};
                                    int[] degree_l = new int[]{3};
                                    double[] eps_l = new double[]{0.05};
                                    double[] gamma_l = new double[]{0.1};
                                    double[] loss_l = new double[]{1};
                                    boolean[] normalize_l = new boolean[]{false};

                                    double[] nu_l = new double[]{0.00001, 0.0005, 0.0001};

                                    boolean[] shrinking_l = new boolean[]{false};

                                    for (boolean normalizeLast : normalize_l)
                                        for (double coef0Last : coef0_l)
                                            for (double cacheSizeLast : cacheSize_l)
                                                for (int degreeLast : degree_l)
                                                    for (double gammaLast : gamma_l)
                                                        for (boolean shrinkingLast : shrinking_l)
                                                            for (double lossLast : loss_l)
                                                                for (double nuLast : nu_l)
                                                                    for (double costLast : cost_l)
                                                                        for (double epsLast : eps_l)
                                                                            for (boolean normalizeLow : normalize_l)
                                                                                for (double coef0Low : coef0_l)
                                                                                    for (double cacheSizeLow : cacheSize_l)
                                                                                        for (int degreeLow : degree_l)
                                                                                            for (double gammaLow : gamma_l)
                                                                                                for (boolean shrinkingLow : shrinking_l)
                                                                                                    for (double lossLow : loss_l)
                                                                                                        for (double nuLow : nu_l)
                                                                                                            for (double costLow : cost_l)
                                                                                                                for (double epsLow : eps_l)
                                                                                                                    for (boolean normalize : normalize_l)
                                                                                                                        for (double coef0 : coef0_l)
                                                                                                                            for (double cacheSize : cacheSize_l)
                                                                                                                                for (int degree : degree_l)
                                                                                                                                    for (double gamma : gamma_l)
                                                                                                                                        for (boolean shrinking : shrinking_l)
                                                                                                                                            for (double loss : loss_l)
                                                                                                                                                for (double nu : nu_l)
                                                                                                                                                    for (double cost : cost_l)
                                                                                                                                                        for (double eps : eps_l) {

                                                                                                                                                            HSPatternClassifier.coef0 = coef0;
                                                                                                                                                            HSPatternClassifier.cost = cost;
                                                                                                                                                            HSPatternClassifier.cacheSize = cacheSize;
                                                                                                                                                            HSPatternClassifier.degree = degree;
                                                                                                                                                            HSPatternClassifier.eps = eps;
                                                                                                                                                            HSPatternClassifier.gamma = gamma;
                                                                                                                                                            HSPatternClassifier.loss = loss;
                                                                                                                                                            HSPatternClassifier.normalize = normalize;
                                                                                                                                                            HSPatternClassifier.nu = nu;
                                                                                                                                                            HSPatternClassifier.shrinking = shrinking;

                                                                                                                                                            HSPatternClassifier.coef0_l = coef0Low;
                                                                                                                                                            HSPatternClassifier.cost_l = costLow;
                                                                                                                                                            HSPatternClassifier.cacheSize_l = cacheSizeLow;
                                                                                                                                                            HSPatternClassifier.degree_l = degreeLow;
                                                                                                                                                            HSPatternClassifier.eps_l = epsLow;
                                                                                                                                                            HSPatternClassifier.gamma_l = gammaLow;
                                                                                                                                                            HSPatternClassifier.loss_l = lossLow;
                                                                                                                                                            HSPatternClassifier.normalize_l = normalizeLow;
                                                                                                                                                            HSPatternClassifier.nu_l = nuLow;
                                                                                                                                                            HSPatternClassifier.shrinking_l = shrinkingLow;

                                                                                                                                                            HSPatternClassifier.coef0_last = coef0Last;
                                                                                                                                                            HSPatternClassifier.cost_last = costLast;
                                                                                                                                                            HSPatternClassifier.cacheSize_last = cacheSizeLast;
                                                                                                                                                            HSPatternClassifier.degree_last = degreeLast;
                                                                                                                                                            HSPatternClassifier.eps_last = epsLast;
                                                                                                                                                            HSPatternClassifier.gamma_last = gammaLast;
                                                                                                                                                            HSPatternClassifier.loss_last = lossLast;
                                                                                                                                                            HSPatternClassifier.normalize_last = normalizeLast;
                                                                                                                                                            HSPatternClassifier.nu_last = nuLast;
                                                                                                                                                            HSPatternClassifier.shrinking_last = shrinkingLast;

                                                                                                                                                            {
                                                                                                                                                                HSPatternClassifier classifier_odd = new HSPatternClassifier(resultsToUse_odd);
                                                                                                                                                                try {

                                                                                                                                                                    classifier_odd.trainClassifier();

                                                                                                                                                                } catch (Exception e) {
                                                                                                                                                                    continue;
                                                                                                                                                                }
                                                                                                                                                                HSPatternClassifier classifier_even = new HSPatternClassifier(resultsToUse_even);

                                                                                                                                                                try {

                                                                                                                                                                    classifier_even.trainClassifier();

                                                                                                                                                                } catch (Exception e) {
                                                                                                                                                                    continue;
                                                                                                                                                                }

                                                                                                                                                                double stop_loss_losses = 0;
                                                                                                                                                                double take_profit_profits = 0;
                                                                                                                                                                double end_game_profits = 0;
                                                                                                                                                                for (Date date : dates) {

                                                                                                                                                                    HSResult result = storedPatterns.get(date);
                                                                                                                                                                    HSPatternClassifier classifiers;
                                                                                                                                                                    if (date.getDay() % 2 == 0)
                                                                                                                                                                        classifiers = classifier_odd;
                                                                                                                                                                    else
                                                                                                                                                                        classifiers = classifier_even;

                                                                                                                                                                    double forecastedHighUntil = classifiers.classifyHSResult(result,
                                                                                                                                                                            HSResult.FORECASTING_HIGHUNTIL);
                                                                                                                                                                    double forecastedHigh = classifiers.classifyHSResult(result,
                                                                                                                                                                            HSResult.FORECASTING_HIGH);
                                                                                                                                                                    double forecastedLow = classifiers.classifyHSResult(result,
                                                                                                                                                                            HSResult.FORECASTING_LOW);
                                                                                                                                                                    double forecastedLowUntil = classifiers.classifyHSResult(result,
                                                                                                                                                                            HSResult.FORECASTING_LOWUNTIL);
                                                                                                                                                                    double forecastedLast = classifiers.classifyHSResult(result,
                                                                                                                                                                            HSResult.FORECASTING_LAST);

                                                                                                                                                                    forecastedHighUntil = Double.parseDouble(InstanceFactory.HighUntilList
                                                                                                                                                                            .get((int) forecastedHighUntil));
                                                                                                                                                                    forecastedHigh = Double.parseDouble(InstanceFactory.HighList
                                                                                                                                                                            .get((int) forecastedHigh));
                                                                                                                                                                    forecastedLow = Double
                                                                                                                                                                            .parseDouble(InstanceFactory.LowList.get((int) forecastedLow));
                                                                                                                                                                    forecastedLowUntil = Double
                                                                                                                                                                            .parseDouble(InstanceFactory.LowUntilList.get((int) forecastedLowUntil));
                                                                                                                                                                    forecastedLast = Double.parseDouble(InstanceFactory.LastList
                                                                                                                                                                            .get((int) forecastedLast));

                                                                                                                                                                    double actualHighUntil = 10000 * (result.highuntil - result.pointList[6]);
                                                                                                                                                                    double actualLow = 10000 * (result.pointList[6] - result.low);
                                                                                                                                                                    double actualLowUntil = 10000 * (result.pointList[6] - result.lowuntil);
                                                                                                                                                                    double actualHigh = 10000 * (result.high - result.pointList[6]);

                                                                                                                                                                    if (svm_optimization_mode.equals("HS")) {
                                                                                                                                                                        if (forecastedHighUntil < actualHighUntil) {
                                                                                                                                                                            stop_loss_losses -= forecastedHighUntil;
                                                                                                                                                                        } else if (forecastedLow > actualLow) {
                                                                                                                                                                            if (forecastedHighUntil > actualHigh)
                                                                                                                                                                                end_game_profits += (result.pointList[6] - result.last) * 10000;
                                                                                                                                                                            else
                                                                                                                                                                                stop_loss_losses -= forecastedHighUntil;
                                                                                                                                                                        } else if (forecastedLow < actualLow) {
                                                                                                                                                                            take_profit_profits += forecastedLow;
                                                                                                                                                                        }
                                                                                                                                                                    } else {
                                                                                                                                                                        if (forecastedLowUntil > actualLowUntil) {
                                                                                                                                                                            stop_loss_losses -= forecastedLowUntil;
                                                                                                                                                                        } else if (forecastedHigh > actualHigh) {
                                                                                                                                                                            if (forecastedLowUntil > actualHigh)
                                                                                                                                                                                end_game_profits += (result.pointList[6] - result.last) * 10000;
                                                                                                                                                                            else
                                                                                                                                                                                stop_loss_losses -= forecastedLowUntil;
                                                                                                                                                                        } else if (forecastedHigh < actualHigh) {
                                                                                                                                                                            take_profit_profits += forecastedHigh;
                                                                                                                                                                        }
                                                                                                                                                                    }


                                                                                                                                                                }

                                                                                                                                                                System.out.print(formatter
                                                                                                                                                                        .format((stop_loss_losses + take_profit_profits + end_game_profits))
                                                                                                                                                                        + "\t"
                                                                                                                                                                        + formatter.format(stop_loss_losses)
                                                                                                                                                                        + "\t"
                                                                                                                                                                        + formatter.format(take_profit_profits)
                                                                                                                                                                        + "\t"
                                                                                                                                                                        + formatter.format(end_game_profits)
                                                                                                                                                                        + "\t"
                                                                                                                                                                        + InstanceFactory.bucketHighUntil
                                                                                                                                                                        + "\t"
                                                                                                                                                                        + InstanceFactory.pipCeilingHighUntil
                                                                                                                                                                        + "\t"
                                                                                                                                                                        + InstanceFactory.bucketLow
                                                                                                                                                                        + "\t" + InstanceFactory.pipCeilingLow + "\t"
                                                                                                                                                                        + InstanceFactory.bucketLast
                                                                                                                                                                        + "\t" + InstanceFactory.pipCeilingLast + "\t");
                                                                                                                                                                System.out.println(classifier_even.toString());
                                                                                                                                                            }
                                                                                                                                                        }

                                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
