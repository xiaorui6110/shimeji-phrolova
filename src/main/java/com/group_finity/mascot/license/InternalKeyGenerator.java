package com.group_finity.mascot.license;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 内部密钥生成器 - 只有特殊密钥用户才能访问
 * Internal Key Generator - only accessible to Special Key users
 */
public class InternalKeyGenerator {
    
    private static final Logger logger = Logger.getLogger(InternalKeyGenerator.class.getName());
    
    // Temporary private key info extracted from special key (simplified, may need more complex key management in practice)
    private static final String INTERNAL_PRIVATE_KEY = 
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUo/QQiwYc8tysw5VmgGhzyAOu5nxifVytAKyOLxzbHoi1IbfOYxPhG6U5uHWhNqme3yyTX1AuXgxuzur8yUs5mpXVojnF10nJnTMclGkfw+OhSWlM0kTP2kVyVkg5ZJ+RPwNgQXp5fe4kvnXTkph2got/n9N2+Op7ScPz05ZEse8Yf1QcX0IAAb62Za6pqLIhE9LNGn2uVanFhBW+L3Rxs9fOsQYGeFMq8sYY3eNzOFeVp9BndBCqnY6ipqKuE+xUor6/rO7jWb5CquGjICqnRCWT7m14syIaLk++lJTW9RhYQcDrAjYg1xRxH1Qox0EwnQBD0eDJvvYJ1l67X1UlAgMBAAECggEAAxC0fkjXqe4qNdfcs13viX+077zqEL/kEt9CL7ZyAmTJTe7xkuyBIvHGgP+4UUhGzDNvAGUFSsd+uyRCPU9LYVc6QssTJOzRJTq8LJk058ZK4e8ZIE/vwleOehKkb6aPEdg6UhhXlbzOlexlkqThxwPfK5UYr8nnT/6P723MGgOHSQ0AlxlfNoBMoNEHeuOL/6ivx+ILxPfFdgZNv9Vb9whkBYFadOQFrhvLsqW/2wNvf4Kf2wW6f7WvjN3V9bXITpDGmm0kQjeCmP7hdZt4bf7DPz//PQhXkMSktgv19vccMApekj4W++dmWC+03z7+k4rOH8rXgTNsj3GPfdxqawKBgQDR4vQk2ZAFdzyyZnx71hjKz7JvaKx49ciFaQQ2fgdbQMVNcWFgUQ8b7Df/S5KWBI4y1Hv5lTaMXuH3f/sLUPeZDRNxnJdVwqOJp90Nh+et7a33D454HrACKxz1r8Jfu5aXlKiRkATBGibbD1B6ppAPihUw/WyWpqMM05jzI2cJ4wKBgQC1TDgxOwztkgyHgeNFNzEGoLZCa5DXuzILfcmBFVPMNnPJXbay07jaZ6YSZBTwSHEIEV33R2FQH1+sPupSaA0S5+Mumi7BgDkI2QhF2tTYgUrvuOR1lTDBlQ3Z+XqZUtrT4/puOKy4vqLq8jr/NkindliEoBeSTDn6qduAuo5zVwKBgEVqZUrGtc5I+EcchDFExuBUvvP/z4MV7uLIr78CAZbZnieVxxne1Ttr7b0+llWNXiZLhlEuW2MjgmKj7jh459qzq2mY26hefk3pub9e4m3wvOeIOptZsuaUsbhED+ovlYe+f93cnuXqq00qDHj0G5M4HPgd6MsOg8CbPuuzGsH3AoGADreJODXa7s4URku87zlzuV4CbZHdyCViApzLZrccfk9NRuNcmVemgdQ4HvYQ3RCEXGtMfW5F8AMb+ReCXf4kUYyRXI8XjI2kE5vKmCETdh/IvXz8zq61roEzBqbB1QBs0xaGlbv0CWswPnI7Z8w4SZ/Jv7pfL3kQm9B+CQ0b0zECgYEAkgRSKeZ3mYwG46dY0nZlIKZkm13vShnDFKm2lSsqIDJZ5gwIT1Yru1PI4ZdFd0HM+3PYwzwzeJPhwhExBBqR1ZHhCADU6Ljzgluf84JqmV9Im/wjuUxe9LUQVWS8V5eDNapD+JpdTOIJfoBPZgohLq1TfsKJXvd/HA27GUgDmxk=";
    
    private JFrame frame;
    private JTextField validityField;
    private JTextArea resultArea;
    private JButton generateButton;
    
