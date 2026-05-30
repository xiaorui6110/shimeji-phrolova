package com.group_finity.mascot.hotspot;

import java.awt.Point;
import java.awt.Shape;
import com.group_finity.mascot.Mascot;

/**
 * Hotspot.
 * <p>
 * Represents a clickable area on a shimeji, along with the behaviour to execute
 * when the user interacts with the area.
 *
 * @author Kilkakon
 */
public record Hotspot(String behaviour, Shape shape) {

    public boolean contains(Mascot mascot, Point point) {
        // flip if facing right
        if (mascot.isLookRight())
            point = new Point(mascot.getBounds().width - point.x, point.y);

        return shape.contains(point);
    }
}
