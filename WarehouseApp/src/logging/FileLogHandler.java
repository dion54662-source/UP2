package logging;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogHandler {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "warehouse_app.log";
    private static final String ERROR_LOG_FILE = "warehouse_errors.log";
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_BACKUP_FILES = 5;

    private static FileLogHandler instance;
    private PrintWriter logWriter;
    private PrintWriter errorWriter;

    private FileLogHandler() {
        try {
            createLogDirectory();
            rotateLogsIfNeeded();
            openWriters();
        } catch (IOException e) {
            System.err.println("Не удалось инициализировать файловое логирование: " + e.getMessage());
        }
    }

    public static synchronized FileLogHandler getInstance() {
        if (instance == null) {
            instance = new FileLogHandler();
        }
        return instance;
    }

    private void createLogDirectory() throws IOException {
        Path logDir = Paths.get(LOG_DIR);
        if (!Files.exists(logDir)) {
            Files.createDirectories(logDir);
        }
    }

    private void rotateLogsIfNeeded() throws IOException {
        Path logPath = Paths.get(LOG_DIR, LOG_FILE);
        if (Files.exists(logPath)) {
            long size = Files.size(logPath);
            if (size > MAX_FILE_SIZE) {
                rotateLog(LOG_FILE);
            }
        }

        Path errorPath = Paths.get(LOG_DIR, ERROR_LOG_FILE);
        if (Files.exists(errorPath)) {
            long size = Files.size(errorPath);
            if (size > MAX_FILE_SIZE) {
                rotateLog(ERROR_LOG_FILE);
            }
        }
    }

    private void rotateLog(String fileName) throws IOException {
        for (int i = MAX_BACKUP_FILES - 1; i > 0; i--) {
            Path oldFile = Paths.get(LOG_DIR, fileName + "." + i);
            Path newFile = Paths.get(LOG_DIR, fileName + "." + (i + 1));
            if (Files.exists(oldFile)) {
                Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Path currentFile = Paths.get(LOG_DIR, fileName);
        Path backupFile = Paths.get(LOG_DIR, fileName + ".1");
        Files.move(currentFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private void openWriters() throws IOException {
        logWriter = new PrintWriter(new FileWriter(Paths.get(LOG_DIR, LOG_FILE).toString(), true));
        errorWriter = new PrintWriter(new FileWriter(Paths.get(LOG_DIR, ERROR_LOG_FILE).toString(), true));
    }

    public synchronized void write(String formattedMessage, LogLevel level) {
        try {
            if (logWriter != null) {
                logWriter.println(formattedMessage);
                logWriter.flush();
            }

            if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
                if (errorWriter != null) {
                    errorWriter.println(formattedMessage);
                    errorWriter.flush();
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка записи в лог-файл: " + e.getMessage());
        }
    }

    public synchronized void close() {
        if (logWriter != null) {
            logWriter.close();
        }
        if (errorWriter != null) {
            errorWriter.close();
        }
    }

    public static String getLogContent() {
        StringBuilder content = new StringBuilder();
        try {
            Path logPath = Paths.get(LOG_DIR, LOG_FILE);
            if (Files.exists(logPath)) {
                content.append(Files.readString(logPath));
            } else {
                content.append("Лог-файл не найден\n");
            }
        } catch (IOException e) {
            content.append("Ошибка чтения лог-файла: ").append(e.getMessage());
        }
        return content.toString();
    }

    public static String getErrorLogContent() {
        StringBuilder content = new StringBuilder();
        try {
            Path errorPath = Paths.get(LOG_DIR, ERROR_LOG_FILE);
            if (Files.exists(errorPath)) {
                content.append(Files.readString(errorPath));
            } else {
                content.append("Файл ошибок не найден\n");
            }
        } catch (IOException e) {
            content.append("Ошибка чтения файла ошибок: ").append(e.getMessage());
        }
        return content.toString();
    }

    public static void clearLogs() {
        try {
            Files.writeString(Paths.get(LOG_DIR, LOG_FILE), "");
            Files.writeString(Paths.get(LOG_DIR, ERROR_LOG_FILE), "");
        } catch (IOException e) {
            System.err.println("Ошибка очистки логов: " + e.getMessage());
        }
    }
}