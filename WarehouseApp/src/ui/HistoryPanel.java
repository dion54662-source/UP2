package ui;

import db.DatabaseHelper;
import models.Operation;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class HistoryPanel extends JPanel {
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextField filterField;
    private JLabel statsLabel;
    private JComboBox<String> typeFilter;

    public HistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхняя панель фильтрации
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Фильтрация и поиск"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(new JLabel("SKU:"));
        filterField = new JTextField(15);
        filterPanel.add(filterField);

        filterPanel.add(new JLabel("Тип операции:"));
        typeFilter = new JComboBox<>(new String[]{"Все", "MOVE", "INVENTORY"});
        filterPanel.add(typeFilter);

        JButton filterBtn = new JButton("🔍 Применить фильтр");
        JButton resetBtn = new JButton("📋 Сбросить");
        filterPanel.add(filterBtn);
        filterPanel.add(resetBtn);

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportBtn = new JButton("💾 Экспорт в буфер обмена");
        JButton printBtn = new JButton("🖨 Печать");
        exportPanel.add(exportBtn);
        exportPanel.add(printBtn);

        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(exportPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Таблица
        String[] columns = {"Дата и время", "SKU", "Тип операции", "Откуда", "Куда", "Пользователь"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        historyTable.setFont(new Font("Dialog", Font.PLAIN, 11));
        historyTable.setAutoCreateRowSorter(true);

        // Настройка ширины
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Журнал операций"));
        add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statsLabel = new JLabel(" ");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bottomPanel.add(statsLabel, BorderLayout.WEST);

        JLabel infoLabel = new JLabel("ℹ️ Двойной клик по строке для деталей");
        infoLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
        bottomPanel.add(infoLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Обработчики
        filterBtn.addActionListener(e -> loadHistory());
        resetBtn.addActionListener(e -> {
            filterField.setText("");
            typeFilter.setSelectedIndex(0);
            loadHistory();
        });
        exportBtn.addActionListener(e -> exportToClipboard());
        printBtn.addActionListener(e -> printHistory());

        // Двойной клик для деталей
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showOperationDetails();
                }
            }
        });

        loadHistory();
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        String skuFilter = filterField.getText().trim();
        String typeFilterValue = (String) typeFilter.getSelectedItem();

        List<Operation> operations = DatabaseHelper.getHistory(skuFilter);

        int count = 0;
        for (Operation op : operations) {
            // Фильтр по типу
            if (!"Все".equals(typeFilterValue) && !op.getOperationType().equals(typeFilterValue)) {
                continue;
            }

            count++;
            String operationDisplay;
            switch (op.getOperationType()) {
                case "MOVE":
                    operationDisplay = "🚚 ПЕРЕМЕЩЕНИЕ";
                    break;
                case "INVENTORY":
                    operationDisplay = "✅ ИНВЕНТАРИЗАЦИЯ";
                    break;
                default:
                    operationDisplay = op.getOperationType();
            }

            tableModel.addRow(new Object[]{
                    op.getTimestamp(),
                    op.getSku(),
                    operationDisplay,
                    op.getFromLoc() != null && !op.getFromLoc().isEmpty() ? op.getFromLoc() : "—",
                    op.getToLoc() != null && !op.getToLoc().isEmpty() ? op.getToLoc() : "—",
                    op.getUser()
            });
        }

        statsLabel.setText("Всего операций: " + count);
    }

    private void showOperationDetails() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) return;

        // Преобразование индекса с учетом сортировки
        int modelRow = historyTable.convertRowIndexToModel(selectedRow);

        String timestamp = (String) tableModel.getValueAt(modelRow, 0);
        String sku = (String) tableModel.getValueAt(modelRow, 1);
        String type = (String) tableModel.getValueAt(modelRow, 2);
        String fromLoc = (String) tableModel.getValueAt(modelRow, 3);
        String toLoc = (String) tableModel.getValueAt(modelRow, 4);
        String user = (String) tableModel.getValueAt(modelRow, 5);

        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════════════════════════\n");
        details.append("ДЕТАЛИ ОПЕРАЦИИ\n");
        details.append("═══════════════════════════════════════════════════\n\n");
        details.append("Дата:     ").append(timestamp).append("\n");
        details.append("SKU:      ").append(sku).append("\n");
        details.append("Тип:      ").append(type).append("\n");
        details.append("Откуда:   ").append(fromLoc).append("\n");
        details.append("Куда:     ").append(toLoc).append("\n");
        details.append("Кто:      ").append(user).append("\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Детали операции", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportToClipboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════════\n");
        sb.append("ИСТОРИЯ ОПЕРАЦИЙ СО СКЛАДОМ\n");
        sb.append("═══════════════════════════════════════════════════════════════════════\n\n");

        sb.append(String.format("%-20s | %-10s | %-15s | %-12s | %-12s | %-12s\n",
                "Дата и время", "SKU", "Тип операции", "Откуда", "Куда", "Пользователь"));
        sb.append("───────────────────────────────────────────────────────────────────────\n");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            sb.append(String.format("%-20s | %-10s | %-15s | %-12s | %-12s | %-12s\n",
                    tableModel.getValueAt(i, 0),
                    tableModel.getValueAt(i, 1),
                    tableModel.getValueAt(i, 2),
                    tableModel.getValueAt(i, 3),
                    tableModel.getValueAt(i, 4),
                    tableModel.getValueAt(i, 5)
            ));
        }

        sb.append("\n═══════════════════════════════════════════════════════════════════════\n");
        sb.append("Всего записей: ").append(tableModel.getRowCount());

        StringSelection selection = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

        JOptionPane.showMessageDialog(this,
                "История скопирована в буфер обмена\nВсего записей: " + tableModel.getRowCount(),
                "Экспорт выполнен", JOptionPane.INFORMATION_MESSAGE);
    }

    private void printHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append("История операций со складом\n");
        sb.append("Дата: ").append(new java.util.Date()).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        for (int i = 0; i < tableModel.getRowCount() && i < 100; i++) {
            sb.append(tableModel.getValueAt(i, 0)).append(" | ");
            sb.append(tableModel.getValueAt(i, 1)).append(" | ");
            sb.append(tableModel.getValueAt(i, 2)).append(" | ");
            sb.append(tableModel.getValueAt(i, 3)).append(" → ");
            sb.append(tableModel.getValueAt(i, 4)).append(" | ");
            sb.append(tableModel.getValueAt(i, 5)).append("\n");
        }

        JTextArea printArea = new JTextArea(sb.toString());
        printArea.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(printArea),
                "Предварительный просмотр", JOptionPane.INFORMATION_MESSAGE);
    }
}