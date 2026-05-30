package com.group_finity.mascot.license;

/**
 * 软件授权级别枚举
 * Enum defining the different authorization levels of the software
 */
public enum LicenseLevel {
    /**
     * 无密钥 - 默认级别，功能受限
     * No key - default level with limited functionality
     */
    NO_KEY("NO_KEY", "Free Version", "Basic functionality only"),
    
    /**
     * 高级密钥 - 解锁高级功能
     * Advanced key - unlocks advanced features
     */
    ADVANCED_KEY("ADVANCED", "Advanced Version", "Full functionality with advanced features"),
    
    /**
     * 特殊密钥 - 解锁所有功能并激活内置密钥生成器
     * Special key - unlocks all features and activates built-in key generator
     */
    SPECIAL_KEY("SPECIAL", "Developer Version", "All features plus key generation capabilities");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    LicenseLevel(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码字符串获取对应的授权级别
     * Get license level by code string
     */
    public static LicenseLevel fromCode(String code) {
        for (LicenseLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return NO_KEY;
    }
    
    /**
     * 检查当前级别是否有权限访问指定功能
     * Check if current level has permission to access specified feature
     */
    public boolean hasPermission(LicenseLevel requiredLevel) {
        return this.ordinal() >= requiredLevel.ordinal();
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
