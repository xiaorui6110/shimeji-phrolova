package com.group_finity.mascot;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.sound.sampled.Clip;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.hotspot.Hotspot;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.menu.JLongMenu;
import com.group_finity.mascot.script.VariableMap;
import com.group_finity.mascot.sound.Sounds;

/**
 * Mascot object.
 * <p>
 * The mascot represents the long-term, complex behavior and (@link Behavior),
 * Represents a short-term movements in the monotonous work with (@link Action).
 * <p>
 * The mascot they have an internal timer, at a constant interval to call (@link
 * Action).
 * (@link Action) is (@link #animate (Point, MascotImage, boolean)) method or by
 * calling
 * To animate the mascot.
 * <p>
 * (@link Action) or exits, the other at a certain time is called (@link
 * Behavior), the next move to (@link Action).
 * <p>
 * Original Author: Yuki Yamada of Group Finity
 * (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */
public class Mascot {
    private static final Logger log = Logger.getLogger(Mascot.class.getName());

    public static final boolean DRAW_DEBUG = false;

    private static final AtomicInteger lastId = new AtomicInteger();

    private final int id;

    private String imageSet = "";
    /**
     * A window that displays the mascot.
     */
    private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();

    /**
     * Managers are managing the mascot.
     */
    private Manager manager = null;

    /**
     * Mascot ground coordinates.
     * Or feet, for example, when part of the hand is hanging.
     */
    private Point anchor = new Point(0, 0);

    /**
     * Image to display.
     */
    private MascotImage image = null;

    /**
     * Whether looking right or left.
     * The original image is treated as left, true means picture must be inverted.
     */
    private boolean lookRight = false;

    /**
     * Object representing the long-term behavior.
     */
    private Behavior behavior = null;

    /**
     * Increases with each tick of the timer.
     */
    private int time = 0;

    /**
     * Whether the animation is running.
     */
    private boolean animating = true;

    private boolean paused = false;

    /**
     * Set by behaviours when the shimeji is being dragged by the mouse cursor,
     * as opposed to hotspots or the like.
     */
    private boolean dragging = false;

    private final MascotEnvironment environment = new MascotEnvironment(this);

    private String sound = null;

    protected DebugWindow debugWindow = null;

    private final ArrayList<String> affordances = new ArrayList<>(5);

    private final ArrayList<Hotspot> hotspots = new ArrayList<>(5);

    private VariableMap variables = null;

    /**
     * Set by behaviours when the user has triggered a hotspot on this shimeji,
     * so that the shimeji knows to check for any new hotspots that emerge while
     * the mouse is held down.
     */
    private Point cursor = null;

