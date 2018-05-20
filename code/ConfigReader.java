package zzmop;

import java.io.*;

public class ConfigReader {
    public static int history_window_size_in_bars;
    public static int zigzag_depth;
    public static int zigzag_pattern_length;
    public static int zigzag_thickness;
    public static double zigzag_coverage;
    public static int em_iterations;
    public static int folds;
    public static double minimum_log_likelihood_improvement;
    public static double minimum_standard_deviation;
    public static int simple_k_means_runs;
    public static int svm_kernel_type;
    public static int function_degree;
    public static double regularization_parameter;
    public static int attribute_normalization;
    public static int training_interval;
    public static int execution_interval;


    public static void readConfig(String config) {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File(config));

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            history_window_size_in_bars = Integer.parseInt(bufferedReader.readLine());
            zigzag_depth = Integer.parseInt(bufferedReader.readLine());
            zigzag_pattern_length = Integer.parseInt(bufferedReader.readLine());
            zigzag_thickness = Integer.parseInt(bufferedReader.readLine());
            zigzag_coverage = Double.parseDouble(bufferedReader.readLine());
            em_iterations = Integer.parseInt(bufferedReader.readLine());
            folds = Integer.parseInt(bufferedReader.readLine());
            minimum_log_likelihood_improvement = Double.parseDouble(bufferedReader.readLine());
            minimum_standard_deviation = Double.parseDouble(bufferedReader.readLine());
            simple_k_means_runs = Integer.parseInt(bufferedReader.readLine());
            svm_kernel_type = Integer.parseInt(bufferedReader.readLine());
            function_degree = Integer.parseInt(bufferedReader.readLine());
            regularization_parameter = Double.parseDouble(bufferedReader.readLine());
            attribute_normalization = Integer.parseInt(bufferedReader.readLine());
            training_interval = Integer.parseInt(bufferedReader.readLine());
            execution_interval = Integer.parseInt(bufferedReader.readLine());

            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
