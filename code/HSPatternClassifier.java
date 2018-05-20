package zzmop;

import libsvm.svm;
import weka.classifiers.functions.LibSVM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.util.Collection;

public class HSPatternClassifier {

    public static double coef0 = 0;
    public static double cost = 1;
    public static double cacheSize = 40;
    public static int degree = 3;
    public static double eps = 0.05;
    public static double gamma = 0.1;
    public static double loss = 1;
    public static boolean normalize = false;
    public static double nu = 0.0005;
    public static boolean shrinking = true;

    public static double coef0_l = 0;
    public static double cost_l = 1;
    public static double cacheSize_l = 40;
    public static int degree_l = 3;
    public static double eps_l = 0.05;
    public static double gamma_l = 0.1;
    public static double loss_l = 1;
    public static boolean normalize_l = false;
    public static double nu_l = 0.0005;
    public static boolean shrinking_l = true;

    public static double coef0_last = 0;
    public static double cost_last = 1;
    public static double cacheSize_last = 40;
    public static int degree_last = 3;
    public static double eps_last = 0.05;
    public static double gamma_last = 0.1;
    public static double loss_last = 1;
    public static boolean normalize_last = false;
    public static double nu_last = 0.0005;
    public static boolean shrinking_last = true;


    private LibSVM highUntilClassifier;
    private LibSVM highClassifier;
    private LibSVM lowClassifier;
    private LibSVM lowUntilClassifier;
    private LibSVM lastClassifier;

    Instances trainingInstancesHigh;
    Instances trainingInstancesHighUntil;
    Instances trainingInstancesLow;
    Instances trainingInstancesLowUntil;
    Instances trainingInstancesLast;

    public HSPatternClassifier(Collection<HSResult> hsPatterns) {
        trainingInstancesHighUntil = InstanceFactory.createHighUntilPatternInstances(hsPatterns.size());
        for (HSResult result : hsPatterns) {
            Instance instance = result.generateHSResultInstance(HSResult.FORECASTING_HIGHUNTIL);
            trainingInstancesHighUntil.add(instance);
        }

        trainingInstancesHigh = InstanceFactory.createHighPatternInstances(hsPatterns.size());
        for (HSResult result : hsPatterns) {
            Instance instance = result.generateHSResultInstance(HSResult.FORECASTING_HIGH);
            trainingInstancesHigh.add(instance);
        }

        trainingInstancesLow = InstanceFactory.createLowPatternInstances(hsPatterns.size());
        for (HSResult result : hsPatterns) {
            Instance instance = result.generateHSResultInstance(HSResult.FORECASTING_LOW);
            trainingInstancesLow.add(instance);
        }

        trainingInstancesLowUntil = InstanceFactory.createLowUntilPatternInstances(hsPatterns.size());
        for (HSResult result : hsPatterns) {
            Instance instance = result.generateHSResultInstance(HSResult.FORECASTING_LOWUNTIL);
            trainingInstancesLowUntil.add(instance);
        }

        trainingInstancesLast = InstanceFactory.createLastPatternInstances(hsPatterns.size());
        for (HSResult result : hsPatterns) {
            Instance instance = result.generateHSResultInstance(HSResult.FORECASTING_LAST);
            trainingInstancesLast.add(instance);
        }
    }

    @Override
    public String toString() {
        String s = "";
        s += "nu\t" + nu + "\t";
        s += "nu_l\t" + nu_l + "\t";
        s += "nu_last\t" + nu_last + "\t";
        s += "normalize\t" + normalize + "\t";
        s += "normalize_l\t" + normalize_l + "\t";
        s += "normalize_last\t" + normalize_last + "\t";
        return s;
    }

