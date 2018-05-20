package zzmop;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.LoadingProgressListener;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.ITesterClient.DataLoadingMethod;
import com.dukascopy.api.system.TesterFactory;
import jforex.devicelocal.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;


public class ZZMOP {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZZMOP.class);
    private static String jnlpUrl = "http://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    static int SIMULATION_RESOLUTION_MILLISECONDS = 100;
    static int SIMULATION_RESOLUTION_PIPS = 1;

    private static ITesterClient client;

    public static void main(String[] args) throws Exception {
        client = TesterFactory.getDefaultInstance();
        client.setCacheDirectory(new File(Constants.cache_path));
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        DataLoadingMethod.TICKS_WITH_TIME_INTERVAL.setTimeIntervalBetweenTicks(SIMULATION_RESOLUTION_MILLISECONDS);
        DataLoadingMethod.TICKS_WITH_PRICE_DIFFERENCE_IN_PIPS.setPriceDifferenceInPips(SIMULATION_RESOLUTION_PIPS);

        client.setDataInterval(DataLoadingMethod.TICKS_WITH_TIME_INTERVAL, format.parse(args[0]).getTime(), format.parse(args[1]).getTime());
        client.setSystemListener(new ISystemListener() {
            @Override
            public void onStart(long processId) {
                LOGGER.info("Strategy started: " + processId);
            }

            @Override
            public void onStop(long processId) {
                LOGGER.info("Strategy stopped: " + processId);
                try {
                    Double maxDeposit = client.getReportData(processId).getFinishDeposit();
                    FileWriter fwr = new FileWriter(new File("zzmop.results"));
                    fwr.write(Double.toString(maxDeposit));
                    fwr.close();
                    File reportFile = new File(Constants.jforex_home + "\\reports\\report-" + System.currentTimeMillis() + ".html");
                    try {
                        client.createReport(processId, reportFile);

                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } catch (Exception e) {

                    LOGGER.error(e.getMessage(), e);
                }
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
            }

            @Override
            public void onConnect() {
                LOGGER.info("Connected");
            }

            @Override
            public void onDisconnect() {

            }
        });

        LOGGER.info("Connecting...");

        client.connect(jnlpUrl, Constants.userName, Constants.password);

        int i = 25;
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            LOGGER.error("Failed to connect Dukascopy servers");
            System.exit(1);
        }

        Set<Instrument> instruments = new HashSet<>();
        instruments.add(Instrument.EURUSD);

        LOGGER.info("Subscribing instruments...");
        client.setSubscribedInstruments(instruments);

        client.setInitialDeposit(Instrument.EURUSD.getSecondaryJFCurrency(), 50000);

        LOGGER.info("Downloading data");
        Future<?> future = client.downloadData(null);

        future.get();

        LOGGER.info("Starting strategy");
        startStrategy();
    }

    public static void startStrategy() {
        client.startStrategy(new Pattern_Mining_SVM_Strategy(client), new LoadingProgressListener() {
            @Override
            public void dataLoaded(long startTime, long endTime, long currentTime, String information) {
                LOGGER.info(information);
            }

            @Override
            public void loadingFinished(boolean allDataLoaded, long startTime, long endTime, long currentTime) {
            }

            @Override
            public boolean stopJob() {
                return false;
            }
        });
    }
}
