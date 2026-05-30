package com.group_finity.mascot.license;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 许可证密钥数据类
 * License key data class containing all key information
 */
public class LicenseKeyData {
    private final LicenseLevel level;
    private final int validityDays;
    private final LocalDateTime issueTime;
    private final String signature;
    private final String keyId;
    
    // 密钥格式版本
    public static final String KEY_VERSION = "1.0";
    
    public LicenseKeyData(LicenseLevel level, int validityDays, LocalDateTime issueTime, 
                         String signature, String keyId) {
        this.level = level;
        this.validityDays = validityDays;
        this.issueTime = issueTime;
        this.signature = signature;
        this.keyId = keyId;
    }
    
    public LicenseLevel getLevel() {
        return level;
    }
    
    public int getValidityDays() {
        return validityDays;
    }
    
    public LocalDateTime getIssueTime() {
        return issueTime;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    /**
     * 获取过期时间
     * Get expiration time
     */
    public LocalDateTime getExpirationTime() {
        return issueTime.plusDays(validityDays);
    }
    
    /**
     * 检查密钥是否已过期
     * Check if the key has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(getExpirationTime());
    }
    
    /**
     * 获取剩余天数
     * Get remaining days
     */
    public long getRemainingDays() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), getExpirationTime()).toDays();
    }
    
    /**
     * 获取剩余小时数（更精确）
     * Get remaining hours (more precise)
     */
    public long getRemainingHours() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), getExpirationTime()).toHours();
    }
    
    /**
     * 生成用于签名的原始数据字符串
     * Generate raw data string for signature
     */
    public String getRawDataForSignature() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.format("%s|%s|%d|%s|%s", 
            KEY_VERSION, level.getCode(), validityDays, 
            issueTime.format(formatter), keyId);
    }
    
    @Override
    public String toString() {
        return String.format("LicenseKey[level=%s, validityDays=%d, remaining=%d days, keyId=%s]",
            level.getDisplayName(), validityDays, getRemainingDays(), keyId);
    }
}
