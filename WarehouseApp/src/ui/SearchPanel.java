package ui;

import models.Product;
import db.DatabaseHelper;
import utils.BarcodeScannerSimulator;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.TitledBorder;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JTextArea resultArea;
    private JButton searchBtn;
    private JButton scanBtn;
    private JButton clearBtn;

    public SearchPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхняя панель поиска
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Поиск товара",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("SKU / Наименование / Штрихкод:"), BorderLayout.WEST);
        searchField = new JTextField();
        inputPanel.add(searchField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBtn = new JButton("🔍 Найти");
        scanBtn = new JButton("📷 Сканировать штрихкод");
        clearBtn = new JButton("🗑 Очистить");

        buttonPanel.add(searchBtn);
        buttonPanel.add(scanBtn);
        buttonPanel.add(clearBtn);

        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Область результатов
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setBackground(new Color(250, 250, 250));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Результат поиска"));
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);

        // Информационная панель
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Справка"));
        JLabel infoLabel = new JLabel("ℹ️ Поддерживается поиск по SKU (например, SKU001), названию или штрихкоду");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.SOUTH);

        // Обработчики
        searchBtn.addActionListener(e -> performSearch());
        scanBtn.addActionListener(e -> performScan());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            resultArea.setText("");
        });
        searchField.addActionListener(e -> performSearch());

        // Пример для демонстрации
        showWelcomeMessage();
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите SKU, наименование или штрихкод для поиска",
                    "Пустой запрос", JOptionPane.WARNING_MESSAGE);
            resultArea.setText("⚠️ Введите поисковый запрос");
            return;
        }

        resultArea.setText("🔍 Поиск: " + query + "\n\n");

        // Имитация загрузки
        resultArea.append("⏳ Выполняется поиск...\n");

        SwingWorker<Product, Void> worker = new SwingWorker<>() {
            @Override
            protected Product doInBackground() {
                try {
                    Thread.sleep(500); // Имитация задержки
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return DatabaseHelper.searchProduct(query);
            }

            @Override
            protected void done() {
                try {
                    Product product = get();
                    if (product != null) {
                        displayProductInfo(product);
                    } else {
                        resultArea.append("❌ Товар не найден\n\n");
                        resultArea.append("Возможные причины:\n");
                        resultArea.append("• Неверно введен SKU\n");
                        resultArea.append("• Товар отсутствует в базе\n");
                        resultArea.append("• Ошибка в штрихкоде\n\n");
                        resultArea.append("Совет: Попробуйте поискать по другому параметру");
                    }
                } catch (Exception e) {
                    resultArea.setText("Ошибка при поиске: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void displayProductInfo(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("✅ ТОВАР НАЙДЕН\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        sb.append("📦 SKU:              ").append(product.getSku()).append("\n");
        sb.append("📝 Наименование:     ").append(product.getName()).append("\n");
        sb.append("📍 Текущая локация:  ").append(product.getLocation()).append("\n");
        sb.append("🔢 Штрихкод:         ").append(product.getBarcode()).append("\n\n");

        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("📐 СХЕМА РАСПОЛОЖЕНИЯ\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");

        String[] locationParts = product.getLocation().split("-");
        if (locationParts.length == 4) {
            sb.append("   Стеллаж: ").append(locationParts[0]).append("\n");
            sb.append("   Секция:  ").append(locationParts[1]).append("\n");
            sb.append("   Полка:   ").append(locationParts[2]).append("\n");
            sb.append("   Ячейка:  ").append(locationParts[3]).append("\n\n");

            // Визуализация
            sb.append("Визуализация:\n");
            sb.append("┌─────────────┐\n");
            sb.append("│ Стеллаж ").append(locationParts[0]).append("     │\n");
            sb.append("├─────────────┤\n");
            sb.append("│ Секция ").append(locationParts[1]).append("    │\n");
            sb.append("├─────────────┤\n");
            sb.append("│ Полка ").append(locationParts[2]).append("     │\n");
            sb.append("├─────────────┤\n");
            sb.append("│ Ячейка ").append(locationParts[3]).append("    │\n");
            sb.append("└─────────────┘\n");
            sb.append("      ↓↓↓\n");
            sb.append("   [ТОВАР ЗДЕСЬ]\n");
        }

        resultArea.setText(sb.toString());
    }

    private void performScan() {
        String barcode = BarcodeScannerSimulator.scanBarcodeWithDialog(this);
        if (barcode != null) {
            searchField.setText(barcode);
            performSearch();
        }
    }

    private void showWelcomeMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("ДОБРО ПОЖАЛОВАТЬ В СИСТЕМУ ПОИСКА ТОВАРОВ НА СКЛАДЕ\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        sb.append("📌 Как пользоваться:\n");
        sb.append("   1. Введите SKU товара (например, SKU001)\n");
        sb.append("   2. Или введите название товара (например, Ноутбук)\n");
        sb.append("   3. Или отсканируйте штрихкод\n\n");
        sb.append("📌 Примеры для поиска:\n");
        sb.append("   • SKU001 - Ноутбук Lenovo\n");
        sb.append("   • SKU002 - Мышь Logitech\n");
        sb.append("   • 4601234567891 - Штрихкод ноутбука\n\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("💡 Совет: Используйте сканер штрихкода для быстрого поиска\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");

        resultArea.setText(sb.toString());
    }
}