    private LibSVM createEmptyClassifier() {
        svm.svm_set_print_string_function(new libsvm.svm_print_interface() {
            @Override
            public void print(String s) {
            }
        });

        LibSVM classifier = null;
        try {
            classifier = new LibSVM();
            if (classifier instanceof LibSVM) {
                classifier.setSVMType(new SelectedTag(LibSVM.SVMTYPE_NU_SVC, LibSVM.TAGS_SVMTYPE));
                classifier.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
            }
        } catch (IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
        return classifier;
    }

    public void trainClassifier() throws Exception {
        highUntilClassifier = createEmptyClassifier();
        {
            highUntilClassifier.setCoef0(coef0);
            highUntilClassifier.setCost(cost);
            highUntilClassifier.setCacheSize(cacheSize);
            highUntilClassifier.setDegree(degree);
            highUntilClassifier.setEps(eps);
            highUntilClassifier.setGamma(gamma);
            highUntilClassifier.setLoss(loss);
            highUntilClassifier.setNormalize(normalize);
            highUntilClassifier.setNu(nu);
            highUntilClassifier.setShrinking(shrinking);
        }
        highUntilClassifier.buildClassifier(trainingInstancesHighUntil);

        highClassifier = createEmptyClassifier();
        {
            highClassifier.setCoef0(coef0);
            highClassifier.setCost(cost);
            highClassifier.setCacheSize(cacheSize);
            highClassifier.setDegree(degree);
            highClassifier.setEps(eps);
            highClassifier.setGamma(gamma);
            highClassifier.setLoss(loss);
            highClassifier.setNormalize(normalize);
            highClassifier.setNu(nu);
            highClassifier.setShrinking(shrinking);
        }
        highClassifier.buildClassifier(trainingInstancesHigh);

        lowClassifier = createEmptyClassifier();
        {
            lowClassifier.setCoef0(coef0_l);
            lowClassifier.setCost(cost_l);
            lowClassifier.setCacheSize(cacheSize_l);
            lowClassifier.setDegree(degree_l);
            lowClassifier.setEps(eps_l);
            lowClassifier.setGamma(gamma_l);
            lowClassifier.setLoss(loss_l);
            lowClassifier.setNormalize(normalize_l);
            lowClassifier.setNu(nu_l);
            lowClassifier.setShrinking(shrinking_l);
        }
        lowClassifier.buildClassifier(trainingInstancesLow);

        lowUntilClassifier = createEmptyClassifier();
        {
            lowUntilClassifier.setCoef0(coef0_l);
            lowUntilClassifier.setCost(cost_l);
            lowUntilClassifier.setCacheSize(cacheSize_l);
            lowUntilClassifier.setDegree(degree_l);
            lowUntilClassifier.setEps(eps_l);
            lowUntilClassifier.setGamma(gamma_l);
            lowUntilClassifier.setLoss(loss_l);
            lowUntilClassifier.setNormalize(normalize_l);
            lowUntilClassifier.setNu(nu_l);
            lowUntilClassifier.setShrinking(shrinking_l);
        }
        lowUntilClassifier.buildClassifier(trainingInstancesLowUntil);

        lastClassifier = createEmptyClassifier();
        {
            lastClassifier.setCoef0(coef0_last);
            lastClassifier.setCost(cost_last);
            lastClassifier.setCacheSize(cacheSize_last);
            lastClassifier.setDegree(degree_last);
            lastClassifier.setEps(eps_last);
            lastClassifier.setGamma(gamma_last);
            lastClassifier.setLoss(loss_last);
            lastClassifier.setNormalize(normalize_last);
            lastClassifier.setNu(nu_last);
            lastClassifier.setShrinking(shrinking_last);
        }
        lastClassifier.buildClassifier(trainingInstancesLast);

    }

    public double classifyHSResult(HSResult result, int forecastingMode) {
        try {
            double d = Double.MAX_VALUE;
            LibSVM classifier = null;
            switch (forecastingMode) {
                case HSResult.FORECASTING_HIGHUNTIL:
                    classifier = highUntilClassifier;
                    break;
                case HSResult.FORECASTING_HIGH:
                    classifier = highClassifier;
                    break;
                case HSResult.FORECASTING_LOW:
                    classifier = lowClassifier;
                    break;
                case HSResult.FORECASTING_LOWUNTIL:
                    classifier = lowUntilClassifier;
                    break;
                case HSResult.FORECASTING_LAST:
                    classifier = lastClassifier;
                    break;
            }
            d = classifier.classifyInstance(result.generateHSResultInstance(forecastingMode));
            return d;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Double.MAX_VALUE;
    }
}
