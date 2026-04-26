package com.github.martinambrus.rdforward.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
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

    /** Console charset for transcoding (null when console is already UTF-8). */
    private static volatile Charset consoleCharset;

    /**
     * Initialize logging. Tees stdout and stderr to logs/latest.log with
     * timestamps. Rotates the previous latest.log to a dated filename
     * and compresses any uncompressed old log files.
     */
    public static void init() {
        try {
            consoleCharset = detectConsoleCharset();

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

            PrintStream teeOut = new PrintStream(new TeeOutputStream(System.out, logFileStream, false), true, StandardCharsets.UTF_8);
            PrintStream teeErr = new PrintStream(new TeeOutputStream(System.err, logFileStream, true), true, StandardCharsets.UTF_8);

            System.setOut(teeOut);
            System.setErr(teeErr);

            configureJulLogging();

        } catch (IOException e) {
            System.err.println("[WARN] Failed to initialize log file: " + e.getMessage());
        }
    }

    /**
     * Replace JUL's default 2-line {@code SimpleFormatter} (which emits a
     * separate header line with the date and source class/method, then a
     * second line with the level and message) with a compact one-liner.
     * Modloader INFO output is gated behind {@code DebugLog.setEnabled} —
     * by default only WARNING and above surface for the modloader
     * package; the {@code /debug on} console command flips the level to
     * ALL so operators can see EventManager/CommandConflictResolver
     * lifecycle traces when they need to.
     */
    private static void configureJulLogging() {
        java.util.logging.Formatter compact = new java.util.logging.Formatter() {
            @Override
            public String format(java.util.logging.LogRecord r) {
                StringBuilder sb = new StringBuilder(128);
                sb.append('[').append(r.getLevel()).append("] ").append(formatMessage(r));
                sb.append(System.lineSeparator());
                Throwable t = r.getThrown();
                if (t != null) {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    t.printStackTrace(new java.io.PrintWriter(sw));
                    sb.append(sw);
                }
                return sb.toString();
            }
        };
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        for (java.util.logging.Handler h : root.getHandlers()) {
            h.setLevel(java.util.logging.Level.ALL);
            h.setFormatter(compact);
        }
        setModLoaderVerbose(false);
    }

    /**
     * Toggle modloader package JUL verbosity. {@code false} (default)
     * shows only WARNING and above; {@code true} surfaces INFO and below
     * for diagnostic use via the {@code /debug} console command.
     */
    public static void setModLoaderVerbose(boolean verbose) {
        java.util.logging.Logger.getLogger("com.github.martinambrus.rdforward.modloader")
                .setLevel(verbose ? java.util.logging.Level.ALL : java.util.logging.Level.WARNING);
    }

    /**
     * Determine the charset the underlying console expects, or {@code null}
     * when no transcoding is needed (console is already UTF-8 or output is
     * piped). On Windows cmd this typically returns the active code page
     * (cp437/cp850/cp1252); when the user has run {@code chcp 65001} it
     * returns UTF-8 and we skip transcoding.
     */
    private static Charset detectConsoleCharset() {
        Charset cs = null;
        if (System.console() != null) {
            try { cs = System.console().charset(); } catch (Throwable ignored) {}
        }
        if (cs == null) {
            String enc = System.getProperty("stdout.encoding");
            if (enc == null) enc = System.getProperty("native.encoding");
            if (enc != null) {
                try { cs = Charset.forName(enc); } catch (Throwable ignored) {}
            }
        }
        if (cs == null || StandardCharsets.UTF_8.equals(cs)) return null;
        return cs;
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

        /** Per-line buffer of UTF-8 bytes for console transcoding. */
        private final ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(256);
        private final CharsetEncoder consoleEncoder;

        TeeOutputStream(OutputStream console, FileOutputStream file, boolean isErr) {
            this.console = console;
            this.isErr = isErr;
            Charset cs = consoleCharset;
            if (cs == null) {
                this.consoleEncoder = null;
            } else {
                this.consoleEncoder = cs.newEncoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE)
                        .replaceWith(new byte[]{'?'});
            }
        }

        @Override
        public void write(int b) throws IOException {
            writeConsoleByte((byte) b);
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
            for (int i = off; i < off + len; i++) {
                writeConsoleByte(buf[i]);
            }
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

        /** Console-side write. Line-buffers when transcoding so multi-byte
         *  UTF-8 sequences are not split across encoder calls. */
        private void writeConsoleByte(byte b) throws IOException {
            if (consoleEncoder == null) {
                console.write(b & 0xFF);
                return;
            }
            lineBuf.write(b & 0xFF);
            if (b == '\n') {
                flushLineBufToConsole();
            }
        }

        private void flushLineBufToConsole() throws IOException {
            if (lineBuf.size() == 0) return;
            String line = lineBuf.toString(StandardCharsets.UTF_8);
            lineBuf.reset();
            line = sanitizeForLegacyConsole(line);
            try {
                consoleEncoder.reset();
                ByteBuffer bb = consoleEncoder.encode(CharBuffer.wrap(line));
                console.write(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
            } catch (java.nio.charset.CharacterCodingException e) {
                // REPLACE action above means this should never throw, but be defensive.
                console.write(line.getBytes(StandardCharsets.US_ASCII));
            }
        }

        /** Map common Unicode punctuation/arrows to ASCII equivalents so a
         *  cp437/cp1252 console renders them naturally instead of as '?'. */
        private static String sanitizeForLegacyConsole(String s) {
            if (s.indexOf('‐') < 0 && s.indexOf('–') < 0
                    && s.indexOf('—') < 0 && s.indexOf('‘') < 0
                    && s.indexOf('’') < 0 && s.indexOf('“') < 0
                    && s.indexOf('”') < 0 && s.indexOf('…') < 0
                    && s.indexOf('←') < 0 && s.indexOf('→') < 0
                    && s.indexOf('≈') < 0 && s.indexOf('×') < 0) {
                return s;
            }
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '‐': case '–': sb.append('-'); break;       // hyphen, en dash
                    case '—': sb.append("--"); break;                     // em dash
                    case '‘': case '’': sb.append('\''); break;      // smart single quotes
                    case '“': case '”': sb.append('"'); break;       // smart double quotes
                    case '…': sb.append("..."); break;                    // ellipsis
                    case '←': sb.append("<-"); break;                     // left arrow
                    case '→': sb.append("->"); break;                     // right arrow
                    case '≈': sb.append("~="); break;                     // approx equal
                    case '×': sb.append('x'); break;                      // multiplication sign
                    default: sb.append(c);
                }
            }
            return sb.toString();
        }

        @Override
        public void flush() throws IOException {
            flushLineBufToConsole();
            console.flush();
            synchronized (LOG_LOCK) {
                logFileStream.flush();
            }
        }

        private void writeTimestamp() throws IOException {
            String prefix = "[" + sdf.format(new Date()) + (isErr ? " ERROR] " : "] ");
            logFileStream.write(prefix.getBytes(StandardCharsets.UTF_8));
        }
    }
}
