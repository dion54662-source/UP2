package logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogHandler {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";

    private static boolean colorEnabled = true;

    public static void setColorEnabled(boolean enabled) {
        colorEnabled = enabled;
    }

    public static void write(String message, LogLevel level, String className, String methodName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

        String color = getColorForLevel(level);
        String levelStr = String.format("%-7s", level.name());

        String formattedMessage = String.format("[%s] %s %s [%s.%s] %s",
                timestamp,
                colorEnabled ? color + levelStr + RESET : levelStr,
                level.getIcon(),
                className,
                methodName,
                message
        );

        System.out.println(formattedMessage);
    }

    private static String getColorForLevel(LogLevel level) {
        if (!colorEnabled) return "";
        switch (level) {
            case DEBUG: return CYAN;
            case INFO: return GREEN;
            case WARNING: return YELLOW;
            case ERROR: return RED;
            case FATAL: return PURPLE;
            default: return RESET;
        }
    }
}