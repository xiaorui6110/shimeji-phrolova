package com.group_finity.mascot.mac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * macOS auto-start manager - uses Launch Agent method.
 */
public class AutoStartManager {
    private static final Logger log = Logger.getLogger(AutoStartManager.class.getName());
    
    private static final String PLIST_NAME = "com.group_finity.shimeji.plist";
    private static final String APP_NAME = "Shimeji-ee";
    
    /**
     * 获取Launch Agents文件夹路径
     */
    private static String getLaunchAgentsPath() {
        try {
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                String launchAgentsPath = userHome + "/Library/LaunchAgents";
                File launchAgentsDir = new File(launchAgentsPath);
                if (launchAgentsDir.exists() || launchAgentsDir.mkdirs()) {
                    return launchAgentsPath;
                }
            }
            
            log.warning("Could not determine Launch Agents folder path");
            return null;
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error getting Launch Agents folder path", e);
            return null;
        }
    }
    
    /**
     * 检查是否已设置开机自启动
     */
    public static boolean isAutoStartEnabled() {
        try {
            String launchAgentsPath = getLaunchAgentsPath();
            if (launchAgentsPath == null) {
                return false;
            }
            
            Path plistPath = Paths.get(launchAgentsPath, PLIST_NAME);
            boolean exists = Files.exists(plistPath);
            
            log.info("Checking auto-start: " + plistPath + " exists: " + exists);
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
            String launchAgentsPath = getLaunchAgentsPath();
            if (launchAgentsPath == null) {
                log.warning("Could not determine Launch Agents folder for auto-start");
                return false;
            }
            
            Path plistPath = Paths.get(launchAgentsPath, PLIST_NAME);
            
            if (enable) {
                // 创建Launch Agent plist文件
                String executablePath = getExecutablePath();
                if (executablePath == null) {
                    log.warning("Could not determine executable path for auto-start");
                    return false;
                }
                
                boolean success = createLaunchAgent(executablePath, plistPath.toString());
                if (success) {
                    // 加载Launch Agent
                    boolean loaded = loadLaunchAgent(plistPath.toString());
                    if (loaded) {
                        log.info("Auto-start enabled: " + plistPath);
                        return true;
                    } else {
                        log.warning("Failed to load Launch Agent");
                        return false;
                    }
                } else {
                    log.warning("Failed to create Launch Agent for auto-start");
                    return false;
                }
                
            } else {
                // 卸载并删除Launch Agent
                if (Files.exists(plistPath)) {
                    unloadLaunchAgent(plistPath.toString());
                    Files.delete(plistPath);
                    log.info("Auto-start disabled: " + plistPath);
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
            // 优先查找 .app 包
            String currentDir = System.getProperty("user.dir");
            
            // 检查当前目录下的 .app
            File appFile = new File(currentDir, APP_NAME + ".app");
            if (appFile.exists() && appFile.isDirectory()) {
                return appFile.getAbsolutePath();
            }
            
            // 检查上级目录的 .app
            File parentDir = new File(currentDir).getParentFile();
            if (parentDir != null) {
                File parentAppFile = new File(parentDir, APP_NAME + ".app");
                if (parentAppFile.exists() && parentAppFile.isDirectory()) {
                    return parentAppFile.getAbsolutePath();
                }
            }
            
            // 检查是否在 .app 包内运行
            if (currentDir.contains(".app/Contents")) {
                String appPath = currentDir.substring(0, currentDir.indexOf(".app/Contents") + 4);
                File appBundle = new File(appPath);
                if (appBundle.exists()) {
                    return appBundle.getAbsolutePath();
                }
            }
            
            // 回退到jar文件路径
            String classPath = System.getProperty("java.class.path");
            if (classPath != null && classPath.endsWith(".jar")) {
                File jarFile = new File(classPath);
                if (jarFile.exists()) {
                    return jarFile.getAbsolutePath();
                }
            }
            
            log.warning("Could not find executable for auto-start");
            return null;
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error determining executable path", e);
            return null;
        }
    }
    
    /**
     * 创建Launch Agent plist文件
     */
    private static boolean createLaunchAgent(String executablePath, String plistPath) {
        try {
            String plistContent;
            
            if (executablePath.endsWith(".app")) {
                // 如果是 .app 包，使用 open 命令启动
                plistContent = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                    "<plist version=\"1.0\">\n" +
                    "<dict>\n" +
                    "    <key>Label</key>\n" +
                    "    <string>com.group_finity.shimeji</string>\n" +
                    "    <key>ProgramArguments</key>\n" +
                    "    <array>\n" +
                    "        <string>/usr/bin/open</string>\n" +
                    "        <string>%s</string>\n" +
                    "    </array>\n" +
                    "    <key>RunAtLoad</key>\n" +
                    "    <true/>\n" +
                    "    <key>KeepAlive</key>\n" +
                    "    <false/>\n" +
                    "    <key>LaunchOnlyOnce</key>\n" +
                    "    <true/>\n" +
                    "</dict>\n" +
                    "</plist>",
                    executablePath
                );
            } else {
                // 如果是jar文件，使用java命令启动
                plistContent = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                    "<plist version=\"1.0\">\n" +
                    "<dict>\n" +
                    "    <key>Label</key>\n" +
                    "    <string>com.group_finity.shimeji</string>\n" +
                    "    <key>ProgramArguments</key>\n" +
                    "    <array>\n" +
                    "        <string>/usr/bin/java</string>\n" +
                    "        <string>-jar</string>\n" +
                    "        <string>%s</string>\n" +
                    "    </array>\n" +
                    "    <key>WorkingDirectory</key>\n" +
                    "    <string>%s</string>\n" +
                    "    <key>RunAtLoad</key>\n" +
                    "    <true/>\n" +
                    "    <key>KeepAlive</key>\n" +
                    "    <false/>\n" +
                    "    <key>LaunchOnlyOnce</key>\n" +
                    "    <true/>\n" +
                    "</dict>\n" +
                    "</plist>",
                    executablePath,
                    new File(executablePath).getParent()
                );
            }
            
            Files.write(Paths.get(plistPath), plistContent.getBytes());
            log.info("Launch Agent plist created: " + plistPath);
            return true;
            
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to create Launch Agent plist", e);
            return false;
        }
    }
    
    /**
     * 加载Launch Agent
     */
    private static boolean loadLaunchAgent(String plistPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("launchctl", "load", plistPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Launch Agent loaded successfully: " + plistPath);
                return true;
            } else {
                log.warning("Failed to load Launch Agent, exit code: " + exitCode);
                return false;
            }
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to load Launch Agent", e);
            return false;
        }
    }
    
    /**
     * 卸载Launch Agent
     */
    private static boolean unloadLaunchAgent(String plistPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("launchctl", "unload", plistPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Launch Agent unloaded successfully: " + plistPath);
                return true;
            } else {
                log.warning("Failed to unload Launch Agent, exit code: " + exitCode);
                return false;
            }
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to unload Launch Agent", e);
            return false;
        }
    }
}
