package com.group_finity.mascot.environment;

import java.awt.Point;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 地板、天花板，包含地板、天花板位置、地板、天花板移动
 * </p>
 */
public class FloorCeiling implements Border {

	private final Area area;

	private final boolean bottom;

	public FloorCeiling(final Area area, final boolean bottom) {
		this.area = area;
		this.bottom = bottom;
	}

	public Area getArea() {
		return this.area;
	}

	public boolean isBottom() {
		return this.bottom;
	}

	public int getY() {
		return isBottom() ? getArea().getBottom() : getArea().getTop();
	}

	public int getLeft() {
		return getArea().getLeft();
	}

	public int getRight() {
		return getArea().getRight();
	}

	public int getDY() {
		return isBottom() ? getArea().getDbottom() : getArea().getDtop();
	}

	public int getDLeft() {
		return getArea().getDleft();
	}

	public int getDRight() {
		return getArea().getDright();
	}

	public int getWidth() {
		return getArea().getWidth();
	}

	@Override
	public boolean isOn(final Point location) {
		//  严格相等
		return getArea().isVisible() && (getY() == location.y) && (getLeft() <= location.x)
				&& (location.x <= getRight());
	}


	@Override
    public Point move(final Point location) {

		if (!getArea().isVisible()) {
			return location;
		}
		
		final int d = getRight() - getDRight() - (getLeft() - getDLeft());
		if (d == 0) {
			return location;
		}

		final Point newLocation = new Point((location.x - (getLeft() - getDLeft())) * ((getRight() - getLeft()) / d)
				+ getLeft(), location.y + getDY());

		if ((Math.abs(newLocation.x - location.x) >= 80) || (newLocation.y - location.y > 20)
				|| (newLocation.y - location.y < -80)) {			
			return location;
		}

		return newLocation;
	}
}