    /**
     * 检查是否有权限访问内部密钥生成器
     * Check if has permission to access internal key generator
     */
    public static boolean canAccess() {
        LicenseManager manager = LicenseManager.getInstance();
        return manager.getCurrentLicenseLevel() == LicenseLevel.SPECIAL_KEY;
    }
    
    /**
     * 显示内部密钥生成器界面
     * Show internal key generator interface
     */
    public static void showKeyGenerator() {
        if (!canAccess()) {
            JOptionPane.showMessageDialog(null, 
                "Access Denied: Special Key required to access the internal key generator.",
                "Permission Required", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            new InternalKeyGenerator().createAndShowGUI();
        });
    }
    
    /**
     * 创建并显示GUI
     * Create and show GUI
     */
    private void createAndShowGUI() {
        frame = new JFrame("Shimeji-Live Internal Key Generator");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 标题
        JLabel titleLabel = new JLabel("Internal Advanced Key Generator");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(10, 10, 20, 10);
        mainPanel.add(titleLabel, gbc);
        
        // 有效期输入
        gbc.gridwidth = 1; gbc.insets = new Insets(5, 10, 5, 5);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Validity (days):"), gbc);
        
        validityField = new JTextField("30", 10);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(validityField, gbc);
        
        // 生成按钮
        generateButton = new JButton("Generate Advanced Key");
        generateButton.addActionListener(new GenerateKeyAction());
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(20, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(generateButton, gbc);
        
        // 结果区域
        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setBackground(Color.BLACK);
        resultArea.setForeground(Color.GREEN);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Generated Key"));
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.insets = new Insets(10, 10, 10, 10);
        mainPanel.add(scrollPane, gbc);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(e -> copyToClipboard());
        buttonPanel.add(copyButton);
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> resultArea.setText(""));
        buttonPanel.add(clearButton);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> frame.dispose());
        buttonPanel.add(closeButton);
        
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置窗口属性
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // 显示当前授权信息
        LicenseManager manager = LicenseManager.getInstance();
        resultArea.setText("Internal Key Generator Activated\n");
        resultArea.append("Current License: " + manager.getLicenseStatusInfo() + "\n");
        resultArea.append("Days Remaining: " + manager.getDaysRemaining() + "\n\n");
        resultArea.append("Ready to generate Advanced Keys...\n");
    }
    
    /**
     * 生成密钥的动作处理器
     * Action handler for key generation
     */
    private class GenerateKeyAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int validityDays = Integer.parseInt(validityField.getText().trim());
                
                if (validityDays <= 0 || validityDays > 3650) { // 最多10年
                    JOptionPane.showMessageDialog(frame, 
                        "Validity days must be between 1 and 3650.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                generateButton.setEnabled(false);
                resultArea.append("\nGenerating Advanced Key...\n");
                
                // 在后台线程中生成密钥
                SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return generateAdvancedKey(validityDays);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            String key = get();
                            resultArea.append("Generated Successfully!\n\n");
                            resultArea.append("Advanced Key (" + validityDays + " days):\n");
                            resultArea.append(key + "\n\n");
                            resultArea.append("Instructions:\n");
                            resultArea.append("1. Copy the key above\n");
                            resultArea.append("2. Share with authorized users\n");
                            resultArea.append("3. Users can activate via License menu\n\n");
                            
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Failed to generate key", ex);
                            resultArea.append("ERROR: Failed to generate key\n");
                            resultArea.append("Reason: " + ex.getMessage() + "\n");
                        } finally {
                            generateButton.setEnabled(true);
                        }
                    }
                };
                
                worker.execute();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Please enter a valid number for validity days.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
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
            signature
        );
        
        return Base64.getEncoder().encodeToString(keyString.getBytes());
    }
    
    /**
     * 复制到剪贴板
     * Copy to clipboard
     */
    private void copyToClipboard() {
        String text = resultArea.getText();
        // 查找密钥部分
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("MS4w")) { // Base64编码的密钥通常以此开头
                java.awt.datatransfer.StringSelection selection = 
                    new java.awt.datatransfer.StringSelection(line.trim());
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(selection, null);
                
                JOptionPane.showMessageDialog(frame, 
                    "Key copied to clipboard!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        JOptionPane.showMessageDialog(frame, 
            "No key found to copy.", 
            "Nothing to Copy", 
            JOptionPane.WARNING_MESSAGE);
    }
}
