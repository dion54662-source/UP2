package debug;

import logging.Logger;
import java.sql.*;
import java.util.Random;

public class TestDataGenerator {
    private static final Random random = new Random();
    private static final String[] PRODUCT_NAMES = {
            "Ноутбук", "Смартфон", "Планшет", "Монитор", "Клавиатура",
            "Мышь", "Наушники", "Колонки", "Принтер", "Сканер",
            "Внешний диск", "Флешка", "Зарядное устройство", "Кабель HDMI",
            "Веб-камера", "Микрофон", "Роутер", "Сетевой фильтр", "ИБП", "Сервер"
    };

    private static final String[] BRANDS = {
            "Lenovo", "HP", "Dell", "Apple", "Samsung", "Xiaomi", "Logitech",
            "Kingston", "Western Digital", "Seagate", "ASUS", "Acer", "MSI"
    };

    private static final String[] RACKS = {"A", "B", "C"};
    private static final String[] SECTIONS = {"01", "02", "03"};
    private static final String[] SHELVES = {"01", "02", "03"};
    private static final String[] CELLS = {"01", "02"};

    public static void generateProducts(final int count) {
        Logger.info(TestDataGenerator.class, "generateProducts", "Генерация " + count + " тестовых товаров");

        try {
            final Connection conn = db.DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            int generated = 0;
            for (int i = 0; i < count; i++) {
                final String sku = generateSKU();
                final String name = generateProductName();
                final String location = generateRandomLocation();
                final String barcode = generateBarcode();

                // Проверяем, не существует ли уже такой SKU
                final PreparedStatement check = conn.prepareStatement("SELECT 1 FROM products WHERE sku = ?");
                check.setString(1, sku);
                final ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    continue;
                }

                final PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO products (sku, name, location, barcode) VALUES (?, ?, ?, ?)");
                insert.setString(1, sku);
                insert.setString(2, name);
                insert.setString(3, location);
                insert.setString(4, barcode);
                insert.executeUpdate();

                // Обновляем статус ячейки
                updateCellStatus(conn, location, "OCCUPIED", sku);

                generated++;
            }

            conn.commit();
            Logger.info(TestDataGenerator.class, "generateProducts",
                    "Сгенерировано " + generated + " товаров из " + count);

        } catch (SQLException e) {
            Logger.error(TestDataGenerator.class, "generateProducts", "Ошибка генерации данных", e);
        }
    }

    private static String generateSKU() {
        return "TEST_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
    }

    private static String generateProductName() {
        final String brand = BRANDS[random.nextInt(BRANDS.length)];
        final String product = PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)];
        final String model = String.valueOf(random.nextInt(1000));
        return brand + " " + product + " " + model;
    }

    private static String generateRandomLocation() {
        final String rack = RACKS[random.nextInt(RACKS.length)];
        final String section = SECTIONS[random.nextInt(SECTIONS.length)];
        final String shelf = SHELVES[random.nextInt(SHELVES.length)];
        final String cell = CELLS[random.nextInt(CELLS.length)];
        return rack + "-" + section + "-" + shelf + "-" + cell;
    }

    private static String generateBarcode() {
        final StringBuilder barcode = new StringBuilder("460");
        for (int i = 0; i < 10; i++) {
            barcode.append(random.nextInt(10));
        }
        return barcode.toString();
    }

    private static void updateCellStatus(final Connection conn, final String location, final String status, final String sku) throws SQLException {
        final PreparedStatement update = conn.prepareStatement(
                "UPDATE warehouse_cells SET status = ?, current_sku = ? WHERE cell_location = ?");
        update.setString(1, status);
        update.setString(2, sku);
        update.setString(3, location);
        update.executeUpdate();
    }

    public static void clearTestData() {
        Logger.info(TestDataGenerator.class, "clearTestData", "Очистка тестовых данных");

        try {
            final Connection conn = db.DatabaseHelper.getConnection();
            final Statement stmt = conn.createStatement();

            // Удаляем тестовые товары
            stmt.execute("DELETE FROM products WHERE sku LIKE 'TEST_%'");

            // Сбрасываем статус ячеек
            stmt.execute("UPDATE warehouse_cells SET status = 'FREE', current_sku = NULL");

            // Восстанавливаем исходные товары
            restoreOriginalProducts(stmt);

            Logger.info(TestDataGenerator.class, "clearTestData", "Тестовые данные очищены");

        } catch (SQLException e) {
            Logger.error(TestDataGenerator.class, "clearTestData", "Ошибка очистки данных", e);
        }
    }

    private static void restoreOriginalProducts(final Statement stmt) throws SQLException {
        // Восстанавливаем исходные товары
        stmt.execute("INSERT OR REPLACE INTO products (sku, name, location, barcode) VALUES " +
                "('SKU001', 'Ноутбук Lenovo IdeaPad', 'A-01-02-03', '4601234567891')," +
                "('SKU002', 'Мышь Logitech MX Master', 'B-02-01-01', '4601234567892')," +
                "('SKU003', 'Клавиатура Mechanic', 'A-01-01-02', '4601234567893')," +
                "('SKU004', 'Монитор Samsung 24\"', 'C-03-02-01', '4601234567894')," +
                "('SKU005', 'SSD Kingston 1TB', 'A-02-01-03', '4601234567895')");

        // Восстанавливаем статус ячеек
        stmt.execute("UPDATE warehouse_cells SET status = 'OCCUPIED', current_sku = 'SKU001' WHERE cell_location = 'A-01-02-03'");
        stmt.execute("UPDATE warehouse_cells SET status = 'OCCUPIED', current_sku = 'SKU002' WHERE cell_location = 'B-02-01-01'");
        stmt.execute("UPDATE warehouse_cells SET status = 'OCCUPIED', current_sku = 'SKU003' WHERE cell_location = 'A-01-01-02'");
        stmt.execute("UPDATE warehouse_cells SET status = 'OCCUPIED', current_sku = 'SKU004' WHERE cell_location = 'C-03-02-01'");
        stmt.execute("UPDATE warehouse_cells SET status = 'OCCUPIED', current_sku = 'SKU005' WHERE cell_location = 'A-02-01-03'");
    }
}