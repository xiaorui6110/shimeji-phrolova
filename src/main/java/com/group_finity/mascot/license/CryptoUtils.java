package com.group_finity.mascot.license;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * 加密工具类
 * Cryptographic utilities for license key generation and verification
 */
public class CryptoUtils {
    
    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;
    
    /**
     * 生成RSA密钥对
     * Generate RSA key pair
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        return keyGen.generateKeyPair();
    }
    
    /**
     * 将公钥转换为Base64字符串
     * Convert public key to Base64 string
     */
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * 将私钥转换为Base64字符串
     * Convert private key to Base64 string
     */
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }
    
    /**
     * 从Base64字符串恢复公钥
     * Restore public key from Base64 string
     */
    public static PublicKey stringToPublicKey(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(spec);
    }
    
    /**
     * 从Base64字符串恢复私钥
     * Restore private key from Base64 string
     */
    public static PrivateKey stringToPrivateKey(String keyString) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }
    
    /**
     * 使用私钥对数据进行签名
     * Sign data using private key
     */
    public static String signData(String data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }
    
    /**
     * 使用公钥验证签名
     * Verify signature using public key
     */
    public static boolean verifySignature(String data, String signatureString, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureString);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 生成随机密钥ID
     * Generate random key ID
     */
    public static String generateKeyId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, 11);
    }
    
    /**
     * 计算字符串的SHA-256哈希值
     * Calculate SHA-256 hash of string
     */
    public static String sha256Hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
