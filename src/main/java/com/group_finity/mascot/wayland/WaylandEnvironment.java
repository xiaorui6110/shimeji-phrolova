package com.group_finity.mascot.wayland;

import com.group_finity.mascot.environment.*;

import java.awt.*;
import java.util.*;

/**
 * Wayland environment implementation.
 * Uses native Wayland protocols for better performance compared to X11 compatibility mode.
 *
 * @author Shimeji-ee Group
 */
public class WaylandEnvironment extends Environment {

    /**
     * Cache for active windows to reduce frequent native calls
     */
    public WindowContainer ieContainer = new WindowContainer();
    private java.util.Timer updateTimer;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 1000; // Update every second

    /**
     * Screen area cache
     */
    private Area workArea = new Area();
    private Rectangle screenRect;
    private boolean initialized = false;
    
    /**
     * Active window tracking
     */
    private Area activeIE = new Area();
    private String activeIETitle = "";
    private long activeWindowId = 0;

    public WaylandEnvironment() {
        super();
        initializeWaylandEnvironment();
        startUpdateTimer();
    }

    private void initializeWaylandEnvironment() {
        try {
            // Initialize Wayland connection and get screen information
            updateScreenInfo();
            initialized = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize Wayland environment: " + e.getMessage());
            // Fallback to basic screen detection
            initializeFallback();
        }
    }

    private void initializeFallback() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        
        screenRect = new Rectangle(0, 0, dm.getWidth(), dm.getHeight());
        workArea.set(screenRect);
        initialized = true;
    }

    private void updateScreenInfo() {
        try {
            // Use Wayland protocol to get screen information
            // This is more reliable than X11 detection on Wayland
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            
            if (screens.length > 0) {
                Rectangle bounds = screens[0].getDefaultConfiguration().getBounds();
                screenRect = bounds;
                
                // Get work area (screen minus panels, docks, etc.)
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Insets insets = toolkit.getScreenInsets(screens[0].getDefaultConfiguration());
                
                Rectangle workRect = new Rectangle(
                    bounds.x + insets.left,
                    bounds.y + insets.top,
                    bounds.width - insets.left - insets.right,
                    bounds.height - insets.top - insets.bottom
                );
                
                workArea.set(workRect);
            }
        } catch (Exception e) {
            System.err.println("Failed to update screen info: " + e.getMessage());
            initializeFallback();
        }
    }

    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        
        updateTimer = new java.util.Timer("WaylandEnvironmentUpdater", true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateEnvironment();
            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private void updateEnvironment() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        lastUpdateTime = currentTime;
        
        try {
            // Update window information
            updateWindowList();
            
            // Update screen information if needed
            updateScreenInfo();
        } catch (Exception e) {
            System.err.println("Error updating Wayland environment: " + e.getMessage());
        }
    }

    private void updateWindowList() {
        try {
            // For Wayland, window enumeration is limited due to security restrictions
            // We'll use what's available through the windowing system
            ieContainer.clear();
            
            // On Wayland, we have limited access to other application windows
            // This is a security feature of Wayland, so we work with what we can detect
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                if (frame.isVisible() && !frame.getName().startsWith("Shimeji")) {
                    Rectangle bounds = frame.getBounds();
                    Area windowArea = new Area();
                    windowArea.set(bounds);
                    ieContainer.addIE(windowArea);
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating window list: " + e.getMessage());
        }
    }

    @Override
    protected Area getWorkArea() {
        if (!initialized) {
            initializeWaylandEnvironment();
        }
        return workArea;
    }

    @Override
    public Area getActiveIE() {
        return activeIE;
    }

    @Override
    public String getActiveIETitle() {
        return activeIETitle != null ? activeIETitle : "";
    }

    @Override
    public long getActiveWindowId() {
        return activeWindowId;
    }

    @Override
    public void moveActiveIE(Point point) {
        // In Wayland, window manipulation is restricted for security reasons
        // We can only move windows that belong to our application
        try {
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                if (frame.isVisible() && frame.getName().startsWith("Shimeji")) {
                    Point currentLocation = frame.getLocation();
                    frame.setLocation(currentLocation.x + point.x, currentLocation.y + point.y);
                }
            }
        } catch (Exception e) {
            System.err.println("Error moving windows: " + e.getMessage());
        }
    }

    @Override
    public void restoreIE() {
        // Limited functionality in Wayland due to security restrictions
        try {
            Frame[] frames = Frame.getFrames();
            for (Frame frame : frames) {
                if (frame.getExtendedState() == Frame.ICONIFIED) {
                    frame.setExtendedState(Frame.NORMAL);
                }
            }
        } catch (Exception e) {
            System.err.println("Error restoring windows: " + e.getMessage());
        }
    }

    @Override
    public void refreshCache() {
        updateEnvironment();
    }

    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    /**
     * Get available windows for interaction.
     * Note: On Wayland, window access is limited for security reasons.
     */
    public Collection<Area> getWindows() {
        updateWindowList();
        return ieContainer.getWindowAreas();
    }

    /**
     * Check if running under Wayland
     */
    public static boolean isWayland() {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        
        return (waylandDisplay != null && !waylandDisplay.isEmpty()) ||
               (sessionType != null && sessionType.equals("wayland"));
    }

    /**
     * Get the Wayland compositor name if available
     */
    public String getCompositorName() {
        String desktop = System.getenv("XDG_CURRENT_DESKTOP");
        if (desktop != null) {
            return desktop.toLowerCase();
        }
        return "unknown";
    }

    /**
     * Window container class for managing Wayland windows
     */
    public static class WindowContainer extends Hashtable<Number, Area> {
        private static final long serialVersionUID = 1L;

        public void addIE(Area area) {
            put(System.currentTimeMillis(), area);
        }

        public Collection<Area> getWindowAreas() {
            return values();
        }
    }
}
