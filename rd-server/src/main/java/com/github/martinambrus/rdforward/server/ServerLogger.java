package com.github.martinambrus.rdforward.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Redirects System.out and System.err to both the console and a timestamped
 * log file. Automatically rotates logs when they exceed a configurable size.
 *
 * <p>Call {@link #init()} early in main() before any output is produced.
 * Log files are written to a {@code logs/} directory in the working directory.
 */
public final class ServerLogger {

    private ServerLogger() {}

    private static final String LOG_DIR = "logs";
    private static final String CURRENT_LOG = "latest.log";
    private static final long MAX_LOG_SIZE = 10 * 1024 * 1024; // 10 MB

    private static volatile FileOutputStream logFileStream;

    /**
     * Initialize logging. Tees stdout and stderr to logs/latest.log with
     * timestamps. Rotates the previous latest.log to a dated filename.
     */
    public static void init() {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) dir.mkdirs();

            File logFile = new File(dir, CURRENT_LOG);

            // Rotate existing log
            if (logFile.exists() && logFile.length() > 0) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(
                        new Date(logFile.lastModified()));
                File rotated = new File(dir, timestamp + ".log");
                logFile.renameTo(rotated);
            }

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
     * OutputStream that writes to both the original console stream and a log file,
     * prepending timestamps to each line written to the file.
     */
    static class TeeOutputStream extends OutputStream {
        private final OutputStream console;
        private final FileOutputStream file;
        private final boolean isErr;
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private boolean atLineStart = true;

        TeeOutputStream(OutputStream console, FileOutputStream file, boolean isErr) {
            this.console = console;
            this.file = file;
            this.isErr = isErr;
        }

        @Override
        public synchronized void write(int b) throws IOException {
            console.write(b);
            if (atLineStart) {
                writeTimestamp();
                atLineStart = false;
            }
            file.write(b);
            if (b == '\n') {
                atLineStart = true;
            }
        }

        @Override
        public synchronized void write(byte[] buf, int off, int len) throws IOException {
            console.write(buf, off, len);
            // Write to file with timestamps at line starts
            for (int i = off; i < off + len; i++) {
                if (atLineStart) {
                    writeTimestamp();
                    atLineStart = false;
                }
                file.write(buf[i]);
                if (buf[i] == '\n') {
                    atLineStart = true;
                }
            }
        }

        @Override
        public synchronized void flush() throws IOException {
            console.flush();
            file.flush();
        }

        private void writeTimestamp() throws IOException {
            String prefix = "[" + sdf.format(new Date()) + (isErr ? " ERROR] " : "] ");
            file.write(prefix.getBytes());
        }
    }
}
