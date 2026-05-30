package com.group_finity.mascot;

import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.Platform;

import java.awt.image.BufferedImage;

/**
 * Provides access to the native environment.
 * {@link #getInstance()} returns an instance of a Windows, Mac, Linux (X11), or general-purpose subclass depending on the execution environment.
 *
 * @author Yuki Yamada
 */
public abstract class NativeFactory {
    private static NativeFactory instance;

    /**
     * Obtains an instance of the subclass according to the execution environment.
     *
     * @return the environment-specific subclass
     */
    public static NativeFactory getInstance() {
        if (instance == null) {
            resetInstance();
        }
        return instance;
    }

    /**
     * Creates an instance of the subclass.
     */
    public static void resetInstance() {
        String environment = "generic";
        try {
            if (Main.getInstance() != null && Main.getInstance().getProperties() != null) {
                environment = Main.getInstance().getProperties().getProperty("Environment", "generic");
            }
        } catch (Exception e) {
            // If Main is not initialized yet, use default "generic"
            environment = "generic";
        }

        if (environment.equals("generic")) {
            if (Platform.isWindows()) {
                instance = new com.group_finity.mascot.win.NativeFactoryImpl();
            } else if (Platform.isMac()) {
                instance = new com.group_finity.mascot.mac.NativeFactoryImpl();
            } else if (/* Platform.isLinux() */ Platform.isX11()) {
                // Check if running under Wayland
                if (isWayland()) {
                    instance = new com.group_finity.mascot.wayland.NativeFactoryImpl();
                } else {
                    // Because Linux uses X11, this functions as the Linux support.
                    instance = new com.group_finity.mascot.x11.NativeFactoryImpl();
                }
            } else {
                // Fallback to generic for other platforms
                instance = new com.group_finity.mascot.generic.NativeFactoryImpl();
            }
        } else if (environment.equals("virtual")) {
            instance = new com.group_finity.mascot.virtual.NativeFactoryImpl();
        } else if (environment.equals("wayland")) {
            // Force Wayland mode
            instance = new com.group_finity.mascot.wayland.NativeFactoryImpl();
        } else {
            // Fallback to generic
            instance = new com.group_finity.mascot.generic.NativeFactoryImpl();
        }
    }

    /**
     * Check if running under Wayland
     */
    private static boolean isWayland() {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        
        return (waylandDisplay != null && !waylandDisplay.isEmpty()) ||
               (sessionType != null && sessionType.equals("wayland"));
    }

    /**
     * Gets the {@link Environment} object.
     *
     * @return the {@link Environment} object
     */
    public abstract Environment getEnvironment();

    /**
     * Creates a {@link NativeImage} with the specified {@link BufferedImage}.
     * This image can be used for masking {@link TranslucentWindow}.
     *
     * @param src the image to use to create the {@link NativeImage}
     * @return the new native image
     */
    public abstract NativeImage newNativeImage(BufferedImage src);

    /**
     * Creates a window that can be displayed semi-transparently.
     *
     * @return the new window
     */
    public abstract TranslucentWindow newTransparentWindow();
}
