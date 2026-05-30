/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.group_finity.mascot;

import java.awt.Desktop;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.LookAndFeel;
import com.formdev.flatlaf.FlatLaf;
import com.group_finity.mascot.license.LicenseManager;
import com.group_finity.mascot.license.LicenseLevel;

/**
 * @author Kilkakon
 */
public class SettingsWindow extends javax.swing.JDialog {
        private final String themeFile;
        private Properties themeProperties = new Properties();
        private final Properties oldThemeProperties = new Properties();
        private LookAndFeel lookAndFeel;
        private final ArrayList<String> listData = new ArrayList<>();
        private final ArrayList<String> blacklistData = new ArrayList<>();
        private Boolean alwaysShowShimejiChooser = false;
        private Boolean alwaysShowInformationScreen = false;
        private String filter = "nearest";
        private double scaling = 1.0;
        private double opacity = 1.0;
        private Boolean windowedMode = false;
        private Dimension windowSize = new Dimension(600, 500);
        private Color backgroundColour = new Color(0, 255, 0);
        private String backgroundMode = "centre";
        private String backgroundImage = null;
        private final String[] backgroundModes = { "centre", "fill", "fit", "stretch" };
        private Color primaryColour1;
        private Color primaryColour2;
        private Color primaryColour3;
        private Color secondaryColour1;
        private Color secondaryColour2;
        private Color secondaryColour3;
        private Color blackColour;
        private Color whiteColour;
        private Font font;
        private double menuOpacity = 1.0;

        private Boolean imageReloadRequired = false;
        private Boolean interactiveWindowReloadRequired = false;
        private Boolean environmentReloadRequired = false;

        /**
         * Creates new form SettingsWindow
         */
        public SettingsWindow(javax.swing.JFrame parent, boolean modal) {
                super(parent, modal);

                // 获取应用程序基础目录
                String baseDir = System.getProperty("app.dir");
                if (baseDir == null) {
                        baseDir = System.getProperty("user.dir");
                }
                if (baseDir == null) {
                        baseDir = ".";
                }
                themeFile = baseDir + "/conf/theme.properties";

                initComponents();
        }

        public void init() {
                // initialise controls
                setLocationRelativeTo(null);
                grpFilter.add(radFilterNearest);
                grpFilter.add(radFilterBicubic);
                grpFilter.add(radFilterHqx);
                
                // 设置scaling滑块 - 支持0.5x到4x
                sldScaling.setMinimum(5);  // 0.5倍
                sldScaling.setMaximum(40); // 4.0倍
                sldScaling.setMajorTickSpacing(5);  // 主刻度间隔0.5倍
                sldScaling.setMinorTickSpacing(1);  // 次刻度间隔0.1倍
                sldScaling.setPaintLabels(true);
                sldScaling.setPaintTicks(true);
                sldScaling.setSnapToTicks(false); // 关闭自动对齐，我们手动实现
                sldScaling.setValue(10); // 默认1.0倍
                
                // 自定义标签
                java.util.Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
                labelTable.put(5, new JLabel("0.5x"));
                labelTable.put(10, new JLabel("1x"));
                labelTable.put(15, new JLabel("1.5x"));
                labelTable.put(20, new JLabel("2x"));
                labelTable.put(25, new JLabel("2.5x"));
                labelTable.put(30, new JLabel("3x"));
                labelTable.put(35, new JLabel("3.5x"));
                labelTable.put(40, new JLabel("4x"));
                sldScaling.setLabelTable(labelTable);

                // load existing settings
                Properties properties = Main.getInstance().getProperties();
                alwaysShowShimejiChooser = Boolean
                                .parseBoolean(properties.getProperty("AlwaysShowShimejiChooser", "false"));
                alwaysShowInformationScreen = Boolean
                                .parseBoolean(properties.getProperty("AlwaysShowInformationScreen", "false"));
                String filterText = Main.getInstance().getProperties().getProperty("Filter", "false");
                filter = "nearest";
                if (filterText.equalsIgnoreCase("true") || filterText.equalsIgnoreCase("hqx"))
                        filter = "hqx";
                else if (filterText.equalsIgnoreCase("bicubic"))
                        filter = "bicubic";
                opacity = Double.parseDouble(properties.getProperty("Opacity", "1.0"));
                scaling = Double.parseDouble(properties.getProperty("Scaling", "1.0"));
                windowedMode = properties.getProperty("Environment", "generic").equals("virtual");
                String[] windowArray = properties.getProperty("WindowSize", "600x500").split("x");
                windowSize = new Dimension(Integer.parseInt(windowArray[0]), Integer.parseInt(windowArray[1]));
                Dimension buttonSize = btnDone.getPreferredSize();
                Dimension aboutButtonSize = btnWebsite.getPreferredSize();
                backgroundColour = Color.decode(properties.getProperty("Background", "#00FF00"));
                backgroundImage = properties.getProperty("BackgroundImage", "");
                backgroundMode = properties.getProperty("BackgroundMode", "centre");
                float menuScaling = Float.parseFloat(properties.getProperty("MenuDPI", "96")) / 96;
                chkAlwaysShowShimejiChooser.setSelected(alwaysShowShimejiChooser);
                chkAlwaysShowInformationScreen.setSelected(alwaysShowInformationScreen);
                if (filter.equals("bicubic"))
                        radFilterBicubic.setSelected(true);
                else if (filter.equals("hqx"))
                        radFilterHqx.setSelected(true);
                else
                        radFilterNearest.setSelected(true);
                sldOpacity.setValue((int) (opacity * 100));
                sldScaling.setValue((int) (scaling * 10));
                spnScaling.setValue(scaling);

                listData.addAll(Arrays.asList(properties.getProperty("InteractiveWindows", "").split("/")));
                lstInteractiveWindows.setListData(listData.toArray(new String[0]));

                for (String item : properties.getProperty("InteractiveWindowsBlacklist", "").split("/")) {
                        if (!item.trim().isEmpty()) {
                                blacklistData.add(item);
                        }
                }
                lstInteractiveWindowsBlacklist.setListData(blacklistData.toArray(new String[0]));

                Properties themeProperties = new Properties();
                FileInputStream input;
                try {
                        input = new FileInputStream(themeFile);
                        themeProperties.load(input);
                } catch (IOException ignored) {
                }
                primaryColour1 = Color.decode(themeProperties.getProperty("PrimaryColour1", "#1EA6EB"));
                primaryColour2 = Color.decode(themeProperties.getProperty("PrimaryColour2", "#28B0F5"));
                primaryColour3 = Color.decode(themeProperties.getProperty("PrimaryColour3", "#32BAFF"));
                secondaryColour1 = Color.decode(themeProperties.getProperty("SecondaryColour1", "#BCBCBE"));
                secondaryColour2 = Color.decode(themeProperties.getProperty("SecondaryColour2", "#C6C6C8"));
                secondaryColour3 = Color.decode(themeProperties.getProperty("SecondaryColour3", "#D0D0D2"));
                blackColour = Color.decode(themeProperties.getProperty("BlackColour", "#000000"));
                whiteColour = Color.decode(themeProperties.getProperty("WhiteColour", "#FFFFFF"));
                menuOpacity = Integer.parseInt(themeProperties.getProperty("MenuOpacity", "255")) / 255.0;
                font = Font.decode(themeProperties.getProperty("FontName", "SansSerif") + "-" +
                                themeProperties.getProperty("FontStyle", "0") + "-" +
                                themeProperties.getProperty("FontSize", "12"));
                font = font.deriveFont(font.getSize() * menuScaling);
                pnlPrimaryColour1Preview.setBackground(primaryColour1);
                txtPrimaryColour1.setText(
                                String.format("#%02X%02X%02X", primaryColour1.getRed(), primaryColour1.getGreen(),
                                                primaryColour1.getBlue()));
                pnlPrimaryColour2Preview.setBackground(primaryColour2);
                txtPrimaryColour2.setText(
                                String.format("#%02X%02X%02X", primaryColour2.getRed(), primaryColour2.getGreen(),
                                                primaryColour2.getBlue()));
                pnlPrimaryColour3Preview.setBackground(primaryColour3);
                txtPrimaryColour3.setText(
                                String.format("#%02X%02X%02X", primaryColour3.getRed(), primaryColour2.getGreen(),
                                                primaryColour3.getBlue()));
                pnlSecondaryColour1Preview.setBackground(secondaryColour1);
                txtSecondaryColour1.setText(String.format("#%02X%02X%02X", secondaryColour1.getRed(),
                                secondaryColour1.getGreen(), secondaryColour1.getBlue()));
                pnlSecondaryColour2Preview.setBackground(secondaryColour2);
                txtSecondaryColour2.setText(String.format("#%02X%02X%02X", secondaryColour2.getRed(),
                                secondaryColour2.getGreen(), secondaryColour2.getBlue()));
                pnlSecondaryColour3Preview.setBackground(secondaryColour3);
                txtSecondaryColour3.setText(String.format("#%02X%02X%02X", secondaryColour3.getRed(),
                                secondaryColour3.getGreen(), secondaryColour3.getBlue()));
                pnlBlackColourPreview.setBackground(blackColour);
                txtBlackColour.setText(
                                String.format("#%02X%02X%02X", blackColour.getRed(), blackColour.getGreen(),
                                                blackColour.getBlue()));
                pnlWhiteColourPreview.setBackground(whiteColour);
                txtWhiteColour.setText(
                                String.format("#%02X%02X%02X", whiteColour.getRed(), whiteColour.getGreen(),
                                                whiteColour.getBlue()));

                // 初始化FlatLaf兼容的主题属性
                initializeFlatLafTheme();

                sldMenuOpacity.setValue((int) (menuOpacity * 100));

                // 保存当前Look and Feel引用
                lookAndFeel = UIManager.getLookAndFeel();

                chkWindowModeEnabled.setSelected(windowedMode);
                spnWindowWidth.setBackground(txtBackgroundColour.getBackground());
                spnWindowHeight.setBackground(txtBackgroundColour.getBackground());
                spnWindowWidth.setEnabled(windowedMode);
                spnWindowHeight.setEnabled(windowedMode);
                spnWindowWidth.setValue(windowSize.width);
                spnWindowHeight.setValue(windowSize.height);
                txtBackgroundColour.setText(String.format("#%02X%02X%02X", backgroundColour.getRed(),
                                backgroundColour.getGreen(), backgroundColour.getBlue()));
                btnBackgroundColourChange.setEnabled(windowedMode);
                btnBackgroundImageChange.setEnabled(windowedMode);
                pnlBackgroundPreview.setBackground(backgroundColour);
                if (backgroundImage != null) {
                        try {
                                Dimension size = pnlBackgroundImage.getPreferredSize();
                                refreshBackgroundImage();
                                pnlBackgroundImage.setPreferredSize(size);
                        } catch (Exception e) {
                                backgroundImage = null;
                                lblBackgroundImage.setIcon(null);
                        }
                }
                cmbBackgroundImageMode.setEnabled(windowedMode && backgroundImage != null);
                btnBackgroundImageRemove.setEnabled(windowedMode && backgroundImage != null);

                // localisation
                ResourceBundle language = Main.getInstance().getLanguageBundle();
                setTitle(language.getString("Settings"));
                pnlTabs.setTitleAt(0, language.getString("General"));
                pnlTabs.setTitleAt(1, language.getString("InteractiveWindows"));
                pnlTabs.setTitleAt(2, language.getString("Theme"));
                pnlTabs.setTitleAt(3, language.getString("WindowMode"));
                pnlTabs.setTitleAt(4, language.getString("About"));
                chkAlwaysShowShimejiChooser.setText(language.getString("AlwaysShowShimejiChooser"));
                chkAlwaysShowInformationScreen.setText(language.getString("AlwaysShowInformationScreen"));
                lblOpacity.setText(language.getString("Opacity"));
                lblScaling.setText(language.getString("Scaling"));
                lblFilter.setText(language.getString("FilterOptions"));
                radFilterNearest.setText(language.getString("NearestNeighbour"));
                radFilterHqx.setText(language.getString("Filter"));
                radFilterBicubic.setText(language.getString("BicubicFilter"));
                pnlInteractiveTabs.setTitleAt(0, language.getString("Whitelist"));
                pnlInteractiveTabs.setTitleAt(1, language.getString("Blacklist"));
                btnAddInteractiveWindow.setText(language.getString("Add"));
                btnRemoveInteractiveWindow.setText(language.getString("Remove"));
                lblPrimaryColour1.setText(language.getString("PrimaryColour1"));
                lblPrimaryColour2.setText(language.getString("PrimaryColour2"));
                lblPrimaryColour3.setText(language.getString("PrimaryColour3"));
                lblSecondaryColour1.setText(language.getString("SecondaryColour1"));
                lblSecondaryColour2.setText(language.getString("SecondaryColour2"));
                lblSecondaryColour3.setText(language.getString("SecondaryColour3"));
                lblBlackColour.setText(language.getString("BlackColour"));
                lblWhiteColour.setText(language.getString("WhiteColour"));
                lblMenuOpacity.setText(language.getString("MenuOpacity"));
                btnChangeFont.setText(language.getString("ChangeFont"));
                btnReset.setText(language.getString("Reset"));
                chkWindowModeEnabled.setText(language.getString("WindowedModeEnabled"));
                lblDimensions.setText(language.getString("Dimensions"));
                lblBackground.setText(language.getString("Background"));
                btnBackgroundColourChange.setText(language.getString("Change"));
                btnBackgroundImageChange.setText(language.getString("Change"));
                cmbBackgroundImageMode.addItem(language.getString("BackgroundModeCentre"));
                cmbBackgroundImageMode.addItem(language.getString("BackgroundModeFill"));
                cmbBackgroundImageMode.addItem(language.getString("BackgroundModeFit"));
                cmbBackgroundImageMode.addItem(language.getString("BackgroundModeStretch"));
                btnBackgroundImageRemove.setText(language.getString("Remove"));
                lblShimejiEE.setText(language.getString("ShimejiEE"));
                lblDevelopedBy.setText(language.getString("DevelopedBy"));
                btnWebsite.setText(language.getString("Website"));
                btnFilterHelp.setToolTipText(language.getString("FilterHelpTooltip"));
                btnDone.setText(language.getString("Done"));
                btnCancel.setText(language.getString("Cancel"));

                // come back around to this one now that the dropdown is populated
                for (int index = 0; index < backgroundModes.length; index++) {
                        if (backgroundMode.equals(backgroundModes[index])) {
                                cmbBackgroundImageMode.setSelectedIndex(index);
                                break;
                        }
                }
        }

