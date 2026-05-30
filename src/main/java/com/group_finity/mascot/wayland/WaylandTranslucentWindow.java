package com.group_finity.mascot.wayland;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wayland translucent window implementation.
 * Optimized to reduce flickering that occurs with X11 compatibility layer.
 *
 * @author Shimeji-ee Group
 */
public class WaylandTranslucentWindow extends JWindow implements TranslucentWindow {

    private static final long serialVersionUID = 1L;

    /**
     * To view images.
     */
    private WaylandNativeImage image;

    private float alpha = 1.0f;
    private boolean windowVisible = false;
    
    /**
     * Vsync and rendering optimization
     */
    private long lastRepaintTime = 0;
    private static final long MIN_REPAINT_INTERVAL = 16; // ~60 FPS limit
    private final AtomicBoolean repaintScheduled = new AtomicBoolean(false);

    public WaylandTranslucentWindow() {
        super();
        init();
        setupWindow();
    }

    private void init() {
        // Optimize for Wayland rendering
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.noddraw", "true");
        
        // Wayland-specific optimizations
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Additional Wayland transparency fixes
        System.setProperty("sun.java2d.transaccel", "true");
        System.setProperty("sun.java2d.ddforcevram", "true");
    }

    private void setupWindow() {
        // Configure the window for complete transparency
        setType(Window.Type.UTILITY);
        setAlwaysOnTop(true);
        setFocusable(false);
        setFocusableWindowState(false);
        setAutoRequestFocus(false);
        
        // Set completely transparent background
        setBackground(new Color(0, 0, 0, 0));
        
        // Create optimized content panel that doesn't paint any background
        JPanel panel = new JPanel(null) { // Use null layout manager
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(final Graphics g) {
                // Don't call super - completely skip default background painting
                if (image != null) {
                    paintOptimized(g);
                }
            }
            
            @Override
            public void paint(Graphics g) {
                // Only paint component, skip children painting
                paintComponent(g);
            }
            
            @Override
            public boolean isOpaque() {
                return false;
            }
        };
        
        panel.setOpaque(false);
        panel.setBackground(new Color(0, 0, 0, 0));
        setContentPane(panel);

        setLayout(new BorderLayout());
        
        // Add component listener for resize events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scheduleRepaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                scheduleRepaint();
            }
        });

        // Fix for JDK-8016530 - graphics configuration changes
        AtomicBoolean updating = new AtomicBoolean();
        addPropertyChangeListener("graphicsConfiguration", evt -> {
            if (updating.compareAndSet(false, true)) {
                try {
                    // Re-apply transparency
                    setBackground(new Color(0, 0, 0, 0));
                } finally {
                    updating.set(false);
                }
            }
        });
    }

    /**
     * Optimized painting method to reduce flickering.
     */
    private void paintOptimized(Graphics g) {
        if (image == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Enable high-quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            
            // Wayland approach: Simply draw the image without any composite manipulation
            // Let the window manager handle the transparency
            BufferedImage sourceImage = image.getManagedImage();
            
            // Use default SrcOver composite with the window's alpha
            if (alpha < 1.0f) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            }
            
            // Draw the image at origin
            g2d.drawImage(sourceImage, 0, 0, null);
            
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public void setVisible(final boolean b) {
        super.setVisible(b);
        windowVisible = b;
        
        if (b) {
            // For Wayland, let the system handle transparency naturally
            // Don't force any opacity settings that might interfere
            toFront();
        }
    }
    
    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
        super.addImpl(comp, constraints, index);
        if (comp instanceof JComponent) {
            final JComponent jcomp = (JComponent) comp;
            jcomp.setOpaque(false);
        }
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(final float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        
        // For Wayland, don't use setOpacity, just repaint with new alpha
        if (windowVisible) {
            scheduleRepaint();
        }
    }

    /**
     * Schedule a repaint with frame rate limiting to reduce flickering.
     */
    private void scheduleRepaint() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastRepaintTime >= MIN_REPAINT_INTERVAL && 
            repaintScheduled.compareAndSet(false, true)) {
            
            SwingUtilities.invokeLater(() -> {
                try {
                    repaint();
                    lastRepaintTime = System.currentTimeMillis();
                } finally {
                    repaintScheduled.set(false);
                }
            });
        }
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public String toString() {
        return "WaylandTranslucentWindow[hashCode=" + hashCode() + ",bounds=" + getBounds() + "]";
    }

    @Override
    public void paint(final Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;

            // Higher-quality image rendering
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        super.paint(g);
    }

    public WaylandNativeImage getImage() {
        return image;
    }

    @Override
    public void setImage(final NativeImage image) {
        this.image = (WaylandNativeImage) image;
        scheduleRepaint();
    }

    @Override
    public void updateImage() {
        validate();
        scheduleRepaint();
    }

    @Override
    public void dispose() {
        if (image != null) {
            image.dispose();
        }
        super.dispose();
    }
}
