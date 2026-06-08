package auth;

import models.User;
import db.DatabaseHelper;
import logging.Logger;
import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private static User currentUser = null;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginDialog(JFrame parent) {
        super(parent, "Авторизация - Складская система", true);
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Заголовок
        JLabel titleLabel = new JLabel("Определение локации товаров на складе", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Форма входа
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Логин:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Пароль:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Информационная панель
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Тестовые учетные записи"));
        infoPanel.setLayout(new GridLayout(3, 2, 10, 5));
        infoPanel.add(new JLabel("📦 Комплектовщик:"));
        infoPanel.add(new JLabel("picker / 123"));
        infoPanel.add(new JLabel("🔧 Кладовщик:"));
        infoPanel.add(new JLabel("storekeeper / 123"));
        infoPanel.add(new JLabel("👑 Администратор:"));
        infoPanel.add(new JLabel("admin / 123"));
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginBtn = new JButton("Войти в систему");
        JButton exitBtn = new JButton("Выход");

        loginBtn.setPreferredSize(new Dimension(150, 35));
        exitBtn.setPreferredSize(new Dimension(100, 35));

        buttonPanel.add(loginBtn);
        buttonPanel.add(exitBtn);

        // Статус
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        mainPanel.add(southPanel, BorderLayout.NORTH);

        add(mainPanel);

        // Обработчики
        loginBtn.addActionListener(e -> login());
        exitBtn.addActionListener(e -> System.exit(0));

        getRootPane().setDefaultButton(loginBtn);

        Logger.info(LoginDialog.class, "LoginDialog", "Диалог авторизации создан");
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Введите логин и пароль");
            Logger.warning(LoginDialog.class, "login", "Попытка входа с пустыми полями");
            return;
        }

        Logger.info(LoginDialog.class, "login", "Попытка входа пользователя: " + username);

        User user = DatabaseHelper.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            statusLabel.setText("");
            Logger.info(LoginDialog.class, "login", "Пользователь " + username + " успешно вошел в систему");
            Logger.userAction(username, "Вход в систему");
            dispose();
        } else {
            statusLabel.setText("Неверный логин или пароль");
            passwordField.setText("");
            Logger.warning(LoginDialog.class, "login", "Неудачная попытка входа: " + username);
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}