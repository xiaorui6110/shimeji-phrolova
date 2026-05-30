package com.group_finity.mascot;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairs;
import com.group_finity.mascot.imagesetchooser.ImageSetChooser;
import com.group_finity.mascot.license.LicenseManager;
import com.group_finity.mascot.license.LicenseChecker;
import com.group_finity.mascot.license.LicenseLevel;
import com.group_finity.mascot.license.LicenseActivationDialog;
import com.group_finity.mascot.sound.Sounds;
import com.joconner.i18n.Utf8ResourceBundleControl;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.UIManager;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * 程序主入口。
 * <p>
 * 最初由 Group Finity 的 Yuki Yamada (<a href="http://www.group-finity.com/Shimeji/">...</a>) 开发。
 * 目前由 Begonia 二次开发。
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());
    
    public static final Path CONFIG_DIRECTORY = Paths.get("conf");
    public static final Path IMAGE_DIRECTORY = Paths.get("img");
    public static final Path SOUND_DIRECTORY = Paths.get("sound");
    public static final Path SETTINGS_FILE = CONFIG_DIRECTORY.resolve("settings.properties");
    public static final Path LOGGING_FILE = CONFIG_DIRECTORY.resolve("logging.properties");
    public static final Path ICON_FILE = IMAGE_DIRECTORY.resolve("icon.png");
    
    // Action that matches the "Gather Around Mouse!" context menu command
    static final String BEHAVIOR_GATHER = "ChaseMouse";

    static {
        try {
            // 获取 系统信息
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");
            System.out.println("当前操作系统: " + osName + " " + osVersion + " " + osArch);

            // 获取 Java 版本信息
            String javaVersion = System.getProperty("java.version");
            System.out.println("当前 Java 版本: " + javaVersion);

            // Force enable D3D hardware acceleration pipeline to improve 2D rendering performance on Windows
            System.setProperty("sun.java2d.d3d", "true");

            // 核心 DPI 设置 - 强制使用 Java 8 行为
            System.setProperty("sun.java2d.dpiaware", "false");
            System.setProperty("sun.java2d.uiScale", "1.0");
            System.setProperty("sun.java2d.uiScale.enabled", "false");

            // Windows 特定的 DPI 设置（Java 9+）
            System.setProperty("sun.java2d.win.uiScaleX", "1.0");
            System.setProperty("sun.java2d.win.uiScaleY", "1.0");
            System.setProperty("sun.java2d.win.dpiAwareness", "false");

            // Java 11+ 额外设置
            System.setProperty("sun.java2d.uiScale.enabled", "false");

            // Java 17+ 额外设置
            System.setProperty("sun.java2d.renderer", "sun.java2d.pipe.hw.AccelGraphicsConfig");

            System.out.println(" Java 9+ DPI 兼容模式已启用");
            System.out.println("  - DPI 感知: 已禁用");
            System.out.println("  - UI 缩放: 已固定为 1.0");
            System.out.println("  - 兼容模式: Java 8 行为");
        } catch (Exception e) {
            System.out.println(" 启用 Java 9+ DPI 兼容模式失败: " + e.getMessage());
            System.out.println("  - 请确保您使用的是 Oracle 或 OpenJDK 的 Java 9 或更高版本");
        }
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
        } catch (final SecurityException | IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError err) {
            log.log(Level.SEVERE, "Out of Memory Exception.  There are probably have too many "
                    + "Shimeji mascots in the image folder for your computer to handle.  Select fewer"
                    + " image sets or move some to the img/unused folder and try again.", err);
            Main.showError("""
                    Out of Memory.  There are probably have too many\s
                    Shimeji mascots for your computer to handle.
                    Select fewer image sets or move some to the\s
                    img/unused folder and try again.""");
            System.exit(0);
        }
    }
    private final Manager manager = new Manager();
    private ArrayList<String> imageSets = new ArrayList<>();
    private final ConcurrentHashMap<String, Configuration> configurations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ArrayList<String>> childImageSets = new ConcurrentHashMap<>();
    private static final Main instance = new Main();
    private Properties properties = new Properties();
    private Platform platform;
    private ResourceBundle languageBundle;

    private JDialog form;
    private TrayIcon trayIcon;

    /**
     * 设置FlatLaf主题
     */
    private void setupFlatLafTheme() {
        // 读取主题配置
        String themeName = properties.getProperty("FlatLafTheme", "light");

        // 设置基础FlatLaf属性
        System.setProperty("flatlaf.useWindowDecorations", "true");
        System.setProperty("flatlaf.menuBarEmbedded", "false");

        // 根据配置选择主题
        switch (themeName.toLowerCase()) {
            case "dark":
                FlatDarkLaf.setup();
                break;
            case "intellij":
                // IntelliJ风格浅色主题
                FlatLightFlatIJTheme.setup();
                break;
            case "cyan":
                // 青色浅色主题
                FlatCyanLightIJTheme.setup();
                break;
            case "solarized":
                // Solarized浅色主题
                FlatSolarizedLightIJTheme.setup();
                break;
            case "atom":
                // Atom One浅色主题 (替换为可用的Cyan主题)
                FlatCyanLightIJTheme.setup();
                break;
            case "owl":
                // Light Owl主题 (替换为可用的Solarized主题)
                FlatSolarizedLightIJTheme.setup();
                break;
            case "light":
            default:
                // 默认使用IntelliJ风格浅色主题
                FlatLightFlatIJTheme.setup();
                break;
        }

        // 处理DPI缩放设置
        float menuScaling = Float.parseFloat(properties.getProperty("MenuDPI", "96")) / 96;

        // 自定义FlatLaf主题颜色（保持与原有主题的兼容性）
        applyCustomThemeColors();

        // 设置字体缩放
        if (menuScaling != 1.0f) {
            UIManager.put("defaultFont", UIManager.getFont("Label.font").deriveFont(
                    UIManager.getFont("Label.font").getSize() * menuScaling));
        }

        // 启用窗口装饰
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // 刷新UI以应用更改
        FlatLaf.updateUI();
    }

    /**
     * 应用自定义主题颜色
     */
    private static void applyCustomThemeColors() {
        // 获取应用程序基础目录
        String baseDir = System.getProperty("app.dir");
        if (baseDir == null) {
            baseDir = System.getProperty("user.dir");
        }
        if (baseDir == null) {
            baseDir = ".";
        }
        
        // 检查是否有自定义主题文件
        File themeFile = new File(baseDir + "/conf/theme.properties");
        if (themeFile.exists()) {
            try {
                Properties themeProps = new Properties();
                themeProps.load(new FileInputStream(themeFile));

                // 将主题属性应用到FlatLaf
                if (themeProps.containsKey("PrimaryColour1")) {
                    UIManager.put("@accentColor", Color.decode(themeProps.getProperty("PrimaryColour1", "#1EA6EB")));
                }
                if (themeProps.containsKey("PrimaryColour2")) {
                    UIManager.put("Button.background",
                            Color.decode(themeProps.getProperty("PrimaryColour2", "#28B0F5")));
                }
                if (themeProps.containsKey("PrimaryColour3")) {
                    UIManager.put("Button.hoverBackground",
                            Color.decode(themeProps.getProperty("PrimaryColour3", "#32BAFF")));
                }

            } catch (Exception e) {
                // 如果读取失败，使用默认颜色
                log.log(Level.WARNING, "Could not load custom theme colors, using defaults", e);
                applyDefaultThemeColors();
            }
        } else {
            applyDefaultThemeColors();
        }
    }

    /**
     * 应用默认主题颜色
     */
    private static void applyDefaultThemeColors() {
        UIManager.put("@accentColor", Color.decode("#1EA6EB"));
        UIManager.put("Button.background", Color.decode("#28B0F5"));
        UIManager.put("Button.hoverBackground", Color.decode("#32BAFF"));
    }

    /**
     * 获取 Main 类的单例实例。
     * 
     * @return Main 类的实例。
     */
    public static Main getInstance() {
        return instance;
    }

    private static final JFrame frame = new javax.swing.JFrame();

    /**
     * 显示一个错误消息对话框。
     * 
     * @param message 要显示的消息。
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 全局配置高DPI支持
     */
    private static void configureHighDPIGlobally() {
        // 检测操作系统
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("mac")) {
            // macOS 特定设置
            System.setProperty("apple.awt.application-appearance", "system");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application-name", "Shimeji-ee");
            
            // 启用 Metal 渲染管道（macOS 10.14+）
            System.setProperty("sun.java2d.metal", "true");
            System.setProperty("sun.java2d.opengl", "false");
            
            // 启用高DPI感知
            System.setProperty("sun.java2d.dpiaware", "true");
            System.setProperty("sun.java2d.uiScale", "1.0");
            
            // macOS Retina 显示支持
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
            System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
            
            log.info("已配置 macOS 高DPI 支持");
        }
        
        // 通用高质量渲染设置
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // 启用 Marlin 渲染引擎（更好的2D渲染）
        System.setProperty("sun.java2d.renderer", "sun.java2d.marlin.MarlinRenderingEngine");
        System.setProperty("sun.java2d.renderer.useThreadLocal", "true");
        
        // 设置默认字体渲染
        System.setProperty("sun.java2d.renderer.useRef", "true");
        System.setProperty("sun.java2d.renderer.pixelsize", "2048");
        
        // 强制使用双缓冲
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.java2d.noddraw", "true");
    }

    /**
     * 程序主方法。
     * 
     * @param args 命令行参数。
     */
    public static void main(final String[] args) {
        // 在应用程序启动前配置高DPI支持
        configureHighDPIGlobally();
        
        try {
            getInstance().run();
        } catch (OutOfMemoryError err) {
            log.log(Level.SEVERE, "Out of Memory Exception.  There are probably have too many "
                    + "Shimeji mascots in the image folder for your computer to handle.  Select fewer"
                    + " image sets or move some to the img/unused folder and try again.", err);
            Main.showError("""
                    Out of Memory.  There are probably have too many\s
                    Shimeji mascots for your computer to handle.
                    Select fewer image sets or move some to the\s
                    img/unused folder and try again.""");
            System.exit(0);
        }
    }

    /**
     * 运行程序的主要逻辑。
     * 初始化、加载配置并启动桌宠管理器。
     */
    public void run() {

        // 检测操作系统
        if (!System.getProperty("sun.arch.data.model").equals("64"))
            platform = Platform.x86;
        else
            platform = Platform.x86_64;
        // load properties
        properties = new Properties();
        
        // 获取应用程序基础目录
        String baseDir = System.getProperty("app.dir");
        if (baseDir == null) {
            baseDir = System.getProperty("user.dir");
        }
        if (baseDir == null) {
            baseDir = ".";
        }
        
        try (FileInputStream input = new FileInputStream(baseDir + "/conf/settings.properties")) {
            properties.load(input);
        } catch (FileNotFoundException ex) {
            // File doesn't exist, continue with defaults
        } catch (IOException ex) {
            log.log(Level.WARNING, "Failed to load settings.properties", ex);
        }

        // 自动检测并配置 DPI
        DPIManager.autoConfigureDPI(properties);
        
        // 保存更新后的配置
        updateConfigFile();

        // load languages
        try {
            ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl(false);
            languageBundle = ResourceBundle.getBundle("language",
                    Locale.forLanguageTag(properties.getProperty("Language", "en-GB")), utf8Control);
        } catch (Exception ex) {
            Main.showError(
                    "The default language file could not be loaded. Ensure that you have the latest shimeji language.properties in your conf directory.");
            exit();
        }

        // load theme
        try {
            // 设置FlatLaf主题
            setupFlatLafTheme();
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex1) {
                log.log(Level.SEVERE, "Look & Feel unsupported.", ex1);
                exit();
            }
        }

        // Get the image sets to use
        if (!Boolean.parseBoolean(properties.getProperty("AlwaysShowShimejiChooser", "false"))) {
            // Use Stream API to process and filter image sets
            String activeShimeji = properties.getProperty("ActiveShimeji", "");
            if (!activeShimeji.isEmpty()) {
                java.util.Arrays.stream(activeShimeji.split("/"))
                        .map(String::trim)
                        .filter(set -> !set.isEmpty())
                        .forEach(imageSets::add);
            }
        }
        if (imageSets.isEmpty()) {
            imageSets = new ImageSetChooser(frame, true).display();
            if (imageSets == null) {
                exit();
            }
        }

        // Load shimejis - use removeIf for more efficient removal
        imageSets.removeIf(imageSet -> {
            if (!loadConfiguration(imageSet)) {
                // failed validation
                configurations.remove(imageSet);
                return true; // remove from imageSets
            }
            return false; // keep in imageSets
        });
        if (imageSets.isEmpty()) {
            exit();
        }

        // Create the tray icon
        createTrayIcon();

        // Initialize license system and show warning if needed
        LicenseManager licenseManager = LicenseManager.getInstance();
        log.info("License System Initialized - Current Level: " + licenseManager.getCurrentLicenseLevel().getDisplayName());
        
        // Show license expiration warning if needed
        LicenseChecker.showExpirationWarningIfNeeded();

        // Create the first mascot
        for (String imageSet : imageSets) {
            String informationAlreadySeen = properties.getProperty("InformationDismissed", "");
            if (configurations.get(imageSet).containsInformationKey("SplashImage") &&
                    (Boolean.parseBoolean(properties.getProperty("AlwaysShowInformationScreen", "false")) ||
                            !informationAlreadySeen.contains(imageSet))) {
                InformationWindow info = new InformationWindow();
                info.init(imageSet, configurations.get(imageSet));
                info.display();
                setMascotInformationDismissed(imageSet);
                updateConfigFile();
            }
            createMascot(imageSet);
        }

        getManager().start();
    }

    /**
     * 为指定的图像集加载配置文件（actions.xml 和 behaviors.xml）。
     * 
     * @param imageSet 要加载配置的图像集名称。
     * @return 如果加载成功，则返回 true；否则返回 false。
     */
    private boolean loadConfiguration(final String imageSet) {
        try {
            // 获取应用程序基础目录
            String baseDir = System.getProperty("app.dir");
            if (baseDir == null) {
                baseDir = System.getProperty("user.dir");
            }
            if (baseDir == null) {
                baseDir = ".";
            }
            
            // try to load in the correct xml files
            String filePath = baseDir + "/conf/";
            String actionsFile = filePath + "actions.xml";
            if (new File(filePath + "動作.xml").exists())
                actionsFile = filePath + "動作.xml";

            filePath = baseDir + "/conf/" + imageSet + "/";
            if (new File(filePath + "actions.xml").exists())
                actionsFile = filePath + "actions.xml";
            else if (new File(filePath + "動作.xml").exists())
                actionsFile = filePath + "動作.xml";
            else if (new File(filePath + "Õïòõ¢£.xml").exists())
                actionsFile = filePath + "Õïòõ¢£.xml";
            else if (new File(filePath + "¦-º@.xml").exists())
                actionsFile = filePath + "¦-º@.xml";
            else if (new File(filePath + "ô«ìý.xml").exists())
                actionsFile = filePath + "ô«ìý.xml";
            else if (new File(filePath + "one.xml").exists())
                actionsFile = filePath + "one.xml";
            else if (new File(filePath + "1.xml").exists())
                actionsFile = filePath + "1.xml";

            filePath = baseDir + "/img/" + imageSet + "/conf/";
            if (new File(filePath + "actions.xml").exists())
                actionsFile = filePath + "actions.xml";
            else if (new File(filePath + "動作.xml").exists())
                actionsFile = filePath + "動作.xml";
            else if (new File(filePath + "Õïòõ¢£.xml").exists())
                actionsFile = filePath + "Õïòõ¢£.xml";
            else if (new File(filePath + "¦-º@.xml").exists())
                actionsFile = filePath + "¦-º@.xml";
            else if (new File(filePath + "ô«ìý.xml").exists())
                actionsFile = filePath + "ô«ìý.xml";
            else if (new File(filePath + "one.xml").exists())
                actionsFile = filePath + "one.xml";
            else if (new File(filePath + "1.xml").exists())
                actionsFile = filePath + "1.xml";

            log.log(Level.INFO, imageSet + " Read Action File ({0})", actionsFile);

            final Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new FileInputStream(actionsFile));

            Configuration configuration = new Configuration();

            configuration.load(new Entry(actions.getDocumentElement()), imageSet);

            filePath = baseDir + "/conf/";
            String behaviorsFile = filePath + "behaviors.xml";
            if (new File(filePath + "行動.xml").exists())
                behaviorsFile = filePath + "行動.xml";

            filePath = baseDir + "/conf/" + imageSet + "/";
            if (new File(filePath + "behaviors.xml").exists())
                behaviorsFile = filePath + "behaviors.xml";
            else if (new File(filePath + "behavior.xml").exists())
                behaviorsFile = filePath + "behavior.xml";
            else if (new File(filePath + "行動.xml").exists())
                behaviorsFile = filePath + "行動.xml";
            else if (new File(filePath + "ÞíîÕïò.xml").exists())
                behaviorsFile = filePath + "ÞíîÕïò.xml";
            else if (new File(filePath + "ªµ¦-.xml").exists())
                behaviorsFile = filePath + "ªµ¦-.xml";
            else if (new File(filePath + "ìsô«.xml").exists())
                behaviorsFile = filePath + "ìsô«.xml";
            else if (new File(filePath + "two.xml").exists())
                behaviorsFile = filePath + "two.xml";
            else if (new File(filePath + "2.xml").exists())
                behaviorsFile = filePath + "2.xml";

            filePath = baseDir + "/img/" + imageSet + "/conf/";
            if (new File(filePath + "behaviors.xml").exists())
                behaviorsFile = filePath + "behaviors.xml";
            else if (new File(filePath + "behavior.xml").exists())
                behaviorsFile = filePath + "behavior.xml";
            else if (new File(filePath + "行動.xml").exists())
                behaviorsFile = filePath + "行動.xml";
            else if (new File(filePath + "ÞíîÕïò.xml").exists())
                behaviorsFile = filePath + "ÞíîÕïò.xml";
            else if (new File(filePath + "ªµ¦-.xml").exists())
                behaviorsFile = filePath + "ªµ¦-.xml";
            else if (new File(filePath + "ìsô«.xml").exists())
                behaviorsFile = filePath + "ìsô«.xml";
            else if (new File(filePath + "two.xml").exists())
                behaviorsFile = filePath + "two.xml";
            else if (new File(filePath + "2.xml").exists())
                behaviorsFile = filePath + "2.xml";

            log.log(Level.INFO, imageSet + " Read Behavior File ({0})", behaviorsFile);

            final Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new FileInputStream(behaviorsFile));

            configuration.load(new Entry(behaviors.getDocumentElement()), imageSet);

            filePath = baseDir + "/conf/";
            String infoFile = filePath + "info.xml";

            filePath = baseDir + "/conf/" + imageSet + "/";
            if (new File(filePath + "info.xml").exists())
                infoFile = filePath + "info.xml";

            filePath = baseDir + "/img/" + imageSet + "/conf/";
            if (new File(filePath + "info.xml").exists())
                infoFile = filePath + "info.xml";

            if (new File(infoFile).exists()) {
                log.log(Level.INFO, imageSet + " Read Information File ({0})", infoFile);

                final Document information = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new FileInputStream(infoFile));

                configuration.load(new Entry(information.getDocumentElement()), imageSet);
            }

            configuration.validate();

            configurations.put(imageSet, configuration);

            ArrayList<String> childMascots = new ArrayList<>();

            // born mascot bit goes here...
            for (final Entry list : new Entry(actions.getDocumentElement()).selectChildren("ActionList")) {
                for (final Entry node : list.selectChildren("Action")) {
                    // Handle BornMascot attribute
                    if (node.getAttributes().containsKey("BornMascot")) {
                        String set = node.getAttribute("BornMascot");
                        if (!childMascots.contains(set))
                            childMascots.add(set);
                        if (!configurations.containsKey(set))
                            loadConfiguration(set);
                    }
                    // Handle TransformMascot attribute
                    if (node.getAttributes().containsKey("TransformMascot")) {
                        String set = node.getAttribute("TransformMascot");
                        if (!childMascots.contains(set))
                            childMascots.add(set);
                        if (!configurations.containsKey(set))
                            loadConfiguration(set);
                    }
                }
            }

            childImageSets.put(imageSet, childMascots);

            return true;
        } catch (final SAXException | ConfigurationException | ParserConfigurationException e) {
            log.log(Level.SEVERE, "Failed to load configuration files", e);
            Main.showError(languageBundle.getString("FailedLoadConfigErrorMessage") + "\n" + e.getMessage() + "\n"
                    + languageBundle.getString("SeeLogForDetails"));
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Failed to load configuration files", e);
            Main.showError(languageBundle.getString("FailedLoadConfigErrorMessage") + "\n" + e.getMessage() + "\n"
                    + languageBundle.getString("SeeLogForDetails"));
        }

        return false;
    }

    /**
     * Load icon image from external file first, fallback to classpath resource
     */
    private static BufferedImage loadIconImage(final String iconPath) throws IOException {
        // 标准化路径：移除开头的 /，确保使用正确的路径分隔符
        String normalizedPath = iconPath.startsWith("/") ? iconPath.substring(1) : iconPath;
        normalizedPath = normalizedPath.replace('/', File.separatorChar);
        
        // 尝试从外部文件加载（用于MSI安装版本）
        String externalPath = "./img/" + normalizedPath;  // 相对于工作目录的 img 文件夹
        File externalFile = new File(externalPath);
        
        if (externalFile.exists()) {
            try (InputStream is = new FileInputStream(externalFile)) {
                return ImageIO.read(is);
            }
        }
        
        // 如果外部文件不存在，尝试从classpath加载（用于JAR内嵌资源）
        InputStream resourceStream = Main.class.getResourceAsStream(iconPath);
        if (resourceStream != null) {
            try (InputStream is = resourceStream) {
                return ImageIO.read(is);
            }
        }
        
        // 如果都失败了，返回null（让调用者处理）
        return null;
    }

    /**
     * 检查当前操作系统是否为 macOS
     */
    private static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * 检查当前环境是否为Wayland
     */
    private static boolean isWaylandEnvironment() {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        
        return (waylandDisplay != null && !waylandDisplay.isEmpty()) ||
               (sessionType != null && sessionType.equals("wayland"));
    }

    /**
     * 检查鼠标事件是否应该触发弹出菜单（包含 macOS 兼容性处理）
     */
    private static boolean shouldShowPopupMenu(MouseEvent event) {
        // 标准的弹出触发器检查
        if (event.isPopupTrigger()) {
            return true;
        }
        
        // macOS 特殊处理：右键点击或 Ctrl+左键点击
        if (isMacOS()) {
            return event.getButton() == MouseEvent.BUTTON3 || 
                   (event.getButton() == MouseEvent.BUTTON1 && event.isControlDown());
        }
        
        return false;
    }

    /**
     * 创建系统托盘图标。
     * 托盘图标提供了用于控制应用程序的菜单。
     *
     */
    private void createTrayIcon() {
        log.log(Level.INFO, "create a tray icon");

        // get the tray icon image
        BufferedImage image = null;
        try {
            image = loadIconImage("/icon.png");
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Failed to create tray icon", e);
            Main.showError(languageBundle.getString("FailedDisplaySystemTrayErrorMessage") + "\n"
                    + languageBundle.getString("SeeLogForDetails"));
        } finally {
            if (image == null)
                image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        }

        try {
            // Create the tray icon
            String caption = properties.getProperty("ShimejiEENameOverride", "").trim();
            if (caption.isEmpty())
                caption = languageBundle.getString("ShimejiEE");
            final TrayIcon icon = new TrayIcon(image, caption);

            // attach menu
            icon.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent event) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (shouldShowPopupMenu(event)) {
                        // close the form if it's open
                        if (form != null)
                            form.dispose();

                        // create the form and border
                        form = new JDialog(frame, false);
                        final JPanel panel = new JPanel();
                        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                        form.add(panel);

                        // buttons and action handling
                        JButton btnCallShimeji = new JButton(languageBundle.getString("CallShimeji"));
                        btnCallShimeji.addActionListener(e -> {
                            createMascot();
                            form.dispose();
                        });

                        JButton btnFollowCursor = new JButton(languageBundle.getString("FollowCursor"));
                        btnFollowCursor.addActionListener(e -> {
                            getManager().setBehaviorAll(BEHAVIOR_GATHER);
                            form.dispose();
                        });

                        JButton btnReduceToOne = new JButton(languageBundle.getString("ReduceToOne"));
                        btnReduceToOne.addActionListener(e -> {
                            getManager().remainOne();
                            form.dispose();
                        });

                        JButton btnRestoreWindows = new JButton(languageBundle.getString("RestoreWindows"));
                        btnRestoreWindows.addActionListener(e -> {
                            NativeFactory.getInstance().getEnvironment().restoreIE();
                            form.dispose();
                        });

                        final JButton btnAllowedBehaviours = new JButton(languageBundle.getString("AllowedBehaviours"));
                        btnAllowedBehaviours.addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                btnAllowedBehaviours.setEnabled(true);
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                            }
                        });
                        btnAllowedBehaviours.addActionListener(event3 -> {
                            // "Disable Breeding" menu item
                            final JCheckBoxMenuItem breedingMenu = new JCheckBoxMenuItem(
                                    languageBundle.getString("BreedingCloning"),
                                    Boolean.parseBoolean(properties.getProperty("Breeding", "true")));
                            breedingMenu.addItemListener(e -> {
                                breedingMenu.setState(toggleBooleanSetting("Breeding"));
                                updateConfigFile();
                                btnAllowedBehaviours.setEnabled(true);
                            });

                            // "Disable Breeding Transient" menu item
                            final JCheckBoxMenuItem transientMenu = new JCheckBoxMenuItem(
                                    languageBundle.getString("BreedingTransient"),
                                    Boolean.parseBoolean(properties.getProperty("Transients", "true")));
                            transientMenu.addItemListener(e -> {
                                transientMenu.setState(toggleBooleanSetting("Transients"));
                                updateConfigFile();
                                btnAllowedBehaviours.setEnabled(true);
                            });

                            // "Disable Transformations" menu item
                            final JCheckBoxMenuItem transformationMenu = new JCheckBoxMenuItem(
                                    languageBundle.getString("Transformation"),
                                    Boolean.parseBoolean(properties.getProperty("Transformation", "true")));
                            transformationMenu.addItemListener(e -> {
                                transformationMenu.setState(toggleBooleanSetting("Transformation"));
                                updateConfigFile();
                                btnAllowedBehaviours.setEnabled(true);
                            });

                            // "Throwing Windows" menu item
                            final JCheckBoxMenuItem throwingMenu = new JCheckBoxMenuItem(
                                    languageBundle.getString("ThrowingWindows"),
                                    Boolean.parseBoolean(properties.getProperty("Throwing", "true")));
                            throwingMenu.addItemListener(e -> {
                                throwingMenu.setState(toggleBooleanSetting("Throwing"));
                                updateConfigFile();
                                btnAllowedBehaviours.setEnabled(true);
                            });

                            // "Mute Sounds" menu item
                            final JCheckBoxMenuItem soundsMenu = new JCheckBoxMenuItem(
                                    languageBundle.getString("SoundEffects"),
                                    Boolean.parseBoolean(properties.getProperty("Sounds", "true")));
                            soundsMenu.addItemListener(e -> {
                                boolean result = toggleBooleanSetting("Sounds");
                                soundsMenu.setState(result);
                                Sounds.setMuted(!result);
                                updateConfigFile();
                                btnAllowedBehaviours.setEnabled(true);
                            });

                            // "Multiscreen" menu item
                            final JCheckBoxMenuItem multiscreenMenu = new JCheckBoxMenuItem(
                                    languageBundle.getString("Multiscreen"),
                                    Boolean.parseBoolean(properties.getProperty("Multiscreen", "true")));
                            multiscreenMenu.addItemListener(e -> {
                                multiscreenMenu.setState(toggleBooleanSetting("Multiscreen"));
                                updateConfigFile();
                                btnAllowedBehaviours.setEnabled(true);
                            });

                            JPopupMenu behaviourPopup = new JPopupMenu();
                            behaviourPopup.add(breedingMenu);
                            behaviourPopup.add(transientMenu);
                            behaviourPopup.add(transformationMenu);
                            behaviourPopup.add(throwingMenu);
                            behaviourPopup.add(soundsMenu);
                            behaviourPopup.add(multiscreenMenu);
                            behaviourPopup.addPopupMenuListener(new PopupMenuListener() {
                                @Override
                                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                }

                                @Override
                                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                                    if (panel.getMousePosition() != null) {
                                        btnAllowedBehaviours.setEnabled(
                                                !(panel.getMousePosition().x > btnAllowedBehaviours.getX() &&
                                                        panel.getMousePosition().x < btnAllowedBehaviours.getX()
                                                                + btnAllowedBehaviours.getWidth()
                                                        &&
                                                        panel.getMousePosition().y > btnAllowedBehaviours.getY() &&
                                                        panel.getMousePosition().y < btnAllowedBehaviours.getY()
                                                                + btnAllowedBehaviours.getHeight()));
                                    } else {
                                        btnAllowedBehaviours.setEnabled(true);
                                    }
                                }

                                @Override
                                public void popupMenuCanceled(PopupMenuEvent e) {
                                }
                            });
                            behaviourPopup.show(btnAllowedBehaviours, 0, btnAllowedBehaviours.getHeight());
                            btnAllowedBehaviours.requestFocusInWindow();
                        });

                        final JButton btnChooseShimeji = new JButton(languageBundle.getString("ChooseShimeji"));
                        btnChooseShimeji.addActionListener(event2 -> {
                            form.dispose();
                            ImageSetChooser chooser = new ImageSetChooser(frame, true);
                            chooser.setIconImage(icon.getImage());
                            setActiveImageSets(chooser.display());
                        });

                        final JButton btnSettings = new JButton(languageBundle.getString("Settings"));
                        btnSettings.addActionListener(event1 -> {
                            form.dispose();
                            SettingsWindow dialog = new SettingsWindow(frame, true);
                            dialog.setIconImage(icon.getImage());
                            dialog.init();
                            dialog.display();

                            if (dialog.getEnvironmentReloadRequired()) {
                                NativeFactory.getInstance().getEnvironment().dispose();
                            }
                            if (dialog.getEnvironmentReloadRequired() || dialog.getImageReloadRequired()) {
                                // need to reload the shimeji as the images have rescaled
                                boolean isExit = getManager().isExitOnLastRemoved();
                                getManager().setExitOnLastRemoved(false);
                                getManager().disposeAll();

                                // Wipe all loaded data
                                ImagePairs.clear();
                                configurations.clear();

                                // Load settings
                                for (String imageSet : imageSets) {
                                    loadConfiguration(imageSet);
                                }

                                // Create the first mascot
                                for (String imageSet : imageSets) {
                                    createMascot(imageSet);
                                }

                                Main.this.getManager().setExitOnLastRemoved(isExit);
                            }
                            if (dialog.getInteractiveWindowReloadRequired())
                                NativeFactory.getInstance().getEnvironment().refreshCache();
                        });

                        final JButton btnLanguage = new JButton(languageBundle.getString("Language"));
                        btnLanguage.addMouseListener(new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                            }

                            @Override
                            public void mousePressed(MouseEvent e) {
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                btnLanguage.setEnabled(true);
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                            }
                        });
                        btnLanguage.addActionListener(e -> {
                            // English menu item
                            final JMenuItem englishMenu = new JMenuItem("English");
                            englishMenu.addActionListener(e1 -> {
                                form.dispose();
                                updateLanguage("en-GB");
                                updateConfigFile();
                            });

                            // Chinese menu item
                            final JMenuItem chineseMenu = new JMenuItem("\u7b80\u4f53\u4e2d\u6587");
                            chineseMenu.addActionListener(e2 -> {
                                form.dispose();
                                updateLanguage("zh-CN");
                                updateConfigFile();
                            });


                            JPopupMenu languagePopup = new JPopupMenu();
                            languagePopup.add(englishMenu);
                            languagePopup.addSeparator();
                            languagePopup.add(chineseMenu);
                            languagePopup.addPopupMenuListener(new PopupMenuListener() {
                                @Override
                                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                }

                                @Override
                                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                                    if (panel.getMousePosition() != null) {
                                        btnLanguage.setEnabled(!(panel.getMousePosition().x > btnLanguage.getX() &&
                                                panel.getMousePosition().x < btnLanguage.getX()
                                                        + btnLanguage.getWidth()
                                                &&
                                                panel.getMousePosition().y > btnLanguage.getY() &&
                                                panel.getMousePosition().y < btnLanguage.getY()
                                                        + btnLanguage.getHeight()));
                                    } else {
                                        btnLanguage.setEnabled(true);
                                    }
                                }

                                @Override
                                public void popupMenuCanceled(PopupMenuEvent e) {
                                }
                            });
                            languagePopup.show(btnLanguage, 0, btnLanguage.getHeight());
                            btnLanguage.requestFocusInWindow();
                        });

                        final JButton btnAutoStart = new JButton();
                        Main.this.updateAutoStartButtonText(btnAutoStart);
                        btnAutoStart.addActionListener(e -> {
                            boolean currentStatus = AutoStartManager.isAutoStartEnabled();
                            boolean success = AutoStartManager.setAutoStart(!currentStatus);
                            
                            if (success) {
                                Main.this.updateAutoStartButtonText(btnAutoStart);
                                // 显示成功消息
                                String message = currentStatus ? 
                                    languageBundle.getString("AutoStartDisabled") :
                                    languageBundle.getString("AutoStartEnabled");
                                Main.this.showInfo(message);
                            } else {
                                Main.showError(languageBundle.getString("AutoStartError"));
                            }
                        });

                        // License button
                        final JButton btnLicense = new JButton();
                        Main.this.updateLicenseButtonText(btnLicense);
                        btnLicense.addActionListener(e -> {
                            form.dispose();
                            LicenseActivationDialog.showDialog(frame, languageBundle);
                            Main.this.updateLicenseButtonText(btnLicense); // Update button text after activation
                        });

                        JButton btnPauseAll = new JButton(
                                getManager().isPaused() ? languageBundle.getString("ResumeAnimations")
                                        : languageBundle.getString("PauseAnimations"));
                        btnPauseAll.addActionListener(e -> {
                            form.dispose();
                            getManager().togglePauseAll();
                        });

                        JButton btnDismissAll = new JButton(languageBundle.getString("DismissAll"));
                        btnDismissAll.addActionListener(e -> exit());

                        // layout
                        float scaling = Float.parseFloat(properties.getProperty("MenuDPI", "96")) / 96;
                        panel.setLayout(new java.awt.GridBagLayout());
                        GridBagConstraints gridBag = new GridBagConstraints();
                        gridBag.fill = GridBagConstraints.HORIZONTAL;
                        gridBag.gridx = 0;
                        gridBag.gridy = 0;
                        panel.add(btnCallShimeji, gridBag);
                        gridBag.insets = new Insets((int) (5 * scaling), 0, 0, 0);
                        gridBag.gridy++;
                        panel.add(btnFollowCursor, gridBag);
                        gridBag.gridy++;
                        panel.add(btnReduceToOne, gridBag);
                        gridBag.gridy++;
                        panel.add(btnRestoreWindows, gridBag);
                        gridBag.gridy++;
                        panel.add(new JSeparator(), gridBag);
                        gridBag.gridy++;
                        panel.add(btnAllowedBehaviours, gridBag);
                        gridBag.gridy++;
                        panel.add(btnChooseShimeji, gridBag);
                        gridBag.gridy++;
                        panel.add(btnSettings, gridBag);
                        gridBag.gridy++;
                        panel.add(btnLanguage, gridBag);
                        gridBag.gridy++;
                        panel.add(btnAutoStart, gridBag);
                        gridBag.gridy++;
                        panel.add(btnLicense, gridBag);
                        gridBag.gridy++;
                        panel.add(new JSeparator(), gridBag);
                        gridBag.gridy++;
                        panel.add(btnPauseAll, gridBag);
                        gridBag.gridy++;
                        panel.add(btnDismissAll, gridBag);

                        form.setIconImage(icon.getImage());
                        form.setTitle(languageBundle.getString("ShimejiEE"));
                        form.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                        form.setAlwaysOnTop(true);

                        // 自动调整窗口大小和位置
                        setupTrayMenuAutoSizing(form, panel, scaling, icon, event,
                            btnCallShimeji, btnFollowCursor, btnReduceToOne, btnRestoreWindows,
                            btnAllowedBehaviours, btnChooseShimeji, btnSettings, btnLanguage,
                            btnAutoStart, btnLicense, btnPauseAll, btnDismissAll);
                        form.setMinimumSize(form.getSize());
                    } else if (event.getButton() == MouseEvent.BUTTON1) {
                        createMascot();
                    } else if (event.getButton() == MouseEvent.BUTTON2 && event.getClickCount() == 2) {
                        if (getManager().isExitOnLastRemoved()) {
                            getManager().setExitOnLastRemoved(false);
                            getManager().disposeAll();
                        } else {
                            for (String imageSet : imageSets) {
                                createMascot(imageSet);
                            }
                            getManager().setExitOnLastRemoved(true);
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });

            // Check if system tray is supported (especially important for Wayland)
            if (SystemTray.isSupported()) {
                SystemTray.getSystemTray().add(icon);
                this.trayIcon = icon;
                log.log(Level.INFO, "System tray icon created successfully");
            } else {
                log.log(Level.WARNING, "System tray is not supported on this platform (possibly Wayland)");
                this.trayIcon = icon; // Keep the icon for other uses
                
                // Show a notification that tray is not available but app is running
                if (isWaylandEnvironment()) {
                    System.out.println("注意：在Wayland环境下，系统托盘功能不可用。");
                    System.out.println("您仍然可以通过右键点击桌宠来访问菜单功能。");
                } else {
                    System.out.println("注意：系统托盘功能在当前环境下不可用。");
                }
            }
        } catch (final UnsupportedOperationException e) {
            // This specifically catches Wayland UnsupportedOperationException
            log.log(Level.WARNING, "System tray not supported in Wayland environment", e);
            this.trayIcon = null;
            System.out.println("注意：在Wayland环境下，系统托盘功能不可用。");
            System.out.println("您仍然可以通过右键点击桌宠来访问菜单功能。");
        } catch (final AWTException e) {
            log.log(Level.SEVERE, "Failed to create tray icon", e);
            Main.showError(languageBundle.getString("FailedDisplaySystemTrayErrorMessage") + "\n"
                    + languageBundle.getString("SeeLogForDetails"));
            exit();
        }
    }

    /**
     * 从当前活动的图像集中随机选择一个来创建新的桌宠。
     */
    public void createMascot() {
        int length = imageSets.size();
        int random = (int) (length * Math.random());
        createMascot(imageSets.get(random));
    }

    /**
     * 使用指定的图像集创建新的桌宠。
     * 
     * @param imageSet 用于创建桌宠的图像集。
     */
    public void createMascot(String imageSet) {
        log.log(Level.INFO, "create a mascot");

        // Create one mascot
        final Mascot mascot = new Mascot(imageSet);

        // Create it outside the bounds of the screen
        mascot.setAnchor(new Point(-4000, -4000));

        // Randomize the initial orientation
        mascot.setLookRight(Math.random() < 0.5);

        try {
            mascot.setBehavior(getConfiguration(imageSet).buildNextBehavior(null, mascot));
            this.getManager().add(mascot);
        } catch (final BehaviorInstantiationException e) {
            log.log(Level.SEVERE, "Failed to initialize the first action", e);
            Main.showError(languageBundle.getString("FailedInitialiseFirstActionErrorMessage") + "\n" + e.getMessage()
                    + "\n" + languageBundle.getString("SeeLogForDetails"));
            mascot.dispose();
        } catch (final CantBeAliveException e) {
            log.log(Level.SEVERE, "Fatal Error", e);
            Main.showError(languageBundle.getString("FailedInitialiseFirstActionErrorMessage") + "\n" + e.getMessage()
                    + "\n" + languageBundle.getString("SeeLogForDetails"));
            mascot.dispose();
        } catch (Exception e) {
            log.log(Level.SEVERE, imageSet + " fatal error, can not be started.", e);
            Main.showError(languageBundle.getString("CouldNotCreateShimejiErrorMessage") + " " + imageSet + ".\n"
                    + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
            mascot.dispose();
        }
    }

    private void refreshLanguage() {
        ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl(false);
        languageBundle = ResourceBundle.getBundle("language",
                Locale.forLanguageTag(properties.getProperty("Language", "en-GB")), utf8Control);

        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);
        getManager().disposeAll();

        // Load settings
        for (String imageSet : imageSets) {
            loadConfiguration(imageSet);
        }

        // Create the first mascot
        for (String imageSet : imageSets) {
            createMascot(imageSet);
        }

        getManager().setExitOnLastRemoved(isExit);
    }

    private void updateLanguage(String language) {
        if (!properties.getProperty("Language", "en-GB").equals(language)) {
            properties.setProperty("Language", language);
            refreshLanguage();
        }
    }

    private boolean toggleBooleanSetting(String propertyName) {
        if (Boolean.parseBoolean(properties.getProperty(propertyName, true + ""))) {
            properties.setProperty(propertyName, "false");
            return false;
        } else {
            properties.setProperty(propertyName, "true");
            return true;
        }
    }

    private void setMascotInformationDismissed(final String imageSet) {
        ArrayList<String> list = new ArrayList<>();
        String[] data = properties.getProperty("InformationDismissed", "").split("/");

        if (data.length > 0 && !data[0].isEmpty())
            list.addAll(Arrays.asList(data));
        if (!list.contains(imageSet))
            list.add(imageSet);

        properties.setProperty("InformationDismissed",
                list.toString().replace("[", "").replace("]", "").replace(", ", "/"));
    }

    public void setMascotBehaviorEnabled(final String name, final Mascot mascot, boolean enabled) {
        ArrayList<String> list = new ArrayList<>();
        String[] data = properties.getProperty("DisabledBehaviours." + mascot.getImageSet(), "").split("/");

        if (data.length > 0 && !data[0].isEmpty())
            list.addAll(Arrays.asList(data));

        if (list.contains(name) && enabled)
            list.remove(name);
        else if (!list.contains(name) && !enabled)
            list.add(name);

        if (!list.isEmpty())
            properties.setProperty("DisabledBehaviours." + mascot.getImageSet(),
                    list.toString().replace("[", "").replace("]", "").replace(", ", "/"));
        else
            properties.remove("DisabledBehaviours." + mascot.getImageSet());

        updateConfigFile();
    }

    private void updateConfigFile() {
        try {
            // 获取应用程序基础目录
            String baseDir = System.getProperty("app.dir");
            if (baseDir == null) {
                baseDir = System.getProperty("user.dir");
            }
            if (baseDir == null) {
                baseDir = ".";
            }
            
            try (FileOutputStream output = new FileOutputStream(baseDir + "/conf/settings.properties")) {
                properties.store(output, "Shimeji-ee Configuration Options");
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 公开方法，用于保存配置文件
     */
    public void saveConfigFile() {
        updateConfigFile();
    }

    /**
     * Replaces the current set of active imageSets without modifying
     * valid imageSets that are already active. Does nothing if newImageSets
     * are null
     *
     * @param newImageSets All the imageSets that should now be active
     * @author snek, with some tweaks by Kilkakon
     */
    private void setActiveImageSets(ArrayList<String> newImageSets) {
        if (newImageSets == null)
            return;

        // I don't think there would be enough imageSets chosen at any given
        // time for it to be worth using HashSet but i might be wrong
        ArrayList<String> toRemove = new ArrayList<>(imageSets);
        toRemove.removeAll(newImageSets);

        ArrayList<String> toAdd = new ArrayList<>();
        ArrayList<String> toRetain = new ArrayList<>();
        for (String set : newImageSets) {
            if (!imageSets.contains(set))
                toAdd.add(set);
            if (!toRetain.contains(set))
                toRetain.add(set);
            populateArrayListWithChildSets(set, toRetain);
        }

        boolean isExit = Main.this.getManager().isExitOnLastRemoved();
        Main.this.getManager().setExitOnLastRemoved(false);

        for (String r : toRemove)
            removeLoadedImageSet(r, toRetain);

        for (String a : toAdd)
            addImageSet(a);

        Main.this.getManager().setExitOnLastRemoved(isExit);
    }

    private void populateArrayListWithChildSets(String imageSet, ArrayList<String> childList) {
        if (childImageSets.containsKey(imageSet)) {
            for (String set : childImageSets.get(imageSet)) {
                if (!childList.contains(set)) {
                    populateArrayListWithChildSets(set, childList);
                    childList.add(set);
                }
            }
        }
    }

    private void removeLoadedImageSet(String imageSet, ArrayList<String> setsToIgnore) {
        if (childImageSets.containsKey(imageSet)) {
            for (String set : childImageSets.get(imageSet)) {
                if (!setsToIgnore.contains(set)) {
                    setsToIgnore.add(set);
                    imageSets.remove(imageSet);
                    getManager().remainNone(imageSet);
                    configurations.remove(imageSet);
                    ImagePairs.removeAll(imageSet);
                    removeLoadedImageSet(set, setsToIgnore);
                }
            }
        }

        if (!setsToIgnore.contains(imageSet)) {
            imageSets.remove(imageSet);
            getManager().remainNone(imageSet);
            configurations.remove(imageSet);
            ImagePairs.removeAll(imageSet);
        }
    }

    private void addImageSet(String imageSet) {
        if (configurations.containsKey(imageSet)) {
            imageSets.add(imageSet);
            createMascot(imageSet);
        } else {
            if (loadConfiguration(imageSet)) {
                imageSets.add(imageSet);
                String informationAlreadySeen = properties.getProperty("InformationDismissed", "");
                if (configurations.get(imageSet).containsInformationKey("SplashImage") &&
                        (Boolean.parseBoolean(properties.getProperty("AlwaysShowInformationScreen", "false")) ||
                                !informationAlreadySeen.contains(imageSet))) {
                    InformationWindow info = new InformationWindow();
                    info.init(imageSet, configurations.get(imageSet));
                    info.display();
                    setMascotInformationDismissed(imageSet);
                    updateConfigFile();
                }
                createMascot(imageSet);
            } else {
                // conf failed
                configurations.remove(imageSet); // maybe move this to the loadConfig catch
            }
        }
    }

    /**
     * 获取指定图像集的配置。
     * 
     * @param imageSet 图像集名称。
     * @return 对应的配置对象。
     */
    public Configuration getConfiguration(String imageSet) {
        return configurations.get(imageSet);
    }

    /**
     * 获取桌宠管理器。
     * 
     * @return 桌宠管理器实例。
     */
    private Manager getManager() {
        return this.manager;
    }

    /**
     * 获取当前运行的平台（32位或64位）。
     * 
     * @return 当前平台。
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * 获取应用程序的属性配置。
     * 
     * @return 属性对象。
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * 获取当前的语言包。
     * 
     * @return 语言包资源。
     */
    public ResourceBundle getLanguageBundle() {
        return languageBundle;
    }

    /**
     * 退出应用程序。
     * 这将销毁所有桌宠并停止管理器。
     */
    public void exit() {
        this.getManager().disposeAll();
        this.getManager().stop();
        System.exit(0);
    }

    /**
     * 自动调整托盘菜单窗口大小和位置
     * 根据当前 DPI 设置和屏幕分辨率自动计算最佳窗口大小
     */
    private void setupTrayMenuAutoSizing(JDialog form, JPanel panel, float scaling, TrayIcon icon, MouseEvent event,
            JButton... buttons) {
        try {
            // 获取显示器信息
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            DisplayMode dm = gd.getDisplayMode();
            
            // 计算字体度量
            Font baseFont = buttons[0].getFont();
            FontMetrics metrics = buttons[0].getFontMetrics(baseFont);
            
            // 计算所有按钮文本的最大宽度
            int maxWidth = 0;
            for (JButton button : buttons) {
                int textWidth = metrics.stringWidth(button.getText());
                maxWidth = Math.max(maxWidth, textWidth);
            }
            
            // 基于 DPI 调整窗口尺寸
            int basePadding = (int) (32 * scaling); // 基础内边距
            int buttonPadding = (int) (16 * scaling); // 按钮额外内边距
            int separatorHeight = (int) (2 * scaling); // 分隔线高度
            int insetHeight = (int) (5 * scaling); // 按钮间距
            
            // 计算窗口宽度（文本宽度 + 内边距）
            int windowWidth = maxWidth + basePadding + buttonPadding;
            
            // 确保最小宽度（适应不同 DPI）
            int minWidth = (int) (200 * scaling);
            windowWidth = Math.max(windowWidth, minWidth);
            
            // 计算窗口高度（按钮数量 + 分隔线 + 内边距）
            int buttonCount = buttons.length;
            int separatorCount = 2; // 当前有2个分隔线
            int buttonHeight = metrics.getHeight() + (int) (8 * scaling); // 按钮高度包含文本高度和内边距
            
            int windowHeight = buttonCount * buttonHeight + 
                              separatorCount * separatorHeight +
                              (buttonCount - 1) * insetHeight + // 按钮间距
                              basePadding; // 顶部和底部内边距
            
            // 根据屏幕分辨率调整最大尺寸
            Rectangle screenBounds = ge.getMaximumWindowBounds();
            int maxWindowWidth = (int) (screenBounds.width * 0.3); // 最大不超过屏幕宽度的30%
            int maxWindowHeight = (int) (screenBounds.height * 0.8); // 最大不超过屏幕高度的80%
            
            windowWidth = Math.min(windowWidth, maxWindowWidth);
            windowHeight = Math.min(windowHeight, maxWindowHeight);
            
            // 设置面板首选大小
            panel.setPreferredSize(new Dimension(windowWidth, windowHeight));
            form.pack();
            
            // 智能定位窗口位置
            Point clickPoint = event.getPoint();
            int formX = clickPoint.x - form.getWidth();
            int formY = clickPoint.y - form.getHeight();
            
            // 确保窗口在屏幕边界内
            if (formX < screenBounds.x) {
                formX = clickPoint.x; // 如果左边超出，放在点击位置右边
            }
            if (formY < screenBounds.y) {
                formY = clickPoint.y; // 如果上边超出，放在点击位置下边
            }
            if (formX + form.getWidth() > screenBounds.x + screenBounds.width) {
                formX = screenBounds.x + screenBounds.width - form.getWidth(); // 右边超出时调整
            }
            if (formY + form.getHeight() > screenBounds.y + screenBounds.height) {
                formY = screenBounds.y + screenBounds.height - form.getHeight(); // 下边超出时调整
            }
            
            form.setLocation(formX, formY);
            form.setVisible(true);
            
            // 记录调试信息
            log.info(String.format("Tray menu auto adjustment: window size=%dx%d, position=(%d,%d), DPI scaling=%.2f, screen=%dx%d", 
                    windowWidth, windowHeight, formX, formY, scaling, dm.getWidth(), dm.getHeight()));
            
        } catch (Exception e) {
            log.warning("Tray menu auto adjustment failed, using default layout: " + e.getMessage());
            
            // 降级到简单布局
            panel.setPreferredSize(new Dimension((int) (250 * scaling), (int) (400 * scaling)));
            form.pack();
            form.setLocation(event.getPoint().x - form.getWidth(), event.getPoint().y - form.getHeight());
            form.setVisible(true);
        }
    }

    /**
     * 更新自启动按钮的文本
     */
    private void updateAutoStartButtonText(JButton btnAutoStart) {
        boolean isEnabled = AutoStartManager.isAutoStartEnabled();
        String text = isEnabled ? 
            languageBundle.getString("DisableAutoStart") : 
            languageBundle.getString("EnableAutoStart");
        btnAutoStart.setText(text);
    }
    
    /**
     * 更新许可证按钮的文本
     */
    private void updateLicenseButtonText(JButton btnLicense) {
        LicenseManager manager = LicenseManager.getInstance();
        LicenseLevel currentLevel = manager.getCurrentLicenseLevel();
        
        String text;
        switch (currentLevel) {
            case NO_KEY:
                text = languageBundle.getString("License") + " - " + languageBundle.getString("FreeVersion");
                break;
            case ADVANCED_KEY:
                long daysRemaining = manager.getDaysRemaining();
                text = String.format("%s - %s (%d %s)", 
                    languageBundle.getString("License"),
                    languageBundle.getString("AdvancedVersion"),
                    daysRemaining,
                    languageBundle.getString("DaysRemaining").toLowerCase());
                break;
            case SPECIAL_KEY:
                long specialDaysRemaining = manager.getDaysRemaining();
                text = String.format("%s - %s (%d %s)", 
                    languageBundle.getString("License"),
                    languageBundle.getString("DeveloperVersion"),
                    specialDaysRemaining,
                    languageBundle.getString("DaysRemaining").toLowerCase());
                break;
            default:
                text = languageBundle.getString("License");
                break;
        }
        btnLicense.setText(text);
    }
    
    /**
     * 显示信息消息
     */
    private void showInfo(String message) {
        if (this.trayIcon != null) {
            this.trayIcon.displayMessage("Shimeji-ee", message, TrayIcon.MessageType.INFO);
        }
    }

    /**
     * 平台无关的自动启动管理器
     */
    private static class AutoStartManager {
        /**
         * 检查是否已设置开机自启动
         */
        public static boolean isAutoStartEnabled() {
            try {
                if (isMacOS()) {
                    return com.group_finity.mascot.mac.AutoStartManager.isAutoStartEnabled();
                } else if (isWindows()) {
                    return com.group_finity.mascot.win.AutoStartManager.isAutoStartEnabled();
                } else {
                    // Linux 或其他平台暂时不支持
                    return false;
                }
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
                if (isMacOS()) {
                    return com.group_finity.mascot.mac.AutoStartManager.setAutoStart(enable);
                } else if (isWindows()) {
                    return com.group_finity.mascot.win.AutoStartManager.setAutoStart(enable);
                } else {
                    // Linux 或其他平台暂时不支持
                    log.warning("Auto-start is not supported on this platform");
                    return false;
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to set auto-start: " + enable, e);
                return false;
            }
        }

        /**
         * 检查是否为 Windows 系统
         */
        private static boolean isWindows() {
            return System.getProperty("os.name").toLowerCase().contains("win");
        }
    }
}
