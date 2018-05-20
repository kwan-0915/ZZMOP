package zzmop;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParameterOptimizer {

    public static void main(String[] args) {

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Date CURRENT_DATE = dateFormat.parse("01.01.2011");
            Date END_DATE = dateFormat.parse("01.01.2017");

            while (CURRENT_DATE.before(END_DATE)) {

                double MAX_RESULT = Double.MIN_VALUE;

                int[] history_window_size_in_bars_l = new int[]{50, 100, 150, 200};
                int[] zigzag_depth_l = new int[]{8, 12, 16, 20};
                int[] zigzag_pattern_length_l = new int[]{5, 6, 7, 8, 9, 11, 13};
                int[] zigzag_thickness_l = new int[]{5, 10, 30, 50};
                double[] zigzag_coverage_l = new double[]{0.5d, 0.6d, 0.7d, 0.8d, 0.9d, 1.0d};

                int[] em_iterations_l = new int[]{100, 500, 1000};
                int[] folds_l = new int[]{10, 50, 100};
                double[] minimum_log_likelihood_improvement_l = new double[]{0.00001d, 0.0001d, 0.001d, 0.01d};
                double[] minimum_standard_deviation_l = new double[]{0.00001d, 0.0001d, 0.001d, 0.01d};
                int[] simple_k_means_runs_l = new int[]{10, 50, 100};

                // 1 = polynomial, 2 = radial_basis
                int[] svm_kernel_type_l = new int[]{1, 2};
                int[] function_degree_l = new int[]{3, 4, 5};
                double[] regularization_parameter_l = new double[]{0.00001d, 0.0001d, 0.01d, 0.1d, 0.25d, 0.5d};
                // 1 = yes, 2 = no
                int[] attribute_normalization_l = new int[]{1, 2};
                int[] training_interval_l = new int[]{1, 2, 3, 4, 5};
                int[] execution_interval_l = new int[]{50, 100, 150, 200, 250};

                for (int history_window_size_in_bars : history_window_size_in_bars_l) {
                    for (int zigzag_depth : zigzag_depth_l) {
                        for (int zigzag_pattern_length : zigzag_pattern_length_l) {
                            for (int zigzag_thickness : zigzag_thickness_l) {
                                for (double zigzag_coverage : zigzag_coverage_l) {
                                    for (int em_iterations : em_iterations_l) {
                                        for (int folds : folds_l) {
                                            for (double minimum_log_likelihood_improvement : minimum_log_likelihood_improvement_l) {
                                                for (double minimum_standard_deviation : minimum_standard_deviation_l) {
                                                    for (int simple_k_means_runs : simple_k_means_runs_l) {
                                                        for (int svm_kernel_type : svm_kernel_type_l) {
                                                            for (int function_degree : function_degree_l) {
                                                                for (double regularization_parameter : regularization_parameter_l) {
                                                                    for (int attribute_normalization : attribute_normalization_l) {
                                                                        for (int training_interval : training_interval_l) {
                                                                            for (int execution_interval : execution_interval_l) {
                                                                                try {
                                                                                    FileWriter fileWriter = new FileWriter(new File("zzmop.config"));
                                                                                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                                                                    bufferedWriter.write(history_window_size_in_bars);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(zigzag_depth);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(zigzag_pattern_length);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(zigzag_thickness);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(Double.toString(zigzag_coverage));
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(em_iterations);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(folds);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(Double.toString(minimum_log_likelihood_improvement));
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(Double.toString(minimum_standard_deviation));
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(simple_k_means_runs);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(svm_kernel_type);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(function_degree);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(Double.toString(regularization_parameter));
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(attribute_normalization);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(training_interval);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.write(execution_interval);
                                                                                    bufferedWriter.newLine();
                                                                                    bufferedWriter.close();
                                                                                    fileWriter.close();
                                                                                } catch (IOException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                                try {
                                                                                    Process p = Runtime
                                                                                            .getRuntime()
                                                                                            .exec("java -cp zzmop.config -jar ZZMOP.jar zzmop.ZZMOP");

                                                                                    byte[] bs = new byte[1024];
                                                                                    int v = p.getInputStream().read(bs);
                                                                                    while (v != -1) {
                                                                                        bs = new byte[1024];
                                                                                        v = p.getInputStream().read(bs);
                                                                                    }

                                                                                    FileReader fileReader = new FileReader("zzmop.results");
                                                                                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                                                                                    double CURRENT_RESULT = Double.parseDouble(bufferedReader.readLine());
                                                                                    if (CURRENT_RESULT > MAX_RESULT) {
                                                                                        MAX_RESULT = CURRENT_RESULT;
                                                                                        FileWriter fileWriter = new FileWriter(new File("max-zzmop.config"));
                                                                                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                                                                        bufferedWriter.write(history_window_size_in_bars);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(zigzag_depth);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(zigzag_pattern_length);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(zigzag_thickness);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(Double.toString(zigzag_coverage));
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(em_iterations);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(folds);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(Double.toString(minimum_log_likelihood_improvement));
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(Double.toString(minimum_standard_deviation));
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(simple_k_means_runs);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(svm_kernel_type);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(function_degree);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(Double.toString(regularization_parameter));
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(attribute_normalization);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.write(training_interval);
                                                                                        bufferedWriter.newLine();
                                                                                        bufferedWriter.close();
                                                                                        fileWriter.close();
                                                                                    }

                                                                                } catch (IOException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ConfigReader.readConfig("max-zzmop.config");
                Date CURRENT_END_DATE = new Date(
                        CURRENT_DATE.getTime() + ConfigReader.execution_interval);

                Process p = null;
                try {
                    p = Runtime
                            .getRuntime()
                            .exec("java -cp max-zzmop.config -jar ZZMOP.jar zzmop.ZZMOP " + dateFormat.format(CURRENT_DATE) + " " + dateFormat.format(CURRENT_END_DATE));

                    byte[] bs = new byte[1024];
                    int v = p.getInputStream().read(bs);
                    while (v != -1) {
                        bs = new byte[1024];
                        v = p.getInputStream().read(bs);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CURRENT_DATE = CURRENT_END_DATE;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
