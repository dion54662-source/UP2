package logging;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;

public class LogViewerDialog extends JDialog {
    private JTextPane logTextPane;
    private JComboBox<String> logTypeCombo;
    private JButton refreshBtn;
    private JButton clearBtn;
    private JButton exportBtn;
    private JCheckBox autoRefreshCheck;
    private Timer autoRefreshTimer;

    public LogViewerDialog(JFrame parent) {
        super(parent, "Просмотр логов", false);
        setSize(900, 600);
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout(10, 10));

        // Верхняя панель управления
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));

        controlPanel.add(new JLabel("Тип лога:"));
        logTypeCombo = new JComboBox<>(new String[]{"Основной лог", "Лог ошибок"});
        controlPanel.add(logTypeCombo);

        refreshBtn = new JButton("🔄 Обновить");
        controlPanel.add(refreshBtn);

        clearBtn = new JButton("🗑 Очистить логи");
        clearBtn.setBackground(new Color(220, 100, 100));
        controlPanel.add(clearBtn);

        exportBtn = new JButton("💾 Экспорт");
        controlPanel.add(exportBtn);

        autoRefreshCheck = new JCheckBox("Автообновление (5 сек)");
        controlPanel.add(autoRefreshCheck);

        add(controlPanel, BorderLayout.NORTH);

        // Панель с логами
        logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        logTextPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(logTextPane);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Содержимое лога"));
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с информацией
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("ℹ️ Двойной клик для копирования строки");
        infoLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
        bottomPanel.add(infoLabel, BorderLayout.WEST);

        JLabel pathLabel = new JLabel("Путь к логам: ./logs/");
        pathLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
        bottomPanel.add(pathLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Обработчики (без лямбд)
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLogs();
            }
        });

        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLogs();
            }
        });

        exportBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportLogs();
            }
        });

        autoRefreshCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleAutoRefresh();
            }
        });

        logTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLogs();
            }
        });

        // Двойной клик для копирования
        logTextPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = logTextPane.getSelectedText();
                    if (selected != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                                .setContents(new java.awt.datatransfer.StringSelection(selected), null);
                        JOptionPane.showMessageDialog(LogViewerDialog.this,
                                "Строка скопирована в буфер обмена", "Копирование",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // Загрузка начальных данных
        loadLogs();
    }

    private void loadLogs() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                final String content = logTypeCombo.getSelectedIndex() == 0 ?
                        FileLogHandler.getLogContent() : FileLogHandler.getErrorLogContent();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        logTextPane.setText(content);
                    }
                });
                return null;
            }
        };
        worker.execute();
    }

    private void clearLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите очистить все логи?\nЭто действие необратимо.",
                "Очистка логов",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            FileLogHandler.clearLogs();
            loadLogs();
            Logger.info(LogViewerDialog.class, "clearLogs", "Логи очищены пользователем");
            JOptionPane.showMessageDialog(this, "Логи успешно очищены");
        }
    }

    private void exportLogs() {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setSelectedFile(new File("log_export_" +
                java.time.LocalDate.now() + ".txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                final String content = logTypeCombo.getSelectedIndex() == 0 ?
                        FileLogHandler.getLogContent() : FileLogHandler.getErrorLogContent();
                Files.writeString(fileChooser.getSelectedFile().toPath(), content);
                JOptionPane.showMessageDialog(this, "Логи экспортированы успешно");
                Logger.info(LogViewerDialog.class, "exportLogs", "Логи экспортированы в " +
                        fileChooser.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка экспорта: " + ex.getMessage());
                Logger.error(LogViewerDialog.class, "exportLogs", "Ошибка экспорта", ex);
            }
        }
    }

    private void toggleAutoRefresh() {
        if (autoRefreshCheck.isSelected()) {
            autoRefreshTimer = new Timer(5000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadLogs();
                }
            });
            autoRefreshTimer.start();
        } else {
            if (autoRefreshTimer != null) {
                autoRefreshTimer.stop();
            }
        }
    }
}