package com.group_finity.mascot.wayland;

import com.group_finity.mascot.image.NativeImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Wayland native image implementation.
 * Optimized for Wayland compositors to reduce flickering.
 *
 * @author Shimeji-ee Group
 */
public class WaylandNativeImage implements NativeImage {

    /**
     * Original image.
     */
    private BufferedImage managedImage;

    /**
     * Icon representation for window masking.
     */
    private Icon icon;

    /**
     * Image data for efficient rendering.
     */
    private int[] imageData;
    private int width;
    private int height;

    public WaylandNativeImage(BufferedImage image) {
        this.managedImage = createOptimizedImage(image);
        this.width = image.getWidth();
        this.height = image.getHeight();
        
        // Extract image data for efficient rendering
        extractImageData();
        
        // Create icon for masking
        this.icon = new ImageIcon(this.managedImage);
    }

    /**
     * Create an optimized image for Wayland rendering.
     * This helps reduce flickering by using compatible pixel formats.
     */
    private BufferedImage createOptimizedImage(BufferedImage source) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        
        // Create compatible image for better performance with proper transparency
        BufferedImage optimized = gc.createCompatibleImage(
            source.getWidth(), 
            source.getHeight(), 
            Transparency.TRANSLUCENT
        );
        
        Graphics2D g2d = optimized.createGraphics();
        
        // Enable anti-aliasing and high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        
        // Wayland-specific: Don't clear background, just copy image with original transparency
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        
        return optimized;
    }

    /**
     * Extract image data for efficient access.
     */
    private void extractImageData() {
        if (managedImage.getType() == BufferedImage.TYPE_INT_ARGB) {
            DataBufferInt buffer = (DataBufferInt) managedImage.getRaster().getDataBuffer();
            imageData = buffer.getData();
        } else {
            // Convert to ARGB format
            BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = argbImage.createGraphics();
            g2d.drawImage(managedImage, 0, 0, null);
            g2d.dispose();
            
            DataBufferInt buffer = (DataBufferInt) argbImage.getRaster().getDataBuffer();
            imageData = buffer.getData();
            managedImage = argbImage;
        }
    }

    public BufferedImage getManagedImage() {
        return managedImage;
    }

    public Icon getIcon() {
        return icon;
    }

    public void dispose() {
        if (managedImage != null) {
            managedImage.flush();
            managedImage = null;
        }
        icon = null;
        imageData = null;
    }

    /**
     * Get the raw image data for efficient rendering.
     */
    public int[] getImageData() {
        return imageData;
    }

    /**
     * Get image width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get image height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Check if the image has transparency.
     */
    public boolean hasTransparency() {
        return managedImage.getColorModel().hasAlpha();
    }
}
