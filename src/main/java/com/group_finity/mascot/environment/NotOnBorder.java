package com.group_finity.mascot.environment;

import java.awt.Point;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public class NotOnBorder implements Border {

	public static final NotOnBorder INSTANCE = new NotOnBorder();

	private NotOnBorder() {

	}

	@Override
	public boolean isOn(final Point location) {
		return false;
	}

	@Override
	public Point move(final Point location) {
		return location;
	}
}
