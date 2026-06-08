package logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static LogLevel currentLevel = LogLevel.DEBUG;
    private static FileLogHandler fileHandler;
    private static boolean fileLoggingEnabled = true;
    private static boolean consoleLoggingEnabled = true;

    static {
        try {
            fileHandler = FileLogHandler.getInstance();
        } catch (Exception e) {
            System.err.println("Не удалось инициализировать файловое логирование: " + e.getMessage());
            fileLoggingEnabled = false;
        }
    }

    public static void setLogLevel(LogLevel level) {
        currentLevel = level;
        info(Logger.class, "setLogLevel", "Уровень логирования изменен на: " + level.name());
    }

    public static void setFileLoggingEnabled(boolean enabled) {
        fileLoggingEnabled = enabled;
    }

    public static void setConsoleLoggingEnabled(boolean enabled) {
        consoleLoggingEnabled = enabled;
    }

    private static void log(LogLevel level, Class<?> clazz, String methodName, String message, Throwable throwable) {
        if (!level.isEnabled(currentLevel)) return;

        String className = clazz != null ? clazz.getSimpleName() : "Unknown";
        String fullMessage = message;
        if (throwable != null) {
            fullMessage = message + " | Exception: " + throwable.getClass().getSimpleName() + " - " + throwable.getMessage();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        String formattedMessage = String.format("[%s] [%s] %s [%s.%s] %s",
                timestamp, level.name(), level.getIcon(), className, methodName, fullMessage);

        if (consoleLoggingEnabled) {
            ConsoleLogHandler.write(message, level, className, methodName);
        }

        if (fileLoggingEnabled && fileHandler != null) {
            fileHandler.write(formattedMessage, level);
        }

        // Вывод stack trace для ошибок
        if (throwable != null && (level == LogLevel.ERROR || level == LogLevel.FATAL)) {
            if (consoleLoggingEnabled) {
                throwable.printStackTrace();
            }
        }
    }

    // Основные методы логирования
    public static void debug(Class<?> clazz, String methodName, String message) {
        log(LogLevel.DEBUG, clazz, methodName, message, null);
    }

    public static void info(Class<?> clazz, String methodName, String message) {
        log(LogLevel.INFO, clazz, methodName, message, null);
    }

    public static void warning(Class<?> clazz, String methodName, String message) {
        log(LogLevel.WARNING, clazz, methodName, message, null);
    }

    public static void warning(Class<?> clazz, String methodName, String message, Throwable t) {
        log(LogLevel.WARNING, clazz, methodName, message, t);
    }

    public static void error(Class<?> clazz, String methodName, String message) {
        log(LogLevel.ERROR, clazz, methodName, message, null);
    }

    public static void error(Class<?> clazz, String methodName, String message, Throwable t) {
        log(LogLevel.ERROR, clazz, methodName, message, t);
    }

    public static void fatal(Class<?> clazz, String methodName, String message) {
        log(LogLevel.FATAL, clazz, methodName, message, null);
    }

    public static void fatal(Class<?> clazz, String methodName, String message, Throwable t) {
        log(LogLevel.FATAL, clazz, methodName, message, t);
    }

    // Метод для измерения времени выполнения
    public static long startTiming(Class<?> clazz, String methodName) {
        info(clazz, methodName, "▶️ Начало выполнения");
        return System.currentTimeMillis();
    }

    public static void endTiming(Class<?> clazz, String methodName, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        info(clazz, methodName, "⏹️ Завершение выполнения | Время: " + duration + " ms");
        if (duration > 1000) {
            warning(clazz, methodName, "⚠️ Медленное выполнение: " + duration + " ms");
        }
    }

    // Логирование SQL запросов
    public static void sql(Class<?> clazz, String methodName, String sql) {
        debug(clazz, methodName, "SQL: " + sql);
    }

    // Логирование пользовательских действий
    public static void userAction(String username, String action) {
        info(Logger.class, "userAction", "Пользователь [" + username + "] выполнил: " + action);
    }
}