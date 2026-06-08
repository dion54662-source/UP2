package ui;

import auth.LoginDialog;
import db.DatabaseHelper;
import models.Product;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    private JTextField filterField;

    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхняя панель
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("ИНВЕНТАРИЗАЦИЯ ТОВАРОВ НА СКЛАДЕ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Управление"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Фильтр по SKU:"));
        filterField = new JTextField(15);
        filterPanel.add(filterField);
        JButton filterBtn = new JButton("🔍 Применить фильтр");
        JButton resetBtn = new JButton("📋 Показать всё");
        filterPanel.add(filterBtn);
        filterPanel.add(resetBtn);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("🔄 Обновить список");
        JButton confirmBtn = new JButton("✅ Подтвердить наличие");
        JButton confirmAllBtn = new JButton("📋 Подтвердить все");
        buttonPanel.add(refreshBtn);
        buttonPanel.add(confirmBtn);
        buttonPanel.add(confirmAllBtn);

        controlPanel.add(filterPanel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        topPanel.add(controlPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Таблица
        String[] columns = {"№", "SKU", "Наименование", "Локация", "Штрихкод", "Статус"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(30);
        inventoryTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        inventoryTable.setFont(new Font("Dialog", Font.PLAIN, 12));
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Настройка ширины колонок
        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        // Рендер для подсветки
        inventoryTable.setDefaultRenderer(Object.class, new InventoryTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список товаров"));
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statsLabel = new JLabel(" ");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Обработчики
        refreshBtn.addActionListener(e -> loadInventory());
        filterBtn.addActionListener(e -> loadInventory());
        resetBtn.addActionListener(e -> {
            filterField.setText("");
            loadInventory();
        });
        confirmBtn.addActionListener(e -> confirmSelectedProduct());
        confirmAllBtn.addActionListener(e -> confirmAllProducts());

        // Загрузка данных
        loadInventory();
    }

    private void loadInventory() {
        tableModel.setRowCount(0);
        List<Product> products = DatabaseHelper.getAllProducts();
        String filter = filterField.getText().trim().toLowerCase();

        int count = 0;
        for (Product p : products) {
            if (filter.isEmpty() || p.getSku().toLowerCase().contains(filter)) {
                count++;
                tableModel.addRow(new Object[]{
                        count,
                        p.getSku(),
                        p.getName(),
                        p.getLocation(),
                        p.getBarcode(),
                        "✓ На месте"
                });
            }
        }

        statsLabel.setText("Всего товаров: " + count + " из " + products.size());
    }

    private void confirmSelectedProduct() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите товар для подтверждения");
            return;
        }

        String sku = (String) tableModel.getValueAt(selectedRow, 1);
        String name = (String) tableModel.getValueAt(selectedRow, 2);
        String location = (String) tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Подтвердить наличие товара?\n\n" +
                                "SKU: %s\n" +
                                "Наименование: %s\n" +
                                "Локация: %s\n\n" +
                                "Товар физически находится на указанной локации?",
                        sku, name, location),
                "Подтверждение инвентаризации",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.confirmInventory(sku,
                    LoginDialog.getCurrentUser().getUsername());
            if (success) {
                statsLabel.setText("✅ Подтвержден товар: " + sku + " - " + name);
                JOptionPane.showMessageDialog(this, "Товар подтвержден!\nОперация записана в историю.");
                loadInventory();
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка при подтверждении",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void confirmAllProducts() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Подтвердить наличие ВСЕХ товаров на складе?\n\n" +
                        "Это действие запишет в историю подтверждение для каждого товара.",
                "Массовое подтверждение",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            List<Product> products = DatabaseHelper.getAllProducts();
            int successCount = 0;

            for (Product p : products) {
                if (DatabaseHelper.confirmInventory(p.getSku(),
                        LoginDialog.getCurrentUser().getUsername())) {
                    successCount++;
                }
            }

            JOptionPane.showMessageDialog(this,
                    String.format("Подтверждено товаров: %d из %d", successCount, products.size()),
                    "Результат массового подтверждения",
                    JOptionPane.INFORMATION_MESSAGE);

            loadInventory();
        }
    }

    // Кастомный рендер для подсветки строк
    class InventoryTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(new Color(240, 248, 255));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }

            return c;
        }
    }
}