package com.group_finity.mascot.license;

import javax.swing.*;
import java.awt.*;

/**
 * 许可证功能演示类
 * License feature demo class to showcase licensing functionality
 */
public class LicenseFeatureDemo {
    
    /**
     * 显示高级功能演示对话框
     * Show advanced feature demo dialog
     */
    public static void showAdvancedFeatureDemo(Frame parent) {
        if (!LicenseChecker.checkAdvancedFeature(true)) {
            return; // Permission denied dialog already shown
        }
        
        JOptionPane.showMessageDialog(parent,
            "🎉 Advanced Feature Unlocked!\n\n" +
            "This is an example of an advanced feature that requires\n" +
            "an Advanced or Special license to access.\n\n" +
            "Advanced features might include:\n" +
            "• Custom mascot behaviors\n" +
            "• Enhanced animations\n" +
            "• Advanced settings\n" +
            "• Export/Import configurations\n" +
            "• And much more!",
            "Advanced Feature Demo",
            JOptionPane.INFORMATION_MESSAGE);
            
        LicenseChecker.logFeatureUsage("AdvancedFeatureDemo", LicenseLevel.ADVANCED_KEY);
    }
    
    /**
     * 显示特殊功能演示对话框
     * Show special feature demo dialog
     */
    public static void showSpecialFeatureDemo(Frame parent) {
        if (!LicenseChecker.checkSpecialFeature(true)) {
            return; // Permission denied dialog already shown
        }
        
        // Show special feature demo
        JDialog dialog = new JDialog(parent, "Special Feature Demo", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel titleLabel = new JLabel("🔥 Special Feature Activated!");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(new Color(150, 0, 150));
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(20, 20, 10, 20);
        contentPanel.add(titleLabel, gbc);
        
        JTextArea textArea = new JTextArea(
            "Congratulations! You have access to special features!\n\n" +
            "Special license features include:\n" +
            "• All Advanced features\n" +
            "• Developer tools and debugging\n" +
            "• Internal key generation capabilities\n" +
            "• Beta feature access\n" +
            "• Priority support\n\n" +
            "You can now generate new Advanced keys for distribution!"
        );
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        contentPanel.add(textArea, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton keyGenButton = new JButton("Open Key Generator");
        keyGenButton.addActionListener(e -> {
            dialog.dispose();
            InternalKeyGenerator.showKeyGenerator();
        });
        buttonPanel.add(keyGenButton);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        
        LicenseChecker.logFeatureUsage("SpecialFeatureDemo", LicenseLevel.SPECIAL_KEY);
    }
    
    /**
     * 显示功能状态信息
     * Show feature status information
     */
    public static void showFeatureStatusInfo(Frame parent) {
        LicenseManager manager = LicenseManager.getInstance();
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Shimeji-Live License Status ===\n\n");
        sb.append("Current License: ").append(manager.getLicenseStatusInfo()).append("\n");
        sb.append("License Level: ").append(manager.getCurrentLicenseLevel().getDisplayName()).append("\n");
        
        if (manager.getCurrentLicenseLevel() != LicenseLevel.NO_KEY) {
            sb.append("Days Remaining: ").append(manager.getDaysRemaining()).append("\n");
            sb.append("Hours Remaining: ").append(manager.getHoursRemaining()).append("\n");
        }
        
        sb.append("\n=== Feature Availability ===\n\n");
        sb.append("Basic Features: Available\n");
        sb.append("Advanced Features: ").append(LicenseChecker.getFeatureStatus(LicenseLevel.ADVANCED_KEY)).append("\n");
        sb.append("Special Features: ").append(LicenseChecker.getFeatureStatus(LicenseLevel.SPECIAL_KEY)).append("\n");
        sb.append("Key Generation: ").append(LicenseChecker.getFeatureStatus(LicenseLevel.SPECIAL_KEY)).append("\n");
        
        if (LicenseChecker.isLicenseExpiringSoon()) {
            sb.append("\n⚠️ WARNING: Your license is expiring soon!\n");
            sb.append("Please renew to continue enjoying premium features.\n");
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(parent, scrollPane, "License Status Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
