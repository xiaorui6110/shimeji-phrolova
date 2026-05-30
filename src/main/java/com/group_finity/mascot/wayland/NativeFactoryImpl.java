package com.group_finity.mascot.wayland;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import java.awt.image.BufferedImage;

/**
 * Wayland platform native factory implementation.
 * Provides native Wayland environment support for better performance and stability.
 *
 * @author Shimeji-ee Group
 */
public class NativeFactoryImpl extends NativeFactory {

    private WaylandEnvironment environment = new WaylandEnvironment();

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public NativeImage newNativeImage(BufferedImage src) {
        return new WaylandNativeImage(src);
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new WaylandTranslucentWindow();
    }
}
