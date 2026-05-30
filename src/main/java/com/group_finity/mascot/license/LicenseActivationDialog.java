package com.group_finity.mascot.license;

import com.group_finity.mascot.DPIManager;
import com.group_finity.mascot.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * 许可证激活界面
 * License activation interface
 */
public class LicenseActivationDialog extends JDialog {
    
    private static final Logger logger = Logger.getLogger(LicenseActivationDialog.class.getName());
    
    private JTextArea keyTextArea;
    private JButton activateButton;
    private JButton clearButton;
    private JButton cancelButton;
    private JButton deactivateButton;
    private JLabel statusLabel;
    private JLabel currentLicenseLabel;
    private JProgressBar progressBar;
    
    // Key generator components
    private JTextField validityField;
    private JTextArea generatorResultArea;
    private JButton generateKeyButton;
    private JTabbedPane tabbedPane;
    
    private boolean licenseActivated = false;
    private ResourceBundle languageBundle;
    
    public LicenseActivationDialog(Frame parent) {
        super(parent, "License Activation", true);
        this.languageBundle = Main.getInstance().getLanguageBundle();
        
        initComponents();
        applyDPIScaling();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    /**
     * 初始化组件
     * Initialize components
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 应用 DPI 缩放
        int scaledWidth = DPIManager.scaleWidth(600);
        int scaledHeight = DPIManager.scaleHeight(500);
        setSize(scaledWidth, scaledHeight);
        setLocationRelativeTo(getParent());
        
        // 顶部面板 - 当前许可证状态
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(languageBundle.getString("CurrentLicense")));
        
        currentLicenseLabel = new JLabel();
        currentLicenseLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, DPIManager.scaleFontSize(12)));
        topPanel.add(currentLicenseLabel, BorderLayout.CENTER);
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        
        // 许可证激活选项卡
        JPanel activationPanel = createActivationPanel();
        tabbedPane.addTab(languageBundle.getString("ActivateLicense"), activationPanel);
        
        // 如果是特殊密钥用户，添加密钥生成选项卡
        if (LicenseManager.getInstance().getCurrentLicenseLevel() == LicenseLevel.SPECIAL_KEY) {
            JPanel generatorPanel = createKeyGeneratorPanel();
            tabbedPane.addTab(languageBundle.getString("InternalKeyGenerator"), generatorPanel);
        }
        
        // 底部按钮面板
        JPanel bottomPanel = createBottomPanel();
        
        // 组装界面
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // 设置默认按钮
        getRootPane().setDefaultButton(activateButton);
    }
    
    /**
     * 创建许可证激活面板
     */
    private JPanel createActivationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 密钥输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(languageBundle.getString("EnterLicenseKey")));
        
