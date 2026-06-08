package debug;

import logging.Logger;
import logging.LogViewerDialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DebugPanel extends JPanel {
    private JTextArea debugArea;
    private JTextField commandField;
    private JButton executeBtn;
    private JButton viewLogsBtn;
    private JButton testDbBtn;
    private JButton perfTestBtn;
    private JButton genDataBtn;
    private JComboBox<String> logLevelCombo;

    public DebugPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("🐞 Панель отладки и диагностики"));

        // Верхняя панель управления
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Уровень логирования
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Уровень логирования:"), gbc);
        gbc.gridx = 1;
        logLevelCombo = new JComboBox<>(new String[]{"DEBUG", "INFO", "WARNING", "ERROR", "FATAL"});
        controlPanel.add(logLevelCombo, gbc);

        // Кнопки
        gbc.gridx = 2;
        viewLogsBtn = new JButton("📋 Просмотр логов");
        controlPanel.add(viewLogsBtn, gbc);

        gbc.gridx = 3;
        testDbBtn = new JButton("🗄 Тест БД");
        controlPanel.add(testDbBtn, gbc);

        gbc.gridx = 4;
        perfTestBtn = new JButton("⚡ Тест производительности");
        controlPanel.add(perfTestBtn, gbc);

        gbc.gridx = 5;
        genDataBtn = new JButton("📊 Генерация данных");
        controlPanel.add(genDataBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 6;
        controlPanel.add(new JLabel("Выполнить команду:"), gbc);

        gbc.gridy = 2;
        JPanel cmdPanel = new JPanel(new BorderLayout(5, 5));
        commandField = new JTextField();
        executeBtn = new JButton("Выполнить");
        cmdPanel.add(commandField, BorderLayout.CENTER);
        cmdPanel.add(executeBtn, BorderLayout.EAST);
        controlPanel.add(cmdPanel, gbc);

        add(controlPanel, BorderLayout.NORTH);

        // Область вывода отладки
        debugArea = new JTextArea();
        debugArea.setEditable(false);
        debugArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        debugArea.setBackground(new Color(30, 30, 30));
        debugArea.setForeground(new Color(0, 255, 0));
        JScrollPane scrollPane = new JScrollPane(debugArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Вывод отладки"));
        add(scrollPane, BorderLayout.CENTER);

        // Обработчики
        viewLogsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLogViewer();
            }
        });

        testDbBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testDatabase();
            }
        });

        perfTestBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performanceTest();
            }
        });

        genDataBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateTestData();
            }
        });

        executeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeCommand();
            }
        });

        logLevelCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeLogLevel();
            }
        });

        // Вывод приветствия
        appendDebug("🐞 Панель отладки загружена", Color.GREEN);
        appendDebug("Доступные команды: help, test_db, test_perf, clear, gc, stats", Color.CYAN);
    }

    private void appendDebug(final String message, final Color color) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                debugArea.append(message + "\n");
                debugArea.setCaretPosition(debugArea.getDocument().getLength());
            }
        });
        Logger.debug(DebugPanel.class, "appendDebug", message);
    }

    private void showLogViewer() {
        LogViewerDialog viewer = new LogViewerDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        viewer.setVisible(true);
    }

    private void testDatabase() {
        appendDebug("🔍 Тестирование базы данных...", Color.YELLOW);
        final long start = System.currentTimeMillis();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Тест подключения
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            appendDebug("  → Проверка подключения к БД...", Color.WHITE);
                        }
                    });
                    db.DatabaseHelper.getConnection();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            appendDebug("  ✅ Подключение успешно", Color.GREEN);
                        }
                    });

                    // Тест поиска
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            appendDebug("  → Тест поиска товара...", Color.WHITE);
                        }
                    });
                    final models.Product product = db.DatabaseHelper.searchProduct("SKU001");
                    if (product != null) {
                        final String productName = product.getName();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                appendDebug("  ✅ Найден товар: " + productName, Color.GREEN);
                            }
                        });
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                appendDebug("  ⚠️ Товар не найден", Color.YELLOW);
                            }
                        });
                    }

                    // Тест количества товаров
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            appendDebug("  → Получение списка товаров...", Color.WHITE);
                        }
                    });
                    final java.util.List<models.Product> products = db.DatabaseHelper.getAllProducts();
                    final int productCount = products.size();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            appendDebug("  ✅ Загружено товаров: " + productCount, Color.GREEN);
                        }
                    });

                    final long duration = System.currentTimeMillis() - start;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            appendDebug("✅ Тест БД завершен за " + duration + " ms", Color.GREEN);
                        }
                    });

                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            appendDebug("❌ Ошибка тестирования БД: " + e.getMessage(), Color.RED);
                        }
                    });
                    Logger.error(DebugPanel.class, "testDatabase", "Ошибка тестирования БД", e);
                }
                return null;
            }
        };
        worker.execute();
    }

    private void performanceTest() {
        appendDebug("⚡ Запуск теста производительности...", Color.YELLOW);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                // Тест поиска (100 запросов)
                final long startSearch = System.currentTimeMillis();
                for (int i = 0; i < 100; i++) {
                    db.DatabaseHelper.searchProduct("SKU00" + (i % 5 + 1));
                }
                final long searchTime = System.currentTimeMillis() - startSearch;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        appendDebug("  → 100 поисковых запросов: " + searchTime + " ms", Color.WHITE);
                    }
                });

                // Тест получения всех товаров (50 раз)
                final long startGetAll = System.currentTimeMillis();
                for (int i = 0; i < 50; i++) {
                    db.DatabaseHelper.getAllProducts();
                }
                final long getAllTime = System.currentTimeMillis() - startGetAll;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        appendDebug("  → 50 получений списка товаров: " + getAllTime + " ms", Color.WHITE);
                    }
                });

                // Тест истории (50 раз)
                final long startHistory = System.currentTimeMillis();
                for (int i = 0; i < 50; i++) {
                    db.DatabaseHelper.getHistory("");
                }
                final long historyTime = System.currentTimeMillis() - startHistory;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        appendDebug("  → 50 получений истории: " + historyTime + " ms", Color.WHITE);
                        appendDebug("✅ Тест производительности завершен", Color.GREEN);
                    }
                });

                return null;
            }
        };
        worker.execute();
    }

    private void generateTestData() {
        final String input = JOptionPane.showInputDialog(this,
                "Введите количество тестовых товаров для генерации (1-100):", "10");

        if (input != null) {
            try {
                final int count = Integer.parseInt(input);
                if (count < 1 || count > 100) {
                    throw new NumberFormatException();
                }

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        TestDataGenerator.generateProducts(count);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                appendDebug("✅ Сгенерировано " + count + " тестовых товаров", Color.GREEN);
                            }
                        });
                        return null;
                    }
                };
                worker.execute();

            } catch (NumberFormatException e) {
                appendDebug("❌ Некорректное число", Color.RED);
            }
        }
    }

    private void executeCommand() {
        final String command = commandField.getText().trim().toLowerCase();
        commandField.setText("");

        appendDebug("> " + command, Color.CYAN);

        if (command.equals("help")) {
            showHelp();
        } else if (command.equals("test_db")) {
            testDatabase();
        } else if (command.equals("test_perf")) {
            performanceTest();
        } else if (command.equals("clear")) {
            debugArea.setText("");
            appendDebug("Экран очищен", Color.GREEN);
        } else if (command.equals("gc")) {
            System.gc();
            appendDebug("Запущен сборщик мусора", Color.GREEN);
        } else if (command.equals("stats")) {
            showStats();
        } else {
            appendDebug("Неизвестная команда. Введите 'help' для списка команд", Color.RED);
        }
    }

    private void showHelp() {
        appendDebug("Доступные команды:", Color.YELLOW);
        appendDebug("  help     - показать эту справку", Color.WHITE);
        appendDebug("  test_db  - тестирование базы данных", Color.WHITE);
        appendDebug("  test_perf- тест производительности", Color.WHITE);
        appendDebug("  clear    - очистить экран отладки", Color.WHITE);
        appendDebug("  gc       - запустить сборщик мусора", Color.WHITE);
        appendDebug("  stats    - показать статистику системы", Color.WHITE);
    }

    private void showStats() {
        appendDebug("📊 Статистика системы:", Color.YELLOW);

        final long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        final long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        final long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        final int processors = Runtime.getRuntime().availableProcessors();

        appendDebug("  Память: " + totalMemory + " MB / " + maxMemory + " MB", Color.WHITE);
        appendDebug("  Свободно: " + freeMemory + " MB", Color.WHITE);
        appendDebug("  Процессоров: " + processors, Color.WHITE);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                final java.util.List<models.Product> products = db.DatabaseHelper.getAllProducts();
                final java.util.List<String> freeCells = db.DatabaseHelper.getFreeCells();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        appendDebug("  Товаров в БД: " + products.size(), Color.WHITE);
                        appendDebug("  Свободных ячеек: " + freeCells.size(), Color.WHITE);
                    }
                });
                return null;
            }
        };
        worker.execute();
    }

    private void changeLogLevel() {
        final String level = (String) logLevelCombo.getSelectedItem();
        logging.LogLevel logLevel = logging.LogLevel.valueOf(level);
        logging.Logger.setLogLevel(logLevel);
        appendDebug("Уровень логирования изменен на: " + level, Color.GREEN);
    }
}