    public Mascot(final String imageSet) {
        id = lastId.incrementAndGet();
        this.imageSet = imageSet;

        log.log(Level.INFO, "Created a mascot ({0})", this);

        // Always show on top
        getWindow().setAlwaysOnTop(true);

        // Register the mouse handler
        getWindow().asComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                Mascot.this.mousePressed(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                Mascot.this.mouseReleased(e);
            }
        });
        getWindow().asComponent().addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                if (paused)
                    refreshCursor(false);
                else {
                    if (isHotspotClicked())
                        setCursorPosition(e.getPoint());
                    else
                        refreshCursor(e.getPoint());
                }
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                if (paused)
                    refreshCursor(false);
                else {
                    if (isHotspotClicked())
                        setCursorPosition(e.getPoint());
                    else
                        refreshCursor(e.getPoint());
                }
            }
        });
    }

    @Override
    public String toString() {
        return "mascot" + id;
    }

    /**
     * 检查当前操作系统是否为 macOS
     */
    private static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
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

    private void mousePressed(final MouseEvent event) {
        // Switch to drag the animation when the mouse is down
        if (!isPaused() && getBehavior() != null) {
            try {
                getBehavior().mousePressed(event);
            } catch (final CantBeAliveException e) {
                log.log(Level.SEVERE, "Fatal Error", e);
                Main.showError(Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage") + "\n"
                        + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                dispose();
            }
        }
    }

    private void mouseReleased(final MouseEvent event) {
        if (shouldShowPopupMenu(event)) {
            SwingUtilities.invokeLater(() -> showPopup(event.getX(), event.getY()));
        } else {
            if (!isPaused() && getBehavior() != null) {
                try {
                    getBehavior().mouseReleased(event);
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "Fatal Error", e);
                    Main.showError(Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage")
                            + "\n" + e.getMessage() + "\n"
                            + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                    dispose();
                }
            }
        }
    }

    private void showPopup(final int x, final int y) {
        final JPopupMenu popup = new JPopupMenu();
        final ResourceBundle languageBundle = Main.getInstance().getLanguageBundle();

        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                setAnimating(true);
            }

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                setAnimating(false);
            }
        });

        // "Another One!" menu item
        final JMenuItem increaseMenu = new JMenuItem(languageBundle.getString("CallAnother"));
        increaseMenu.addActionListener(event -> Main.getInstance().createMascot(imageSet));

        // "Bye Bye!" menu item
        final JMenuItem disposeMenu = new JMenuItem(languageBundle.getString("Dismiss"));
        disposeMenu.addActionListener(e -> dispose());

        // "Follow Mouse!" Menu item
        final JMenuItem gatherMenu = new JMenuItem(languageBundle.getString("FollowCursor"));
        gatherMenu.addActionListener(event -> getManager().setBehaviorAll(Main.getInstance().getConfiguration(imageSet), Main.BEHAVIOR_GATHER,
                imageSet));

        // "Reduce to One!" menu item
        final JMenuItem oneMenu = new JMenuItem(languageBundle.getString("DismissOthers"));
        oneMenu.addActionListener(event -> getManager().remainOne(imageSet));

        // "Reduce to One!" menu item
        final JMenuItem onlyOneMenu = new JMenuItem(languageBundle.getString("DismissAllOthers"));
        onlyOneMenu.addActionListener(event -> getManager().remainOne(Mascot.this));

        // "Restore IE!" menu item
        final JMenuItem restoreMenu = new JMenuItem(languageBundle.getString("RestoreWindows"));
        restoreMenu.addActionListener(event -> NativeFactory.getInstance().getEnvironment().restoreIE());

        // Debug menu item
        final JMenuItem debugMenu = new JMenuItem(languageBundle.getString("RevealStatistics"));
        debugMenu.addActionListener(event -> {
            if (debugWindow == null) {
                debugWindow = new DebugWindow();
            }
            debugWindow.setVisible(true);
        });

        // "Bye Everyone!" menu item
        final JMenuItem closeMenu = new JMenuItem(languageBundle.getString("DismissAll"));
        closeMenu.addActionListener(e -> Main.getInstance().exit());

        // "Paused" Menu item
        final JMenuItem pauseMenu = new JMenuItem(isAnimating() ? languageBundle.getString("PauseAnimations")
                : languageBundle.getString("ResumeAnimations"));
        pauseMenu.addActionListener(event -> Mascot.this.setPaused(!Mascot.this.isPaused()));

        // Add the Behaviors submenu. Currently slightly buggy, sometimes the menu
        // ghosts.
        JLongMenu submenu = new JLongMenu(languageBundle.getString("SetBehaviour"), 30);
        JLongMenu allowedSubmenu = new JLongMenu(languageBundle.getString("AllowedBehaviours"), 30);
        // The MenuScroller would look better than the JLongMenu, but the initial
        // positioning is not working correctly.
        // MenuScroller.setScrollerFor(submenu, 30, 125);
        submenu.setAutoscrolls(true);
        JMenuItem item;
        JCheckBoxMenuItem toggleItem;
        final com.group_finity.mascot.config.Configuration config = Main.getInstance().getConfiguration(getImageSet());
        for (String behaviorName : config.getBehaviorNames()) {
            final String command = behaviorName;
            try {
                if (!config.isBehaviorHidden(command)) {
                    String caption = behaviorName.replaceAll("([a-z])(IE)?([A-Z])", "$1 $2 $3").replaceAll("  ", " ");
                    if (config.isBehaviorEnabled(command, Mascot.this) && !command.contains("/")) {
                        item = new JMenuItem(
                                languageBundle.containsKey(behaviorName) ? languageBundle.getString(behaviorName)
                                        : caption);
                        item.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                try {
                                    setBehavior(config.buildBehavior(command));
                                } catch (Exception err) {
                                    log.log(Level.SEVERE, "Error ({0})", this);
                                    Main.showError(languageBundle.getString("CouldNotSetBehaviourErrorMessage") + "\n"
                                            + err.getMessage() + "\n"
                                            + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                                }
                            }
                        });
                        submenu.add(item);
                    }

                    if (config.isBehaviorToggleable(command) && !command.contains("/")) {
                        toggleItem = new JCheckBoxMenuItem(caption, config.isBehaviorEnabled(command, Mascot.this));
                        toggleItem.addItemListener(e -> Main.getInstance().setMascotBehaviorEnabled(command, Mascot.this,
                                !config.isBehaviorEnabled(command, Mascot.this)));
                        allowedSubmenu.add(toggleItem);
                    }
                }
            } catch (Exception err) {
                // just skip if something goes wrong
            }
        }

        popup.add(increaseMenu);
        popup.add(new JSeparator());
        popup.add(gatherMenu);
        popup.add(restoreMenu);
        popup.add(debugMenu);
        popup.add(new JSeparator());
        if (submenu.getMenuComponentCount() > 0)
            popup.add(submenu);
        if (allowedSubmenu.getMenuComponentCount() > 0)
            popup.add(allowedSubmenu);
        popup.add(new JSeparator());
        popup.add(pauseMenu);
        popup.add(new JSeparator());
        popup.add(disposeMenu);
        popup.add(oneMenu);
        popup.add(onlyOneMenu);
        popup.add(closeMenu);

        getWindow().asComponent().requestFocus();

        // lightweight popups expect the shimeji window to draw them if they fall inside
        // the shimeji window boundary
        // as the shimeji window can't support this we need to set them to heavyweight
        popup.setLightWeightPopupEnabled(false);
        popup.show(getWindow().asComponent(), x, y);
    }

    void tick() {
        if (isAnimating()) {
            if (getBehavior() != null) {
                try {
                    getBehavior().next();
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "Fatal Error.", e);
                    Main.showError(
                            Main.getInstance().getLanguageBundle().getString("CouldNotGetNextBehaviourErrorMessage")
                                    + "\n" + e.getMessage() + "\n"
                                    + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                    dispose();
                }

                setTime(getTime() + 1);
            }

            if (debugWindow != null) {
                debugWindow.setBehaviour(behavior.toString().substring(9, behavior.toString().length() - 1)
                        .replaceAll("([a-z])(IE)?([A-Z])", "$1 $2 $3").replaceAll("  ", " "));
                debugWindow.setShimejiX(anchor.x);
                debugWindow.setShimejiY(anchor.y);

                Area activeWindow = environment.getActiveIE();
                debugWindow.setWindowTitle(environment.getActiveIETitle());
                debugWindow.setWindowX(activeWindow.getLeft());
                debugWindow.setWindowY(activeWindow.getTop());
                debugWindow.setWindowWidth(activeWindow.getWidth());
                debugWindow.setWindowHeight(activeWindow.getHeight());

                Area workArea = environment.getWorkArea();
                debugWindow.setEnvironmentX(workArea.getLeft());
                debugWindow.setEnvironmentY(workArea.getTop());
                debugWindow.setEnvironmentWidth(workArea.getWidth());
                debugWindow.setEnvironmentHeight(workArea.getHeight());
            }
        }
    }

    public void apply() {
        if (isAnimating()) {
            // Make sure there's an image
            if (getImage() != null) {
                // Set the window region
                getWindow().asComponent().setBounds(getBounds());

                // Set Images
                getWindow().setImage(getImage().image());

                // Display
                if (!getWindow().asComponent().isVisible()) {
                    getWindow().asComponent().setVisible(true);
                }

                // Redraw
                getWindow().updateImage();
            } else {
                if (getWindow().asComponent().isVisible()) {
                    getWindow().asComponent().setVisible(false);
                }
            }

            // play sound if requested
            if (Sounds.isMuted() && sound != null && Sounds.contains(sound)) {
                synchronized (log) {
                    Clip clip = Sounds.getSound(sound);
                    if (!clip.isRunning()) {
                        clip.stop();
                        clip.setMicrosecondPosition(0);
                        clip.start();
                    }
                }
            }
        }
    }

    public void dispose() {
        log.log(Level.INFO, "destroy mascot ({0})", this);

        if (debugWindow != null) {
            debugWindow.setVisible(false);
            debugWindow = null;
        }

        animating = false;
        getWindow().dispose();
        affordances.clear();
        if (getManager() != null) {
            getManager().remove(Mascot.this);
        }
    }

    private void refreshCursor(Point position) {
        boolean useHand = false;
        for (final Hotspot hotspot : hotspots) {
            if (hotspot.contains(this, position) &&
                    Main.getInstance().getConfiguration(imageSet).isBehaviorEnabled(hotspot.behaviour(), this)) {
                useHand = true;
                break;
            }
        }

        refreshCursor(useHand);
    }

    private void refreshCursor(Boolean useHand) {
        getWindow().asComponent()
                .setCursor(Cursor.getPredefinedCursor(useHand ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(final Manager manager) {
        this.manager = manager;
    }

    public Point getAnchor() {
        return anchor;
    }

    public void setAnchor(Point anchor) {
        this.anchor = anchor;
    }

    public MascotImage getImage() {
        return image;
    }

    public void setImage(final MascotImage image) {
        this.image = image;
    }

    public boolean isLookRight() {
        return lookRight;
    }

    public void setLookRight(final boolean lookRight) {
        this.lookRight = lookRight;
    }

    public Rectangle getBounds() {
        if (getImage() != null) {
            // Central area of the window find the image coordinates and ground coordinates.
            // The centre has already been adjusted for scaling
            final int top = getAnchor().y - getImage().center().y;
            final int left = getAnchor().x - getImage().center().x;

            return new Rectangle(left, top, getImage().size().width, getImage().size().height);
        } else {
            // as we have no image let's return what we were last frame
            return getWindow().asComponent().getBounds();
        }
    }

    public int getTime() {
        return time;
    }

    private void setTime(final int time) {
        this.time = time;
    }

    public Behavior getBehavior() {
        return behavior;
    }

    public void setBehavior(final Behavior behavior) throws CantBeAliveException {
        this.behavior = behavior;
        this.behavior.init(this);
    }

    public int getCount() {
        return manager != null ? getManager().getCount(imageSet) : 0;
    }

    public int getTotalCount() {
        return manager != null ? getManager().getCount() : 0;
    }

    private boolean isAnimating() {
        return animating && !paused;
    }

    private void setAnimating(final boolean animating) {
        this.animating = animating;
    }

    private TranslucentWindow getWindow() {
        return this.window;
    }

    public MascotEnvironment getEnvironment() {
        return environment;
    }

    public ArrayList<String> getAffordances() {
        return affordances;
    }

    /**
     * 设置桌宠的affordance（用于交互系统）
     * @param affordance 要添加的affordance字符串
     */
    public void setAffordance(String affordance) {
        if (affordance != null && !affordance.isEmpty()) {
            if (!affordances.contains(affordance)) {
                affordances.add(affordance);
            }
        }
    }

    /**
     * 清除指定的affordance
     * @param affordance 要移除的affordance字符串
     */
    public void removeAffordance(String affordance) {
        affordances.remove(affordance);
    }

    /**
     * 清除所有affordances
     */
    public void clearAffordances() {
        affordances.clear();
    }

    public ArrayList<Hotspot> getHotspots() {
        return hotspots;
    }

    public void setImageSet(final String set) {
        imageSet = set;
    }

    public String getImageSet() {
        return imageSet;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(final String name) {
        sound = name;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(final boolean paused) {
        this.paused = paused;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(final boolean isDragging) {
        dragging = isDragging;
    }

    public boolean isHotspotClicked() {
        return cursor != null;
    }

    public Point getCursorPosition() {
        return cursor;
    }

    public void setCursorPosition(final Point point) {
        cursor = point;

        if (point == null)
            refreshCursor(false);
        else
            refreshCursor(point);
    }

    public VariableMap getVariables() {
        if (variables == null)
            variables = new VariableMap();
        return variables;
    }
}
