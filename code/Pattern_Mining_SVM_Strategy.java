package zzmop;

import com.dukascopy.api.*;
import com.dukascopy.api.system.IClient;
import jforex.devicelocal.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Pattern_Mining_SVM_Strategy implements IStrategy {
    private IAccount account;
    private IClient client;
    private IContext context;
    private IEngine engine = null;
    private IHistory history = null;

    private int sellOrderLabelCounter = 0;
    private int buyOrderLabelCounter = 0;

    private HashMap<Date, HSResult> storedPatterns = new HashMap<>();
    private DateFormat excelDateFormat;
    private DecimalFormat decimalFormatter;
    private Instrument TRADING_INSTRUMENT = Instrument.EURUSD;

    private double sellLots = 0.01;
    private double buyLots = 0.01;

    private HashMap<String, Integer> orderLengthTracker = new HashMap<>();
    private HashMap<HSResult, Integer> patternTracker = new HashMap<>();

    private boolean loadClusterer = false;
    private boolean tradeSell = false;
    private boolean tradeBuy = false;

    private Period TRAINING_PERIOD = Period.FIFTEEN_MINS;
    private int patternMiningWindow = 320;
    private int extDepth = 16, extDeviation = 5;
    private int zigZagPatternLength = 7;

    private IIndicators indicators;

    public Pattern_Mining_SVM_Strategy(IClient client) {
        this.client = client;
    }

    public void onStart(IContext context) throws JFException {
        ConfigReader.readConfig("max-zzmop.config");
        extDepth = ConfigReader.zigzag_depth;
        zigZagPatternLength = ConfigReader.zigzag_pattern_length;
        patternMiningWindow = ConfigReader.history_window_size_in_bars;

        engine = context.getEngine();
        history = context.getHistory();
        account = context.getAccount();
        indicators = context.getIndicators();
        this.context = context;

        decimalFormatter = (DecimalFormat) NumberFormat
                .getNumberInstance(Locale.GERMAN);
        decimalFormatter.setMinimumFractionDigits(5);

        excelDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        if (loadClusterer) {
            PatternAnalyzer.loadClusterer();
            try {
                PatternAnalyzer.clusterer.evaluateClusterer(false, PatternAnalyzer.storedDates);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    public void onBar(Instrument instrument, Period period, IBar satisBar,
                      IBar alisBar) {

        if (period.equals(TRAINING_PERIOD)) {

            if (isFlat(alisBar))
                return;

            trackOrdersToDestroyExpiredOrders();
            trackPatternsToAdjustTheirForwardParameters(satisBar);

            try {

                double[] zigzag = indicators.zigzag(instrument, period,
                        OfferSide.BID, extDepth, extDeviation, 3,
                        Filter.ALL_FLATS, patternMiningWindow,
                        satisBar.getTime() - period.getInterval(), 0);
                int numberOfZigZagPoints = countZigzagPoints(zigzag);

                if (numberOfZigZagPoints >= zigZagPatternLength) {
                    HSResult pattern = findPattern(zigzag);
                    double[] continuousZigzag = createContinuousZigZag(zigzag);
                    List<IBar> patternBarHistory = history.getBars(TRADING_INSTRUMENT,
                            TRAINING_PERIOD, OfferSide.ASK, Filter.ALL_FLATS,
                            patternMiningWindow, satisBar.getTime(), 0);
                    double[] quality = measurePatternQuality(continuousZigzag, patternBarHistory);
                    double patternQuality = quality[0];
                    double patternBodyQuality = quality[1];
                    pattern.patternQuality = patternQuality;
                    pattern.patternBodyQuality = patternBodyQuality;

                    if (!storedPatterns.containsValue(pattern)) {
                        int offset = pattern.right;
                        Date patternDate = new Date(satisBar.getTime()
                                - period.getInterval() * offset);

                        storedPatterns.put(patternDate, pattern);

                        int patternLength = pattern.left - pattern.right;
                        patternTracker.put(pattern, patternLength);
                        if (loadClusterer) {
                            PatternClusterer clusterer = PatternAnalyzer.clusterer;

                            int patternCluster = clusterer
                                    .clusterInstance(pattern
                                            .generateGenericResultInstance());

                            if (patternCluster == PatternClusterer.bestShortCluster) {
                                String orderKey = getSellOrderLabel(TRADING_INSTRUMENT)
                                        + "sell";
                                adjustSellLots(orderKey);

                                if (tradeSell) {
                                    engine.submitOrder(
                                            orderKey,
                                            TRADING_INSTRUMENT,
                                            IEngine.OrderCommand.SELL,
                                            sellLots,
                                            0d,
                                            0d,
                                            satisBar.getClose()
                                                    + 100
                                                    * TRADING_INSTRUMENT
                                                    .getPipValue(),
                                            satisBar.getClose()
                                                    - (50 + patternLength / 10)
                                                    * TRADING_INSTRUMENT
                                                    .getPipValue());
                                }
                                orderLengthTracker.put(orderKey, patternLength);
                            } else if (patternCluster == PatternClusterer.bestLongCluster) {
                                String orderKey = getBuyOrderLabel(TRADING_INSTRUMENT)
                                        + "buy";
                                adjustBuyLots(orderKey);

                                if (tradeBuy) {
                                    engine.submitOrder(
                                            orderKey,
                                            TRADING_INSTRUMENT,
                                            IEngine.OrderCommand.BUY,
                                            buyLots,
                                            0d,
                                            0d,
                                            satisBar.getClose()
                                                    - 100
                                                    * TRADING_INSTRUMENT
                                                    .getPipValue(),
                                            satisBar.getClose()
                                                    + (50 + patternLength / 10)
                                                    * TRADING_INSTRUMENT
                                                    .getPipValue());
                                }
                                orderLengthTracker.put(orderKey, patternLength);
                            }

                        }
                    }
                }

            } catch (JFException e) {
                e.printStackTrace();
            }
        }
    }

    private double[] measurePatternQuality(double[] continuousZigzag,
                                           List<IBar> patternBarHistory) {
        double error = 0;
        double bodyError = 0;
        int numberOfContinuousZigZagPoints = 0;
        for (int i = 0; i < continuousZigzag.length; i++) {
            if (Double.isNaN(continuousZigzag[i]))
                continue;
            double zigzagValue = continuousZigzag[i];
            double low = patternBarHistory.get(i).getLow();
            double high = patternBarHistory.get(i).getHigh();
            double open = patternBarHistory.get(i).getOpen();
            double close = patternBarHistory.get(i).getClose();

            error += Math.max(Math.abs(zigzagValue - low), Math.abs(high - zigzagValue));
            bodyError += Math.max(Math.abs(zigzagValue - open), Math.abs(close - zigzagValue));
            numberOfContinuousZigZagPoints++;
        }
        return new double[]{error / numberOfContinuousZigZagPoints, bodyError / numberOfContinuousZigZagPoints};
    }

    private double[] createContinuousZigZag(double[] zigzag) {
        double[] continuousZigzag = new double[zigzag.length];

        LinkedList<Integer> zzpList = new LinkedList<>();
        for (int i = 0; i < zigzag.length; i++) {
            if (!Double.isNaN(zigzag[i])) {
                zzpList.add(i);
            }
            continuousZigzag[i] = zigzag[i];
        }

        for (int i = 0; i < zzpList.size() - 1; i++) {
            int index1 = zzpList.get(i);
            int index2 = zzpList.get(i + 1);
            double d1 = continuousZigzag[index1];
            double d2 = continuousZigzag[index2];
            double difference = d2 - d1;
            int steps = index2 - index1;
            double increment = difference / steps;
            for (int b = index1 + 1; b < index2; b++) {
                continuousZigzag[b] = continuousZigzag[b - 1] + increment;
            }
        }
        return continuousZigzag;
    }

    public void onStop() throws JFException {

        if (!loadClusterer) {
            clearUnfinishedPatterns(storedPatterns);
            storePatternsToFile();
            System.out.println("PATTERNS");
            printPatterns(storedPatterns);
            System.out.println("SIMPLIFIED PATTERNS");
            printSimplifiedPatterns(storedPatterns);
        } else {

        }

        for (IOrder order : engine.getOrders()) {
            order.close();
        }
        System.out.println("Matrix_SVM_Strategy Stopped");
    }

    private void clearUnfinishedPatterns(HashMap<Date, HSResult> patternsToTest) {
        Set<Date> p_set = patternsToTest.keySet();
        ArrayList<Date> removal = new ArrayList<>();
        for (Date d : p_set) {
            if (patternTracker.containsKey(patternsToTest.get(d))) {
                removal.add(d);
            }
        }
        for (Date r : removal)
            patternsToTest.remove(r);
    }

    private void trackPatternsToAdjustTheirForwardParameters(IBar satisBar) {
        Set<HSResult> results = patternTracker.keySet();

        IBar historyBar = null;
        try {
            historyBar = history.getBars(TRADING_INSTRUMENT, TRAINING_PERIOD,
                    OfferSide.ASK, Filter.ALL_FLATS, 1, satisBar.getTime(), 0)
                    .get(0);
        } catch (JFException e1) {
            e1.printStackTrace();
        }

        double barHigh = historyBar.getHigh();
        double barLow = historyBar.getLow();
        double close = historyBar.getClose();

        Set<HSResult> resultsToRemove = new HashSet<>();
        for (HSResult result : results) {
            int life = patternTracker.get(result);
            life--;
            if (life == 0) {
                result.last = close;
                resultsToRemove.add(result);
            }

            if (barLow < result.low) {
                result.low = barLow;
            }

            if (barHigh > result.high) {
                result.high = barHigh;
            }
            patternTracker.put(result, life);
        }

        for (HSResult resultToRemove : resultsToRemove) {
            int patternLength = (resultToRemove.left - resultToRemove.right);
            try {
                List<IBar> future = history.getBars(TRADING_INSTRUMENT,
                        TRAINING_PERIOD, OfferSide.ASK, Filter.ALL_FLATS,
                        patternLength, historyBar.getTime(), 0);
                boolean lowUntilFound = false, highUntilFound = false;

                for (IBar bar : future) {
                    barHigh = bar.getHigh();
                    barLow = bar.getLow();
                    if (barHigh == resultToRemove.high)
                        lowUntilFound = true;

                    if (barLow == resultToRemove.low)
                        highUntilFound = true;

                    if (barLow < resultToRemove.lowuntil && !lowUntilFound)
                        resultToRemove.lowuntil = barLow;

                    if (barHigh > resultToRemove.highuntil && !highUntilFound)
                        resultToRemove.highuntil = barHigh;

                    if (highUntilFound && lowUntilFound) {
                        continue;
                    }
                }
            } catch (JFException e) {
                e.printStackTrace();
            }

            if (resultToRemove.high == resultToRemove.highuntil) {
                if (resultToRemove.lowuntil == Double.MAX_VALUE)
                    resultToRemove.lowuntil = resultToRemove.low;
            }

            if (resultToRemove.low == resultToRemove.lowuntil) {
                if (resultToRemove.highuntil == Double.MIN_VALUE)
                    resultToRemove.highuntil = resultToRemove.high;
            }

            patternTracker.remove(resultToRemove);
        }
    }

    private void printPatterns(HashMap<Date, HSResult> patternsToPrint) {
        Set<Date> dates = patternsToPrint.keySet();
        System.out
                .println("Date\tP1\tP2\tP3\tP4\tP5\tP6\tP7\tHU\tL\tLU\tH\tLST\tLen");
        for (Date date : dates) {
            System.out.print(excelDateFormat.format(date) + "\t");

            HSResult result = patternsToPrint.get(date);
            for (double d : result.pointList)
                System.out.print(decimalFormatter.format(d) + "\t");

            System.out.print(decimalFormatter.format(result.highuntil) + "\t");
            System.out.print(decimalFormatter.format(result.low) + "\t");
            System.out.print(decimalFormatter.format(result.lowuntil) + "\t");
            System.out.print(decimalFormatter.format(result.high) + "\t");
            System.out.print(decimalFormatter.format(result.last) + "\t");
            System.out.println((result.left - result.right) + "\t");
        }
    }

    private void printSimplifiedPatterns(HashMap<Date, HSResult> patternsToPrint) {
        Set<Date> dates = patternsToPrint.keySet();
        System.out
                .println("Date\tP1\tP2\tP3\tP4\tP5\tP6\tP7\tHU\tL\tLU\tH\tLST\tLen");
        for (Date date : dates) {
            System.out.print(excelDateFormat.format(date) + "\t");

            HSResult result = patternsToPrint.get(date);
            double o = result.pointList[0];
            for (double d : result.pointList)
                System.out.print(Math.round((d - o) * 10000) + "\t");

            System.out.print(Math.round((result.highuntil - o) * 10000) + "\t");
            System.out.print(Math.round((result.low - o) * 10000) + "\t");
            System.out.print(Math.round((result.lowuntil - o) * 10000) + "\t");
            System.out.print(Math.round((result.high - o) * 10000) + "\t");
            System.out.print(Math.round((result.last - o) * 10000) + "\t");
            System.out.println(Math.round(result.left - result.right) + "\t");
        }
    }

    private void printPatternsAndClassifications(
            HashMap<Date, HSResult> patternsToPrint) {
        Set<Date> dates = patternsToPrint.keySet();

        for (Date patternDate : dates) {
            System.out.print(excelDateFormat.format(patternDate) + "\t");
            HSResult result = patternsToPrint.get(patternDate);

            for (double d : result.pointList)
                System.out.print(decimalFormatter.format(d) + "\t");

            HSPatternClassifier classifier;
            if (result.type == HSResult.HS_TOP) {
                if (patternDate.getDay() % 2 == 0)
                    classifier = SVMTester.classifier_odd_hs;
                else
                    classifier = SVMTester.classifier_even_hs;
            } else {
                if (patternDate.getDay() % 2 == 0)
                    classifier = SVMTester.classifier_odd_inverse_hs;
                else
                    classifier = SVMTester.classifier_even_inverse_hs;
            }

            double forecastedHighUntil = classifier.classifyHSResult(result,
                    HSResult.FORECASTING_HIGHUNTIL);
            double forecastedHigh = classifier.classifyHSResult(result,
                    HSResult.FORECASTING_HIGH);
            double forecastedLow = classifier.classifyHSResult(result,
                    HSResult.FORECASTING_LOW);
            double forecastedLowUntil = classifier.classifyHSResult(result,
                    HSResult.FORECASTING_LOWUNTIL);
            double forecastedLast = classifier.classifyHSResult(result,
                    HSResult.FORECASTING_LAST);

            Integer fhu = Integer.parseInt(InstanceFactory.HighUntilList
                    .get((int) forecastedHighUntil));
            Integer fl = Integer.parseInt(InstanceFactory.LowList
                    .get((int) forecastedLow));
            Integer flu = Integer.parseInt(InstanceFactory.LowUntilList
                    .get((int) forecastedLowUntil));
            Integer fh = Integer.parseInt(InstanceFactory.HighList
                    .get((int) forecastedHigh));
            Integer flast = Integer.parseInt(InstanceFactory.LastList
                    .get((int) forecastedLast));

            System.out.print(decimalFormatter.format(result.highuntil) + "\t");
            System.out.print(decimalFormatter.format(result.low) + "\t");
            System.out.print(decimalFormatter.format(result.lowuntil) + "\t");
            System.out.print(decimalFormatter.format(result.high) + "\t");
            System.out.print(decimalFormatter.format(result.last) + "\t");
            System.out.print((result.left - result.right) + "\t");
            System.out.print(fhu + "\t");
            System.out.print(fl + "\t");
            System.out.print(flu + "\t");
            System.out.print(fh + "\t");
            System.out.println(flast + "\t");
        }
    }

    private HSResult findPattern(double[] zigzag) {
        HSResult candidatePattern = new HSResult();

        double[] nPoints = assignLastNPoints(zigzag, candidatePattern);
        candidatePattern.pointList = nPoints;

        return candidatePattern;
    }

    private double[] assignLastNPoints(double[] zigzag,
                                       HSResult candidatePattern) {
        double[] nPoints = new double[zigZagPatternLength];
        int reviewed = 0;
        for (int i = zigzag.length - 1; i >= 0; i--) {
            Double d = zigzag[i];

            if (Double.isInfinite(d) || Double.isNaN(d))
                continue;
            else {
                nPoints[(zigZagPatternLength - 1) - reviewed] = d;
                reviewed++;
            }
            if (reviewed == 1)
                candidatePattern.right = (zigzag.length - 1) - i;
            if (reviewed > (zigZagPatternLength - 1)) {
                candidatePattern.left = (zigzag.length - 1) - i;
                break;
            }
        }
        return nPoints;
    }


    private void storePatternsToFile() {
        long fileKey = System.nanoTime();
        try {
            FileOutputStream fos = new FileOutputStream(new File(
                    Constants.jforex_home + "\\objects\\" + fileKey
                            + "-patterns.java"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(storedPatterns);
            oos.close();
            fos.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void trackOrdersToDestroyExpiredOrders() {
        Set<String> order = orderLengthTracker.keySet();

        for (String key : order) {
            int l = orderLengthTracker.get(key);
            if (l == 0) {
                destroyOrder(key);
            } else {
                l--;
                orderLengthTracker.put(key, l);
            }
        }
    }

    private void destroyOrder(String key) {
        try {
            if (engine.getOrder(key) != null)
                engine.getOrder(key).close();
        } catch (JFException e) {
            e.printStackTrace();
        }
    }

    private int countZigzagPoints(double[] zigzag) {
        int c = 0;
        for (Double d : zigzag) {
            if (!Double.isNaN(d) && !Double.isInfinite(d)) {
                c++;
            }
        }
        return c;
    }

    protected String getSellOrderLabel(Instrument instrument) {
        String label = instrument.name();
        label = label.substring(0, 2) + label.substring(3, 5);
        label = label + (sellOrderLabelCounter++);
        label = label.toLowerCase();
        return label;
    }

    protected String getBuyOrderLabel(Instrument instrument) {
        String label = instrument.name();
        label = label.substring(0, 2) + label.substring(3, 5);
        label = label + (buyOrderLabelCounter++);
        label = label.toLowerCase();
        return label;
    }

    private boolean isFlat(IBar bar) {
        if (bar.getLow() == bar.getHigh())
            return true;
        return false;
    }

    public void onMessage(IMessage message) throws JFException {
        if (message.getReasons().contains(IMessage.Reason.ORDER_CLOSED_BY_SL)
                || message.getReasons().contains(
                IMessage.Reason.ORDER_CLOSED_BY_TP)) {
        }
    }

    public void onAccount(IAccount account) throws JFException {
    }

    private void adjustSellLots(String orderKey) {

    }

    private void adjustBuyLots(String orderKey) {
    }
}