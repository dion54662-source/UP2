package logging;

public enum LogLevel {
    DEBUG(0, "🐛 DEBUG"),
    INFO(1, "ℹ️ INFO"),
    WARNING(2, "⚠️ WARNING"),
    ERROR(3, "❌ ERROR"),
    FATAL(4, "💀 FATAL");

    private final int level;
    private final String icon;

    LogLevel(int level, String icon) {
        this.level = level;
        this.icon = icon;
    }

    public int getLevel() { return level; }
    public String getIcon() { return icon; }

    public boolean isEnabled(LogLevel currentLevel) {
        return this.level >= currentLevel.level;
    }
}