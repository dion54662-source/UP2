package utils;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class BarcodeScannerSimulator {
    private static final String[] TEST_BARCODES = {
            "4601234567891", "4601234567892", "4601234567893",
            "4601234567894", "4601234567895", "4601234567896",
            "4601234567897", "4601234567898"
    };
    private static final Random random = new Random();

    /**
     * Симуляция сканирования штрихкода с задержкой
     * @return отсканированный штрихкод
     */
    public static String scanBarcode() {
        try {
            // Симуляция задержки сканера
            Thread.sleep(800 + random.nextInt(500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String barcode = TEST_BARCODES[random.nextInt(TEST_BARCODES.length)];
        System.out.println("[Сканер] Отсканирован код: " + barcode);
        return barcode;
    }

    /**
     * Симуляция сканирования с диалоговым окном
     * @param parent родительский компонент
     * @return отсканированный штрихкод или null если отменено
     */
    public static String scanBarcodeWithDialog(Component parent) {
        // Создаем кастомный диалог для имитации сканера
        JDialog scanDialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Сканер штрихкодов", Dialog.ModalityType.APPLICATION_MODAL);
        scanDialog.setSize(400, 300);
        scanDialog.setLocationRelativeTo(parent);
        scanDialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel("📷", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        panel.add(iconLabel, BorderLayout.NORTH);

        JLabel messageLabel = new JLabel("Наведите камеру на штрихкод...", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(messageLabel, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.SOUTH);

        scanDialog.add(panel);

        // Таймер для имитации сканирования
        Timer timer = new Timer(1500, e -> {
            scanDialog.dispose();
        });
        timer.setRepeats(false);
        timer.start();

        scanDialog.setVisible(true);

        // Возвращаем случайный штрихкод
        String barcode = TEST_BARCODES[random.nextInt(TEST_BARCODES.length)];

        // Показываем результат
        JOptionPane.showMessageDialog(parent,
                "✅ Отсканирован штрихкод:\n" + barcode,
                "Результат сканирования",
                JOptionPane.INFORMATION_MESSAGE);

        return barcode;
    }

    /**
     * Получить случайный штрихкод
     */
    public static String getRandomBarcode() {
        return TEST_BARCODES[random.nextInt(TEST_BARCODES.length)];
    }

    /**
     * Проверить, существует ли штрихкод в тестовой базе
     */
    public static boolean isValidTestBarcode(String barcode) {
        for (String code : TEST_BARCODES) {
            if (code.equals(barcode)) {
                return true;
            }
        }
        return false;
    }
}