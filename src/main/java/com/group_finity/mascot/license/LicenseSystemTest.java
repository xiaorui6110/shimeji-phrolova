package com.group_finity.mascot.license;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 许可证系统测试程序
 * License system test application
 */
public class LicenseSystemTest {
    
    private JFrame frame;
    private JLabel statusLabel;
    private JButton generateKeyButton;
    private JButton activateButton;
    private JButton advancedFeatureButton;
    private JButton specialFeatureButton;
    private JButton statusButton;
    private JButton clearButton;
    private ResourceBundle languageBundle;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback to default look and feel
            }
            new LicenseSystemTest().createAndShowGUI();
        });
    }
    
    private void createAndShowGUI() {
        // Initialize language bundle with fallback
        try {
            languageBundle = ResourceBundle.getBundle("language", Locale.getDefault());
        } catch (Exception e) {
            // Fallback to English if language bundle is not found
            try {
                languageBundle = ResourceBundle.getBundle("language", Locale.forLanguageTag("en-GB"));
            } catch (Exception ex) {
                // Create a minimal fallback bundle
                languageBundle = new ResourceBundle() {
                    @Override
                    protected Object handleGetObject(String key) {
                        // Provide basic English fallbacks for license system
                        switch (key) {
                            case "LicenseActivation": return "License Activation";
                            case "CurrentLicense": return "Current License Status";
                            case "EnterLicenseKey": return "Enter License Key";
                            case "ActivateLicense": return "Activate License";
                            case "DeactivateLicense": return "Deactivate";
                            case "Clear": return "Clear";
                            case "Close": return "Close";
                            default: return key; // Return the key itself as fallback
                        }
                    }
                    @Override
                    public java.util.Enumeration<String> getKeys() {
                        return java.util.Collections.emptyEnumeration();
                    }
                };
            }
        }
        
        frame = new JFrame("Shimeji-Live License System Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // 顶部状态面板
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("License Status"));
        
        statusLabel = new JLabel();
        updateStatusLabel();
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        // 主控制面板
        JPanel controlPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 密钥生成按钮
        generateKeyButton = new JButton("Generate Test Keys");
        generateKeyButton.addActionListener(e -> showKeyGenerationDialog());
        controlPanel.add(generateKeyButton);
        
        // 激活许可证按钮
        activateButton = new JButton("Activate License");
        activateButton.addActionListener(e -> {
            LicenseActivationDialog.showDialog(frame, languageBundle);
            updateStatusLabel();
        });
        controlPanel.add(activateButton);
        
        // 高级功能按钮
        advancedFeatureButton = new JButton("Test Advanced Feature");
        advancedFeatureButton.addActionListener(e -> LicenseFeatureDemo.showAdvancedFeatureDemo(frame));
        controlPanel.add(advancedFeatureButton);
        
        // 特殊功能按钮
        specialFeatureButton = new JButton("Test Special Feature");
        specialFeatureButton.addActionListener(e -> LicenseFeatureDemo.showSpecialFeatureDemo(frame));
        controlPanel.add(specialFeatureButton);
        
        // 状态信息按钮
        statusButton = new JButton("Show Status Info");
        statusButton.addActionListener(e -> LicenseFeatureDemo.showFeatureStatusInfo(frame));
        controlPanel.add(statusButton);
        
        // 清除许可证按钮
        clearButton = new JButton("Clear License");
        clearButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to clear the current license?",
                "Confirm Clear",
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                LicenseManager.getInstance().clearActivation();
                updateStatusLabel();
            }
        });
        controlPanel.add(clearButton);
        
        // 底部说明面板
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));
        
        JTextArea infoText = new JTextArea(
            "1. Click 'Generate Test Keys' to create sample license keys\n" +
            "2. Copy a key and use 'Activate License' to test activation\n" +
            "3. Try 'Test Advanced Feature' and 'Test Special Feature' to see access control\n" +
            "4. Use 'Show Status Info' to view detailed license information\n" +
            "5. Special keys unlock the internal key generator\n\n" +
            "Note: This is a demonstration of the license system functionality."
        );
        infoText.setEditable(false);
        infoText.setOpaque(false);
        infoText.setWrapStyleWord(true);
        infoText.setLineWrap(true);
        infoText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        infoPanel.add(infoText, BorderLayout.CENTER);
        
        // 组装界面
        frame.add(statusPanel, BorderLayout.NORTH);
        frame.add(controlPanel, BorderLayout.CENTER);
        frame.add(infoPanel, BorderLayout.SOUTH);
        
        frame.setSize(600, 450);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    private void updateStatusLabel() {
        LicenseManager manager = LicenseManager.getInstance();
        String statusInfo = manager.getLicenseStatusInfo();
        statusLabel.setText("<html><font size='4'>" + statusInfo + "</font></html>");
        
        // 根据许可证级别设置颜色
        LicenseLevel level = manager.getCurrentLicenseLevel();
        switch (level) {
            case NO_KEY:
                statusLabel.setForeground(Color.GRAY);
                break;
            case ADVANCED_KEY:
                statusLabel.setForeground(new Color(0, 150, 0));
                break;
            case SPECIAL_KEY:
                statusLabel.setForeground(new Color(150, 0, 150));
                break;
        }
    }
    
    private JTextArea currentResultArea; // Store reference to current result area
    
    private void showKeyGenerationDialog() {
        JDialog dialog = new JDialog(frame, "Generate Test Keys", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titleLabel = new JLabel("Test License Key Generator");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(10, 10, 20, 10);
        contentPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1; gbc.insets = new Insets(5, 10, 5, 5);
        
        // Advanced key generation
        gbc.gridx = 0; gbc.gridy = 1;
        JButton advancedKeyBtn = new JButton("Generate Advanced Key (30 days)");
        advancedKeyBtn.addActionListener(e -> generateAndShowKey(LicenseLevel.ADVANCED_KEY, 30));
        contentPanel.add(advancedKeyBtn, gbc);
        
        gbc.gridx = 1;
        JButton advanced90Btn = new JButton("Generate Advanced Key (90 days)");
        advanced90Btn.addActionListener(e -> generateAndShowKey(LicenseLevel.ADVANCED_KEY, 90));
        contentPanel.add(advanced90Btn, gbc);
        
        // Special key generation
        gbc.gridx = 0; gbc.gridy = 2;
        JButton specialKeyBtn = new JButton("Generate Special Key (30 days)");
        specialKeyBtn.addActionListener(e -> generateAndShowKey(LicenseLevel.SPECIAL_KEY, 30));
        contentPanel.add(specialKeyBtn, gbc);
        
        gbc.gridx = 1;
        JButton special365Btn = new JButton("Generate Special Key (365 days)");
        special365Btn.addActionListener(e -> generateAndShowKey(LicenseLevel.SPECIAL_KEY, 365));
        contentPanel.add(special365Btn, gbc);
        
        // Result area
        currentResultArea = new JTextArea(8, 50);
        currentResultArea.setEditable(false);
        currentResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        currentResultArea.setBackground(Color.BLACK);
        currentResultArea.setForeground(Color.GREEN);
        JScrollPane scrollPane = new JScrollPane(currentResultArea);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.insets = new Insets(20, 10, 10, 10);
        contentPanel.add(scrollPane, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> {
            currentResultArea = null;
            dialog.dispose();
        });
        buttonPanel.add(closeButton);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        currentResultArea.setText("Click a button above to generate a test license key...\n\n");
        
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    
    private void generateAndShowKey(LicenseLevel level, int days) {
        try {
            String key = KeyGenerator.generateLicenseKey(level, days);
            
            if (currentResultArea != null) {
                currentResultArea.append("Generated " + level.getDisplayName() + " (" + days + " days):\n");
                currentResultArea.append(key + "\n\n");
                currentResultArea.append("Copy this key to test license activation.\n\n");
                currentResultArea.setCaretPosition(currentResultArea.getDocument().getLength());
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                "Failed to generate key: " + ex.getMessage(),
                "Generation Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
