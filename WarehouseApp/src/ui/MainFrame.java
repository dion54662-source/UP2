package ui;

import auth.LoginDialog;
import debug.DebugPanel;
import logging.Logger;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    public MainFrame() {
        setTitle("Складская система - Определение локации товаров");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // Создание вкладок
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("🔍 Поиск товара", new SearchPanel());
        tabbedPane.addTab("📍 Перемещение товара", new MovePanel());
        tabbedPane.addTab("📋 Инвентаризация", new InventoryPanel());
        tabbedPane.addTab("📜 История операций", new HistoryPanel());

        // Добавляем панель отладки (только для администратора)
        if (LoginDialog.getCurrentUser() != null && "ADMIN".equals(LoginDialog.getCurrentUser().getRole())) {
            tabbedPane.addTab("🐞 Отладка", new DebugPanel());
            Logger.info(MainFrame.class, "MainFrame", "Панель отладки добавлена (режим администратора)");
        }

        // Разграничение прав
        if (LoginDialog.getCurrentUser() != null) {
            String role = LoginDialog.getCurrentUser().getRole();

            if ("PICKER".equals(role)) {
                tabbedPane.setEnabledAt(1, false);
                tabbedPane.setToolTipTextAt(1, "⛔ Доступ запрещен: перемещение товаров доступно только кладовщикам");
                Logger.info(MainFrame.class, "MainFrame", "Режим комплектовщика: перемещение отключено");
            } else if ("STOREKEEPER".equals(role) || "ADMIN".equals(role)) {
                tabbedPane.setEnabledAt(1, true);
                Logger.info(MainFrame.class, "MainFrame", "Режим " + role + ": все функции доступны");
            }
        }

        add(tabbedPane, BorderLayout.CENTER);

        // Статусная строка
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());

        String userInfo = "👤 Пользователь: " + LoginDialog.getCurrentUser().getUsername() +
                " | Роль: " + getRoleDisplayName(LoginDialog.getCurrentUser().getRole());

        if ("PICKER".equals(LoginDialog.getCurrentUser().getRole())) {
            userInfo += " | 📋 Доступно: поиск, инвентаризация, история";
        } else {
            userInfo += " | 🔧 Доступно: все функции (включая перемещение)";
        }

        statusLabel = new JLabel(userInfo);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(new java.util.Date().toString());
        timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.add(timeLabel, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);

        // Таймер для обновления времени
        new Timer(1000, e -> {
            ((JLabel)((JPanel)getContentPane().getComponent(1)).getComponent(1))
                    .setText(new java.util.Date().toString());
        }).start();

        // Логируем запуск приложения
        Logger.info(MainFrame.class, "MainFrame", "Приложение запущено пользователем: " +
                LoginDialog.getCurrentUser().getUsername());
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "PICKER": return "Комплектовщик";
            case "STOREKEEPER": return "Кладовщик";
            case "ADMIN": return "Администратор";
            default: return role;
        }
    }
}