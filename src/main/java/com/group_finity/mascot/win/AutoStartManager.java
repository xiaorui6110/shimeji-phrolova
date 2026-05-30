package com.group_finity.mascot.win;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Windows auto-start manager - uses shortcut method.
 */
public class AutoStartManager {
    private static final Logger log = Logger.getLogger(AutoStartManager.class.getName());
    
    private static final String SHORTCUT_NAME = "Shimeji-ee.lnk";
    
    /**
     * 获取启动文件夹路径
     */
    private static String getStartupFolderPath() {
        try {
            // 使用环境变量获取用户启动文件夹路径
            String userProfile = System.getenv("USERPROFILE");
            if (userProfile != null) {
                String startupPath = userProfile + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
                File startupDir = new File(startupPath);
                if (startupDir.exists() || startupDir.mkdirs()) {
                    return startupPath;
                }
            }
            
            // 备用方案：使用 APPDATA 环境变量
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                String startupPath = appData + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
                File startupDir = new File(startupPath);
                if (startupDir.exists() || startupDir.mkdirs()) {
                    return startupPath;
                }
            }
            
            log.warning("Could not determine startup folder path");
            return null;
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error getting startup folder path", e);
            return null;
        }
    }
    
    /**
     * 检查是否已设置开机自启动
     */
    public static boolean isAutoStartEnabled() {
        try {
            String startupPath = getStartupFolderPath();
            if (startupPath == null) {
                return false;
            }
            
            Path shortcutPath = Paths.get(startupPath, SHORTCUT_NAME);
            Path batPath = Paths.get(startupPath, "Shimeji-ee.bat");
            boolean exists = Files.exists(shortcutPath) || Files.exists(batPath);
            
            log.info("Checking auto-start: " + shortcutPath + " exists: " + exists);
            return exists;
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to check auto-start status", e);
            return false;
        }
    }
    
    /**
     * 设置开机自启动
     */
    public static boolean setAutoStart(boolean enable) {
        try {
            String startupPath = getStartupFolderPath();
            if (startupPath == null) {
                log.warning("Could not determine startup folder for auto-start");
                return false;
            }
            
            Path shortcutPath = Paths.get(startupPath, SHORTCUT_NAME);
            Path batPath = Paths.get(startupPath, "Shimeji-ee.bat");
            
            if (enable) {
                // 创建快捷方式
                String exePath = getExecutablePath();
                if (exePath == null) {
                    log.warning("Could not determine executable path for auto-start");
                    return false;
                }
                
                boolean success = createShortcut(exePath, shortcutPath.toString());
                if (success) {
                    log.info("Auto-start enabled: " + shortcutPath);
                } else {
                    log.warning("Failed to create shortcut for auto-start");
                }
                return success;
                
            } else {
                // 删除快捷方式和批处理文件
                if (Files.exists(shortcutPath)) {
                    Files.delete(shortcutPath);
                    log.info("Auto-start disabled: " + shortcutPath);
                }
                if (Files.exists(batPath)) {
                    Files.delete(batPath);
                    log.info("Auto-start disabled: " + batPath);
                }
                return true;
            }
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to set auto-start: " + enable, e);
            return false;
        }
    }
    
    /**
     * 获取当前可执行文件路径
     */
    private static String getExecutablePath() {
        try {
            // jpackage 生成的 exe 文件
            String currentDir = System.getProperty("user.dir");
            File exeFile = new File(currentDir, "Shimeji-ee.exe");
            
            if (exeFile.exists()) {
                return exeFile.getAbsolutePath();
            }
            
            // 如果找不到，尝试从 java.class.path 推断
            String classPath = System.getProperty("java.class.path");
            if (classPath != null && classPath.endsWith("Shimeji-ee.exe")) {
                return classPath;
            }
            
            log.warning("Could not find Shimeji-ee.exe for auto-start");
            return null;
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error determining executable path", e);
            return null;
        }
    }
    
    /**
     * 创建快捷方式
     * 使用 PowerShell 脚本创建快捷方式
     */
    private static boolean createShortcut(String targetPath, String shortcutPath) {
        try {
            // 使用 PowerShell 创建快捷方式的脚本
            String script = String.format(
                "$WshShell = New-Object -comObject WScript.Shell; " +
                "$Shortcut = $WshShell.CreateShortcut('%s'); " +
                "$Shortcut.TargetPath = '%s'; " +
                "$Shortcut.WorkingDirectory = '%s'; " +
                "$Shortcut.Description = 'Shimeji-ee Desktop Mascot'; " +
                "$Shortcut.Save()",
                shortcutPath.replace("\\", "\\\\"),
                targetPath.replace("\\", "\\\\"),
                new File(targetPath).getParent().replace("\\", "\\\\")
            );
            
            // 执行 PowerShell 命令
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe", 
                "-ExecutionPolicy", "Bypass",
                "-Command", script
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Shortcut created successfully: " + shortcutPath);
                return true;
            } else {
                log.warning("PowerShell script failed with exit code: " + exitCode);
                
                // 备用方案：使用批处理文件
                return createShortcutFallback(targetPath, shortcutPath);
            }
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to create shortcut with PowerShell", e);
            // 备用方案：使用批处理文件
            return createShortcutFallback(targetPath, shortcutPath);
        }
    }
    
    /**
     * 备用方案：创建批处理文件代替快捷方式
     */
    private static boolean createShortcutFallback(String targetPath, String shortcutPath) {
        try {
            // 创建批处理文件 (.bat) 代替快捷方式
            String batPath = shortcutPath.replace(".lnk", ".bat");
            String batContent = String.format(
                "@echo off\n" +
                "cd /d \"%s\"\n" +
                "start \"\" \"%s\"\n",
                new File(targetPath).getParent(),
                targetPath
            );
            
            Files.write(Paths.get(batPath), batContent.getBytes());
            log.info("Fallback batch file created: " + batPath);
            return true;
            
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to create fallback batch file", e);
            return false;
        }
    }
}
