package com.group_finity.mascot.license;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 许可证管理器 - 负责密钥验证和授权级别管理
 * License Manager - responsible for key validation and authorization level management
 */
public class LicenseManager {
    
    private static final Logger logger = Logger.getLogger(LicenseManager.class.getName());
    
    // 嵌入的公钥用于验证签名（在实际部署时替换为真实的公钥）
    // Embedded public key for signature verification (replace with real public key in deployment)
    private static final String EMBEDDED_PUBLIC_KEY = 
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlKP0EIsGHPLcrMOVZoBoc8gDruZ8Yn1crQCsji8c2x6ItSG3zmMT4RulObh1oTapnt8sk19QLl4Mbs7q/MlLOZqV1aI5xddJyZ0zHJRpH8PjoUlpTNJEz9pFclZIOWSfkT8DYEF6eX3uJL5105KYdoKLf5/Tdvjqe0nD89OWRLHvGH9UHF9CAAG+tmWuqaiyIRPSzRp9rlWpxYQVvi90cbPXzrEGBnhTKvLGGN3jczhXlafQZ3QQqp2OoqairhPsVKK+v6zu41m+QqrhoyAqp0Qlk+5teLMiGi5PvpSU1vUYWEHA6wI2INcUcR9UKMdBMJ0AQ9Hgyb72CdZeu19VJQIDAQAB";
    
    private static final String LICENSE_FILE = "license.properties";
    private static final String ACTIVATION_TIME_KEY = "activation.time";
    private static final String LICENSE_KEY_KEY = "license.key";
    private static final String LICENSE_LEVEL_KEY = "license.level";
    
    private static LicenseManager instance;
    private LicenseKeyData currentLicenseKey;
    private LocalDateTime activationTime;
    private final Path licenseFilePath;
    
    private LicenseManager() {
        this.licenseFilePath = Paths.get(System.getProperty("user.home"), ".shimeji", LICENSE_FILE);
        loadLicenseFromFile();
    }
    
    /**
     * 获取单例实例
     * Get singleton instance
     */
    public static synchronized LicenseManager getInstance() {
        if (instance == null) {
            instance = new LicenseManager();
        }
        return instance;
    }
    
    /**
     * 验证许可证密钥
     * Validate license key
     */
    public boolean validateKey(String keyString) {
        try {
            LicenseKeyData keyData = parseKey(keyString);
            if (keyData == null) {
                logger.warning("Failed to parse license key");
                return false;
            }
            
            // 验证签名
            PublicKey publicKey = CryptoUtils.stringToPublicKey(EMBEDDED_PUBLIC_KEY);
            String rawData = keyData.getRawDataForSignature();
            
            if (!CryptoUtils.verifySignature(rawData, keyData.getSignature(), publicKey)) {
                logger.warning("License key signature verification failed");
                return false;
            }
            
            // 检查是否过期
            if (keyData.isExpired()) {
                logger.warning("License key has expired");
                return false;
            }
            
            // 保存激活信息
            this.currentLicenseKey = keyData;
            this.activationTime = LocalDateTime.now();
            saveLicenseToFile(keyString, keyData.getLevel());
            
            logger.info("License key validated successfully: " + keyData.toString());
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error validating license key", e);
            return false;
        }
    }
    
    /**
     * 获取当前授权级别
     * Get current license level
     */
    public LicenseLevel getCurrentLicenseLevel() {
        if (currentLicenseKey == null) {
            return LicenseLevel.NO_KEY;
        }
        
        if (currentLicenseKey.isExpired()) {
            // 密钥已过期，清除激活状态
            clearActivation();
            return LicenseLevel.NO_KEY;
        }
        
        return currentLicenseKey.getLevel();
    }
    
    /**
     * 获取剩余天数
     * Get remaining days
     */
    public long getDaysRemaining() {
        if (currentLicenseKey == null || currentLicenseKey.isExpired()) {
            return 0;
        }
        return currentLicenseKey.getRemainingDays();
    }
    
    /**
     * 获取剩余小时数
     * Get remaining hours
     */
    public long getHoursRemaining() {
        if (currentLicenseKey == null || currentLicenseKey.isExpired()) {
            return 0;
        }
        return currentLicenseKey.getRemainingHours();
    }
    
