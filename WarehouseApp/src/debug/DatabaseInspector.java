package debug;

import logging.Logger;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class DatabaseInspector extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> tableCombo;
    private JTextField sqlField;
    private JTextArea resultArea;

    public DatabaseInspector(JFrame parent) {
        super(parent, "Инспектор базы данных", false);
        setSize(800, 600);
        setLocationRelativeTo(parent);

        setLayout(new BorderLayout(10, 10));

        // Верхняя панель
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Выбор таблицы"));

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("Таблица:"));
        tableCombo = new JComboBox<>(new String[]{"products", "users", "operations", "warehouse_cells"});
        JButton loadBtn = new JButton("Загрузить");
        selectPanel.add(tableCombo);
        selectPanel.add(loadBtn);

        JButton refreshBtn = new JButton("🔄 Обновить");
        selectPanel.add(refreshBtn);

        topPanel.add(selectPanel, BorderLayout.NORTH);

        // SQL панель
        JPanel sqlPanel = new JPanel(new BorderLayout(5, 5));
        sqlPanel.setBorder(BorderFactory.createTitledBorder("Выполнить SQL"));
        sqlField = new JTextField("SELECT * FROM products LIMIT 10");
        JButton executeBtn = new JButton("Выполнить");
        sqlPanel.add(sqlField, BorderLayout.CENTER);
        sqlPanel.add(executeBtn, BorderLayout.EAST);
        topPanel.add(sqlPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Таблица
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setFont(new Font("Monospaced", Font.PLAIN, 11));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 11));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Данные"));
        add(tableScroll, BorderLayout.CENTER);

        // Результат SQL
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Результат SQL"));
        resultScroll.setPreferredSize(new Dimension(800, 150));
        add(resultScroll, BorderLayout.SOUTH);

        // Обработчики
        loadBtn.addActionListener(e -> loadTableData());
        refreshBtn.addActionListener(e -> loadTableData());
        executeBtn.addActionListener(e -> executeSQL());

        // Загрузка начальных данных
        loadTableData();
    }

    private void loadTableData() {
        String tableName = (String) tableCombo.getSelectedItem();
        String sql = "SELECT * FROM " + tableName + " LIMIT 100";
        executeSQLQuery(sql);
    }

    private void executeSQL() {
        String sql = sqlField.getText().trim();
        executeSQLQuery(sql);
    }

    private void executeSQLQuery(String sql) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try (Statement stmt = db.DatabaseHelper.getConnection().createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    // Получаем метаданные
                    ResultSetMetaData meta = rs.getMetaData();
                    final int columnCount = meta.getColumnCount();

                    // Создаем модель таблицы
                    final Vector<String> columns = new Vector<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(meta.getColumnName(i));
                    }

                    final Vector<Vector<Object>> data = new Vector<>();
                    final StringBuilder resultText = new StringBuilder();
                    resultText.append("Выполнен SQL: ").append(sql).append("\n");
                    resultText.append("Количество столбцов: ").append(columnCount).append("\n");
                    resultText.append("============================================================\n");

                    int rowCount = 0;
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.add(rs.getObject(i));
                        }
                        data.add(row);
                        rowCount++;
                    }

                    final int finalRowCount = rowCount;
                    resultText.append("Количество строк: ").append(finalRowCount).append("\n");

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            tableModel.setDataVector(data, columns);
                            resultArea.setText(resultText.toString());
                            Logger.info(DatabaseInspector.class, "executeSQLQuery",
                                    "Выполнен SQL: " + sql + " | Строк: " + finalRowCount);
                        }
                    });

                } catch (SQLException e) {
                    final String errorMessage = e.getMessage();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            resultArea.setText("Ошибка выполнения SQL:\n" + errorMessage);
                            Logger.error(DatabaseInspector.class, "executeSQLQuery", "Ошибка SQL", e);
                        }
                    });
                }
                return null;
            }
        };
        worker.execute();
    }
}