package com.github.martinambrus.rdforward.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * Redirects System.out and System.err to both the console and a timestamped
 * log file. Automatically rotates logs at midnight and gzip-compresses
 * rotated files.
 *
 * <p>Call {@link #init()} early in main() before any output is produced.
 * Log files are written to a {@code logs/} directory in the working directory.
 */
public final class ServerLogger {

    private ServerLogger() {}

    private static final String LOG_DIR = "logs";
    private static final String CURRENT_LOG = "latest.log";

    private static final int MAX_LOG_AGE_DAYS = 30;

    private static volatile FileOutputStream logFileStream;

    /** Shared lock for all rotation and log file write operations. */
    private static final Object LOG_LOCK = new Object();

    /** The date string (yyyy-MM-dd) when the current log file was started. */
    private static volatile String currentLogDay;

    /** Millis timestamp at which the next midnight rotation check is needed.
     *  Avoids creating Date objects and formatting on every log line. */
    private static volatile long nextRotationCheckMs;

    /**
     * Initialize logging. Tees stdout and stderr to logs/latest.log with
     * timestamps. Rotates the previous latest.log to a dated filename
     * and compresses any uncompressed old log files.
     */
    public static void init() {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) dir.mkdirs();

            File logFile = new File(dir, CURRENT_LOG);

            // Rotate existing log from previous run
            if (logFile.exists() && logFile.length() > 0) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(
                        new Date(logFile.lastModified()));
                File rotated = new File(dir, timestamp + ".log");
                logFile.renameTo(rotated);
                gzipAndDelete(rotated);
            }

            // Compress any old uncompressed .log files left from before
            compressOldLogs(dir);

            currentLogDay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            nextRotationCheckMs = computeNextMidnightMs();
            logFileStream = new FileOutputStream(new File(dir, CURRENT_LOG), false);

            PrintStream teeOut = new PrintStream(new TeeOutputStream(System.out, logFileStream, false), true);
            PrintStream teeErr = new PrintStream(new TeeOutputStream(System.err, logFileStream, true), true);

            System.setOut(teeOut);
            System.setErr(teeErr);

        } catch (IOException e) {
            System.err.println("[WARN] Failed to initialize log file: " + e.getMessage());
        }
    }

    /**
     * Flush and close the log file. Called on shutdown.
     */
    public static void close() {
        if (logFileStream != null) {
            try {
                logFileStream.flush();
                logFileStream.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Check if the date has rolled past midnight and rotate if so.
     * Called from the write path; must be called under synchronization.
     * Uses a cached timestamp to avoid Date allocation on every log line.
     */
    private static void rotateIfNeeded() {
        if (System.currentTimeMillis() < nextRotationCheckMs) return;
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (today.equals(currentLogDay)) {
            // Not midnight yet — push the next check forward 60 seconds
            nextRotationCheckMs = System.currentTimeMillis() + 60_000;
            return;
        }

        try {
            // Flush and close the current log
            logFileStream.flush();
            logFileStream.close();

            File dir = new File(LOG_DIR);
            File logFile = new File(dir, CURRENT_LOG);

            // Rename using the OLD day (the day the log was for)
            String timestamp = currentLogDay + "_23-59-59";
            File rotated = new File(dir, timestamp + ".log");
            // Avoid name collision if server was restarted the same day
            if (rotated.exists()) {
                timestamp = currentLogDay + "_" + new SimpleDateFormat("HH-mm-ss").format(new Date());
                rotated = new File(dir, timestamp + ".log");
            }

            if (!logFile.renameTo(rotated)) {
                System.err.println("[WARN] Log rotation failed: could not rename " + logFile + " to " + rotated + ". Continuing with current log file.");
                // Re-open the existing file in append mode and keep going
                logFileStream = new FileOutputStream(logFile, true);
                nextRotationCheckMs = System.currentTimeMillis() + 60_000;
                return;
            }

            // Compress in a background thread to not block logging
            final File toCompress = rotated;
            Thread compressor = new Thread(() -> gzipAndDelete(toCompress), "log-compressor");
            compressor.setDaemon(true);
            compressor.start();

            // Open new log file
            currentLogDay = today;
            nextRotationCheckMs = computeNextMidnightMs();
            logFileStream = new FileOutputStream(new File(dir, CURRENT_LOG), false);
        } catch (IOException e) {
            System.err.println("[WARN] Log rotation failed: " + e.getMessage());
            // If rotation fails, try to keep logging to the existing file
            try {
                logFileStream = new FileOutputStream(new File(LOG_DIR, CURRENT_LOG), true);
            } catch (IOException fatal) {
                System.err.println("[ERROR] Could not reopen log file after failed rotation: " + fatal.getMessage());
            }
        }
    }

    /**
     * Compute millis timestamp for the next midnight (local time).
     */
    private static long computeNextMidnightMs() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Gzip-compress a file and delete the original.
     */
    private static void gzipAndDelete(File source) {
        File gzFile = new File(source.getPath() + ".gz");
        try (FileInputStream fis = new FileInputStream(source);
             GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(gzFile))) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) != -1) {
                gzos.write(buf, 0, len);
            }
        } catch (IOException e) {
            System.err.println("[WARN] Failed to gzip log file " + source.getName() + ": " + e.getMessage());
            return;
        }
        source.delete();
    }

    /**
     * Compress any .log files in the log directory that aren't the current log.
     */
    private static void compressOldLogs(File dir) {
        File[] oldLogs = dir.listFiles((d, name) ->
                name.endsWith(".log") && !name.equals(CURRENT_LOG));
        if (oldLogs == null) return;
        for (File old : oldLogs) {
            gzipAndDelete(old);
        }

        // Delete .gz files older than MAX_LOG_AGE_DAYS
        long cutoffMs = System.currentTimeMillis() - MAX_LOG_AGE_DAYS * 24L * 60 * 60 * 1000;
        File[] gzFiles = dir.listFiles((d, name) -> name.endsWith(".log.gz"));
        if (gzFiles == null) return;
        for (File gz : gzFiles) {
            if (gz.lastModified() < cutoffMs) {
                if (!gz.delete()) {
                    System.err.println("[WARN] Failed to delete old log file: " + gz.getName());
                }
            }
        }
    }

    /**
     * OutputStream that writes to both the original console stream and a log file,
     * prepending timestamps to each line written to the file. Triggers midnight
     * log rotation when the date changes.
     */
    static class TeeOutputStream extends OutputStream {
        private final OutputStream console;
        private final boolean isErr;
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private boolean atLineStart = true;

        TeeOutputStream(OutputStream console, FileOutputStream file, boolean isErr) {
            this.console = console;
            this.isErr = isErr;
        }

        @Override
        public void write(int b) throws IOException {
            console.write(b);
            synchronized (LOG_LOCK) {
                if (atLineStart) {
                    rotateIfNeeded();
                    writeTimestamp();
                    atLineStart = false;
                }
                logFileStream.write(b);
                if (b == '\n') {
                    atLineStart = true;
                }
            }
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            console.write(buf, off, len);
            synchronized (LOG_LOCK) {
                for (int i = off; i < off + len; i++) {
                    if (atLineStart) {
                        rotateIfNeeded();
                        writeTimestamp();
                        atLineStart = false;
                    }
                    logFileStream.write(buf[i]);
                    if (buf[i] == '\n') {
                        atLineStart = true;
                    }
                }
            }
        }

        @Override
        public void flush() throws IOException {
            console.flush();
            synchronized (LOG_LOCK) {
                logFileStream.flush();
            }
        }

        private void writeTimestamp() throws IOException {
            String prefix = "[" + sdf.format(new Date()) + (isErr ? " ERROR] " : "] ");
            logFileStream.write(prefix.getBytes());
        }
    }
}