        public void display() {
                float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96"))
                                / 96;

                // scale controls to fit
                getContentPane().setPreferredSize(new Dimension((int) (600 * menuScaling), (int) (497 * menuScaling)));
                sldOpacity.setPreferredSize(new Dimension((int) (sldOpacity.getPreferredSize().width * menuScaling),
                                (int) (sldOpacity.getPreferredSize().height * menuScaling)));
                sldScaling.setPreferredSize(new Dimension((int) (sldScaling.getPreferredSize().width * menuScaling),
                                (int) (sldScaling.getPreferredSize().height * menuScaling)));
                spnScaling.setPreferredSize(new Dimension(DPIManager.scaleWidth(120),
                                DPIManager.scaleHeight(50)));
                
                // 设置 Spinner 的字体大小
                spnScaling.setFont(spnScaling.getFont().deriveFont((float) DPIManager.scaleFontSize(spnScaling.getFont().getSize())));
                btnAddInteractiveWindow
                                .setPreferredSize(new Dimension(
                                                (int) (btnAddInteractiveWindow.getPreferredSize().width * menuScaling),
                                                (int) (btnAddInteractiveWindow.getPreferredSize().height
                                                                * menuScaling)));
                btnRemoveInteractiveWindow.setPreferredSize(
                                new Dimension((int) (btnRemoveInteractiveWindow.getPreferredSize().width * menuScaling),
                                                (int) (btnRemoveInteractiveWindow.getPreferredSize().height
                                                                * menuScaling)));
                pnlInteractiveButtons.setPreferredSize(new Dimension(pnlInteractiveButtons.getPreferredSize().width,
                                btnAddInteractiveWindow.getPreferredSize().height + 6));
                txtPrimaryColour1
                                .setPreferredSize(new Dimension(
                                                (int) (txtPrimaryColour1.getPreferredSize().width * menuScaling),
                                                (int) (txtPrimaryColour1.getPreferredSize().height * menuScaling)));
                txtPrimaryColour2
                                .setPreferredSize(new Dimension(
                                                (int) (txtPrimaryColour2.getPreferredSize().width * menuScaling),
                                                (int) (txtPrimaryColour2.getPreferredSize().height * menuScaling)));
                txtPrimaryColour3
                                .setPreferredSize(new Dimension(
                                                (int) (txtPrimaryColour3.getPreferredSize().width * menuScaling),
                                                (int) (txtPrimaryColour3.getPreferredSize().height * menuScaling)));
                txtSecondaryColour1
                                .setPreferredSize(new Dimension(
                                                (int) (txtSecondaryColour1.getPreferredSize().width * menuScaling),
                                                (int) (txtSecondaryColour1.getPreferredSize().height * menuScaling)));
                txtSecondaryColour2
                                .setPreferredSize(new Dimension(
                                                (int) (txtSecondaryColour2.getPreferredSize().width * menuScaling),
                                                (int) (txtSecondaryColour2.getPreferredSize().height * menuScaling)));
                txtSecondaryColour3
                                .setPreferredSize(new Dimension(
                                                (int) (txtSecondaryColour3.getPreferredSize().width * menuScaling),
                                                (int) (txtSecondaryColour3.getPreferredSize().height * menuScaling)));
                txtBlackColour.setPreferredSize(
                                new Dimension((int) (txtBlackColour.getPreferredSize().width * menuScaling),
                                                (int) (txtBlackColour.getPreferredSize().height * menuScaling)));
                txtWhiteColour.setPreferredSize(
                                new Dimension((int) (txtWhiteColour.getPreferredSize().width * menuScaling),
                                                (int) (txtWhiteColour.getPreferredSize().height * menuScaling)));
                pnlPrimaryColour1PreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlPrimaryColour1PreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlPrimaryColour1PreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlPrimaryColour1Preview
                                .setPreferredSize(new Dimension(
                                                (int) (pnlPrimaryColour1Preview.getPreferredSize().width * menuScaling),
                                                (int) (pnlPrimaryColour1Preview.getPreferredSize().height
                                                                * menuScaling)));
                pnlPrimaryColour2PreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlPrimaryColour2PreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlPrimaryColour2PreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlPrimaryColour2Preview
                                .setPreferredSize(new Dimension(
                                                (int) (pnlPrimaryColour2Preview.getPreferredSize().width * menuScaling),
                                                (int) (pnlPrimaryColour2Preview.getPreferredSize().height
                                                                * menuScaling)));
                pnlPrimaryColour3PreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlPrimaryColour3PreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlPrimaryColour3PreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlPrimaryColour3Preview
                                .setPreferredSize(new Dimension(
                                                (int) (pnlPrimaryColour3Preview.getPreferredSize().width * menuScaling),
                                                (int) (pnlPrimaryColour3Preview.getPreferredSize().height
                                                                * menuScaling)));
                pnlSecondaryColour1PreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlSecondaryColour1PreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlSecondaryColour1PreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlSecondaryColour1Preview.setPreferredSize(
                                new Dimension((int) (pnlSecondaryColour1Preview.getPreferredSize().width * menuScaling),
                                                (int) (pnlSecondaryColour1Preview.getPreferredSize().height
                                                                * menuScaling)));
                pnlSecondaryColour2PreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlSecondaryColour2PreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlSecondaryColour2PreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlSecondaryColour2Preview.setPreferredSize(
                                new Dimension((int) (pnlSecondaryColour2Preview.getPreferredSize().width * menuScaling),
                                                (int) (pnlSecondaryColour2Preview.getPreferredSize().height
                                                                * menuScaling)));
                pnlSecondaryColour3PreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlSecondaryColour3PreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlSecondaryColour3PreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlSecondaryColour3Preview.setPreferredSize(
                                new Dimension((int) (pnlSecondaryColour3Preview.getPreferredSize().width * menuScaling),
                                                (int) (pnlSecondaryColour3Preview.getPreferredSize().height
                                                                * menuScaling)));
                pnlBlackColourPreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlBlackColourPreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlBlackColourPreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlBlackColourPreview
                                .setPreferredSize(new Dimension(
                                                (int) (pnlBlackColourPreview.getPreferredSize().width * menuScaling),
                                                (int) (pnlBlackColourPreview.getPreferredSize().height * menuScaling)));
                pnlWhiteColourPreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlWhiteColourPreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlWhiteColourPreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlWhiteColourPreview
                                .setPreferredSize(new Dimension(
                                                (int) (pnlWhiteColourPreview.getPreferredSize().width * menuScaling),
                                                (int) (pnlWhiteColourPreview.getPreferredSize().height * menuScaling)));
                btnPrimaryColour1Change
                                .setPreferredSize(new Dimension(
                                                (int) (btnPrimaryColour1Change.getPreferredSize().width * menuScaling),
                                                (int) (btnPrimaryColour1Change.getPreferredSize().height
                                                                * menuScaling)));
                btnPrimaryColour2Change
                                .setPreferredSize(new Dimension(
                                                (int) (btnPrimaryColour2Change.getPreferredSize().width * menuScaling),
                                                (int) (btnPrimaryColour2Change.getPreferredSize().height
                                                                * menuScaling)));
                btnPrimaryColour3Change
                                .setPreferredSize(new Dimension(
                                                (int) (btnPrimaryColour3Change.getPreferredSize().width * menuScaling),
                                                (int) (btnPrimaryColour3Change.getPreferredSize().height
                                                                * menuScaling)));
                btnSecondaryColour1Change.setPreferredSize(
                                new Dimension((int) (btnSecondaryColour1Change.getPreferredSize().width * menuScaling),
                                                (int) (btnSecondaryColour1Change.getPreferredSize().height
                                                                * menuScaling)));
                btnSecondaryColour2Change.setPreferredSize(
                                new Dimension((int) (btnSecondaryColour2Change.getPreferredSize().width * menuScaling),
                                                (int) (btnSecondaryColour2Change.getPreferredSize().height
                                                                * menuScaling)));
                btnSecondaryColour3Change.setPreferredSize(
                                new Dimension((int) (btnSecondaryColour3Change.getPreferredSize().width * menuScaling),
                                                (int) (btnSecondaryColour3Change.getPreferredSize().height
                                                                * menuScaling)));
                btnBlackColourChange
                                .setPreferredSize(new Dimension(
                                                (int) (btnBlackColourChange.getPreferredSize().width * menuScaling),
                                                (int) (btnBlackColourChange.getPreferredSize().height * menuScaling)));
                btnWhiteColourChange
                                .setPreferredSize(new Dimension(
                                                (int) (btnWhiteColourChange.getPreferredSize().width * menuScaling),
                                                (int) (btnWhiteColourChange.getPreferredSize().height * menuScaling)));
                sldMenuOpacity.setPreferredSize(
                                new Dimension((int) (sldMenuOpacity.getPreferredSize().width * menuScaling),
                                                (int) (sldMenuOpacity.getPreferredSize().height * menuScaling)));
                btnChangeFont.setPreferredSize(
                                new Dimension((int) (btnChangeFont.getPreferredSize().width * menuScaling),
                                                (int) (btnChangeFont.getPreferredSize().height * menuScaling)));
                btnReset.setPreferredSize(new Dimension((int) (btnReset.getPreferredSize().width * menuScaling),
                                (int) (btnReset.getPreferredSize().height * menuScaling)));
                pnlThemeButtons.setPreferredSize(
                                new Dimension(pnlThemeButtons.getPreferredSize().width,
                                                btnReset.getPreferredSize().height + 6));
                spnWindowWidth.setPreferredSize(
                                new Dimension((int) (spnWindowWidth.getPreferredSize().width * menuScaling),
                                                (int) (spnWindowWidth.getPreferredSize().height * menuScaling)));
                spnWindowHeight.setPreferredSize(
                                new Dimension((int) (spnWindowHeight.getPreferredSize().width * menuScaling),
                                                (int) (spnWindowHeight.getPreferredSize().height * menuScaling)));
                txtBackgroundColour
                                .setPreferredSize(new Dimension(
                                                (int) (txtBackgroundColour.getPreferredSize().width * menuScaling),
                                                (int) (txtBackgroundColour.getPreferredSize().height * menuScaling)));
                pnlBackgroundPreviewContainer.setPreferredSize(
                                new Dimension((int) (pnlBackgroundPreviewContainer.getPreferredSize().width
                                                * menuScaling),
                                                (int) (pnlBackgroundPreviewContainer.getPreferredSize().height
                                                                * menuScaling)));
                pnlBackgroundPreview
                                .setPreferredSize(new Dimension(
                                                (int) (pnlBackgroundPreview.getPreferredSize().width * menuScaling),
                                                (int) (pnlBackgroundPreview.getPreferredSize().height * menuScaling)));
                btnBackgroundColourChange.setPreferredSize(
                                new Dimension((int) (btnBackgroundColourChange.getPreferredSize().width * menuScaling),
                                                (int) (btnBackgroundColourChange.getPreferredSize().height
                                                                * menuScaling)));
                btnBackgroundImageChange
                                .setPreferredSize(new Dimension(
                                                (int) (btnBackgroundImageChange.getPreferredSize().width * menuScaling),
                                                (int) (btnBackgroundImageChange.getPreferredSize().height
                                                                * menuScaling)));
                btnBackgroundImageRemove
                                .setPreferredSize(new Dimension(
                                                (int) (btnBackgroundImageRemove.getPreferredSize().width * menuScaling),
                                                (int) (btnBackgroundImageRemove.getPreferredSize().height
                                                                * menuScaling)));
                cmbBackgroundImageMode.setPreferredSize(btnBackgroundImageRemove.getPreferredSize());
                pnlBackgroundImage
                                .setPreferredSize(new Dimension(
                                                (int) (pnlBackgroundImage.getPreferredSize().width * menuScaling),
                                                (int) (pnlBackgroundImage.getPreferredSize().height * menuScaling)));
                pnlBackgroundImage.setMaximumSize(pnlBackgroundImage.getPreferredSize());
                lblIcon.setPreferredSize(new Dimension((int) (lblIcon.getPreferredSize().width * menuScaling),
                                (int) (lblIcon.getPreferredSize().height * menuScaling)));
                lblIcon.setMaximumSize(lblIcon.getPreferredSize());
                if (!getIconImages().isEmpty())
                        lblIcon.setIcon(new ImageIcon(getIconImages().getFirst().getScaledInstance(
                                        lblIcon.getPreferredSize().width,
                                        lblIcon.getPreferredSize().height, java.awt.Image.SCALE_DEFAULT)));
                btnWebsite.setPreferredSize(new Dimension((int) (btnWebsite.getPreferredSize().width * menuScaling),
                                (int) (btnWebsite.getPreferredSize().height * menuScaling)));
                btnDiscord.setPreferredSize(new Dimension((int) (btnDiscord.getPreferredSize().width * menuScaling),
                                (int) (btnDiscord.getPreferredSize().height * menuScaling)));
                btnPatreon.setPreferredSize(new Dimension((int) (btnPatreon.getPreferredSize().width * menuScaling),
                                (int) (btnPatreon.getPreferredSize().height * menuScaling)));
                pnlAboutButtons.setPreferredSize(
                                new Dimension(pnlAboutButtons.getPreferredSize().width,
                                                btnWebsite.getPreferredSize().height + 6));
                btnDone.setPreferredSize(new Dimension((int) (btnDone.getPreferredSize().width * menuScaling),
                                (int) (btnDone.getPreferredSize().height * menuScaling)));
                btnCancel.setPreferredSize(new Dimension((int) (btnCancel.getPreferredSize().width * menuScaling),
                                (int) (btnCancel.getPreferredSize().height * menuScaling)));
                pnlFooter.setPreferredSize(
                                new Dimension(pnlFooter.getPreferredSize().width,
                                                btnDone.getPreferredSize().height + 6));
                pack();
                setVisible(true);

        }