        keyTextArea = new JTextArea(6, 40);
        keyTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, DPIManager.scaleFontSize(11)));
        keyTextArea.setLineWrap(true);
        keyTextArea.setWrapStyleWord(false);
        
        // 应用 DPI 缩放到边距
        int scaledPadding = DPIManager.scaleWidth(5);
        keyTextArea.setBorder(BorderFactory.createEmptyBorder(scaledPadding, scaledPadding, scaledPadding, scaledPadding));
        
        JScrollPane scrollPane = new JScrollPane(keyTextArea);
        inputPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 状态和进度条
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, DPIManager.scaleFontSize(11)));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.SOUTH);
        
        inputPanel.add(statusPanel, BorderLayout.SOUTH);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * 创建密钥生成器面板
     */
    private JPanel createKeyGeneratorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 顶部控制面板
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 有效期输入
        int scaledMarginLarge = DPIManager.scaleWidth(10);
        int scaledMarginSmall = DPIManager.scaleWidth(5);
        gbc.insets = new Insets(scaledMarginLarge, scaledMarginLarge, scaledMarginSmall, scaledMarginSmall);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        controlPanel.add(new JLabel(languageBundle.getString("ValidityDays") + ":"), gbc);
        
        validityField = new JTextField("30", 10);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(validityField, gbc);
        
        // 生成按钮
        generateKeyButton = new JButton(languageBundle.getString("GenerateAdvancedKey"));
        generateKeyButton.addActionListener(new GenerateKeyAction());
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; 
        int scaledMarginMedium = DPIManager.scaleWidth(20);
        gbc.insets = new Insets(scaledMarginMedium, scaledMarginLarge, scaledMarginLarge, scaledMarginLarge);
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(generateKeyButton, gbc);
        
        // 结果显示区域
        generatorResultArea = new JTextArea(12, 50);
        generatorResultArea.setEditable(false);
        generatorResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, DPIManager.scaleFontSize(12)));
        generatorResultArea.setBackground(Color.BLACK);
        generatorResultArea.setForeground(Color.GREEN);
        generatorResultArea.setText(languageBundle.getString("InternalKeyGenerator") + " " + languageBundle.getString("Generated") + "\n" +
                languageBundle.getString("CurrentLicense") + ": " + LicenseManager.getInstance().getLicenseStatusInfo() + "\n" +
                languageBundle.getString("DaysRemaining") + ": " + LicenseManager.getInstance().getDaysRemaining() + "\n\n" +
                "Ready to generate Advanced Keys...\n");
        
        JScrollPane resultScrollPane = new JScrollPane(generatorResultArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder(languageBundle.getString("Generated") + " Key"));
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(resultScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建底部按钮面板
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        activateButton = new JButton(languageBundle.getString("ActivateLicense"));
        activateButton.addActionListener(new ActivateAction());
        
        clearButton = new JButton(languageBundle.getString("Clear"));
        clearButton.addActionListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                // 清除激活选项卡的内容
                keyTextArea.setText("");
                statusLabel.setText(" ");
            } else {
                // 清除生成器选项卡的内容
                generatorResultArea.setText(languageBundle.getString("InternalKeyGenerator") + " " + languageBundle.getString("Generated") + "\n" +
                        languageBundle.getString("CurrentLicense") + ": " + LicenseManager.getInstance().getLicenseStatusInfo() + "\n" +
                        languageBundle.getString("DaysRemaining") + ": " + LicenseManager.getInstance().getDaysRemaining() + "\n\n" +
                        "Ready to generate Advanced Keys...\n");
            }
        });
        
        JButton deactivateButton = new JButton(languageBundle.getString("DeactivateLicense"));
        this.deactivateButton = deactivateButton;
        deactivateButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                languageBundle.getString("DeactivateConfirmMessage"),
                languageBundle.getString("ConfirmDeactivation"),
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                LicenseManager.getInstance().clearActivation();
                updateCurrentLicenseDisplay();
                statusLabel.setText(languageBundle.getString("LicenseDeactivatedSuccessfully"));
                statusLabel.setForeground(Color.BLUE);
                
                // 如果当前不是特殊密钥，移除生成器选项卡
                if (LicenseManager.getInstance().getCurrentLicenseLevel() != LicenseLevel.SPECIAL_KEY && tabbedPane.getTabCount() > 1) {
                    tabbedPane.removeTabAt(1);
                }
            }
        });
        
        cancelButton = new JButton(languageBundle.getString("Close"));
        cancelButton.addActionListener(e -> dispose());
        
        // 设置统一的按钮尺寸 - 参考 SettingsWindow 的实现
        Dimension buttonSize = new Dimension(130, 26);
        activateButton.setMaximumSize(buttonSize);
        activateButton.setMinimumSize(new Dimension(95, 23));
        activateButton.setPreferredSize(buttonSize);
        
        clearButton.setMaximumSize(buttonSize);
        clearButton.setMinimumSize(new Dimension(95, 23));
        clearButton.setPreferredSize(buttonSize);
        
        deactivateButton.setMaximumSize(buttonSize);
        deactivateButton.setMinimumSize(new Dimension(95, 23));
        deactivateButton.setPreferredSize(buttonSize);
        
        cancelButton.setMaximumSize(buttonSize);
        cancelButton.setMinimumSize(new Dimension(95, 23));
        cancelButton.setPreferredSize(buttonSize);
        
        bottomPanel.add(activateButton);
        bottomPanel.add(clearButton);
        bottomPanel.add(deactivateButton);
        bottomPanel.add(cancelButton);
        
        return bottomPanel;
    }
    
    /**
     * 更新当前许可证显示
     * Update current license display
     */
    private void updateCurrentLicenseDisplay() {
        LicenseManager manager = LicenseManager.getInstance();
        LicenseLevel level = manager.getCurrentLicenseLevel();
        String statusInfo = manager.getLicenseStatusInfo();
        
        currentLicenseLabel.setText(statusInfo);
        
        // 根据许可证级别设置颜色
        switch (level) {
            case NO_KEY:
                currentLicenseLabel.setForeground(Color.GRAY);
                break;
            case ADVANCED_KEY:
                currentLicenseLabel.setForeground(new Color(0, 150, 0)); // 深绿色
                break;
            case SPECIAL_KEY:
                currentLicenseLabel.setForeground(new Color(150, 0, 150)); // 紫色
                break;
        }
    }
    
    /**
     * 激活许可证的动作处理器
     * Action handler for license activation
     */
    private class ActivateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String keyText = keyTextArea.getText().trim();
            
            if (keyText.isEmpty()) {
                JOptionPane.showMessageDialog(LicenseActivationDialog.this,
                    languageBundle.getString("EnterLicenseKey"),
                    languageBundle.getString("InvalidInput"),
                    JOptionPane.WARNING_MESSAGE);
                keyTextArea.requestFocus();
                return;
            }
            
            // 禁用按钮并显示进度
            activateButton.setEnabled(false);
            clearButton.setEnabled(false);
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            statusLabel.setText(languageBundle.getString("ValidationInProgress"));
            statusLabel.setForeground(Color.BLUE);
            
            // 在后台线程中验证密钥
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    // 模拟验证过程（可以添加延迟使用户看到进度）
                    Thread.sleep(1000);
                    return LicenseManager.getInstance().validateKey(keyText);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean isValid = get();
                        
                        if (isValid) {
                            statusLabel.setText(languageBundle.getString("LicenseActivatedSuccessfully"));
                            statusLabel.setForeground(new Color(0, 150, 0));
                            licenseActivated = true;
                            updateCurrentLicenseDisplay();
                            keyTextArea.setText("");
                            
                            // 显示激活成功对话框
                            LicenseManager manager = LicenseManager.getInstance();
                            JOptionPane.showMessageDialog(LicenseActivationDialog.this,
                                String.format("%s\n\n%s: %s\n%s: %d",
                                    languageBundle.getString("LicenseActivatedSuccessfully"),
                                    languageBundle.getString("LicenseStatus"),
                                    manager.getCurrentLicenseLevel().getDisplayName(),
                                    languageBundle.getString("DaysRemaining"),
                                    manager.getDaysRemaining()),
                                languageBundle.getString("Success"),
                                JOptionPane.INFORMATION_MESSAGE);
                            
                        } else {
                            statusLabel.setText(languageBundle.getString("InvalidLicenseKey"));
                            statusLabel.setForeground(Color.RED);
                            
                            JOptionPane.showMessageDialog(LicenseActivationDialog.this,
                                languageBundle.getString("InvalidLicenseKey") + "\n\n" +
                                languageBundle.getString("EnterLicenseKey"),
                                languageBundle.getString("InvalidLicenseKey"),
                                JOptionPane.ERROR_MESSAGE);
                        }
                        
                    } catch (Exception ex) {
                        logger.severe("Error during license activation: " + ex.getMessage());
                        statusLabel.setText("Error occurred during activation.");
                        statusLabel.setForeground(Color.RED);
                        
                        JOptionPane.showMessageDialog(LicenseActivationDialog.this,
                            "An error occurred during license activation:\n" + ex.getMessage(),
                            "Activation Error",
                            JOptionPane.ERROR_MESSAGE);
                    } finally {
                        // 重新启用按钮并隐藏进度条
                        activateButton.setEnabled(true);
                        clearButton.setEnabled(true);
                        progressBar.setVisible(false);
                        progressBar.setIndeterminate(false);
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * 检查是否激活了许可证
     * Check if license was activated
     */
    public boolean isLicenseActivated() {
        return licenseActivated;
    }
    
    /**
     * 密钥生成动作处理器
     */
    private class GenerateKeyAction implements ActionListener {
        // InternalKeyGenerator中的私钥（仅供内部使用）
        private static final String INTERNAL_PRIVATE_KEY = 
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUo/QQiwYc8tysw5VmgGhzyAOu5nxifVytAKyOLxzbHoi1IbfOYxPhG6U5uHWhNqme3yyTX1AuXgxuzur8yUs5mpXVojnF10nJnTMclGkfw+OhSWlM0kTP2kVyVkg5ZJ+RPwNgQXp5fe4kvnXTkph2got/n9N2+Op7ScPz05ZEse8Yf1QcX0IAAb62Za6pqLIhE9LNGn2uVanFhBW+L3Rxs9fOsQYGeFMq8sYY3eNzOFeVp9BndBCqnY6ipqKuE+xUor6/rO7jWb5CquGjICqnRCWT7m14syIaLk++lJTW9RhYQcDrAjYg1xRxH1Qox0EwnQBD0eDJvvYJ1l67X1UlAgMBAAECggEAAxC0fkjXqe4qNdfcs13viX+077zqEL/kEt9CL7ZyAmTJTe7xkuyBIvHGgP+4UUhGzDNvAGUFSsd+uyRCPU9LYVc6QssTJOzRJTq8LJk058ZK4e8ZIE/vwleOehKkb6aPEdg6UhhXlbzOlexlkqThxwPfK5UYr8nnT/6P723MGgOHSQ0AlxlfNoBMoNEHeuOL/6ivx+ILxPfFdgZNv9Vb9whkBYFadOQFrhvLsqW/2wNvf4Kf2wW6f7WvjN3V9bXITpDGmm0kQjeCmP7hdZt4bf7DPz//PQhXkMSktgv19vccMApekj4W++dmWC+03z7+k4rOH8rXgTNsj3GPfdxqawKBgQDR4vQk2ZAFdzyyZnx71hjKz7JvaKx49ciFaQQ2fgdbQMVNcWFgUQ8b7Df/S5KWBI4y1Hv5lTaMXuH3f/sLUPeZDRNxnJdVwqOJp90Nh+et7a33D454HrACKxz1r8Jfu5aXlKiRkATBGibbD1B6ppAPihUw/WyWpqMM05jzI2cJ4wKBgQC1TDgxOwztkgyHgeNFNzEGoLZCa5DXuzILfcmBFVPMNnPJXbay07jaZ6YSZBTwSHEIEV33R2FQH1+sPupSaA0S5+Mumi7BgDkI2QhF2tTYgUrvuOR1lTDBlQ3Z+XqZUtrT4/puOKy4vqLq8jr/NkindliEoBeSTDn6qduAuo5zVwKBgEVqZUrGtc5I+EcchDFExuBUvvP/z4MV7uLIr78CAZbZnieVxxne1Ttr7b0+llWNXiZLhlEuW2MjgmKj7jh459qzq2mY26hefk3pub9e4m3wvOeIOptZsuaUsbhED+ovlYe+f93cnuXqq00qDHj0G5M4HPgd6MsOg8CbPuuzGsH3AoGADreJODXa7s4URku87zlzuV4CbZHdyCViApzLZrccfk9NRuNcmVemgdQ4HvYQ3RCEXGtMfW5F8AMb+ReCXf4kUYyRXI8XjI2kE5vKmCETdh/IvXz8zq61roEzBqbB1QBs0xaGlbv0CWswPnI7Z8w4SZ/Jv7pfL3kQm9B+CQ0b0zECgYEAkgRSKeZ3mYwG46dY0nZlIKZkm13vShnDFKm2lSsqIDJZ5gwIT1Yru1PI4ZdFd0HM+3PYwzwzeJPhwhExBBqR1ZHhCADU6Ljzgluf84JqmV9Im/wjuUxe9LUQVWS8V5eDNapD+JpdTOIJfoBPZgohLq1TfsKJXvd/HA27GUgDmxk=";

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                SwingUtilities.invokeLater(() -> {
                    generateKeyButton.setEnabled(false);
                    generateKeyButton.setText(languageBundle.getString("Generating") + "...");
                });
                
                try {
                    int validityDays = Integer.parseInt(validityField.getText().trim());
                    if (validityDays <= 0 || validityDays > 3650) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(LicenseActivationDialog.this,
                                    "有效期必须在1-3650天之间", "输入错误", JOptionPane.ERROR_MESSAGE);
                        });
                        return;
                    }
                    
                    // 使用正确的方法生成密钥
                    String generatedKey = generateAdvancedKey(validityDays);
                    
                    SwingUtilities.invokeLater(() -> {
                        generatorResultArea.setText(languageBundle.getString("InternalKeyGenerator") + " " + languageBundle.getString("Generated") + "\n" +
                                languageBundle.getString("CurrentLicense") + ": " + LicenseManager.getInstance().getLicenseStatusInfo() + "\n" +
                                languageBundle.getString("DaysRemaining") + ": " + LicenseManager.getInstance().getDaysRemaining() + "\n\n" +
                                "Generated Advanced Key:\n" +
                                "Level: " + LicenseLevel.ADVANCED_KEY.getDisplayName() + "\n" +
                                "Valid Days: " + validityDays + "\n" +
                                "Issue Time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n" +
                                "Expiry Time: " + LocalDateTime.now().plusDays(validityDays).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n\n" +
                                "Key Data:\n" + generatedKey + "\n\n" +
                                "Status: Key generation completed successfully! Key copied to clipboard.");
                        
                        // 自动复制密钥到剪切板
                        try {
                            StringSelection selection = new StringSelection(generatedKey);
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                        } catch (Exception clipboardEx) {
                            logger.warning("Failed to copy key to clipboard: " + clipboardEx.getMessage());
                        }
                    });
                    
                } catch (NumberFormatException ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LicenseActivationDialog.this,
                                "请输入有效的天数", "输入错误", JOptionPane.ERROR_MESSAGE);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LicenseActivationDialog.this,
                                "密钥生成失败: " + ex.getMessage(), "生成错误", JOptionPane.ERROR_MESSAGE);
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        generateKeyButton.setEnabled(true);
                        generateKeyButton.setText(languageBundle.getString("GenerateAdvancedKey"));
                    });
                }
            }).start();
        }
        
        /**
         * 生成高级密钥
         * Generate advanced key
         */
        private String generateAdvancedKey(int validityDays) throws Exception {
            LocalDateTime issueTime = LocalDateTime.now();
            String keyId = CryptoUtils.generateKeyId();
            
            // 创建密钥数据
            LicenseKeyData keyData = new LicenseKeyData(
                LicenseLevel.ADVANCED_KEY, validityDays, issueTime, "", keyId);
            
            // 生成签名
            PrivateKey privateKey = CryptoUtils.stringToPrivateKey(INTERNAL_PRIVATE_KEY);
            String rawData = keyData.getRawDataForSignature();
            String signature = CryptoUtils.signData(rawData, privateKey);
            
            // 编码为Base64字符串
            String keyString = String.format("%s|%s|%d|%s|%s|%s",
                LicenseKeyData.KEY_VERSION,
                LicenseLevel.ADVANCED_KEY.getCode(),
                validityDays,
                issueTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                keyId,
                signature);
            
            return java.util.Base64.getEncoder().encodeToString(keyString.getBytes());
        }
    }

    /**
     * 显示许可证激活对话框
     * Show license activation dialog
     */
    public static void showDialog(Frame parent, ResourceBundle languageBundle) {
        LicenseActivationDialog dialog = new LicenseActivationDialog(parent);
        dialog.setVisible(true);
    }
    
    /**
     * 应用DPI缩放
     * Apply DPI scaling similar to SettingsWindow
     */
    private void applyDPIScaling() {
        // 获取菜单缩放因子，参考SettingsWindow的实现
        float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96")) / 96;
        
        // 缩放对话框整体大小
        Dimension preferredSize = getContentPane().getPreferredSize();
        getContentPane().setPreferredSize(new Dimension(
            (int) (preferredSize.width * menuScaling),
            (int) (preferredSize.height * menuScaling)
        ));
        
        // 按照 SettingsWindow 的方式缩放按钮
        activateButton.setPreferredSize(new Dimension(
            (int) (activateButton.getPreferredSize().width * menuScaling),
            (int) (activateButton.getPreferredSize().height * menuScaling)));
        
        clearButton.setPreferredSize(new Dimension(
            (int) (clearButton.getPreferredSize().width * menuScaling),
            (int) (clearButton.getPreferredSize().height * menuScaling)));
        
        deactivateButton.setPreferredSize(new Dimension(
            (int) (deactivateButton.getPreferredSize().width * menuScaling),
            (int) (deactivateButton.getPreferredSize().height * menuScaling)));
        
        cancelButton.setPreferredSize(new Dimension(
            (int) (cancelButton.getPreferredSize().width * menuScaling),
            (int) (cancelButton.getPreferredSize().height * menuScaling)));
        
        if (generateKeyButton != null) {
            generateKeyButton.setPreferredSize(new Dimension(
                (int) (generateKeyButton.getPreferredSize().width * menuScaling),
                (int) (generateKeyButton.getPreferredSize().height * menuScaling)));
        }
        
        // 缩放文本组件
        if (keyTextArea != null) {
            Font currentFont = keyTextArea.getFont();
            if (currentFont != null) {
                keyTextArea.setFont(currentFont.deriveFont(currentFont.getSize() * menuScaling));
            }
        }
        
        if (validityField != null) {
            validityField.setPreferredSize(new Dimension(
                (int) (validityField.getPreferredSize().width * menuScaling),
                (int) (validityField.getPreferredSize().height * menuScaling)));
        }
        
        if (generatorResultArea != null) {
            Font currentFont = generatorResultArea.getFont();
            if (currentFont != null) {
                generatorResultArea.setFont(currentFont.deriveFont(currentFont.getSize() * menuScaling));
            }
        }
        
        // 重新打包对话框
        pack();
    }
}
