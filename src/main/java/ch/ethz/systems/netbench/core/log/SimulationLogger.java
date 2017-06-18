package ch.ethz.systems.netbench.core.log;

import ch.ethz.systems.netbench.core.Simulator;
import ch.ethz.systems.netbench.core.config.NBProperties;
import ch.ethz.systems.netbench.core.run.MainFromProperties;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimulationLogger {

    // Main token identifying the run log folder
    private static String runFolderName;
    private static String baseDir;

    // Access to files for logging (are kept open during simulation run)
    private static BufferedWriter writerRunInfoFile;
    private static BufferedWriter writerFlowCompletionCsvFile;
    private static BufferedWriter writerFlowThroughputFile;
    private static BufferedWriter writerFlowCompletionFile;
    private static BufferedWriter writerPortQueueStateFile;
    private static BufferedWriter writerPortUtilizationFile;
    private static BufferedWriter writerPortUtilizationCsvFile;
    private static Map<String, BufferedWriter> writersAdded = new HashMap<>();

    // Specific component loggers
    private static List<PortLogger> portLoggers = new ArrayList<>();
    private static List<FlowLogger> flowLoggers = new ArrayList<>();
    private static List<LoggerCallback> callbacks = new ArrayList<>();

    // Statistic counters
    private static Map<String, Long> statisticCounters = new HashMap<>();

    // Print streams used
    private static PrintStream originalOutOutputStream;
    private static PrintStream originalErrOutputStream;
    private static OutputStream underlyingFileOutputStream;

    // Settings
    private static boolean logHumanReadableFlowCompletionEnabled;

    /**
     * Increase a basic statistic counter with the given name by one.
     *
     * @param name  Statistic name
     */
    public static void increaseStatisticCounter(String name) {
        Long val = statisticCounters.get(name);
        if (val == null) {
            statisticCounters.put(name, 1L);
        } else {
            statisticCounters.put(name, val + 1L);
        }
    }

    /**
     * Register a port logger so that it can be
     * later called after the run is over to collect
     * is statistics.
     *
     * @param logger    Port logger instance
     */
    static void registerPortLogger(PortLogger logger) {
        portLoggers.add(logger);
    }

    /**
     * Register a flow logger so that it
     * can be later called after the run is over to
     * collect its statistics.
     *
     * @param logger    Flow logger instance
     */
    static void registerFlowLogger(FlowLogger logger) {
        flowLoggers.add(logger);
    }

    /**
     * Retrieve the full absolute path of the run folder.
     *
     * @return  Full run folder path
     */
    public static String getRunFolderFull() {
        return baseDir + "/" + runFolderName;
    }

    /**
     * Open log file writer without a specific run folder name.
     */
    public static void open() {
        open(null);
    }

    /**
     * Open log file writers with a specific run folder name.
     *
     * @param tempRunConfiguration  Temporary run configuration (not yet centrally loaded)
     */
    public static void open(NBProperties tempRunConfiguration) {

        // Settings
        String specificRunFolderName = null;
        String specificRunFolderBaseDirectory = null;
        if (tempRunConfiguration != null) {
            // logPacketBurstGapEnabled = tempRunConfiguration.getBooleanPropertyWithDefault("enable_log_packet_burst_gap", false);

            // Run folder
            specificRunFolderName = tempRunConfiguration.getPropertyWithDefault("run_folder_name", null);
            specificRunFolderBaseDirectory = tempRunConfiguration.getPropertyWithDefault("run_folder_base_dir", null);

            // Enabling human readable version
            logHumanReadableFlowCompletionEnabled = tempRunConfiguration.getBooleanPropertyWithDefault("enable_generate_human_readable_flow_completion_log", true);

        }

        // Overwrite if run folder name was specified in run configuration
        if (specificRunFolderName == null) {
            runFolderName = "nameless_run_" + new SimpleDateFormat("yyyy-MM-dd--HH'h'mm'm'ss's'").format(new Date());
        } else {
            runFolderName = specificRunFolderName;
        }

        // Overwrite if run folder name was specified in run configuration
        if (specificRunFolderBaseDirectory == null) {
            baseDir = "./temp";
        } else {
            baseDir = specificRunFolderBaseDirectory;
        }

        try {

            // Create run token folder
            new File(getRunFolderFull()).mkdirs();

            // Copy console output to the run folder
            FileOutputStream fosOS = new FileOutputStream(getRunFolderFull() + "/console.txt");
            TeeOutputStream customOutputStreamOut = new TeeOutputStream(System.out, fosOS);
            TeeOutputStream customOutputStreamErr = new TeeOutputStream(System.err, fosOS);
            underlyingFileOutputStream = fosOS;
            originalOutOutputStream = System.out;
            originalErrOutputStream = System.err;
            System.setOut(new PrintStream(customOutputStreamOut));
            System.setErr(new PrintStream(customOutputStreamErr));

            // Info
            writerRunInfoFile = openWriter("initialization.info");

            // Port log writers
            writerPortQueueStateFile = openWriter("port_queue_length.csv.log");
            writerPortUtilizationCsvFile = openWriter("port_utilization.csv.log");
            writerPortUtilizationFile = openWriter("port_utilization.log");

            // Flow log writers
            writerFlowThroughputFile = openWriter("flow_throughput.csv.log");
            writerFlowCompletionCsvFile = openWriter("flow_completion.csv.log");
            writerFlowCompletionFile = openWriter("flow_completion.log");

            // Writer out the final properties' values
            if (tempRunConfiguration != null) {
                BufferedWriter finalPropertiesInfoFile = openWriter("final_properties.info");
                finalPropertiesInfoFile.write(tempRunConfiguration.getAllPropertiesToString());
                finalPropertiesInfoFile.close();
            }

        } catch (IOException e) {
            throw new LogFailureException(e);
        }

    }

    /**
     * Register the call back of a logger before the close of the simulation logger.
     *
     * @param callback  Callback instance
     */
    public static void registerCallbackBeforeClose(LoggerCallback callback) {
        callbacks.add(callback);
    }

    /**
     * Open a log writer in the run directory.
     *
     * @param logFileName   Log file name
     *
     * @return Writer of the log
     */
    private static BufferedWriter openWriter(String logFileName) {
        try {
            return new BufferedWriter(
                    new FileWriter(getRunFolderFull() + "/" + logFileName)
            );
        } catch (IOException e) {
            throw new LogFailureException(e);
        }
    }

    /**
     * Create (or fetch) an external writer, which can be used to create your own personal logs.
     *
     * @param logFileName   Log file name
     *
     * @return Writer instance (already opened, is automatically closed when calling {@link #close()})
     */
    public static BufferedWriter getExternalWriter(String logFileName) {
        BufferedWriter writer = writersAdded.get(logFileName);
        if (writer == null) {
            writer = openWriter(logFileName);
            writersAdded.put(logFileName, writer);
        }
        return writer;
    }

    /**
     * Log summaries and close log file writers.
     */
    public static void close() {

        // Callback loggers to finalize their logs
        for (LoggerCallback callback : callbacks) {
            callback.callBeforeClose();
        }
        callbacks.clear();

        // Most important logs
        logFlowSummary();
        logPortUtilization();

        try {

            // Write basic statistics about the run
            BufferedWriter writerStatistics = openWriter("statistics.log");
            ArrayList<String> stats = new ArrayList<>();
            stats.addAll(statisticCounters.keySet());
            Collections.sort(stats);
            for (String s : stats) {
                writerStatistics.write(s + ": " + statisticCounters.get(s) + "\n");
            }
            writerStatistics.close();

            // Close *all* the running log files
            writerRunInfoFile.close();
            writerFlowCompletionCsvFile.close();
            writerFlowThroughputFile.close();
            writerPortQueueStateFile.close();
            writerPortUtilizationFile.close();
            writerPortUtilizationCsvFile.close();
            writerFlowCompletionFile.close();

            // Also added ones are closed automatically at the end
            for (BufferedWriter writer : writersAdded.values()) {
                writer.close();
            }
            writersAdded.clear();

            // Set diverted print streams back
            System.out.flush();
            System.err.flush();
            System.setOut(originalOutOutputStream);
            System.setErr(originalErrOutputStream);
            underlyingFileOutputStream.close();

            // Clear loggers
            portLoggers.clear();
            flowLoggers.clear();

        } catch (IOException e) {
            throw new LogFailureException(e);
        }

    }

    /**
     * Log a general parameter to indicate some information
     * about what was done in the run.
     *
     * @param key       Key string
     * @param value     Value string
     */
    public static void logInfo(String key, String value) {
        try {
            writerRunInfoFile.write(key + ": " + value + "\n");
            writerRunInfoFile.flush();
        } catch (IOException e) {
            throw new LogFailureException(e);
        }
    }

    /**
     * Log that flow <code>flowId</code> originating from network device <code>sourceId</code> has
     * sent a total of <code>amountBytes</code> in the past <code>timeNs</code> nanoseconds.
     *
     * @param flowId            Unique flow identifier
     * @param sourceId          Source network device identifier
     * @param targetId          Target network device identifier
     * @param amountBytes       Amount of bytes sent in the interval
     * @param absStartTimeNs    Interval start in nanoseconds
     * @param absEndTimeNs      Interval end in nanoseconds
     */
    static void logFlowThroughput(long flowId, int sourceId, int targetId, long amountBytes, long absStartTimeNs, long absEndTimeNs) {
        try {
            writerFlowThroughputFile.write(flowId + "," + sourceId + "," + targetId + "," + amountBytes + "," + absStartTimeNs + "," + absEndTimeNs + "\n");
        } catch (IOException e) {
            throw new LogFailureException(e);
        }
    }

    /**
     * Log the queue length of a specific output port at a certain point in time.
     *
     * @param ownId                 Port source network device identifier (device to which it is attached)
     * @param targetId              Port target network device identifier (where the other end of the cable is connected to)
     * @param queueLength           Current length of the queue
     * @param bufferOccupiedBits    Amount of bits occupied in the buffer
     * @param absTimeNs             Absolute timestamp in nanoseconds since simulation epoch
     */
    static void logPortQueueState(long ownId, long targetId, int queueLength, long bufferOccupiedBits, long absTimeNs) {
        try {
            writerPortQueueStateFile.write(ownId + "," + targetId + "," + queueLength + "," + bufferOccupiedBits + "," + absTimeNs + "\n");
        } catch (IOException e) {
            throw new LogFailureException(e);
        }
    }

    /**
     * Print a human-readable summary of all the flows and whether they were completed.
     */
    private static void logFlowSummary() {
        try {

            // Header
            if (logHumanReadableFlowCompletionEnabled) {
                writerFlowCompletionFile.write(
                        String.format(
                                "%-11s%-6s%-6s%-13s%-13s%-15s%-10s\n",
                                "FlowId",
                                "Src",
                                "Dst",
                                "Sent (byte)",
                                "Total (byte)",
                                "Duration (ms)",
                                "Progress"
                        )
                );
            }

            // Sort them based on starting time
            Collections.sort(flowLoggers, new Comparator<FlowLogger>() {
                @Override
                public int compare(FlowLogger o1, FlowLogger o2) {
                    long delta = o2.getFlowStartTime() - o1.getFlowStartTime();
                    if (delta < 0) {
                        return 1;
                    } else if (delta > 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            for (FlowLogger logger : flowLoggers) {

                if (logHumanReadableFlowCompletionEnabled) {
                    writerFlowCompletionFile.write(
                            String.format(
                                    "%-11s%-6s%-6s%-13s%-13s%-8.2f%-7s%.2f%%\n",
                                    logger.getFlowId(),
                                    logger.getSourceId(),
                                    logger.getTargetId(),
                                    logger.getTotalBytesReceived(),
                                    logger.getFlowSizeByte(),
                                    (logger.isCompleted() ? (logger.getFlowEndTime() - logger.getFlowStartTime()) / 1e6 : (Simulator.getCurrentTime() - logger.getFlowStartTime()) / 1e6),
                                    (logger.isCompleted() ? "" : " (DNF)"),
                                    ((double) logger.getTotalBytesReceived() / (double) logger.getFlowSizeByte()) * 100
                            )
                    );
                }

                // flowId, sourceId, targetId, sentBytes, totalBytes, flowStartTime, flowEndTime, flowDuration, isCompleted
                writerFlowCompletionCsvFile.write(
                        logger.getFlowId() + "," +
                        logger.getSourceId() + "," +
                        logger.getTargetId() + "," +
                        logger.getTotalBytesReceived() + "," +
                        logger.getFlowSizeByte() + "," +
                        logger.getFlowStartTime() + "," +
                        (logger.isCompleted() ? logger.getFlowEndTime() : Simulator.getCurrentTime()) + "," +
                        (logger.isCompleted() ? (logger.getFlowEndTime() - logger.getFlowStartTime()) : (Simulator.getCurrentTime() - logger.getFlowStartTime())) + "," +
                        (logger.isCompleted() ? "TRUE" : "FALSE") + "\n"
                );

            }

        } catch (IOException e) {
            throw new LogFailureException(e);
        }

    }

    /**
     * Print a human-readable summary of all the port utilization.
     */
    private static void logPortUtilization() {

        try {

            // Header
            writerPortUtilizationFile.write(
                    String.format(
                            "%-6s%-6s%-9s%-16s%s\n",
                            "Src",
                            "Dst",
                            "Srvport",
                            "Utilized (ns)",
                            "Utilization"
                    )
            );

            // Sort them based on utilization
            Collections.sort(portLoggers, new Comparator<PortLogger>() {
                @Override
                public int compare(PortLogger o1, PortLogger o2) {
                    long delta = o2.getUtilizedNs() - o1.getUtilizedNs();
                    if (delta < 0) {
                        return -1;
                    } else if (delta > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            // Data entries
            for (PortLogger logger : portLoggers) {
                writerPortUtilizationCsvFile.write(
                        logger.getOwnId() + "," +
                        logger.getTargetId() + "," +
                        (logger.isAttachedToServer() ? "Y" : "N") + "," +
                        logger.getUtilizedNs() + "," +
                        (((double) logger.getUtilizedNs() / (double) Simulator.getCurrentTime()) * 100) + "\n"
                );
                writerPortUtilizationFile.write(
                        String.format(
                                "%-6d%-6d%-9s%-16d%.2f%%\n",
                                logger.getOwnId(),
                                logger.getTargetId(),
                                (logger.isAttachedToServer() ? "YES" : "NO"),
                                logger.getUtilizedNs(),
                                ((double) logger.getUtilizedNs() / (double) Simulator.getCurrentTime()) * 100
                        )
                );
            }

        } catch (IOException e) {
            throw new LogFailureException(e);
        }

    }

    /**
     * Completely throw away all the logs generated in this run.
     *
     * Adapted from:
     * http://stackoverflow.com/questions/7768071/how-to-delete-directory-content-in-java
     */
    private static void throwaway() {
        boolean success = false;
        String fol = getRunFolderFull();
        File folder = new File(fol);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    success = f.delete() || success;
                }
            }
        }
        success = folder.delete() || success;

        // Failure to throw away log files
        if (!success) {
            throw new RuntimeException("Throw away failed, could not delete one or more files/directories.");
        }

    }

    /**
     * Copy the configuration files.
     */
    public static void copyRunConfiguration() {
        copyFileToRunFolder(Simulator.getConfiguration().getFileName());
    }

    /**
     * Copy any desired file to the run folder.
     *
     * @param fileName  File name
     */
    private static void copyFileToRunFolder(String fileName) {
        System.out.println("Copying file \"" + fileName + "\" to run folder...");
        MainFromProperties.runCommand("cp " + fileName + " " + getRunFolderFull(), false);
    }

    /**
     * Copy any desired file to the run folder under a new name.
     *
     * @param fileName      File name
     * @param newFileName   New file name
     */
    public static void copyFileToRunFolder(String fileName, String newFileName) {
        System.out.println("Copying file \"" + fileName + "\" to run folder using new file name \"" + newFileName + "\"...");
        MainFromProperties.runCommand("cp " + fileName + " " + getRunFolderFull() + "/" + newFileName, false);
    }

    /**
     * Close log streams and throw away logs.
     */
    public static void closeAndThrowaway() {
        close();
        throwaway();
    }

}