        /**
         * 初始化FlatLaf兼容的主题属性
         */
        private void initializeFlatLafTheme() {
                // 初始化当前主题属性
                themeProperties.clear();
                themeProperties.setProperty("PrimaryColour1", String.format("#%02X%02X%02X",
                                primaryColour1.getRed(), primaryColour1.getGreen(), primaryColour1.getBlue()));
                themeProperties.setProperty("PrimaryColour2", String.format("#%02X%02X%02X",
                                primaryColour2.getRed(), primaryColour2.getGreen(), primaryColour2.getBlue()));
                themeProperties.setProperty("PrimaryColour3", String.format("#%02X%02X%02X",
                                primaryColour3.getRed(), primaryColour3.getGreen(), primaryColour3.getBlue()));
                themeProperties.setProperty("SecondaryColour1", String.format("#%02X%02X%02X",
                                secondaryColour1.getRed(), secondaryColour1.getGreen(), secondaryColour1.getBlue()));
                themeProperties.setProperty("SecondaryColour2", String.format("#%02X%02X%02X",
                                secondaryColour2.getRed(), secondaryColour2.getGreen(), secondaryColour2.getBlue()));
                themeProperties.setProperty("SecondaryColour3", String.format("#%02X%02X%02X",
                                secondaryColour3.getRed(), secondaryColour3.getGreen(), secondaryColour3.getBlue()));
                themeProperties.setProperty("BlackColour", String.format("#%02X%02X%02X",
                                blackColour.getRed(), blackColour.getGreen(), blackColour.getBlue()));
                themeProperties.setProperty("WhiteColour", String.format("#%02X%02X%02X",
                                whiteColour.getRed(), whiteColour.getGreen(), whiteColour.getBlue()));
                themeProperties.setProperty("MenuOpacity", String.valueOf((int) (menuOpacity * 255)));
                themeProperties.setProperty("FontName", font.getName());
                themeProperties.setProperty("FontStyle", String.valueOf(font.getStyle()));
                themeProperties.setProperty("FontSize", String.valueOf(font.getSize()));

                // 复制到旧主题属性（用于恢复）
                oldThemeProperties.clear();
                oldThemeProperties.putAll(themeProperties);
        }

