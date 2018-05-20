package zzmop;

import weka.core.DenseInstance;
import weka.core.Instance;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class HSResult implements Serializable {
    public static final int FORECASTING_HIGHUNTIL = 1;
    public static final int FORECASTING_LOW = 2;
    public static final int FORECASTING_LAST = 3;
    public static final int FORECASTING_HIGH = 4;
    public static final int FORECASTING_LOWUNTIL = 5;
    public static final int HS_TOP = 1;
    public static final int HS_BOTTOM = 2;
    private static final long serialVersionUID = 37352423472026754L;
    public static int bucketLow = InstanceFactory.bucketLow;
    public static int pipCeilingLow = InstanceFactory.pipCeilingLow;
    public static int bucketLowUntil = InstanceFactory.bucketLowUntil;
    public static int pipCeilingLowUntil = InstanceFactory.pipCeilingLowUntil;
    public static int bucketLast = InstanceFactory.bucketLast;
    public static int pipCeilingLast = InstanceFactory.pipCeilingLast;
    public static int bucketHighUntil = InstanceFactory.bucketHighUntil;
    public static int pipCeilingHighUntil = InstanceFactory.pipCeilingHighUntil;
    public static int bucketHigh = InstanceFactory.bucketHigh;
    public static int pipCeilingHigh = InstanceFactory.pipCeilingHigh;
    public static int bucketGeneric = 10;
    public static int pipCeilingGeneric = 200;
    public boolean result;
    public double[] pointList;
    public int right;
    public int left;
    public double highuntil = Double.MIN_VALUE;
    public double low = Double.MAX_VALUE;
    public double lowuntil = Double.MAX_VALUE;
    public double high = Double.MIN_VALUE;
    public double last;
    public double patternQuality;
    public double patternBodyQuality;
    public int type = HS_TOP;

    public void setTypeToHSBottom() {
        type = HS_BOTTOM;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null)
            return false;
        if (!(arg0 instanceof HSResult))
            return false;
        HSResult other = (HSResult) arg0;

        if (other.pointList == null)
            return false;

        for (int i = 0; i < pointList.length - 1; i++) {
            if (pointList[i] != other.pointList[i]) {
                return false;
            }
        }

        return true;
    }


    public Instance generateGenericResultInstance() {
        Instance i = null;
        i = new DenseInstance(
                InstanceFactory.GenericInstances.numAttributes());
        i.setDataset(InstanceFactory.GenericInstances);

        for (int v = 0; v < pointList.length - 1; v++) {
            i.setValue(
                    v,
                    createBucketValueGeneric(
                            (pointList[v + 1] - pointList[0]) * 10000));
        }
        i.setValue(i.numAttributes() - 1, patternBodyQuality * 100000);

        return i;
    }

    public Instance generateHSResultInstance(int forecastingMode) {
        Instance i = null;
        switch (forecastingMode) {
            case FORECASTING_HIGHUNTIL:
                i = new DenseInstance(
                        InstanceFactory.HighUntilInstances.numAttributes());
                i.setDataset(InstanceFactory.HighUntilInstances);

                i.setValue(
                        0,
                        createBucketValueHighUntil(
                                (pointList[1] - pointList[0]) * 10000, null));
                i.setValue(
                        1,
                        createBucketValueHighUntil(
                                (pointList[2] - pointList[0]) * 10000, null));
                i.setValue(
                        2,
                        createBucketValueHighUntil(
                                (pointList[3] - pointList[0]) * 10000, null));
                i.setValue(
                        3,
                        createBucketValueHighUntil(
                                (pointList[4] - pointList[0]) * 10000, null));
                i.setValue(
                        4,
                        createBucketValueHighUntil(
                                (pointList[5] - pointList[0]) * 10000, null));
                i.setValue(
                        5,
                        createBucketValueHighUntil(
                                (pointList[6] - pointList[0]) * 10000, null));
                i.setValue(6, createBucketValueHighUntil(left - right, null));

                i.setValue(
                        7,
                        createBucketStringHighUntil(
                                (highuntil - pointList[6]) * 10000, "ceil"));
                break;
            case FORECASTING_HIGH:
                i = new DenseInstance(InstanceFactory.HighInstances.numAttributes());
                i.setDataset(InstanceFactory.HighInstances);

                i.setValue(
                        0,
                        createBucketValueHigh(
                                (pointList[1] - pointList[0]) * 10000, null));
                i.setValue(
                        1,
                        createBucketValueHigh(
                                (pointList[2] - pointList[0]) * 10000, null));
                i.setValue(
                        2,
                        createBucketValueHigh(
                                (pointList[3] - pointList[0]) * 10000, null));
                i.setValue(
                        3,
                        createBucketValueHigh(
                                (pointList[4] - pointList[0]) * 10000, null));
                i.setValue(
                        4,
                        createBucketValueHigh(
                                (pointList[5] - pointList[0]) * 10000, null));
                i.setValue(
                        5,
                        createBucketValueHigh(
                                (pointList[6] - pointList[0]) * 10000, null));
                i.setValue(6, createBucketValueHigh(left - right, null));

                i.setValue(
                        7,
                        createBucketStringHigh((high - pointList[6]) * 10000,
                                "ceil"));
                break;
            case FORECASTING_LOW:
                i = new DenseInstance(InstanceFactory.LowInstances.numAttributes());
                i.setDataset(InstanceFactory.LowInstances);

                i.setValue(
                        0,
                        createBucketValueLow((pointList[1] - pointList[0]) * 10000,
                                null));
                i.setValue(
                        1,
                        createBucketValueLow((pointList[2] - pointList[0]) * 10000,
                                null));
                i.setValue(
                        2,
                        createBucketValueLow((pointList[3] - pointList[0]) * 10000,
                                null));
                i.setValue(
                        3,
                        createBucketValueLow((pointList[4] - pointList[0]) * 10000,
                                null));
                i.setValue(
                        4,
                        createBucketValueLow((pointList[5] - pointList[0]) * 10000,
                                null));
                i.setValue(
                        5,
                        createBucketValueLow((pointList[6] - pointList[0]) * 10000,
                                null));
                i.setValue(6, createBucketValueLow(left - right, null));

                i.setValue(7,
                        createBucketStringLow((pointList[6] - low) * 10000, "ceil"));
                break;
            case FORECASTING_LOWUNTIL:
                i = new DenseInstance(
                        InstanceFactory.LowUntilInstances.numAttributes());
                i.setDataset(InstanceFactory.LowUntilInstances);

                i.setValue(
                        0,
                        createBucketValueLowUntil(
                                (pointList[1] - pointList[0]) * 10000, null));
                i.setValue(
                        1,
                        createBucketValueLowUntil(
                                (pointList[2] - pointList[0]) * 10000, null));
                i.setValue(
                        2,
                        createBucketValueLowUntil(
                                (pointList[3] - pointList[0]) * 10000, null));
                i.setValue(
                        3,
                        createBucketValueLowUntil(
                                (pointList[4] - pointList[0]) * 10000, null));
                i.setValue(
                        4,
                        createBucketValueLowUntil(
                                (pointList[5] - pointList[0]) * 10000, null));
                i.setValue(
                        5,
                        createBucketValueLowUntil(
                                (pointList[6] - pointList[0]) * 10000, null));
                i.setValue(6, createBucketValueLowUntil(left - right, null));

                i.setValue(
                        7,
                        createBucketStringLowUntil(
                                (pointList[6] - lowuntil) * 10000, "ceil"));
                break;
            case FORECASTING_LAST:

                i = new DenseInstance(InstanceFactory.LastInstances.numAttributes());
                i.setDataset(InstanceFactory.LastInstances);

                i.setValue(
                        0,
                        createBucketValueLast(
                                (pointList[1] - pointList[0]) * 10000, null));
                i.setValue(
                        1,
                        createBucketValueLast(
                                (pointList[2] - pointList[0]) * 10000, null));
                i.setValue(
                        2,
                        createBucketValueLast(
                                (pointList[3] - pointList[0]) * 10000, null));
                i.setValue(
                        3,
                        createBucketValueLast(
                                (pointList[4] - pointList[0]) * 10000, null));
                i.setValue(
                        4,
                        createBucketValueLast(
                                (pointList[5] - pointList[0]) * 10000, null));
                i.setValue(
                        5,
                        createBucketValueLast(
                                (pointList[6] - pointList[0]) * 10000, null));
                i.setValue(6, createBucketValueLast(left - right, null));

                i.setValue(7,
                        createBucketStringLast((last - pointList[6]) * 10000, null));
                break;
        }

        return i;
    }

    public double createBucketValueLast(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketLast)) * bucketLast;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketLast)) * bucketLast;
        else
            val = ((int) (d / bucketLast)) * bucketLast;

        if (val < 0)
            val = 0;
        if (val > pipCeilingLast)
            val = pipCeilingLast;
        return val;
    }

    public double createBucketValueLow(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketLow)) * bucketLow;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketLow)) * bucketLow;
        else
            val = ((int) (d / bucketLow)) * bucketLow;

        if (val < 0)
            val = 0;
        if (val > pipCeilingLow)
            val = pipCeilingLow;
        return val;
    }

    public double createBucketValueLowUntil(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketLowUntil)) * bucketLowUntil;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketLowUntil)) * bucketLowUntil;
        else
            val = ((int) (d / bucketLowUntil)) * bucketLowUntil;

        if (val < 0)
            val = 0;
        if (val > pipCeilingLowUntil)
            val = pipCeilingLowUntil;
        return val;
    }

    public double createBucketValueHighUntil(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketHighUntil)) * bucketHighUntil;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketHighUntil)) * bucketHighUntil;
        else
            val = ((int) (d / bucketHighUntil)) * bucketHighUntil;

        if (val < 0)
            val = 0;
        if (val > pipCeilingHighUntil)
            val = pipCeilingHighUntil;
        return val;
    }

    public double createBucketValueHigh(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketHigh)) * bucketHigh;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketHigh)) * bucketHigh;
        else
            val = ((int) (d / bucketHigh)) * bucketHigh;

        if (val < 0)
            val = 0;
        if (val > pipCeilingHigh)
            val = pipCeilingHigh;
        return val;
    }

    public double createBucketValueGeneric(double d) {
        int val;
        val = ((int) Math.ceil(d / bucketGeneric)) * bucketGeneric;

        if (val > pipCeilingGeneric)
            val = pipCeilingGeneric;
        return val;
    }


    public String createBucketStringLow(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketLow)) * bucketLow;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketLow)) * bucketLow;
        else
            val = ((int) (d / bucketLow)) * bucketLow;

        if (val < 0)
            val = 0;
        if (val > pipCeilingLow)
            val = pipCeilingLow;

        return "" + val;
    }

    public String createBucketStringLowUntil(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketLowUntil)) * bucketLowUntil;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketLowUntil)) * bucketLowUntil;
        else
            val = ((int) (d / bucketLowUntil)) * bucketLowUntil;

        if (val < 0)
            val = 0;
        if (val > pipCeilingLowUntil)
            val = pipCeilingLowUntil;

        return "" + val;
    }

    public String createBucketStringLast(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketLast)) * bucketLast;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketLast)) * bucketLast;
        else
            val = ((int) (d / bucketLast)) * bucketLast;

        if (val < 0)
            val = 0;
        if (val > pipCeilingLast)
            val = pipCeilingLast;

        return "" + val;
    }

    public String createBucketStringHighUntil(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketHighUntil)) * bucketHighUntil;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketHighUntil)) * bucketHighUntil;
        else
            val = ((int) (d / bucketHighUntil)) * bucketHighUntil;

        if (val < 0)
            val = 0;
        if (val > pipCeilingHighUntil)
            val = pipCeilingHighUntil;

        return "" + val;
    }

    public String createBucketStringHigh(double d, String mode) {
        int val;
        if ("ceil".equals(mode))
            val = ((int) Math.ceil(d / bucketHigh)) * bucketHigh;
        else if ("floor".equals(mode))
            val = ((int) Math.floor(d / bucketHigh)) * bucketHigh;
        else
            val = ((int) (d / bucketHigh)) * bucketHigh;

        if (val < 0)
            val = 0;
        if (val > pipCeilingHigh)
            val = pipCeilingHigh;

        return "" + val;
    }

    public double calculateAverageChange() {
        double high = 0, low = 0;
        boolean startHigh = pointList[0] > pointList[1];
        int hc = 0, lc = 0;

        for (int i = 0; i < pointList.length; i++) {
            if ((i % 2 == 0 && startHigh) || (i % 2 == 1 && !startHigh)) {
                high += pointList[i];
                hc++;
            } else {
                low += pointList[i];
                lc++;
            }
        }
        double averageChange = (high / hc) - (low / lc);

        BigDecimal value = new BigDecimal(averageChange);
        value = value.setScale(5, RoundingMode.UP);

        return value.doubleValue();
    }
}
