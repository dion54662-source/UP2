package db;

import models.Product;
import models.User;
import models.Operation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:warehouse.db";
    private static Connection connection = null;

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                initializeDatabase();
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
            return null;
        }
    }

    private static void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Таблица продуктов
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sku TEXT UNIQUE NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "location TEXT NOT NULL, " +
                    "barcode TEXT UNIQUE)");

            // Таблица пользователей
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL)");

            // Таблица операций
            stmt.execute("CREATE TABLE IF NOT EXISTS operations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sku TEXT NOT NULL, " +
                    "from_loc TEXT, " +
                    "to_loc TEXT, " +
                    "user TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "operation_type TEXT NOT NULL)");

            // Таблица ячеек склада
            stmt.execute("CREATE TABLE IF NOT EXISTS warehouse_cells (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "cell_location TEXT UNIQUE NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "current_sku TEXT)");

            // Вставка тестовых данных
            insertTestData(stmt);
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }

    private static void insertTestData(Statement stmt) throws SQLException {
        // Пользователи
        stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES " +
                "('picker', '123', 'PICKER')," +
                "('storekeeper', '123', 'STOREKEEPER')," +
                "('admin', '123', 'ADMIN')");

        // Товары
        stmt.execute("INSERT OR IGNORE INTO products (sku, name, location, barcode) VALUES " +
                "('SKU001', 'Ноутбук Lenovo IdeaPad', 'A-01-02-03', '4601234567891')," +
                "('SKU002', 'Мышь Logitech MX Master', 'B-02-01-01', '4601234567892')," +
                "('SKU003', 'Клавиатура Mechanic', 'A-01-01-02', '4601234567893')," +
                "('SKU004', 'Монитор Samsung 24\"', 'C-03-02-01', '4601234567894')," +
                "('SKU005', 'SSD Kingston 1TB', 'A-02-01-03', '4601234567895')");

        // Создание ячеек склада
        String[] racks = {"A", "B", "C"};
        String[] sections = {"01", "02", "03"};
        String[] shelves = {"01", "02", "03"};
        String[] cells = {"01", "02"};

        for (String rack : racks) {
            for (String section : sections) {
                for (String shelf : shelves) {
                    for (String cell : cells) {
                        String location = rack + "-" + section + "-" + shelf + "-" + cell;
                        String status = "FREE";
                        String currentSku = null;

                        // Заполняем занятые ячейки
                        if (location.equals("A-01-02-03")) {
                            status = "OCCUPIED";
                            currentSku = "SKU001";
                        } else if (location.equals("B-02-01-01")) {
                            status = "OCCUPIED";
                            currentSku = "SKU002";
                        } else if (location.equals("A-01-01-02")) {
                            status = "OCCUPIED";
                            currentSku = "SKU003";
                        } else if (location.equals("C-03-02-01")) {
                            status = "OCCUPIED";
                            currentSku = "SKU004";
                        } else if (location.equals("A-02-01-03")) {
                            status = "OCCUPIED";
                            currentSku = "SKU005";
                        }

                        stmt.execute("INSERT OR IGNORE INTO warehouse_cells (cell_location, status, current_sku) VALUES " +
                                "('" + location + "', '" + status + "', " + (currentSku == null ? "NULL" : "'" + currentSku + "'") + ")");
                    }
                }
            }
        }
    }

    // Аутентификация
    public static User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Поиск товара
    public static Product searchProduct(String searchText) {
        String sql = "SELECT * FROM products WHERE sku LIKE ? OR name LIKE ? OR barcode LIKE ? LIMIT 1";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            String param = "%" + searchText + "%";
            pstmt.setString(1, param);
            pstmt.setString(2, param);
            pstmt.setString(3, param);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setSku(rs.getString("sku"));
                product.setName(rs.getString("name"));
                product.setLocation(rs.getString("location"));
                product.setBarcode(rs.getString("barcode"));
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Получить все товары
    public static List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY location";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setSku(rs.getString("sku"));
                product.setName(rs.getString("name"));
                product.setLocation(rs.getString("location"));
                product.setBarcode(rs.getString("barcode"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * ПЕРЕМЕЩЕНИЕ ТОВАРА - ПОЛНОСТЬЮ РАБОЧАЯ ВЕРСИЯ
     */
    public static boolean moveProduct(String sku, String newLocation, String username) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Получаем старую локацию товара
            String oldLocation = null;
            String productName = null;
            PreparedStatement getProduct = conn.prepareStatement("SELECT location, name FROM products WHERE sku = ?");
            getProduct.setString(1, sku);
            ResultSet rs = getProduct.executeQuery();
            if (rs.next()) {
                oldLocation = rs.getString("location");
                productName = rs.getString("name");
            } else {
                System.err.println("Товар не найден: " + sku);
                return false;
            }

            // 2. Проверяем, что новая локация существует в warehouse_cells
            PreparedStatement checkCell = conn.prepareStatement("SELECT status FROM warehouse_cells WHERE cell_location = ?");
            checkCell.setString(1, newLocation);
            ResultSet cellRs = checkCell.executeQuery();
            if (!cellRs.next()) {
                System.err.println("Ячейка не существует: " + newLocation);
                return false;
            }

            // 3. Проверяем, что новая локация свободна (если это не редактирование той же локации)
            if (!oldLocation.equals(newLocation)) {
                String cellStatus = cellRs.getString("status");
                if ("OCCUPIED".equals(cellStatus)) {
                    System.err.println("Ячейка занята: " + newLocation);
                    return false;
                }
            }

            // 4. Обновляем локацию товара в таблице products
            PreparedStatement updateProduct = conn.prepareStatement("UPDATE products SET location = ? WHERE sku = ?");
            updateProduct.setString(1, newLocation);
            updateProduct.setString(2, sku);
            int productUpdated = updateProduct.executeUpdate();

            if (productUpdated == 0) {
                conn.rollback();
                return false;
            }

            // 5. Освобождаем старую ячейку (если она существует и отличается от новой)
            if (oldLocation != null && !oldLocation.equals(newLocation)) {
                PreparedStatement freeOldCell = conn.prepareStatement(
                        "UPDATE warehouse_cells SET status = 'FREE', current_sku = NULL WHERE cell_location = ?");
                freeOldCell.setString(1, oldLocation);
                freeOldCell.executeUpdate();
            }

            // 6. Занимаем новую ячейку
            PreparedStatement occupyNewCell = conn.prepareStatement(
                    "UPDATE warehouse_cells SET status = 'OCCUPIED', current_sku = ? WHERE cell_location = ?");
            occupyNewCell.setString(1, sku);
            occupyNewCell.setString(2, newLocation);
            occupyNewCell.executeUpdate();

            // 7. Логируем операцию в историю
            PreparedStatement logOperation = conn.prepareStatement(
                    "INSERT INTO operations (sku, from_loc, to_loc, user, timestamp, operation_type) " +
                            "VALUES (?, ?, ?, ?, datetime('now', 'localtime'), 'MOVE')");
            logOperation.setString(1, sku);
            logOperation.setString(2, oldLocation);
            logOperation.setString(3, newLocation);
            logOperation.setString(4, username);
            logOperation.executeUpdate();

            // 8. Подтверждаем транзакцию
            conn.commit();

            System.out.println("✅ Товар " + sku + " перемещен из " + oldLocation + " в " + newLocation);
            return true;

        } catch (SQLException e) {
            System.err.println("Ошибка при перемещении товара: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Редактирование локации
    public static boolean editLocation(String sku, String newLocation, String username) {
        return moveProduct(sku, newLocation, username);
    }

    // Подтверждение инвентаризации
    public static boolean confirmInventory(String sku, String username) {
        try {
            PreparedStatement log = getConnection().prepareStatement(
                    "INSERT INTO operations (sku, from_loc, to_loc, user, timestamp, operation_type) " +
                            "VALUES (?, ?, ?, ?, datetime('now', 'localtime'), 'INVENTORY')");
            log.setString(1, sku);
            log.setString(2, null);
            log.setString(3, null);
            log.setString(4, username);
            log.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить историю операций
    public static List<Operation> getHistory(String skuFilter) {
        List<Operation> operations = new ArrayList<>();
        String sql = "SELECT * FROM operations WHERE sku LIKE ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + (skuFilter == null || skuFilter.isEmpty() ? "" : skuFilter) + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Operation op = new Operation();
                op.setId(rs.getInt("id"));
                op.setSku(rs.getString("sku"));
                op.setFromLoc(rs.getString("from_loc"));
                op.setToLoc(rs.getString("to_loc"));
                op.setUser(rs.getString("user"));
                op.setTimestamp(rs.getString("timestamp"));
                op.setOperationType(rs.getString("operation_type"));
                operations.add(op);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operations;
    }

    // Получить свободные ячейки
    public static List<String> getFreeCells() {
        List<String> freeCells = new ArrayList<>();
        String sql = "SELECT cell_location FROM warehouse_cells WHERE status = 'FREE' ORDER BY cell_location";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                freeCells.add(rs.getString("cell_location"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return freeCells;
    }

    // Получить занятые ячейки
    public static List<String> getOccupiedCells() {
        List<String> occupiedCells = new ArrayList<>();
        String sql = "SELECT cell_location FROM warehouse_cells WHERE status = 'OCCUPIED' ORDER BY cell_location";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                occupiedCells.add(rs.getString("cell_location"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return occupiedCells;
    }

    // Получить все ячейки
    public static List<String> getAllCells() {
        List<String> allCells = new ArrayList<>();
        String sql = "SELECT cell_location FROM warehouse_cells ORDER BY cell_location";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allCells.add(rs.getString("cell_location"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allCells;
    }

    // Валидация формата локации
    public static boolean isValidLocation(String location) {
        return location != null && location.matches("^[A-C]-0[1-3]-0[1-3]-0[1-2]$");
    }

    // Проверка существования SKU
    public static boolean skuExists(String sku) {
        String sql = "SELECT 1 FROM products WHERE sku = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, sku);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получить статус ячейки
    public static String getCellStatus(String location) {
        String sql = "SELECT status FROM warehouse_cells WHERE cell_location = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, location);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }
}