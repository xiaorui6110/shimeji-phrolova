package com.group_finity.mascot.environment;

import java.awt.Point;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 边界接口，包含边界位置、边界移动
 * </p>
 */
public interface Border {

	boolean isOn(Point location);

	Point move(Point location);
}
