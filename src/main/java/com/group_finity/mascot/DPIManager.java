package com.group_finity.mascot;

import java.awt.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * DPI 管理器 - 自动检测和设置合适的 DPI 缩放
 * 
 * @author DCShimeji Team
 */
public class DPIManager {
    private static final Logger log = Logger.getLogger(DPIManager.class.getName());
    
    // DPI 预设值
    private static final int DEFAULT_DPI = 96;
    private static final int[] VALID_DPI_VALUES = {96, 120, 144, 168, 192, 216, 240, 288, 336, 384};
    
    /**
     * 自动检测并设置最佳 MenuDPI
     */
    public static void autoConfigureDPI(Properties properties) {
        try {
            DisplayInfo displayInfo = getDisplayInfo();
            int recommendedDPI = calculateOptimalDPI(displayInfo);
            
            // 检查是否需要更新 DPI 设置
            String currentDPIStr = properties.getProperty("MenuDPI", "96");
            int currentDPI = Integer.parseInt(currentDPIStr);
            
            if (shouldUpdateDPI(currentDPI, recommendedDPI)) {
                properties.setProperty("MenuDPI", String.valueOf(recommendedDPI));
                
                log.info(String.format("DPI 自动配置: %d -> %d (显示器: %dx%d@%ddpi, 缩放: %.0f%%)", 
                        currentDPI, recommendedDPI, 
                        displayInfo.width, displayInfo.height, displayInfo.dpi, 
                        displayInfo.scaleFactor * 100));
            } else {
                log.info(String.format("DPI 保持不变: %d (显示器: %dx%d@%ddpi, 缩放: %.0f%%)", 
                        currentDPI, 
                        displayInfo.width, displayInfo.height, displayInfo.dpi, 
                        displayInfo.scaleFactor * 100));
            }
            
        } catch (Exception e) {
            log.warning("DPI 自动配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取显示器信息
     */
    private static DisplayInfo getDisplayInfo() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        
        // 获取系统 DPI
        int systemDPI = Toolkit.getDefaultToolkit().getScreenResolution();
        
        // 获取系统缩放因子（Windows 10/11）
        double scaleFactor = getSystemScaleFactor();
        
        return new DisplayInfo(dm.getWidth(), dm.getHeight(), systemDPI, scaleFactor);
    }
    
    /**
     * 获取系统缩放因子
     */
    private static double getSystemScaleFactor() {
        try {
            // macOS Retina 显示器特殊处理
            if (isMacOS()) {
                return getMacOSScaleFactor();
            }
            
            // 尝试获取 Windows 系统缩放
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            
            // Java 9+ 方法
            if (gc.getDefaultTransform() != null) {
                return gc.getDefaultTransform().getScaleX();
            }
        } catch (Exception e) {
            // 忽略错误，使用备用方法
        }
        
        // 备用方法：基于 DPI 计算
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return dpi / 96.0;
    }

    /**
     * macOS Retina 显示器特殊处理
     */
    private static double getMacOSScaleFactor() {
        try {
            // 获取 macOS 的实际缩放因子
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            GraphicsConfiguration config = device.getDefaultConfiguration();
            java.awt.geom.AffineTransform transform = config.getDefaultTransform();
            
            double scaleFactor = Math.max(transform.getScaleX(), transform.getScaleY());
            log.info("macOS scale factor detected: " + scaleFactor);
            return scaleFactor;
        } catch (Exception e) {
            // 备用方法：检查系统属性
            String retinaFactor = System.getProperty("apple.awt.contentScaleFactor");
            if (retinaFactor != null) {
                try {
                    double factor = Double.parseDouble(retinaFactor);
                    log.info("macOS scale factor from system property: " + factor);
                    return factor;
                } catch (NumberFormatException nfe) {
                    log.warning("Invalid apple.awt.contentScaleFactor value: " + retinaFactor);
                }
            }
            
            // 最后的备用方法：检查屏幕分辨率
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
            
            // 典型的 Retina 显示器特征：高分辨率 + 高 DPI
            if (screenSize.width >= 2560 && screenRes >= 144) {
                log.info("Retina display detected by resolution, using 2.0 scale factor");
                return 2.0;
            } else if (screenSize.width >= 1920 && screenRes >= 120) {
                log.info("High DPI display detected, using 1.5 scale factor");
                return 1.5;
            }
            
            log.warning("Failed to detect macOS scale factor, using default: " + e.getMessage());
            return 1.0;
        }
    }
    
    /**
     * 计算最佳 DPI
     */
    private static int calculateOptimalDPI(DisplayInfo info) {
        int baseDPI = DEFAULT_DPI;
        
        // 根据分辨率类别确定基础 DPI
        if (info.width >= 3840 && info.height >= 2160) {
            // 4K+ 显示器
            baseDPI = 192;
        } else if (info.width >= 2560 && info.height >= 1440) {
            // 1440p 显示器
            baseDPI = 144;
        } else if (info.width >= 1920 && info.height >= 1080) {
            // 1080p 显示器
            if (info.dpi > 96 || info.scaleFactor > 1.0) {
                baseDPI = 120;
            } else {
                baseDPI = 96;
            }
        }
        
        // 根据系统缩放因子调整
        int scaledDPI = (int) (baseDPI * info.scaleFactor);
        
        // 取系统 DPI 和计算值的较大者
        int finalDPI = Math.max(scaledDPI, info.dpi);
        
        // 确保 DPI 值在有效范围内
        return findNearestValidDPI(finalDPI);
    }
    
    /**
     * 查找最接近的有效 DPI 值
     */
    private static int findNearestValidDPI(int targetDPI) {
        int nearest = VALID_DPI_VALUES[0];
        int minDiff = Math.abs(targetDPI - nearest);
        
        for (int validDPI : VALID_DPI_VALUES) {
            int diff = Math.abs(targetDPI - validDPI);
            if (diff < minDiff) {
                minDiff = diff;
                nearest = validDPI;
            }
        }
        
        return nearest;
    }
    
    /**
     * 判断是否需要更新 DPI
     */
    private static boolean shouldUpdateDPI(int currentDPI, int recommendedDPI) {
        // 如果当前是默认值，且推荐值不同，总是更新
        if (currentDPI == DEFAULT_DPI && recommendedDPI != DEFAULT_DPI) {
            return true;
        }
        
        // 如果推荐值与当前值差异较大（超过 30%），自动更新
        double difference = Math.abs(currentDPI - recommendedDPI) / (double) currentDPI;
        return difference > 0.30;
    }
    
    /**
     * 显示器信息数据类
     */
    private static class DisplayInfo {
        final int width;
        final int height;
        final int dpi;
        final double scaleFactor;
        
        DisplayInfo(int width, int height, int dpi, double scaleFactor) {
            this.width = width;
            this.height = height;
            this.dpi = dpi;
            this.scaleFactor = scaleFactor;
        }
    }
    
    /**
     * 获取 DPI 缩放建议文本
     */
    public static String getDPIRecommendationText(Properties properties) {
        try {
            DisplayInfo info = getDisplayInfo();
            int currentDPI = Integer.parseInt(properties.getProperty("MenuDPI", "96"));
            int recommendedDPI = calculateOptimalDPI(info);
            
            if (currentDPI == recommendedDPI) {
                return "当前 DPI 设置最佳";
            } else {
                return String.format("建议 DPI: %d (当前: %d)", recommendedDPI, currentDPI);
            }
        } catch (Exception e) {
            return "DPI 检测失败";
        }
    }
    
    /**
     * 强制更新 DPI 设置（用于初次启动或重置）
     */
    public static void forceUpdateDPI(Properties properties) {
        try {
            DisplayInfo displayInfo = getDisplayInfo();
            int recommendedDPI = calculateOptimalDPI(displayInfo);
            
            properties.setProperty("MenuDPI", String.valueOf(recommendedDPI));
            
            log.info(String.format("DPI 强制更新: %d (显示器: %dx%d@%ddpi, 缩放: %.0f%%)", 
                    recommendedDPI, 
                    displayInfo.width, displayInfo.height, displayInfo.dpi, 
                    displayInfo.scaleFactor * 100));
            
        } catch (Exception e) {
            log.warning("DPI 强制更新失败: " + e.getMessage());
        }
    }

    /**
     * 检查当前操作系统是否为 macOS
     */
    private static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    
    /**
     * 获取当前系统的 UI 缩放因子
     * @return UI 缩放因子，用于调整组件大小
     */
    public static double getUIScaleFactor() {
        try {
            DisplayInfo info = getDisplayInfo();
            return Math.max(1.0, info.scaleFactor);
        } catch (Exception e) {
            log.warning("获取 UI 缩放因子失败: " + e.getMessage());
            return 1.0;
        }
    }
    
    /**
     * 根据缩放因子调整宽度
     * @param originalWidth 原始宽度
     * @return 调整后的宽度
     */
    public static int scaleWidth(int originalWidth) {
        double scaleFactor = getUIScaleFactor();
        return (int) (originalWidth * scaleFactor);
    }
    
    /**
     * 根据缩放因子调整高度
     * @param originalHeight 原始高度
     * @return 调整后的高度
     */
    public static int scaleHeight(int originalHeight) {
        double scaleFactor = getUIScaleFactor();
        return (int) (originalHeight * scaleFactor);
    }
    
    /**
     * 根据缩放因子调整字体大小
     * @param originalSize 原始字体大小
     * @return 调整后的字体大小
     */
    public static int scaleFontSize(int originalSize) {
        double scaleFactor = getUIScaleFactor();
        return Math.max(8, (int) (originalSize * scaleFactor));
    }
}