    /**
     * 获取许可证状态信息
     * Get license status information
     */
    public String getLicenseStatusInfo() {
        LicenseLevel level = getCurrentLicenseLevel();
        
        if (level == LicenseLevel.NO_KEY) {
            return "Free Version - No active license";
        }
        
        long daysRemaining = getDaysRemaining();
        long hoursRemaining = getHoursRemaining();
        
        if (daysRemaining > 0) {
            return String.format("%s - %d days remaining", level.getDisplayName(), daysRemaining);
        } else if (hoursRemaining > 0) {
            return String.format("%s - %d hours remaining", level.getDisplayName(), hoursRemaining);
        } else {
            return String.format("%s - Expires soon", level.getDisplayName());
        }
    }
    
    /**
     * 检查是否有特定功能的权限
     * Check if has permission for specific feature
     */
    public boolean hasPermission(LicenseLevel requiredLevel) {
        return getCurrentLicenseLevel().hasPermission(requiredLevel);
    }
    
    /**
     * 清除激活状态
     * Clear activation status
     */
    public void clearActivation() {
        this.currentLicenseKey = null;
        this.activationTime = null;
        
        try {
            Files.deleteIfExists(licenseFilePath);
            logger.info("License activation cleared");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to delete license file", e);
        }
    }
    
    /**
     * 解析密钥字符串
     * Parse key string
     */
    private LicenseKeyData parseKey(String keyString) {
        try {
            // 密钥格式: VERSION|LEVEL|VALIDITY_DAYS|ISSUE_TIME|KEY_ID|SIGNATURE
            String decoded = new String(Base64.getDecoder().decode(keyString));
            String[] parts = decoded.split("\\|");
            
            if (parts.length != 6) {
                logger.warning("Invalid key format: incorrect number of parts");
                return null;
            }
            
            String version = parts[0];
            if (!LicenseKeyData.KEY_VERSION.equals(version)) {
                logger.warning("Unsupported key version: " + version);
                return null;
            }
            
            LicenseLevel level = LicenseLevel.fromCode(parts[1]);
            int validityDays = Integer.parseInt(parts[2]);
            LocalDateTime issueTime = LocalDateTime.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String keyId = parts[4];
            String signature = parts[5];
            
            return new LicenseKeyData(level, validityDays, issueTime, signature, keyId);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse key string", e);
            return null;
        }
    }
    
    /**
     * 从文件加载许可证信息
     * Load license information from file
     */
    private void loadLicenseFromFile() {
        try {
            if (!Files.exists(licenseFilePath)) {
                return;
            }
            
            Properties props = new Properties();
            try (InputStream input = Files.newInputStream(licenseFilePath)) {
                props.load(input);
            }
            
            String keyString = props.getProperty(LICENSE_KEY_KEY);
            String activationTimeStr = props.getProperty(ACTIVATION_TIME_KEY);
            
            if (keyString != null && activationTimeStr != null) {
                LicenseKeyData keyData = parseKey(keyString);
                if (keyData != null && !keyData.isExpired()) {
                    this.currentLicenseKey = keyData;
                    this.activationTime = LocalDateTime.parse(activationTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    logger.info("License loaded from file: " + keyData.toString());
                } else {
                    // 密钥无效或已过期，删除文件
                    Files.deleteIfExists(licenseFilePath);
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load license from file", e);
        }
    }
    
    /**
     * 保存许可证信息到文件
     * Save license information to file
     */
    private void saveLicenseToFile(String keyString, LicenseLevel level) {
        try {
            // 确保目录存在
            Files.createDirectories(licenseFilePath.getParent());
            
            Properties props = new Properties();
            props.setProperty(LICENSE_KEY_KEY, keyString);
            props.setProperty(LICENSE_LEVEL_KEY, level.getCode());
            props.setProperty(ACTIVATION_TIME_KEY, activationTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            try (OutputStream output = Files.newOutputStream(licenseFilePath)) {
                props.store(output, "Shimeji-Live License Information");
            }
            
            logger.info("License information saved to file");
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to save license to file", e);
        }
    }
}
