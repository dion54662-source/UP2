package ui;

import auth.LoginDialog;
import db.DatabaseHelper;
import models.Product;
import utils.BarcodeScannerSimulator;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MovePanel extends JPanel {
    private JTextField skuField;
    private JTextField locationField;
    private JTextArea infoArea;
    private JComboBox<String> filterCombo;
    private JList<String> cellsList;
    private DefaultListModel<String> cellsListModel;
    private Product currentProduct;
    private JLabel statusLabel;

    public MovePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Левая панель - информация о товаре
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setPreferredSize(new Dimension(400, 0));

        // Панель поиска SKU
        JPanel skuPanel = new JPanel(new BorderLayout(5, 5));
        skuPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLUE),
                "Выбор товара",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        JPanel skuInputPanel = new JPanel(new BorderLayout(5, 5));
        skuInputPanel.add(new JLabel("SKU товара:"), BorderLayout.WEST);
        skuField = new JTextField();
        skuInputPanel.add(skuField, BorderLayout.CENTER);

        JPanel skuButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton searchBtn = new JButton("🔍 Найти товар");
        JButton scanBtn = new JButton("📷 Сканер");
        skuButtons.add(searchBtn);
        skuButtons.add(scanBtn);

        skuPanel.add(skuInputPanel, BorderLayout.NORTH);
        skuPanel.add(skuButtons, BorderLayout.SOUTH);

        // Информация о товаре
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoArea.setBackground(new Color(240, 248, 255));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setBorder(BorderFactory.createTitledBorder("Информация о товаре"));
        infoScroll.setPreferredSize(new Dimension(380, 200));

        leftPanel.add(skuPanel, BorderLayout.NORTH);
        leftPanel.add(infoScroll, BorderLayout.CENTER);

        // Правая панель - выбор новой локации
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        JPanel locationPanel = new JPanel(new BorderLayout(10, 10));
        locationPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GREEN),
                "Новая локация",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        // Фильтр
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Фильтр ячеек:"));
        filterCombo = new JComboBox<>(new String[]{"Свободные", "Занятые", "Все ячейки"});
        filterCombo.setSelectedIndex(0); // По умолчанию показываем свободные
        filterPanel.add(filterCombo);

        JButton refreshCellsBtn = new JButton("🔄 Обновить список");
        filterPanel.add(refreshCellsBtn);

        // Список ячеек
        cellsListModel = new DefaultListModel<>();
        cellsList = new JList<>(cellsListModel);
        cellsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cellsList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane cellsScroll = new JScrollPane(cellsList);
        cellsScroll.setBorder(BorderFactory.createTitledBorder("Доступные ячейки"));
        cellsScroll.setPreferredSize(new Dimension(300, 300));

        // Ручной ввод
        JPanel manualPanel = new JPanel(new BorderLayout(5, 5));
        manualPanel.setBorder(BorderFactory.createTitledBorder("Ручной ввод локации"));
        manualPanel.add(new JLabel("Формат: СТЕЛЛАЖ-СЕКЦИЯ-ПОЛКА-ЯЧЕЙКА"), BorderLayout.NORTH);
        locationField = new JTextField();
        locationField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        manualPanel.add(locationField, BorderLayout.CENTER);

        locationPanel.add(filterPanel, BorderLayout.NORTH);
        locationPanel.add(cellsScroll, BorderLayout.CENTER);
        locationPanel.add(manualPanel, BorderLayout.SOUTH);

        rightPanel.add(locationPanel, BorderLayout.CENTER);

        // Кнопки действий
        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton moveBtn = new JButton("🚚 ПЕРЕМЕСТИТЬ ТОВАР");
        moveBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        moveBtn.setBackground(new Color(70, 130, 200));
        moveBtn.setForeground(Color.WHITE);
        moveBtn.setPreferredSize(new Dimension(200, 40));

        JButton editBtn = new JButton("✏️ РЕДАКТИРОВАТЬ ЛОКАЦИЮ");
        editBtn.setFont(new Font("Dialog", Font.BOLD, 14));
        editBtn.setBackground(new Color(100, 150, 100));
        editBtn.setForeground(Color.WHITE);
        editBtn.setPreferredSize(new Dimension(200, 40));

        actionPanel.add(moveBtn);
        actionPanel.add(editBtn);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(Color.BLUE);

        buttonPanel.add(actionPanel, BorderLayout.CENTER);
        buttonPanel.add(statusLabel, BorderLayout.SOUTH);

        // Сборка
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.4);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Обработчики
        searchBtn.addActionListener(e -> searchProduct());
        scanBtn.addActionListener(e -> scanAndSearch());
        refreshCellsBtn.addActionListener(e -> loadCells());
        moveBtn.addActionListener(e -> moveProduct(false));
        editBtn.addActionListener(e -> moveProduct(true));

        filterCombo.addActionListener(e -> loadCells());
        cellsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = cellsList.getSelectedValue();
                if (selected != null) {
                    locationField.setText(selected);
                    checkCellStatus(selected);
                }
            }
        });

        // Загрузка начальных данных
        loadCells();
        showInstructions();
    }

    private void searchProduct() {
        String sku = skuField.getText().trim();
        if (sku.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите SKU товара");
            return;
        }

        currentProduct = DatabaseHelper.searchProduct(sku);
        if (currentProduct != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════\n");
            sb.append("✅ НАЙДЕН ТОВАР\n");
            sb.append("═══════════════════════════════════\n\n");
            sb.append("SKU:        ").append(currentProduct.getSku()).append("\n");
            sb.append("Название:   ").append(currentProduct.getName()).append("\n");
            sb.append("Локация:    ").append(currentProduct.getLocation()).append("\n");
            sb.append("Штрихкод:   ").append(currentProduct.getBarcode()).append("\n\n");
            sb.append("Статус ячейки: ").append(DatabaseHelper.getCellStatus(currentProduct.getLocation())).append("\n");
            infoArea.setText(sb.toString());
            statusLabel.setText("Товар найден. Выберите новую локацию.");

            // Подсветка текущей ячейки в списке
            highlightCell(currentProduct.getLocation());
        } else {
            infoArea.setText("❌ Товар с SKU '" + sku + "' не найден");
            currentProduct = null;
            statusLabel.setText("Товар не найден!");
        }
    }

    private void scanAndSearch() {
        String barcode = BarcodeScannerSimulator.scanBarcodeWithDialog(this);
        if (barcode != null) {
            Product product = DatabaseHelper.searchProduct(barcode);
            if (product != null) {
                skuField.setText(product.getSku());
                searchProduct();
            } else {
                JOptionPane.showMessageDialog(this, "Товар со штрихкодом " + barcode + " не найден");
            }
        }
    }

    private void loadCells() {
        cellsListModel.clear();
        String filter = (String) filterCombo.getSelectedItem();
        java.util.List<String> cells;

        if ("Свободные".equals(filter)) {
            cells = DatabaseHelper.getFreeCells();
        } else if ("Занятые".equals(filter)) {
            cells = DatabaseHelper.getOccupiedCells();
        } else {
            cells = DatabaseHelper.getAllCells();
        }

        for (String cell : cells) {
            cellsListModel.addElement(cell);
        }

        if (cells.isEmpty()) {
            cellsListModel.addElement("--- Нет доступных ячеек ---");
        }

        statusLabel.setText("Загружено ячеек: " + cells.size());
    }

    private void highlightCell(String location) {
        for (int i = 0; i < cellsListModel.size(); i++) {
            String cell = cellsListModel.get(i);
            if (cell.equals(location)) {
                cellsList.setSelectedIndex(i);
                cellsList.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    private void checkCellStatus(String location) {
        String status = DatabaseHelper.getCellStatus(location);
        if ("OCCUPIED".equals(status) && currentProduct != null && !location.equals(currentProduct.getLocation())) {
            statusLabel.setText("⚠️ ВНИМАНИЕ: Ячейка " + location + " ЗАНЯТА другим товаром!");
            statusLabel.setForeground(Color.RED);
        } else if ("FREE".equals(status)) {
            statusLabel.setText("✅ Ячейка " + location + " СВОБОДНА");
            statusLabel.setForeground(Color.GREEN);
        } else if (currentProduct != null && location.equals(currentProduct.getLocation())) {
            statusLabel.setText("ℹ️ Это текущая локация товара");
            statusLabel.setForeground(Color.ORANGE);
        } else {
            statusLabel.setText("Статус ячейки: " + status);
            statusLabel.setForeground(Color.BLUE);
        }
    }

    private void moveProduct(boolean isEdit) {
        if (currentProduct == null) {
            JOptionPane.showMessageDialog(this, "Сначала найдите товар по SKU");
            return;
        }

        String newLocation = locationField.getText().trim();
        if (newLocation.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Выберите или введите новую локацию");
            return;
        }

        // Валидация формата
        if (!DatabaseHelper.isValidLocation(newLocation)) {
            JOptionPane.showMessageDialog(this,
                    "❌ Неверный формат локации!\n\n" +
                            "Формат должен быть: БУКВА-ДВЕЦИФРЫ-ДВЕЦИФРЫ-ДВЕЦИФРЫ\n" +
                            "Примеры: A-01-02-03, B-02-01-01, C-03-02-01\n\n" +
                            "Допустимые значения:\n" +
                            "Стеллаж: A, B, C\n" +
                            "Секция: 01, 02, 03\n" +
                            "Полка: 01, 02, 03\n" +
                            "Ячейка: 01, 02",
                    "Ошибка формата",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Проверка, что новая локация не занята (если перемещаем не в ту же)
        String cellStatus = DatabaseHelper.getCellStatus(newLocation);
        if (!newLocation.equals(currentProduct.getLocation()) && "OCCUPIED".equals(cellStatus)) {
            JOptionPane.showMessageDialog(this,
                    "❌ Невозможно переместить товар!\n\n" +
                            "Ячейка " + newLocation + " уже ЗАНЯТА другим товаром.\n" +
                            "Пожалуйста, выберите свободную ячейку.",
                    "Ячейка занята",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String operationName = isEdit ? "редактирование локации" : "перемещение";

        // Подтверждение операции
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Подтвердите %s:\n\n" +
                                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                "📦 Товар:     %s\n" +
                                "📝 Название:  %s\n" +
                                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                "📍 Текущая локация:  %s\n" +
                                "🎯 Новая локация:     %s\n" +
                                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                                "Продолжить?",
                        operationName,
                        currentProduct.getSku(),
                        currentProduct.getName(),
                        currentProduct.getLocation(),
                        newLocation),
                "Подтверждение операции",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            statusLabel.setText("Операция отменена");
            return;
        }

        // Выполнение операции
        boolean success;
        if (isEdit) {
            success = DatabaseHelper.editLocation(currentProduct.getSku(), newLocation,
                    LoginDialog.getCurrentUser().getUsername());
        } else {
            success = DatabaseHelper.moveProduct(currentProduct.getSku(), newLocation,
                    LoginDialog.getCurrentUser().getUsername());
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    String.format("✅ %s успешно выполнено!\n\n" +
                                    "Товар: %s - %s\n" +
                                    "Новая локация: %s",
                            operationName.toUpperCase(),
                            currentProduct.getSku(),
                            currentProduct.getName(),
                            newLocation),
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);

            locationField.setText("");
            loadCells();
            searchProduct(); // Обновить информацию о товаре
            statusLabel.setText("✅ Операция выполнена успешно!");
        } else {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка при выполнении операции!\n\n" +
                            "Возможные причины:\n" +
                            "• Проблема с подключением к базе данных\n" +
                            "• Указанная ячейка не существует\n" +
                            "• Ошибка в формате данных\n\n" +
                            "Проверьте логи для деталей.",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("❌ Ошибка при выполнении операции!");
        }
    }

    private void showInstructions() {
        infoArea.setText("═══════════════════════════════════\n" +
                "ИНСТРУКЦИЯ ПО ПЕРЕМЕЩЕНИЮ\n" +
                "═══════════════════════════════════\n\n" +
                "1️⃣ Введите SKU товара и нажмите 'Найти'\n" +
                "2️⃣ Выберите новую локацию из списка\n" +
                "   или введите вручную\n" +
                "3️⃣ Нажмите 'Переместить' или 'Редактировать'\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "📐 ФОРМАТ ЛОКАЦИИ:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Стеллаж: A, B, C\n" +
                "Секция:  01, 02, 03\n" +
                "Полка:   01, 02, 03\n" +
                "Ячейка:  01, 02\n\n" +
                "Пример: A-01-02-03\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💡 СОВЕТ:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "• Используйте фильтр 'Свободные' для выбора\n" +
                "  доступных ячеек\n" +
                "• Нельзя переместить товар в занятую ячейку\n" +
                "• История всех перемещений сохраняется");
    }
}