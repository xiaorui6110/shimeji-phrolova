package com.group_finity.mascot.license;

import javax.swing.JOptionPane;
import java.util.logging.Logger;

/**
 * 许可证检查工具类
 * License checking utility class
 */
public class LicenseChecker {
    
    private static final Logger logger = Logger.getLogger(LicenseChecker.class.getName());
    
    /**
     * 检查是否有权限访问指定功能
     * Check if has permission to access specified feature
     */
    public static boolean checkPermission(LicenseLevel requiredLevel) {
        return checkPermission(requiredLevel, false);
    }
    
    /**
     * 检查是否有权限访问指定功能
     * Check if has permission to access specified feature
     * 
     * @param requiredLevel 需要的许可证级别
     * @param showDialog 如果权限不足是否显示对话框
     * @return 是否有权限
     */
    public static boolean checkPermission(LicenseLevel requiredLevel, boolean showDialog) {
        LicenseManager manager = LicenseManager.getInstance();
        LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
        
        boolean hasPermission = currentLevel.hasPermission(requiredLevel);
        
        if (!hasPermission && showDialog) {
            showPermissionDeniedDialog(requiredLevel);
        }
        
        return hasPermission;
    }
    
    /**
     * 检查高级功能权限
     * Check advanced feature permission
     */
    public static boolean checkAdvancedFeature() {
        return checkAdvancedFeature(false);
    }
    
    /**
     * 检查高级功能权限
     * Check advanced feature permission
     */
    public static boolean checkAdvancedFeature(boolean showDialog) {
        return checkPermission(LicenseLevel.ADVANCED_KEY, showDialog);
    }
    
    /**
     * 检查特殊功能权限
     * Check special feature permission
     */
    public static boolean checkSpecialFeature() {
        return checkSpecialFeature(false);
    }
    
    /**
     * 检查特殊功能权限
     * Check special feature permission
     */
    public static boolean checkSpecialFeature(boolean showDialog) {
        return checkPermission(LicenseLevel.SPECIAL_KEY, showDialog);
    }
    
    /**
     * 显示权限不足对话框
     * Show permission denied dialog
     */
    private static void showPermissionDeniedDialog(LicenseLevel requiredLevel) {
        LicenseManager manager = LicenseManager.getInstance();
        String currentStatus = manager.getLicenseStatusInfo();
        
        String message;
        String title;
        
        switch (requiredLevel) {
            case ADVANCED_KEY:
                message = "This feature requires an Advanced or Special license.\n\n" +
                         "Current Status: " + currentStatus + "\n\n" +
                         "Please activate a valid license to access this feature.\n" +
                         "You can purchase a license from our website or contact support.";
                title = "Advanced License Required";
                break;
                
            case SPECIAL_KEY:
                message = "This feature requires a Special license.\n\n" +
                         "Current Status: " + currentStatus + "\n\n" +
                         "Please activate a Special license to access this feature.\n" +
                         "Special licenses are available for developers and premium users.";
                title = "Special License Required";
                break;
                
            default:
                message = "This feature requires a higher license level.\n\n" +
                         "Current Status: " + currentStatus + "\n\n" +
                         "Please upgrade your license to access this feature.";
                title = "License Upgrade Required";
                break;
        }
        
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * 获取功能可用性状态文本
     * Get feature availability status text
     */
    public static String getFeatureStatus(LicenseLevel requiredLevel) {
        LicenseManager manager = LicenseManager.getInstance();
        boolean hasPermission = manager.getCurrentLicenseLevel().hasPermission(requiredLevel);
        
        if (hasPermission) {
            return "Available";
        } else {
            return "Requires " + requiredLevel.getDisplayName();
        }
    }
    
    /**
     * 检查许可证是否即将过期（少于7天）
     * Check if license is about to expire (less than 7 days)
     */
    public static boolean isLicenseExpiringSoon() {
        LicenseManager manager = LicenseManager.getInstance();
        long daysRemaining = manager.getDaysRemaining();
        return daysRemaining > 0 && daysRemaining <= 7;
    }
    
    /**
     * 显示许可证过期警告
     * Show license expiration warning
     */
    public static void showExpirationWarningIfNeeded() {
        LicenseManager manager = LicenseManager.getInstance();
        LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
        
        if (currentLevel != LicenseLevel.NO_KEY) {
            long daysRemaining = manager.getDaysRemaining();
            
            if (daysRemaining <= 0) {
                JOptionPane.showMessageDialog(null,
                    "Your license has expired!\n\n" +
                    "The software will continue to work with limited functionality.\n" +
                    "Please renew your license to restore full features.",
                    "License Expired",
                    JOptionPane.WARNING_MESSAGE);
            } else if (daysRemaining <= 3) {
                JOptionPane.showMessageDialog(null,
                    "Your license will expire in " + daysRemaining + " day(s)!\n\n" +
                    "Please renew your license to avoid interruption of service.\n" +
                    "Contact support or visit our website to renew.",
                    "License Expiring Soon",
                    JOptionPane.WARNING_MESSAGE);
            } else if (daysRemaining <= 7) {
                // 只在启动时显示一次警告
                logger.info("License expiring in " + daysRemaining + " days");
            }
        }
    }
    
    /**
     * 记录功能使用情况（用于统计和审计）
     * Log feature usage (for statistics and audit)
     */
    public static void logFeatureUsage(String featureName, LicenseLevel requiredLevel) {
        LicenseManager manager = LicenseManager.getInstance();
        LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
        
        boolean hasPermission = currentLevel.hasPermission(requiredLevel);
        
        logger.info(String.format("Feature access: %s, Required: %s, Current: %s, Allowed: %s",
            featureName, requiredLevel.getDisplayName(), currentLevel.getDisplayName(), hasPermission));
    }
}