        private void browseToUrl(String url) {
                try {
                        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
                                desktop.browse(new URI(url));
                        else
                                throw new UnsupportedOperationException(
                                                Main.getInstance().getLanguageBundle()
                                                                .getString("FailedOpenWebBrowserErrorMessage") + " "
                                                                + url);
                } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);
                }
        }

        private Font showFontDialog(JFrame parent, Font currentFont) {
                // 简单的字体对话框实现
                String[] fontNames = { "SansSerif", "Serif", "Monospaced", "Dialog", "DialogInput" };

                String selectedName = (String) JOptionPane.showInputDialog(parent,
                                "选择字体:", "字体选择", JOptionPane.QUESTION_MESSAGE, null, fontNames, currentFont.getName());
                if (selectedName == null)
                        return null;

                int style = Font.PLAIN;

                return new Font(selectedName, style, currentFont.getSize());
        }

        public boolean getEnvironmentReloadRequired() {
                return environmentReloadRequired;
        }

        public boolean getImageReloadRequired() {
                return imageReloadRequired;
        }

        public boolean getInteractiveWindowReloadRequired() {
                return interactiveWindowReloadRequired;
        }

        /**
         * This method is called from within the constructor to initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is always
         * regenerated by the Form Editor.
         */
        // <editor-fold defaultstate="collapsed" desc="Generated
        // Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                grpFilter = new javax.swing.ButtonGroup();
                pnlTabs = new javax.swing.JTabbedPane();
                JPanel pnlGeneral = new JPanel();
                chkAlwaysShowShimejiChooser = new javax.swing.JCheckBox();
                lblScaling = new javax.swing.JLabel();
                sldScaling = new javax.swing.JSlider();
                spnScaling = new javax.swing.JSpinner();
                lblFilter = new javax.swing.JLabel();
                radFilterNearest = new javax.swing.JRadioButton();
                radFilterBicubic = new javax.swing.JRadioButton();
                radFilterHqx = new javax.swing.JRadioButton();
                btnFilterHelp = new javax.swing.JButton();
                sldOpacity = new javax.swing.JSlider();
                lblOpacity = new javax.swing.JLabel();
                chkAlwaysShowInformationScreen = new javax.swing.JCheckBox();
                JPanel pnlInteractiveWindows = new JPanel();
                pnlInteractiveTabs = new javax.swing.JTabbedPane();
                pnlWhitelistTab = new javax.swing.JPanel();
                pnlBlacklistTab = new javax.swing.JPanel();
                pnlInteractiveButtons = new javax.swing.JPanel();
                btnAddInteractiveWindow = new javax.swing.JButton();
                btnRemoveInteractiveWindow = new javax.swing.JButton();
                javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
                lstInteractiveWindows = new javax.swing.JList<>();
                javax.swing.JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
                lstInteractiveWindowsBlacklist = new javax.swing.JList<>();
                JPanel pnlTheme = new JPanel();
                pnlThemeButtons = new javax.swing.JPanel();
                btnChangeFont = new javax.swing.JButton();
                btnReset = new javax.swing.JButton();
                lblPrimaryColour1 = new javax.swing.JLabel();
                txtPrimaryColour1 = new javax.swing.JTextField();
                pnlPrimaryColour1PreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler gluePrimaryColour1a = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlPrimaryColour1Preview = new javax.swing.JPanel();
                javax.swing.Box.Filler gluePrimaryColour1b = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnPrimaryColour1Change = new javax.swing.JButton();
                lblPrimaryColour2 = new javax.swing.JLabel();
                txtPrimaryColour2 = new javax.swing.JTextField();
                pnlPrimaryColour2PreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler gluePrimaryColour2a = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlPrimaryColour2Preview = new javax.swing.JPanel();
                javax.swing.Box.Filler gluePrimaryColour2b = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnPrimaryColour2Change = new javax.swing.JButton();
                lblPrimaryColour3 = new javax.swing.JLabel();
                txtPrimaryColour3 = new javax.swing.JTextField();
                pnlPrimaryColour3PreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler gluePrimaryColour3a = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlPrimaryColour3Preview = new javax.swing.JPanel();
                javax.swing.Box.Filler gluePrimaryColour3b = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnPrimaryColour3Change = new javax.swing.JButton();
                lblSecondaryColour1 = new javax.swing.JLabel();
                txtSecondaryColour1 = new javax.swing.JTextField();
                pnlSecondaryColour1PreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler glueSecondaryColour1a = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlSecondaryColour1Preview = new javax.swing.JPanel();
                javax.swing.Box.Filler glueSecondaryColour1b = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnSecondaryColour1Change = new javax.swing.JButton();
                lblSecondaryColour2 = new javax.swing.JLabel();
                txtSecondaryColour2 = new javax.swing.JTextField();
                pnlSecondaryColour2PreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler glueSecondaryColour2a = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlSecondaryColour2Preview = new javax.swing.JPanel();
                javax.swing.Box.Filler glueSecondaryColour2b = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnSecondaryColour2Change = new javax.swing.JButton();
                lblSecondaryColour3 = new javax.swing.JLabel();
                txtSecondaryColour3 = new javax.swing.JTextField();
                pnlSecondaryColour3PreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler glueSecondaryColour3a = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlSecondaryColour3Preview = new javax.swing.JPanel();
                javax.swing.Box.Filler glueSecondaryColour3b = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnSecondaryColour3Change = new javax.swing.JButton();
                lblMenuOpacity = new javax.swing.JLabel();
                sldMenuOpacity = new javax.swing.JSlider();
                lblBlackColour = new javax.swing.JLabel();
                txtBlackColour = new javax.swing.JTextField();
                pnlBlackColourPreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler glueBlackColoura = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlBlackColourPreview = new javax.swing.JPanel();
                javax.swing.Box.Filler glueBlackColourb = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnBlackColourChange = new javax.swing.JButton();
                lblWhiteColour = new javax.swing.JLabel();
                txtWhiteColour = new javax.swing.JTextField();
                pnlWhiteColourPreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler glueWhiteColoura = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlWhiteColourPreview = new javax.swing.JPanel();
                javax.swing.Box.Filler glueWhiteColourb = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                btnWhiteColourChange = new javax.swing.JButton();
                JPanel pnlWindowMode = new JPanel();
                chkWindowModeEnabled = new javax.swing.JCheckBox();
                lblDimensions = new javax.swing.JLabel();
                JLabel lblDimensionsX = new JLabel();
                lblBackground = new javax.swing.JLabel();
                txtBackgroundColour = new javax.swing.JTextField();
                btnBackgroundColourChange = new javax.swing.JButton();
                spnWindowWidth = new javax.swing.JSpinner();
                spnWindowHeight = new javax.swing.JSpinner();
                JLabel lblBackgroundColour = new JLabel();
                pnlBackgroundPreviewContainer = new javax.swing.JPanel();
                javax.swing.Box.Filler glueBackground = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlBackgroundPreview = new javax.swing.JPanel();
                javax.swing.Box.Filler glueBackground2 = new javax.swing.Box.Filler(new Dimension(0, 0),
                                new Dimension(0, 0),
                                new Dimension(0, 0));
                JLabel lblBackgroundImageCaption = new JLabel();
                pnlBackgroundImage = new javax.swing.JPanel();
                lblBackgroundImage = new javax.swing.JLabel();
                btnBackgroundImageChange = new javax.swing.JButton();
                btnBackgroundImageRemove = new javax.swing.JButton();
                cmbBackgroundImageMode = new javax.swing.JComboBox<>();
                JPanel pnlAbout = new JPanel();
                javax.swing.Box.Filler glue1 = new javax.swing.Box.Filler(new Dimension(0, 0), new Dimension(0, 0),
                                new Dimension(0, 32767));
                lblIcon = new javax.swing.JLabel();
                javax.swing.Box.Filler rigid1 = new javax.swing.Box.Filler(new Dimension(0, 15), new Dimension(0, 15),
                                new Dimension(0, 15));
                lblShimejiEE = new javax.swing.JLabel();
                javax.swing.Box.Filler rigid2 = new javax.swing.Box.Filler(new Dimension(0, 10), new Dimension(0, 5),
                                new Dimension(0, 10));
                JLabel lblVersion = new JLabel();
                javax.swing.Box.Filler rigid3 = new javax.swing.Box.Filler(new Dimension(0, 15), new Dimension(0, 15),
                                new Dimension(0, 15));
                lblDevelopedBy = new javax.swing.JLabel();
                JLabel lblKilkakon = new JLabel();
                javax.swing.Box.Filler rigid4 = new javax.swing.Box.Filler(new Dimension(0, 30), new Dimension(0, 30),
                                new Dimension(0, 30));
                pnlAboutButtons = new javax.swing.JPanel();
                btnWebsite = new javax.swing.JButton();
                btnDiscord = new javax.swing.JButton();
                btnPatreon = new javax.swing.JButton();
                javax.swing.Box.Filler glue2 = new javax.swing.Box.Filler(new Dimension(0, 0), new Dimension(0, 0),
                                new Dimension(0, 32767));
                pnlFooter = new javax.swing.JPanel();
                btnDone = new javax.swing.JButton();
                btnCancel = new javax.swing.JButton();

                setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

                chkAlwaysShowShimejiChooser.setText("Always Show Shimeji Chooser");
                chkAlwaysShowShimejiChooser.addItemListener(evt -> chkAlwaysShowShimejiChooserItemStateChanged(evt));

                lblScaling.setText("Scaling");

                sldScaling.setMajorTickSpacing(10);
                sldScaling.setMaximum(40);
                sldScaling.setMinorTickSpacing(5);
                sldScaling.setPaintLabels(true);
                sldScaling.setPaintTicks(true);
                sldScaling.setSnapToTicks(true);
                sldScaling.setValue(10);
                sldScaling.setPreferredSize(new java.awt.Dimension(300, 45));
                sldScaling.addChangeListener(evt -> sldScalingStateChanged(evt));

                spnScaling.setModel(new javax.swing.SpinnerNumberModel(1.0, 0.5, 4.0, 0.1));
                spnScaling.setPreferredSize(new java.awt.Dimension(120, 50));
                spnScaling.addChangeListener(evt -> spnScalingStateChanged(evt));

                lblFilter.setText("Filter");

                radFilterNearest.setText("Nearest");
                radFilterNearest.addItemListener(evt -> radFilterItemStateChanged(evt));

                radFilterBicubic.setText("Bicubic");
                radFilterBicubic.addItemListener(evt -> radFilterItemStateChanged(evt));

                radFilterHqx.setText("hqx");
                radFilterHqx.addItemListener(evt -> radFilterItemStateChanged(evt));

                btnFilterHelp.setText("?");
                btnFilterHelp.setFont(
                                lblFilter.getFont().deriveFont(java.awt.Font.BOLD, lblFilter.getFont().getSize()));
                btnFilterHelp.setPreferredSize(new java.awt.Dimension(32, 32));
                btnFilterHelp.setMinimumSize(new java.awt.Dimension(32, 32));
                btnFilterHelp.setMaximumSize(new java.awt.Dimension(32, 32));
                btnFilterHelp.addActionListener(evt -> btnFilterHelpActionPerformed(evt));

                sldOpacity.setMajorTickSpacing(10);
                sldOpacity.setMinorTickSpacing(5);
                sldOpacity.setPaintLabels(true);
                sldOpacity.setPaintTicks(true);
                sldOpacity.setSnapToTicks(true);
                sldOpacity.setValue(10);
                sldOpacity.setPreferredSize(new java.awt.Dimension(300, 45));
                sldOpacity.addChangeListener(evt -> sldOpacityStateChanged(evt));

                lblOpacity.setText("Opacity");

                chkAlwaysShowInformationScreen.setText("Always Show Information Screen");
                chkAlwaysShowInformationScreen
                                .addItemListener(evt -> chkAlwaysShowInformationScreenItemStateChanged(evt));

                javax.swing.GroupLayout pnlGeneralLayout = new javax.swing.GroupLayout(pnlGeneral);
                pnlGeneral.setLayout(pnlGeneralLayout);
                pnlGeneralLayout.setHorizontalGroup(
                                pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlGeneralLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addGroup(pnlGeneralLayout
                                                                                .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(chkAlwaysShowShimejiChooser)
                                                                                .addGroup(pnlGeneralLayout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(lblFilter)
                                                                                                .addGap(8, 8, 8)
                                                                                                .addComponent(btnFilterHelp,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addComponent(lblScaling)
                                                                                .addGroup(pnlGeneralLayout
                                                                                                .createSequentialGroup()
                                                                                                .addGap(10, 10, 10)
                                                                                                .addGroup(pnlGeneralLayout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                                .addComponent(sldOpacity,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                .addGroup(pnlGeneralLayout
                                                                                                                                .createParallelGroup(
                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                .addComponent(radFilterNearest)
                                                                                                                                .addGroup(pnlGeneralLayout
                                                                                                                                                .createSequentialGroup()
                                                                                                                                                .addComponent(sldScaling,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                .addGap(10, 10, 10)
                                                                                                                                                .addComponent(spnScaling,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                                .addComponent(radFilterBicubic)
                                                                                                                                .addComponent(radFilterHqx))))
                                                                                .addComponent(lblOpacity)
                                                                                .addComponent(chkAlwaysShowInformationScreen))
                                                                .addContainerGap(80, Short.MAX_VALUE)));
                pnlGeneralLayout.setVerticalGroup(
                                pnlGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlGeneralLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(chkAlwaysShowShimejiChooser)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(chkAlwaysShowInformationScreen)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(lblOpacity)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(sldOpacity,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(lblScaling)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlGeneralLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(sldScaling,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(spnScaling,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addGroup(pnlGeneralLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(lblFilter)
                                                                                .addComponent(btnFilterHelp,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(radFilterNearest)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(radFilterBicubic)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(radFilterHqx)
                                                                .addContainerGap(75, Short.MAX_VALUE)));

                pnlTabs.addTab("General", pnlGeneral);

                pnlInteractiveButtons.setPreferredSize(new java.awt.Dimension(380, 36));
                pnlInteractiveButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

                btnAddInteractiveWindow.setText("Add");
                btnAddInteractiveWindow.setMaximumSize(new java.awt.Dimension(130, 26));
                btnAddInteractiveWindow.setMinimumSize(new java.awt.Dimension(95, 23));
                btnAddInteractiveWindow.setName(""); // NOI18N
                btnAddInteractiveWindow.setPreferredSize(new java.awt.Dimension(130, 26));
                btnAddInteractiveWindow.addActionListener(evt -> btnAddInteractiveWindowActionPerformed(evt));
                pnlInteractiveButtons.add(btnAddInteractiveWindow);

                btnRemoveInteractiveWindow.setText("Remove");
                btnRemoveInteractiveWindow.setMaximumSize(new java.awt.Dimension(130, 26));
                btnRemoveInteractiveWindow.setMinimumSize(new java.awt.Dimension(95, 23));
                btnRemoveInteractiveWindow.setPreferredSize(new java.awt.Dimension(130, 26));
                btnRemoveInteractiveWindow.addActionListener(evt -> btnRemoveInteractiveWindowActionPerformed(evt));
                pnlInteractiveButtons.add(btnRemoveInteractiveWindow);

                lstInteractiveWindows.setModel(new javax.swing.AbstractListModel<>() {
                        final String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

                        public int getSize() {
                                return strings.length;
                        }

                        public String getElementAt(int i) {
                                return strings[i];
                        }
                });
                jScrollPane1.setViewportView(lstInteractiveWindows);

                lstInteractiveWindowsBlacklist.setModel(new javax.swing.AbstractListModel<>() {
                        final String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

                        public int getSize() {
                                return strings.length;
                        }

                        public String getElementAt(int i) {
                                return strings[i];
                        }
                });
                jScrollPane3.setViewportView(lstInteractiveWindowsBlacklist);

                // Setup whitelist tab
                javax.swing.GroupLayout pnlWhitelistTabLayout = new javax.swing.GroupLayout(pnlWhitelistTab);
                pnlWhitelistTab.setLayout(pnlWhitelistTabLayout);
                pnlWhitelistTabLayout.setHorizontalGroup(
                                pnlWhitelistTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlWhitelistTabLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jScrollPane1,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                301, Short.MAX_VALUE)
                                                                .addContainerGap()));
                pnlWhitelistTabLayout.setVerticalGroup(
                                pnlWhitelistTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlWhitelistTabLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jScrollPane1,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                237, Short.MAX_VALUE)
                                                                .addContainerGap()));

                pnlInteractiveTabs.addTab("Whitelist", pnlWhitelistTab);

                // Setup blacklist tab
                javax.swing.GroupLayout pnlBlacklistTabLayout = new javax.swing.GroupLayout(pnlBlacklistTab);
                pnlBlacklistTab.setLayout(pnlBlacklistTabLayout);
                pnlBlacklistTabLayout.setHorizontalGroup(
                                pnlBlacklistTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlBlacklistTabLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jScrollPane3,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                301, Short.MAX_VALUE)
                                                                .addContainerGap()));
                pnlBlacklistTabLayout.setVerticalGroup(
                                pnlBlacklistTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlBlacklistTabLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(jScrollPane3,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                237, Short.MAX_VALUE)
                                                                .addContainerGap()));

                pnlInteractiveTabs.addTab("Blacklist", pnlBlacklistTab);

                javax.swing.GroupLayout pnlInteractiveWindowsLayout = new javax.swing.GroupLayout(
                                pnlInteractiveWindows);
                pnlInteractiveWindows.setLayout(pnlInteractiveWindowsLayout);
                pnlInteractiveWindowsLayout.setHorizontalGroup(
                                pnlInteractiveWindowsLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                pnlInteractiveWindowsLayout
                                                                                .createSequentialGroup()
                                                                                .addContainerGap()
                                                                                .addGroup(pnlInteractiveWindowsLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                .addComponent(pnlInteractiveTabs)
                                                                                                .addComponent(pnlInteractiveButtons,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE))
                                                                                .addContainerGap()));
                pnlInteractiveWindowsLayout.setVerticalGroup(
                                pnlInteractiveWindowsLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                pnlInteractiveWindowsLayout
                                                                                .createSequentialGroup()
                                                                                .addContainerGap()
                                                                                .addComponent(pnlInteractiveTabs)
                                                                                .addPreferredGap(
                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(pnlInteractiveButtons,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addContainerGap()));

                pnlTabs.addTab("InteractiveWindows", pnlInteractiveWindows);

                pnlThemeButtons.setPreferredSize(new java.awt.Dimension(380, 36));
                pnlThemeButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

                btnChangeFont.setText("Change Font");
                btnChangeFont.setMaximumSize(new java.awt.Dimension(130, 26));
                btnChangeFont.setName(""); // NOI18N
                btnChangeFont.setPreferredSize(new java.awt.Dimension(130, 26));
                btnChangeFont.addActionListener(evt -> btnChangeFontActionPerformed(evt));
                pnlThemeButtons.add(btnChangeFont);

                btnReset.setText("Reset");
                btnReset.setMaximumSize(new java.awt.Dimension(130, 26));
                btnReset.setMinimumSize(new java.awt.Dimension(95, 23));
                btnReset.setPreferredSize(new java.awt.Dimension(130, 26));
                btnReset.addActionListener(evt -> btnResetActionPerformed(evt));
                pnlThemeButtons.add(btnReset);

                lblPrimaryColour1.setText("Primary 1");

                txtPrimaryColour1.setEditable(false);
                txtPrimaryColour1.setText("#00FF00");
                txtPrimaryColour1.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlPrimaryColour1PreviewContainer
                                .setLayout(new javax.swing.BoxLayout(pnlPrimaryColour1PreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlPrimaryColour1PreviewContainer.add(gluePrimaryColour1a);

                pnlPrimaryColour1Preview
                                .setBorder(new javax.swing.border.SoftBevelBorder(
                                                javax.swing.border.BevelBorder.LOWERED));
                pnlPrimaryColour1Preview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlPrimaryColour1PreviewLayout = new javax.swing.GroupLayout(
                                pnlPrimaryColour1Preview);
                pnlPrimaryColour1Preview.setLayout(pnlPrimaryColour1PreviewLayout);
                pnlPrimaryColour1PreviewLayout.setHorizontalGroup(
                                pnlPrimaryColour1PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlPrimaryColour1PreviewLayout.setVerticalGroup(
                                pnlPrimaryColour1PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlPrimaryColour1PreviewContainer.add(pnlPrimaryColour1Preview);
                pnlPrimaryColour1PreviewContainer.add(gluePrimaryColour1b);

                btnPrimaryColour1Change.setText("Change");
                btnPrimaryColour1Change.addActionListener(evt -> btnPrimaryColour1ChangeActionPerformed(evt));

                lblPrimaryColour2.setText("Primary 2");

                txtPrimaryColour2.setEditable(false);
                txtPrimaryColour2.setText("#00FF00");
                txtPrimaryColour2.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlPrimaryColour2PreviewContainer
                                .setLayout(new javax.swing.BoxLayout(pnlPrimaryColour2PreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlPrimaryColour2PreviewContainer.add(gluePrimaryColour2a);

                pnlPrimaryColour2Preview
                                .setBorder(new javax.swing.border.SoftBevelBorder(
                                                javax.swing.border.BevelBorder.LOWERED));
                pnlPrimaryColour2Preview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlPrimaryColour2PreviewLayout = new javax.swing.GroupLayout(
                                pnlPrimaryColour2Preview);
                pnlPrimaryColour2Preview.setLayout(pnlPrimaryColour2PreviewLayout);
                pnlPrimaryColour2PreviewLayout.setHorizontalGroup(
                                pnlPrimaryColour2PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlPrimaryColour2PreviewLayout.setVerticalGroup(
                                pnlPrimaryColour2PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlPrimaryColour2PreviewContainer.add(pnlPrimaryColour2Preview);
                pnlPrimaryColour2PreviewContainer.add(gluePrimaryColour2b);

                btnPrimaryColour2Change.setText("Change");
                btnPrimaryColour2Change.addActionListener(evt -> btnPrimaryColour2ChangeActionPerformed(evt));

                lblPrimaryColour3.setText("Primary 3");

                txtPrimaryColour3.setEditable(false);
                txtPrimaryColour3.setText("#00FF00");
                txtPrimaryColour3.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlPrimaryColour3PreviewContainer
                                .setLayout(new javax.swing.BoxLayout(pnlPrimaryColour3PreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlPrimaryColour3PreviewContainer.add(gluePrimaryColour3a);

                pnlPrimaryColour3Preview
                                .setBorder(new javax.swing.border.SoftBevelBorder(
                                                javax.swing.border.BevelBorder.LOWERED));
                pnlPrimaryColour3Preview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlPrimaryColour3PreviewLayout = new javax.swing.GroupLayout(
                                pnlPrimaryColour3Preview);
                pnlPrimaryColour3Preview.setLayout(pnlPrimaryColour3PreviewLayout);
                pnlPrimaryColour3PreviewLayout.setHorizontalGroup(
                                pnlPrimaryColour3PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlPrimaryColour3PreviewLayout.setVerticalGroup(
                                pnlPrimaryColour3PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlPrimaryColour3PreviewContainer.add(pnlPrimaryColour3Preview);
                pnlPrimaryColour3PreviewContainer.add(gluePrimaryColour3b);

                btnPrimaryColour3Change.setText("Change");
                btnPrimaryColour3Change.addActionListener(evt -> btnPrimaryColour3ChangeActionPerformed(evt));

                lblSecondaryColour1.setText("Secondary 1");

                txtSecondaryColour1.setEditable(false);
                txtSecondaryColour1.setText("#00FF00");
                txtSecondaryColour1.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlSecondaryColour1PreviewContainer.setLayout(
                                new javax.swing.BoxLayout(pnlSecondaryColour1PreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlSecondaryColour1PreviewContainer.add(glueSecondaryColour1a);

                pnlSecondaryColour1Preview
                                .setBorder(new javax.swing.border.SoftBevelBorder(
                                                javax.swing.border.BevelBorder.LOWERED));
                pnlSecondaryColour1Preview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlSecondaryColour1PreviewLayout = new javax.swing.GroupLayout(
                                pnlSecondaryColour1Preview);
                pnlSecondaryColour1Preview.setLayout(pnlSecondaryColour1PreviewLayout);
                pnlSecondaryColour1PreviewLayout.setHorizontalGroup(
                                pnlSecondaryColour1PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlSecondaryColour1PreviewLayout.setVerticalGroup(
                                pnlSecondaryColour1PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlSecondaryColour1PreviewContainer.add(pnlSecondaryColour1Preview);
                pnlSecondaryColour1PreviewContainer.add(glueSecondaryColour1b);

                btnSecondaryColour1Change.setText("Change");
                btnSecondaryColour1Change.addActionListener(evt -> btnSecondaryColour1ChangeActionPerformed(evt));

                lblSecondaryColour2.setText("Secondary 2");

                txtSecondaryColour2.setEditable(false);
                txtSecondaryColour2.setText("#00FF00");
                txtSecondaryColour2.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlSecondaryColour2PreviewContainer.setLayout(
                                new javax.swing.BoxLayout(pnlSecondaryColour2PreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlSecondaryColour2PreviewContainer.add(glueSecondaryColour2a);

                pnlSecondaryColour2Preview
                                .setBorder(new javax.swing.border.SoftBevelBorder(
                                                javax.swing.border.BevelBorder.LOWERED));
                pnlSecondaryColour2Preview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlSecondaryColour2PreviewLayout = new javax.swing.GroupLayout(
                                pnlSecondaryColour2Preview);
                pnlSecondaryColour2Preview.setLayout(pnlSecondaryColour2PreviewLayout);
                pnlSecondaryColour2PreviewLayout.setHorizontalGroup(
                                pnlSecondaryColour2PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlSecondaryColour2PreviewLayout.setVerticalGroup(
                                pnlSecondaryColour2PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlSecondaryColour2PreviewContainer.add(pnlSecondaryColour2Preview);
                pnlSecondaryColour2PreviewContainer.add(glueSecondaryColour2b);

                btnSecondaryColour2Change.setText("Change");
                btnSecondaryColour2Change.addActionListener(evt -> btnSecondaryColour2ChangeActionPerformed(evt));

                lblSecondaryColour3.setText("Secondary 3");

                txtSecondaryColour3.setEditable(false);
                txtSecondaryColour3.setText("#00FF00");
                txtSecondaryColour3.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlSecondaryColour3PreviewContainer.setLayout(
                                new javax.swing.BoxLayout(pnlSecondaryColour3PreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlSecondaryColour3PreviewContainer.add(glueSecondaryColour3a);

                pnlSecondaryColour3Preview
                                .setBorder(new javax.swing.border.SoftBevelBorder(
                                                javax.swing.border.BevelBorder.LOWERED));
                pnlSecondaryColour3Preview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlSecondaryColour3PreviewLayout = new javax.swing.GroupLayout(
                                pnlSecondaryColour3Preview);
                pnlSecondaryColour3Preview.setLayout(pnlSecondaryColour3PreviewLayout);
                pnlSecondaryColour3PreviewLayout.setHorizontalGroup(
                                pnlSecondaryColour3PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlSecondaryColour3PreviewLayout.setVerticalGroup(
                                pnlSecondaryColour3PreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlSecondaryColour3PreviewContainer.add(pnlSecondaryColour3Preview);
                pnlSecondaryColour3PreviewContainer.add(glueSecondaryColour3b);

                btnSecondaryColour3Change.setText("Change");
                btnSecondaryColour3Change.addActionListener(evt -> btnSecondaryColour3ChangeActionPerformed(evt));

                lblMenuOpacity.setText("Menu Opacity");

                sldMenuOpacity.setMajorTickSpacing(10);
                sldMenuOpacity.setMinorTickSpacing(5);
                sldMenuOpacity.setPaintLabels(true);
                sldMenuOpacity.setPaintTicks(true);
                sldMenuOpacity.setSnapToTicks(true);
                sldMenuOpacity.setValue(10);
                sldMenuOpacity.setPreferredSize(new java.awt.Dimension(300, 45));
                sldMenuOpacity.addChangeListener(evt -> sldMenuOpacityStateChanged(evt));

                lblBlackColour.setText("Text");

                txtBlackColour.setEditable(false);
                txtBlackColour.setText("#00FF00");
                txtBlackColour.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlBlackColourPreviewContainer
                                .setLayout(new javax.swing.BoxLayout(pnlBlackColourPreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlBlackColourPreviewContainer.add(glueBlackColoura);

                pnlBlackColourPreview.setBorder(
                                new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                pnlBlackColourPreview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlBlackColourPreviewLayout = new javax.swing.GroupLayout(
                                pnlBlackColourPreview);
                pnlBlackColourPreview.setLayout(pnlBlackColourPreviewLayout);
                pnlBlackColourPreviewLayout.setHorizontalGroup(
                                pnlBlackColourPreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlBlackColourPreviewLayout.setVerticalGroup(
                                pnlBlackColourPreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlBlackColourPreviewContainer.add(pnlBlackColourPreview);
                pnlBlackColourPreviewContainer.add(glueBlackColourb);

                btnBlackColourChange.setText("Change");
                btnBlackColourChange.addActionListener(evt -> btnBlackColourChangeActionPerformed(evt));

                lblWhiteColour.setText("Background");
                lblWhiteColour.setToolTipText("");

                txtWhiteColour.setEditable(false);
                txtWhiteColour.setText("#00FF00");
                txtWhiteColour.setPreferredSize(new java.awt.Dimension(70, 24));

                pnlWhiteColourPreviewContainer
                                .setLayout(new javax.swing.BoxLayout(pnlWhiteColourPreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlWhiteColourPreviewContainer.add(glueWhiteColoura);

                pnlWhiteColourPreview.setBorder(
                                new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                pnlWhiteColourPreview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlWhiteColourPreviewLayout = new javax.swing.GroupLayout(
                                pnlWhiteColourPreview);
                pnlWhiteColourPreview.setLayout(pnlWhiteColourPreviewLayout);
                pnlWhiteColourPreviewLayout.setHorizontalGroup(
                                pnlWhiteColourPreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlWhiteColourPreviewLayout.setVerticalGroup(
                                pnlWhiteColourPreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlWhiteColourPreviewContainer.add(pnlWhiteColourPreview);
                pnlWhiteColourPreviewContainer.add(glueWhiteColourb);

                btnWhiteColourChange.setText("Change");
                btnWhiteColourChange.addActionListener(evt -> btnWhiteColourChangeActionPerformed(evt));

                javax.swing.GroupLayout pnlThemeLayout = new javax.swing.GroupLayout(pnlTheme);
                pnlTheme.setLayout(pnlThemeLayout);
                pnlThemeLayout.setHorizontalGroup(
                                pnlThemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlThemeLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createSequentialGroup()
                                                                                                .addGroup(pnlThemeLayout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                                .addComponent(pnlThemeButtons,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                Short.MAX_VALUE)
                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                .createParallelGroup(
                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                .addComponent(lblPrimaryColour1)
                                                                                                                                                .addComponent(lblPrimaryColour2)
                                                                                                                                                .addComponent(lblPrimaryColour3)
                                                                                                                                                .addComponent(lblSecondaryColour1)
                                                                                                                                                .addComponent(lblSecondaryColour2)
                                                                                                                                                .addComponent(lblSecondaryColour3)
                                                                                                                                                .addComponent(lblBlackColour)
                                                                                                                                                .addComponent(lblWhiteColour))
                                                                                                                                .addGap(18, 18, 18)
                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                .createParallelGroup(
                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addComponent(txtPrimaryColour1,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                pnlPrimaryColour1PreviewContainer,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                .addComponent(btnPrimaryColour1Change))
                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addComponent(txtPrimaryColour2,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                pnlPrimaryColour2PreviewContainer,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                .addComponent(btnPrimaryColour2Change))
                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addComponent(txtPrimaryColour3,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                pnlPrimaryColour3PreviewContainer,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                .addComponent(btnPrimaryColour3Change))
                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addComponent(txtSecondaryColour1,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                pnlSecondaryColour1PreviewContainer,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                btnSecondaryColour1Change))
                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addComponent(txtSecondaryColour2,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                pnlSecondaryColour2PreviewContainer,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                btnSecondaryColour2Change))
                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addComponent(txtSecondaryColour3,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                pnlSecondaryColour3PreviewContainer,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                .addComponent(
                                                                                                                                                                                btnSecondaryColour3Change))
                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                                .createParallelGroup(
                                                                                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                                                                                                                false)
                                                                                                                                                                                .addGroup(
                                                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                                                                pnlThemeLayout
                                                                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                                                                .addComponent(
                                                                                                                                                                                                                                txtWhiteColour,
                                                                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                                                                .addComponent(
                                                                                                                                                                                                                                pnlWhiteColourPreviewContainer,
                                                                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                                                .createSequentialGroup()
                                                                                                                                                                                                .addComponent(
                                                                                                                                                                                                                txtBlackColour,
                                                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                                                                                .addComponent(
                                                                                                                                                                                                                pnlBlackColourPreviewContainer,
                                                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                                                                                                .addPreferredGap(
                                                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                                                                .createParallelGroup(
                                                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                                                .addComponent(
                                                                                                                                                                                                btnBlackColourChange)
                                                                                                                                                                                .addComponent(
                                                                                                                                                                                                btnWhiteColourChange,
                                                                                                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING))))
                                                                                                                                .addGap(0, 0, Short.MAX_VALUE)))
                                                                                                .addContainerGap())
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createSequentialGroup()
                                                                                                .addGroup(pnlThemeLayout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addComponent(lblMenuOpacity)
                                                                                                                .addGroup(pnlThemeLayout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(10, 10, 10)
                                                                                                                                .addComponent(sldMenuOpacity,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                                .addGap(0, 0, Short.MAX_VALUE)))));
                pnlThemeLayout.setVerticalGroup(
                                pnlThemeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThemeLayout
                                                                .createSequentialGroup()
                                                                .addContainerGap()
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblPrimaryColour1)
                                                                                                .addComponent(txtPrimaryColour1,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnPrimaryColour1Change))
                                                                                .addComponent(pnlPrimaryColour1PreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblPrimaryColour2)
                                                                                                .addComponent(txtPrimaryColour2,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnPrimaryColour2Change))
                                                                                .addComponent(pnlPrimaryColour2PreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblPrimaryColour3)
                                                                                                .addComponent(txtPrimaryColour3,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnPrimaryColour3Change))
                                                                                .addComponent(pnlPrimaryColour3PreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblSecondaryColour1)
                                                                                                .addComponent(txtSecondaryColour1,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnSecondaryColour1Change))
                                                                                .addComponent(pnlSecondaryColour1PreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblSecondaryColour2)
                                                                                                .addComponent(txtSecondaryColour2,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnSecondaryColour2Change))
                                                                                .addComponent(pnlSecondaryColour2PreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblSecondaryColour3)
                                                                                                .addComponent(txtSecondaryColour3,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnSecondaryColour3Change))
                                                                                .addComponent(pnlSecondaryColour3PreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlThemeLayout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblBlackColour)
                                                                                                .addComponent(txtBlackColour,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnBlackColourChange))
                                                                                .addComponent(pnlBlackColourPreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlThemeLayout
                                                                                .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                false)
                                                                                .addGroup(pnlThemeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblWhiteColour)
                                                                                                .addComponent(txtWhiteColour,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnWhiteColourChange))
                                                                                .addComponent(pnlWhiteColourPreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 18, 18)
                                                                .addComponent(lblMenuOpacity)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(sldMenuOpacity,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                Short.MAX_VALUE)
                                                                .addComponent(pnlThemeButtons,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addContainerGap()));

                pnlTabs.addTab("Theme", pnlTheme);

                chkWindowModeEnabled.setText("Enable Windowed Mode");
                chkWindowModeEnabled.addItemListener(evt -> chkWindowModeEnabledItemStateChanged(evt));

                lblDimensions.setText("Dimensions");

                lblDimensionsX.setText("x");

                lblBackground.setText("Background");

                txtBackgroundColour.setEditable(false);
                txtBackgroundColour.setText("#00FF00");
                txtBackgroundColour.setPreferredSize(new java.awt.Dimension(70, 24));

                btnBackgroundColourChange.setText("Change");
                btnBackgroundColourChange.addActionListener(evt -> btnBackgroundColourChangeActionPerformed(evt));

                spnWindowWidth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10000, 1));
                spnWindowWidth.setPreferredSize(new java.awt.Dimension(60, 24));
                spnWindowWidth.addChangeListener(evt -> spnWindowWidthStateChanged(evt));

                spnWindowHeight.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10000, 1));
                spnWindowHeight.setMinimumSize(new java.awt.Dimension(30, 20));
                spnWindowHeight.setPreferredSize(new java.awt.Dimension(60, 24));
                spnWindowHeight.addChangeListener(evt -> spnWindowHeightStateChanged(evt));

                lblBackgroundColour.setText("Colour");

                pnlBackgroundPreviewContainer
                                .setLayout(new javax.swing.BoxLayout(pnlBackgroundPreviewContainer,
                                                javax.swing.BoxLayout.Y_AXIS));
                pnlBackgroundPreviewContainer.add(glueBackground);

                pnlBackgroundPreview.setBorder(
                                new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                pnlBackgroundPreview.setPreferredSize(new java.awt.Dimension(20, 20));

                javax.swing.GroupLayout pnlBackgroundPreviewLayout = new javax.swing.GroupLayout(pnlBackgroundPreview);
                pnlBackgroundPreview.setLayout(pnlBackgroundPreviewLayout);
                pnlBackgroundPreviewLayout.setHorizontalGroup(
                                pnlBackgroundPreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));
                pnlBackgroundPreviewLayout.setVerticalGroup(
                                pnlBackgroundPreviewLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGap(0, 0, Short.MAX_VALUE));

                pnlBackgroundPreviewContainer.add(pnlBackgroundPreview);
                pnlBackgroundPreviewContainer.add(glueBackground2);

                lblBackgroundImageCaption.setText("Image");

                pnlBackgroundImage.setBorder(
                                new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                pnlBackgroundImage.setPreferredSize(new java.awt.Dimension(96, 96));
                pnlBackgroundImage.setLayout(new java.awt.BorderLayout());

                lblBackgroundImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                pnlBackgroundImage.add(lblBackgroundImage, java.awt.BorderLayout.CENTER);

                btnBackgroundImageChange.setText("Change");
                btnBackgroundImageChange.addActionListener(evt -> btnBackgroundImageChangeActionPerformed(evt));

                btnBackgroundImageRemove.setText("Remove");
                btnBackgroundImageRemove.addActionListener(evt -> btnBackgroundImageRemoveActionPerformed(evt));

                cmbBackgroundImageMode.setModel(new javax.swing.DefaultComboBoxModel<>());
                cmbBackgroundImageMode.addActionListener(evt -> cmbBackgroundImageModeActionPerformed(evt));

                javax.swing.GroupLayout pnlWindowModeLayout = new javax.swing.GroupLayout(pnlWindowMode);
                pnlWindowMode.setLayout(pnlWindowModeLayout);
                pnlWindowModeLayout.setHorizontalGroup(
                                pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlWindowModeLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addGroup(pnlWindowModeLayout
                                                                                .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(chkWindowModeEnabled)
                                                                                .addGroup(pnlWindowModeLayout
                                                                                                .createSequentialGroup()
                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                .addComponent(lblDimensions)
                                                                                                                .addComponent(lblBackground)
                                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addGap(10, 10, 10)
                                                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                                                .createParallelGroup(
                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                                                .addComponent(lblBackgroundImageCaption)
                                                                                                                                                .addComponent(lblBackgroundColour))))
                                                                                                .addGap(18, 18, 18)
                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                false)
                                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addComponent(spnWindowWidth,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addPreferredGap(
                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                .addComponent(lblDimensionsX)
                                                                                                                                .addPreferredGap(
                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                .addComponent(spnWindowHeight,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addComponent(txtBackgroundColour,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addPreferredGap(
                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                                                .addComponent(pnlBackgroundPreviewContainer,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addPreferredGap(
                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                .addComponent(btnBackgroundColourChange,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                Short.MAX_VALUE))
                                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                                .createSequentialGroup()
                                                                                                                                .addComponent(pnlBackgroundImage,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                                                .addPreferredGap(
                                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                                .addGroup(pnlWindowModeLayout
                                                                                                                                                .createParallelGroup(
                                                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                                                                false)
                                                                                                                                                .addComponent(btnBackgroundImageRemove,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                Short.MAX_VALUE)
                                                                                                                                                .addComponent(btnBackgroundImageChange,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                Short.MAX_VALUE)
                                                                                                                                                .addComponent(cmbBackgroundImageMode,
                                                                                                                                                                0,
                                                                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                                                                Short.MAX_VALUE))))))
                                                                .addContainerGap(139, Short.MAX_VALUE)));
                pnlWindowModeLayout.setVerticalGroup(
                                pnlWindowModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlWindowModeLayout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(chkWindowModeEnabled)
                                                                .addGap(18, 18, 18)
                                                                .addGroup(pnlWindowModeLayout
                                                                                .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(lblDimensions)
                                                                                .addComponent(lblDimensionsX)
                                                                                .addComponent(spnWindowWidth,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(spnWindowHeight,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 18, 18)
                                                                .addComponent(lblBackground)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlWindowModeLayout
                                                                                .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlWindowModeLayout
                                                                                                .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                                .addComponent(lblBackgroundColour)
                                                                                                .addComponent(txtBackgroundColour,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addComponent(btnBackgroundColourChange))
                                                                                .addComponent(pnlBackgroundPreviewContainer,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                24,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(pnlWindowModeLayout
                                                                                .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addGroup(pnlWindowModeLayout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(btnBackgroundImageChange)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(cmbBackgroundImageMode,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(btnBackgroundImageRemove))
                                                                                .addGroup(pnlWindowModeLayout
                                                                                                .createSequentialGroup()
                                                                                                .addGap(4, 4, 4)
                                                                                                .addComponent(lblBackgroundImageCaption))
                                                                                .addComponent(pnlBackgroundImage,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addContainerGap(150, Short.MAX_VALUE)));

                pnlTabs.addTab("WindowMode", pnlWindowMode);

                pnlAbout.setLayout(new javax.swing.BoxLayout(pnlAbout, javax.swing.BoxLayout.Y_AXIS));
                pnlAbout.add(glue1);

                lblIcon.setAlignmentX(0.5F);
                lblIcon.setMinimumSize(new java.awt.Dimension(64, 64));
                lblIcon.setPreferredSize(new java.awt.Dimension(64, 64));
                pnlAbout.add(lblIcon);
                pnlAbout.add(rigid1);

                lblShimejiEE.setFont(lblShimejiEE.getFont().deriveFont(
                                lblShimejiEE.getFont().getStyle() | java.awt.Font.BOLD,
                                lblShimejiEE.getFont().getSize() + 10));
                lblShimejiEE.setText("Shimeji");
                lblShimejiEE.setAlignmentX(0.5F);
                pnlAbout.add(lblShimejiEE);
                pnlAbout.add(rigid2);

                lblVersion.setFont(lblVersion.getFont().deriveFont(lblVersion.getFont().getSize() + 4f));
                lblVersion.setText("2.0.0");
                lblVersion.setAlignmentX(0.5F);
                pnlAbout.add(lblVersion);
                pnlAbout.add(rigid3);

                lblDevelopedBy.setText("developed by");
                lblDevelopedBy.setAlignmentX(0.5F);
                pnlAbout.add(lblDevelopedBy);

                lblKilkakon.setText("Kilkakon & Zero & Begonia");
                lblKilkakon.setAlignmentX(0.5F);
                pnlAbout.add(lblKilkakon);
                pnlAbout.add(rigid4);

                pnlAboutButtons.setMaximumSize(new java.awt.Dimension(32767, 36));
                pnlAboutButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

                btnWebsite.setText("Website");
                btnWebsite.setAlignmentX(0.5F);
                btnWebsite.setMaximumSize(new java.awt.Dimension(130, 26));
                btnWebsite.setPreferredSize(new java.awt.Dimension(100, 26));
                btnWebsite.addActionListener(evt -> btnWebsiteActionPerformed(evt));
                pnlAboutButtons.add(btnWebsite);

                btnDiscord.setText("Discord");
                btnDiscord.setAlignmentX(0.5F);
                btnDiscord.setMaximumSize(new java.awt.Dimension(130, 26));
                btnDiscord.setPreferredSize(new java.awt.Dimension(100, 26));
                btnDiscord.addActionListener(evt -> btnDiscordActionPerformed(evt));
                pnlAboutButtons.add(btnDiscord);

                btnPatreon.setText("Patreon");
                btnPatreon.setAlignmentX(0.5F);
                btnPatreon.setMaximumSize(new java.awt.Dimension(130, 26));
                btnPatreon.setPreferredSize(new java.awt.Dimension(100, 26));
                btnPatreon.addActionListener(evt -> btnPatreonActionPerformed(evt));
                pnlAboutButtons.add(btnPatreon);

                pnlAbout.add(pnlAboutButtons);
                pnlAbout.add(glue2);

                pnlTabs.addTab("About", pnlAbout);

                pnlFooter.setPreferredSize(new java.awt.Dimension(380, 36));
                pnlFooter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

                btnDone.setText("Done");
                btnDone.setMaximumSize(new java.awt.Dimension(130, 26));
                btnDone.setMinimumSize(new java.awt.Dimension(95, 23));
                btnDone.setName(""); // NOI18N
                btnDone.setPreferredSize(new java.awt.Dimension(130, 26));
                btnDone.addActionListener(evt -> btnDoneActionPerformed(evt));
                pnlFooter.add(btnDone);

                btnCancel.setText("Cancel");
                btnCancel.setMaximumSize(new java.awt.Dimension(130, 26));
                btnCancel.setMinimumSize(new java.awt.Dimension(95, 23));
                btnCancel.setPreferredSize(new java.awt.Dimension(130, 26));
                btnCancel.addActionListener(evt -> btnCancelActionPerformed(evt));
                pnlFooter.add(btnCancel);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addGroup(layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(pnlFooter,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                0,
                                                                                                Short.MAX_VALUE)
                                                                                .addComponent(pnlTabs))
                                                                .addContainerGap()));
                layout.setVerticalGroup(
                                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(pnlTabs)
                                                                .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(pnlFooter,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addContainerGap()));

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void btnDoneActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnDoneActionPerformed
        {// GEN-HEADEREND:event_btnDoneActionPerformed
         // done button
                try {
                        Properties properties = Main.getInstance().getProperties();
                        String interactiveWindows = listData.toString().replace("[", "").replace("]", "").replace(", ",
                                        "/");
                        String interactiveWindowsBlacklist = blacklistData.toString().replace("[", "").replace("]", "")
                                        .replace(", ", "/");
                        String[] windowArray = properties.getProperty("WindowSize", "600x500").split("x");
                        Dimension window = new Dimension(Integer.parseInt(windowArray[0]),
                                        Integer.parseInt(windowArray[1]));

                        environmentReloadRequired = properties.getProperty("Environment", "generic")
                                        .equals("virtual") != windowedMode ||
                                        !window.equals(windowSize) ||
                                        !Color.decode(properties.getProperty("Background", "#00FF00"))
                                                        .equals(backgroundColour)
                                        ||
                                        !properties.getProperty("BackgroundMode", "centre").equals(backgroundMode) ||
                                        !properties.getProperty("BackgroundImage", "")
                                                        .equalsIgnoreCase(
                                                                        backgroundImage == null ? "" : backgroundImage);
                        imageReloadRequired = !properties.getProperty("Filter", "false").equalsIgnoreCase(filter) ||
                                        Double.parseDouble(properties.getProperty("Scaling", "1.0")) != scaling ||
                                        Double.parseDouble(properties.getProperty("Opacity", "1.0")) != opacity;
                        interactiveWindowReloadRequired = !properties.getProperty("InteractiveWindows", "")
                                        .equals(interactiveWindows) ||
                                        !properties.getProperty("InteractiveWindowsBlacklist", "")
                                                        .equals(interactiveWindowsBlacklist);
                        // Config file name
                        String baseDir = System.getProperty("app.dir", System.getProperty("user.dir", "."));
                        String configFile = baseDir + "/conf/settings.properties";

                        try (FileOutputStream output = new FileOutputStream(configFile)) {
                                properties.setProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString());
                                properties.setProperty("AlwaysShowInformationScreen",
                                                alwaysShowInformationScreen.toString());
                                properties.setProperty("Opacity", Double.toString(opacity));
                                properties.setProperty("Scaling", Double.toString(scaling));
                                properties.setProperty("Filter", filter);
                                properties.setProperty("InteractiveWindows", interactiveWindows);
                                properties.setProperty("InteractiveWindowsBlacklist", interactiveWindowsBlacklist);
                                properties.setProperty("Environment", windowedMode ? "virtual" : "generic");
                                if (windowedMode) {
                                        properties.setProperty("WindowSize",
                                                        windowSize.width + "x" + windowSize.height);
                                        properties.setProperty("Background",
                                                        String.format("#%02X%02X%02X", backgroundColour.getRed(),
                                                                        backgroundColour.getGreen(),
                                                                        backgroundColour.getBlue()));
                                        properties.setProperty("BackgroundMode", backgroundMode);
                                        properties.setProperty("BackgroundImage",
                                                        backgroundImage == null ? "" : backgroundImage);
                                }

                                properties.store(output, "Shimeji-ee Configuration Options");
                        }

                        try (FileOutputStream themeOutput = new FileOutputStream(themeFile)) {
                                Properties themeProps = new Properties();
                                themeProps.setProperty("PrimaryColour1",
                                                String.format("#%02X%02X%02X", primaryColour1.getRed(),
                                                                primaryColour1.getGreen(), primaryColour1.getBlue()));
                                themeProps.setProperty("PrimaryColour2",
                                                String.format("#%02X%02X%02X", primaryColour2.getRed(),
                                                                primaryColour2.getGreen(), primaryColour2.getBlue()));
                                themeProps.setProperty("PrimaryColour3",
                                                String.format("#%02X%02X%02X", primaryColour3.getRed(),
                                                                primaryColour3.getGreen(), primaryColour3.getBlue()));
                                themeProps.setProperty("SecondaryColour1",
                                                String.format("#%02X%02X%02X", secondaryColour1.getRed(),
                                                                secondaryColour1.getGreen(),
                                                                secondaryColour1.getBlue()));
                                themeProps.setProperty("SecondaryColour2",
                                                String.format("#%02X%02X%02X", secondaryColour2.getRed(),
                                                                secondaryColour2.getGreen(),
                                                                secondaryColour2.getBlue()));
                                themeProps.setProperty("SecondaryColour3",
                                                String.format("#%02X%02X%02X", secondaryColour3.getRed(),
                                                                secondaryColour3.getGreen(),
                                                                secondaryColour3.getBlue()));
                                themeProps.setProperty("BlackColour",
                                                String.format("#%02X%02X%02X", blackColour.getRed(),
                                                                blackColour.getGreen(), blackColour.getBlue()));
                                themeProps.setProperty("WhiteColour",
                                                String.format("#%02X%02X%02X", whiteColour.getRed(),
                                                                whiteColour.getGreen(), whiteColour.getBlue()));
                                themeProps.setProperty("MenuOpacity", String.valueOf((int) (menuOpacity * 255)));
                                themeProps.setProperty("FontName", font.getName());
                                themeProps.setProperty("FontStyle", String.valueOf(font.getStyle()));
                                themeProps.setProperty("FontSize", String.valueOf(font.getSize()));

                                themeProps.store(themeOutput, "FlatLaf Theme Configuration");
                        }
                } catch (Exception ignored) {
                }
                dispose();
        }// GEN-LAST:event_btnDoneActionPerformed

        private void btnCancelActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnCancelActionPerformed
        {// GEN-HEADEREND:event_btnCancelActionPerformed
         // 暂时注释掉主题相关代码，恢复为旧版本兼容
                themeProperties = oldThemeProperties;
                refreshTheme();
                dispose();
        }// GEN-LAST:event_btnCancelActionPerformed

        private void btnAddInteractiveWindowActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnAddInteractiveWindowActionPerformed
        {// GEN-HEADEREND:event_btnAddInteractiveWindowActionPerformed
         // add button
                String inputValue = JOptionPane.showInputDialog(rootPane,
                                Main.getInstance().getLanguageBundle().getString("InteractiveWindowHintMessage"),
                                Main.getInstance().getLanguageBundle()
                                                .getString(pnlInteractiveTabs.getSelectedIndex() == 0
                                                                ? "AddInteractiveWindow"
                                                                : "BlacklistInteractiveWindow"),
                                JOptionPane.QUESTION_MESSAGE);
                if (inputValue != null && !inputValue.trim().isEmpty() && !inputValue.contains("/")) {
                        if (pnlInteractiveTabs.getSelectedIndex() == 0) {
                                listData.add(inputValue.trim());
                                lstInteractiveWindows.setListData(listData.toArray(new String[0]));
                        } else {
                                blacklistData.add(inputValue.trim());
                                lstInteractiveWindowsBlacklist.setListData(blacklistData.toArray(new String[0]));
                        }
                }
        }// GEN-LAST:event_btnAddInteractiveWindowActionPerformed

        private void btnRemoveInteractiveWindowActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnRemoveInteractiveWindowActionPerformed
        {// GEN-HEADEREND:event_btnRemoveInteractiveWindowActionPerformed
         // delete button
                if (pnlInteractiveTabs.getSelectedIndex() == 0) {
                        if (lstInteractiveWindows.getSelectedIndex() != -1) {
                                listData.remove(lstInteractiveWindows.getSelectedIndex());
                                lstInteractiveWindows.setListData(listData.toArray(new String[0]));
                        }
                } else {
                        if (lstInteractiveWindowsBlacklist.getSelectedIndex() != -1) {
                                blacklistData.remove(lstInteractiveWindowsBlacklist.getSelectedIndex());
                                lstInteractiveWindowsBlacklist.setListData(blacklistData.toArray(new String[0]));
                        }
                }
        }// GEN-LAST:event_btnRemoveInteractiveWindowActionPerformed

        private void chkAlwaysShowShimejiChooserItemStateChanged(java.awt.event.ItemEvent evt)// GEN-FIRST:event_chkAlwaysShowShimejiChooserItemStateChanged
        {// GEN-HEADEREND:event_chkAlwaysShowShimejiChooserItemStateChanged
                alwaysShowShimejiChooser = evt.getStateChange() == ItemEvent.SELECTED;
        }// GEN-LAST:event_chkAlwaysShowShimejiChooserItemStateChanged

        private void radFilterItemStateChanged(java.awt.event.ItemEvent evt)// GEN-FIRST:event_radFilterItemStateChanged
        {// GEN-HEADEREND:event_radFilterItemStateChanged
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                        Object source = evt.getItemSelectable();

                        if (source == radFilterNearest)
                                filter = "nearest";
                        else if (source == radFilterHqx) {
                                // Check license for HQX filter
                                LicenseManager licenseManager = LicenseManager.getInstance();
                                LicenseLevel currentLevel = licenseManager.getCurrentLicenseLevel();

                                if (currentLevel == LicenseLevel.NO_KEY) {
                                        // For free version, deny HQX filter access
                                        JOptionPane.showMessageDialog(this,
                                                        Main.getInstance().getLanguageBundle()
                                                                        .getString("PremiumFeatureRequired"),
                                                        Main.getInstance().getLanguageBundle()
                                                                        .getString("LicenseRequired"),
                                                        JOptionPane.WARNING_MESSAGE);

                                        // Revert to nearest neighbor filter
                                        radFilterNearest.setSelected(true);
                                        filter = "nearest";
                                } else {
                                        // Advanced or Special license - allow HQX
                                        filter = "hqx";
                                }
                        } else
                                filter = "bicubic";
                }
        }// GEN-LAST:event_radFilterItemStateChanged

        private void sldScalingStateChanged(javax.swing.event.ChangeEvent evt)// GEN-FIRST:event_sldScalingStateChanged
        {// GEN-HEADEREND:event_sldScalingStateChanged
                int currentValue = sldScaling.getValue();
                double newScaling = currentValue / 10.0;
                
                if (sldScaling.getValueIsAdjusting()) {
                        // 智能吸附到关键点
                        int[] snapPoints = {5, 10, 15, 20, 25, 30, 35, 40}; // 对应0.5x到4x
                        for (int snapPoint : snapPoints) {
                                if (Math.abs(currentValue - snapPoint) <= 1) { // ±0.1的范围
                                        if (currentValue != snapPoint) {
                                                SwingUtilities.invokeLater(() -> sldScaling.setValue(snapPoint));
                                                return;
                                        }
                                        break;
                                }
                        }
                        
                        // 显示实时提示
                        sldScaling.setToolTipText(String.format("%.1fx", newScaling));
                }
                
                // 更新值并处理许可证检查
                if (!sldScaling.getValueIsAdjusting()) {
                        if (sldScaling.getValue() == 0)
                                sldScaling.setValue(5);
                        else {
                                scaling = sldScaling.getValue() / 10.0;
                                
                                // 同步更新 Spinner 值
                                double spinnerValue = (Double) spnScaling.getValue();
                                if (Math.abs(spinnerValue - scaling) > 0.01) {
                                        spnScaling.setValue(scaling);
                                }
                                
                                if (scaling == 2 || scaling == 3 || scaling == 4) {
                                        // Check license before enabling HQX filter
                                        LicenseManager licenseManager = LicenseManager.getInstance();
                                        LicenseLevel currentLevel = licenseManager.getCurrentLicenseLevel();

                                        if (currentLevel == LicenseLevel.NO_KEY) {
                                                // For free version, disable HQX filter
                                                radFilterHqx.setEnabled(false);
                                                if (filter.equals("hqx")) {
                                                        radFilterNearest.setSelected(true);
                                                        filter = "nearest";
                                                }
                                        } else {
                                                // Advanced or Special license - enable HQX
                                                radFilterHqx.setEnabled(true);
                                        }
                                } else {
                                        radFilterHqx.setEnabled(false);
                                        if (filter.equals("hqx")) {
                                                radFilterNearest.setSelected(true);
                                                filter = "nearest";
                                        }
                                }
                                imageReloadRequired = true;
                        }
                }
        }// GEN-LAST:event_sldScalingStateChanged

        private void spnScalingStateChanged(javax.swing.event.ChangeEvent evt) {
                double value = (Double) spnScaling.getValue();
                
                // 同步滑块值
                int sliderValue = (int) (value * 10);
                if (sldScaling.getValue() != sliderValue) {
                        sldScaling.setValue(sliderValue);
                }
                
                // 更新scaling值
                if (Math.abs(scaling - value) > 0.01) {
                        scaling = value;
                        imageReloadRequired = true;
                }
        }

        private void btnFilterHelpActionPerformed(java.awt.event.ActionEvent evt) {
                String title = Main.getInstance().getLanguageBundle().getString("FilterHelpTitle");
                String content = Main.getInstance().getLanguageBundle().getString("FilterHelpContent");

                // 获取DPI缩放因子
                float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96"))
                                / 96;

                // 使用 FlatLaf 样式的对话框
                javax.swing.JDialog dialog = new javax.swing.JDialog(this, title, true);
                dialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);

                // 只创建内容标签，不再重复标题
                javax.swing.JLabel contentLabel = new javax.swing.JLabel(content);
                contentLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

                // 关闭按钮根据DPI缩放
                javax.swing.JButton closeButton = new javax.swing.JButton(
                                Main.getInstance().getLanguageBundle().getString("Close"));
                int buttonWidth = (int) (80 * menuScaling);
                int buttonHeight = (int) (32 * menuScaling);
                closeButton.setPreferredSize(new java.awt.Dimension(buttonWidth, buttonHeight));
                closeButton.addActionListener(e -> dialog.dispose());

                javax.swing.JPanel buttonPanel = new javax.swing.JPanel(new java.awt.FlowLayout());
                buttonPanel.add(closeButton);

                dialog.setLayout(new java.awt.BorderLayout());
                dialog.add(contentLabel, java.awt.BorderLayout.CENTER);
                dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

                dialog.pack();
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
        }

        private void btnWebsiteActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnWebsiteActionPerformed
        {// GEN-HEADEREND:event_btnWebsiteActionPerformed
                String websiteUrl = Main.getInstance().getProperties().getProperty("WebsiteURL",
                                "https://github.com/DCRepairCenter/DCShimeji");
                browseToUrl(websiteUrl);
        }// GEN-LAST:event_btnWebsiteActionPerformed

        private void btnDiscordActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnDiscordActionPerformed
        {// GEN-HEADEREND:event_btnDiscordActionPerformed
                browseToUrl("https://discord.gg/NBq3zqfA2B");
        }// GEN-LAST:event_btnDiscordActionPerformed

        private void btnPatreonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnPatreonActionPerformed
        {// GEN-HEADEREND:event_btnPatreonActionPerformed
                browseToUrl("https://patreon.com/kilkakon");
        }// GEN-LAST:event_btnPatreonActionPerformed

        private void sldOpacityStateChanged(javax.swing.event.ChangeEvent evt)// GEN-FIRST:event_sldOpacityStateChanged
        {// GEN-HEADEREND:event_sldOpacityStateChanged
                if (!sldOpacity.getValueIsAdjusting()) {
                        if (sldOpacity.getValue() == 0)
                                sldOpacity.setValue(5);
                        else
                                opacity = sldOpacity.getValue() / 100.0;
                }
        }// GEN-LAST:event_sldOpacityStateChanged

        private void spnWindowHeightStateChanged(javax.swing.event.ChangeEvent evt)// GEN-FIRST:event_spnWindowHeightStateChanged
        {// GEN-HEADEREND:event_spnWindowHeightStateChanged
                windowSize.height = ((SpinnerNumberModel) spnWindowHeight.getModel()).getNumber().intValue();
        }// GEN-LAST:event_spnWindowHeightStateChanged

        private void spnWindowWidthStateChanged(javax.swing.event.ChangeEvent evt)// GEN-FIRST:event_spnWindowWidthStateChanged
        {// GEN-HEADEREND:event_spnWindowWidthStateChanged
                windowSize.width = ((SpinnerNumberModel) spnWindowWidth.getModel()).getNumber().intValue();
        }// GEN-LAST:event_spnWindowWidthStateChanged

        private void btnBackgroundColourChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnBackgroundColourChangeActionPerformed
        {// GEN-HEADEREND:event_btnBackgroundColourChangeActionPerformed
                backgroundColour = chooseColour(backgroundColour, txtBackgroundColour, pnlBackgroundPreview,
                                "ChooseBackgroundColour");
        }// GEN-LAST:event_btnBackgroundColourChangeActionPerformed

        private void chkWindowModeEnabledItemStateChanged(java.awt.event.ItemEvent evt)// GEN-FIRST:event_chkWindowModeEnabledItemStateChanged
        {// GEN-HEADEREND:event_chkWindowModeEnabledItemStateChanged
                windowedMode = evt.getStateChange() == ItemEvent.SELECTED;
                spnWindowWidth.setEnabled(windowedMode);
                spnWindowHeight.setEnabled(windowedMode);
                btnBackgroundColourChange.setEnabled(windowedMode);
                btnBackgroundImageChange.setEnabled(windowedMode);
                cmbBackgroundImageMode.setEnabled(windowedMode && backgroundImage != null);
                btnBackgroundImageRemove.setEnabled(windowedMode && backgroundImage != null);
        }// GEN-LAST:event_chkWindowModeEnabledItemStateChanged

        private void btnBackgroundImageChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnBackgroundImageChangeActionPerformed
        {// GEN-HEADEREND:event_btnBackgroundImageChangeActionPerformed
                final JFileChooser dialog = new JFileChooser();
                dialog.setDialogTitle(Main.getInstance().getLanguageBundle().getString("ChooseBackgroundImage"));
                // dialog.setFileFilter( );

                if (dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        try {
                                backgroundImage = dialog.getSelectedFile().getCanonicalPath();
                                refreshBackgroundImage();
                        } catch (Exception e) {
                                backgroundImage = null;
                                lblBackgroundImage.setIcon(null);
                        }
                        cmbBackgroundImageMode.setEnabled(windowedMode && backgroundImage != null);
                        btnBackgroundImageRemove.setEnabled(windowedMode && backgroundImage != null);
                }
        }// GEN-LAST:event_btnBackgroundImageChangeActionPerformed

        private void btnBackgroundImageRemoveActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnBackgroundImageRemoveActionPerformed
        {// GEN-HEADEREND:event_btnBackgroundImageRemoveActionPerformed
                backgroundImage = null;
                lblBackgroundImage.setIcon(null);
                cmbBackgroundImageMode.setEnabled(false);
                btnBackgroundImageRemove.setEnabled(false);
        }// GEN-LAST:event_btnBackgroundImageRemoveActionPerformed

        private void cmbBackgroundImageModeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_cmbBackgroundImageModeActionPerformed
        {// GEN-HEADEREND:event_cmbBackgroundImageModeActionPerformed
                if (cmbBackgroundImageMode.getSelectedIndex() > -1)
                        backgroundMode = backgroundModes[cmbBackgroundImageMode.getSelectedIndex()];
                refreshBackgroundImage();
        }// GEN-LAST:event_cmbBackgroundImageModeActionPerformed

        private void btnPrimaryColour1ChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnPrimaryColour1ChangeActionPerformed
        {// GEN-HEADEREND:event_btnPrimaryColour1ChangeActionPerformed
                primaryColour1 = chooseColour(primaryColour1, txtPrimaryColour1, pnlPrimaryColour1Preview,
                                "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnPrimaryColour1ChangeActionPerformed

        private void btnPrimaryColour2ChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnPrimaryColour2ChangeActionPerformed
        {// GEN-HEADEREND:event_btnPrimaryColour2ChangeActionPerformed
                primaryColour2 = chooseColour(primaryColour2, txtPrimaryColour2, pnlPrimaryColour2Preview,
                                "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnPrimaryColour2ChangeActionPerformed

        private void btnPrimaryColour3ChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnPrimaryColour3ChangeActionPerformed
        {// GEN-HEADEREND:event_btnPrimaryColour3ChangeActionPerformed
                primaryColour3 = chooseColour(primaryColour3, txtPrimaryColour3, pnlPrimaryColour3Preview,
                                "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnPrimaryColour3ChangeActionPerformed

        private void btnSecondaryColour1ChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnSecondaryColour1ChangeActionPerformed
        {// GEN-HEADEREND:event_btnSecondaryColour1ChangeActionPerformed
                secondaryColour1 = chooseColour(secondaryColour1, txtSecondaryColour1, pnlSecondaryColour1Preview,
                                "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnSecondaryColour1ChangeActionPerformed

        private void btnSecondaryColour2ChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnSecondaryColour2ChangeActionPerformed
        {// GEN-HEADEREND:event_btnSecondaryColour2ChangeActionPerformed
                secondaryColour2 = chooseColour(secondaryColour2, txtSecondaryColour2, pnlSecondaryColour2Preview,
                                "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnSecondaryColour2ChangeActionPerformed

        private void btnSecondaryColour3ChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnSecondaryColour3ChangeActionPerformed
        {// GEN-HEADEREND:event_btnSecondaryColour3ChangeActionPerformed
                secondaryColour3 = chooseColour(secondaryColour3, txtSecondaryColour3, pnlSecondaryColour3Preview,
                                "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnSecondaryColour3ChangeActionPerformed

        private void sldMenuOpacityStateChanged(javax.swing.event.ChangeEvent evt)// GEN-FIRST:event_sldMenuOpacityStateChanged
        {// GEN-HEADEREND:event_sldMenuOpacityStateChanged
                if (!sldMenuOpacity.getValueIsAdjusting()) {
                        if (sldMenuOpacity.getValue() == 0)
                                sldMenuOpacity.setValue(1);
                        else {
                                menuOpacity = sldMenuOpacity.getValue() / 100.0;
                                // Menu opacity for FlatLaf will be handled differently
                                refreshTheme();
                        }
                }
        }// GEN-LAST:event_sldMenuOpacityStateChanged

        private void btnChangeFontActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnChangeFontActionPerformed
        {// GEN-HEADEREND:event_btnChangeFontActionPerformed
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                // 使用简单的字体选择对话框
                Font newFont = showFontDialog(frame, font);
                if (newFont != null) {
                        font = newFont;
                        refreshTheme();
                }
        }// GEN-LAST:event_btnChangeFontActionPerformed

        private void btnResetActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnResetActionPerformed
        {// GEN-HEADEREND:event_btnResetActionPerformed
                float menuScaling = Float.parseFloat(Main.getInstance().getProperties().getProperty("MenuDPI", "96"))
                                / 96;
                primaryColour1 = Color.decode("#1EA6EB");
                primaryColour2 = Color.decode("#28B0F5");
                primaryColour3 = Color.decode("#32BAFF");
                secondaryColour1 = Color.decode("#BCBCBE");
                secondaryColour2 = Color.decode("#C6C6C8");
                secondaryColour3 = Color.decode("#D0D0D2");
                blackColour = Color.decode("#000000");
                whiteColour = Color.decode("#FFFFFF");
                font = Font.decode("SansSerif-PLAIN-12");
                font = font.deriveFont(font.getSize() * menuScaling);
                pnlPrimaryColour1Preview.setBackground(primaryColour1);
                txtPrimaryColour1.setText(
                                String.format("#%02X%02X%02X", primaryColour1.getRed(), primaryColour1.getGreen(),
                                                primaryColour1.getBlue()));
                pnlPrimaryColour2Preview.setBackground(primaryColour2);
                txtPrimaryColour2.setText(
                                String.format("#%02X%02X%02X", primaryColour2.getRed(), primaryColour2.getGreen(),
                                                primaryColour2.getBlue()));
                pnlPrimaryColour3Preview.setBackground(primaryColour3);
                txtPrimaryColour3.setText(
                                String.format("#%02X%02X%02X", primaryColour3.getRed(), primaryColour2.getGreen(),
                                                primaryColour3.getBlue()));
                pnlSecondaryColour1Preview.setBackground(secondaryColour1);
                txtSecondaryColour1.setText(String.format("#%02X%02X%02X", secondaryColour1.getRed(),
                                secondaryColour1.getGreen(), secondaryColour1.getBlue()));
                pnlSecondaryColour2Preview.setBackground(secondaryColour2);
                txtSecondaryColour2.setText(String.format("#%02X%02X%02X", secondaryColour2.getRed(),
                                secondaryColour2.getGreen(), secondaryColour2.getBlue()));
                pnlSecondaryColour3Preview.setBackground(secondaryColour3);
                txtSecondaryColour3.setText(String.format("#%02X%02X%02X", secondaryColour3.getRed(),
                                secondaryColour3.getGreen(), secondaryColour3.getBlue()));
                pnlBlackColourPreview.setBackground(blackColour);
                txtBlackColour.setText(
                                String.format("#%02X%02X%02X", blackColour.getRed(), blackColour.getGreen(),
                                                blackColour.getBlue()));
                pnlWhiteColourPreview.setBackground(whiteColour);
                txtWhiteColour.setText(
                                String.format("#%02X%02X%02X", whiteColour.getRed(), whiteColour.getGreen(),
                                                whiteColour.getBlue()));
                menuOpacity = 1.0;
                sldMenuOpacity.setValue((int) (menuOpacity * 100));
                refreshTheme();
        }// GEN-LAST:event_btnResetActionPerformed

        private void chkAlwaysShowInformationScreenItemStateChanged(java.awt.event.ItemEvent evt)// GEN-FIRST:event_chkAlwaysShowInformationScreenItemStateChanged
        {// GEN-HEADEREND:event_chkAlwaysShowInformationScreenItemStateChanged
                alwaysShowInformationScreen = evt.getStateChange() == ItemEvent.SELECTED;
        }// GEN-LAST:event_chkAlwaysShowInformationScreenItemStateChanged

        private void btnBlackColourChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnBlackColourChangeActionPerformed
        {// GEN-HEADEREND:event_btnBlackColourChangeActionPerformed
                blackColour = chooseColour(blackColour, txtBlackColour, pnlBlackColourPreview, "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnBlackColourChangeActionPerformed

        private void btnWhiteColourChangeActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_btnWhiteColourChangeActionPerformed
        {// GEN-HEADEREND:event_btnWhiteColourChangeActionPerformed
                whiteColour = chooseColour(whiteColour, txtWhiteColour, pnlWhiteColourPreview, "ChooseColour");
                refreshTheme();
        }// GEN-LAST:event_btnWhiteColourChangeActionPerformed

        private Color chooseColour(Color colour, JTextField field, JPanel preview, String title) {
                Color newColour = JColorChooser.showDialog(this,
                                Main.getInstance().getLanguageBundle().getString(title),
                                colour);

                if (newColour != null) {
                        colour = newColour;
                        field.setText(String.format("#%02X%02X%02X", colour.getRed(), colour.getGreen(),
                                        colour.getBlue()));
                        preview.setBackground(colour);
                }

                return colour;
        }

        private void refreshBackgroundImage() {
                Dimension size = pnlBackgroundImage.getPreferredSize();
                Image image = new ImageIcon(backgroundImage).getImage();

                if (backgroundMode.equals("stretch")) {
                        image = image.getScaledInstance(size.width,
                                        size.height,
                                        java.awt.Image.SCALE_SMOOTH);

                } else if (!backgroundMode.equals("centre")) {
                        double factor = backgroundMode.equals("fit")
                                        ? Math.min(size.width / (double) image.getWidth(null),
                                                        size.height / (double) image.getHeight(null))
                                        : Math.max(size.width / (double) image.getWidth(null),
                                                        size.height / (double) image.getHeight(null));
                        image = image.getScaledInstance((int) (factor * image.getWidth(null)),
                                        (int) (factor * image.getHeight(null)),
                                        java.awt.Image.SCALE_SMOOTH);
                }

                lblBackgroundImage.setIcon(new ImageIcon(image));
                lblBackgroundImage.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
        }

        private void refreshTheme() {
                try {
                        // 应用FlatLaf主题颜色到UIManager
                        UIManager.put("@accentColor", primaryColour1);
                        UIManager.put("Button.background", primaryColour2);
                        UIManager.put("Button.hoverBackground", primaryColour3);
                        UIManager.put("Panel.background", secondaryColour1);
                        UIManager.put("TextField.background", secondaryColour2);
                        UIManager.put("ComboBox.background", secondaryColour3);

                        // 如果是FlatLaf，更新UI
                        if (lookAndFeel instanceof FlatLaf) {
                                FlatLaf.updateUI();
                        }

                        SwingUtilities.updateComponentTreeUI(this);
                        pack();
                } catch (Exception ex) {
                        // 静默处理异常
                }
        }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton btnAddInteractiveWindow;
        private javax.swing.JButton btnBackgroundColourChange;
        private javax.swing.JButton btnBackgroundImageChange;
        private javax.swing.JButton btnBackgroundImageRemove;
        private javax.swing.JButton btnBlackColourChange;
        private javax.swing.JButton btnCancel;
        private javax.swing.JButton btnChangeFont;
        private javax.swing.JButton btnDiscord;
        private javax.swing.JButton btnDone;
        private javax.swing.JButton btnFilterHelp;
        private javax.swing.JButton btnPatreon;
        private javax.swing.JButton btnPrimaryColour1Change;
        private javax.swing.JButton btnPrimaryColour2Change;
        private javax.swing.JButton btnPrimaryColour3Change;
        private javax.swing.JButton btnRemoveInteractiveWindow;
        private javax.swing.JButton btnReset;
        private javax.swing.JButton btnSecondaryColour1Change;
        private javax.swing.JButton btnSecondaryColour2Change;
        private javax.swing.JButton btnSecondaryColour3Change;
        private javax.swing.JButton btnWebsite;
        private javax.swing.JButton btnWhiteColourChange;
        private javax.swing.JCheckBox chkAlwaysShowInformationScreen;
        private javax.swing.JCheckBox chkAlwaysShowShimejiChooser;
        private javax.swing.JCheckBox chkWindowModeEnabled;
        private javax.swing.JComboBox<String> cmbBackgroundImageMode;
        private javax.swing.ButtonGroup grpFilter;
        private javax.swing.JLabel lblBackground;
        private javax.swing.JLabel lblBackgroundImage;
        private javax.swing.JLabel lblBlackColour;
        private javax.swing.JLabel lblDevelopedBy;
        private javax.swing.JLabel lblDimensions;
        private javax.swing.JLabel lblFilter;
        private javax.swing.JLabel lblIcon;
        private javax.swing.JLabel lblMenuOpacity;
        private javax.swing.JLabel lblOpacity;
        private javax.swing.JLabel lblPrimaryColour1;
        private javax.swing.JLabel lblPrimaryColour2;
        private javax.swing.JLabel lblPrimaryColour3;
        private javax.swing.JLabel lblScaling;
        private javax.swing.JLabel lblSecondaryColour1;
        private javax.swing.JLabel lblSecondaryColour2;
        private javax.swing.JLabel lblSecondaryColour3;
        private javax.swing.JLabel lblShimejiEE;
        private javax.swing.JLabel lblWhiteColour;
        private javax.swing.JList<String> lstInteractiveWindows;
        private javax.swing.JList<String> lstInteractiveWindowsBlacklist;
        private javax.swing.JTabbedPane pnlInteractiveTabs;
        private javax.swing.JPanel pnlWhitelistTab;
        private javax.swing.JPanel pnlBlacklistTab;
        private javax.swing.JPanel pnlAboutButtons;
        private javax.swing.JPanel pnlBackgroundImage;
        private javax.swing.JPanel pnlBackgroundPreview;
        private javax.swing.JPanel pnlBackgroundPreviewContainer;
        private javax.swing.JPanel pnlBlackColourPreview;
        private javax.swing.JPanel pnlBlackColourPreviewContainer;
        private javax.swing.JPanel pnlFooter;
        private javax.swing.JPanel pnlInteractiveButtons;
        private javax.swing.JPanel pnlPrimaryColour1Preview;
        private javax.swing.JPanel pnlPrimaryColour1PreviewContainer;
        private javax.swing.JPanel pnlPrimaryColour2Preview;
        private javax.swing.JPanel pnlPrimaryColour2PreviewContainer;
        private javax.swing.JPanel pnlPrimaryColour3Preview;
        private javax.swing.JPanel pnlPrimaryColour3PreviewContainer;
        private javax.swing.JPanel pnlSecondaryColour1Preview;
        private javax.swing.JPanel pnlSecondaryColour1PreviewContainer;
        private javax.swing.JPanel pnlSecondaryColour2Preview;
        private javax.swing.JPanel pnlSecondaryColour2PreviewContainer;
        private javax.swing.JPanel pnlSecondaryColour3Preview;
        private javax.swing.JPanel pnlSecondaryColour3PreviewContainer;
        private javax.swing.JTabbedPane pnlTabs;
        private javax.swing.JPanel pnlThemeButtons;
        private javax.swing.JPanel pnlWhiteColourPreview;
        private javax.swing.JPanel pnlWhiteColourPreviewContainer;
        private javax.swing.JRadioButton radFilterBicubic;
        private javax.swing.JRadioButton radFilterHqx;
        private javax.swing.JRadioButton radFilterNearest;
        private javax.swing.JSlider sldMenuOpacity;
        private javax.swing.JSlider sldOpacity;
        private javax.swing.JSlider sldScaling;
        private javax.swing.JSpinner spnScaling;
        private javax.swing.JSpinner spnWindowHeight;
        private javax.swing.JSpinner spnWindowWidth;
        private javax.swing.JTextField txtBackgroundColour;
        private javax.swing.JTextField txtBlackColour;
        private javax.swing.JTextField txtPrimaryColour1;
        private javax.swing.JTextField txtPrimaryColour2;
        private javax.swing.JTextField txtPrimaryColour3;
        private javax.swing.JTextField txtSecondaryColour1;
        private javax.swing.JTextField txtSecondaryColour2;
        private javax.swing.JTextField txtSecondaryColour3;
        private javax.swing.JTextField txtWhiteColour;
        // End of variables declaration//GEN-END:variables
}
