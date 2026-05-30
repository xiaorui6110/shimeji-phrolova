package com.group_finity.mascot.image;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;

import com.group_finity.mascot.NativeFactory;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public record MascotImage(NativeImage image, Point center, Dimension size) {

    public MascotImage(final BufferedImage image, final Point center) {
        this(NativeFactory.getInstance().newNativeImage(image), center, new Dimension(image.getWidth(), image.getHeight()));
    }


}
