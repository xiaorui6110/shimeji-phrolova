package com.group_finity.mascot.environment;

import java.awt.Point;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 表示 {@link Area} 的左墙或右墙，是 {@link Border} 的垂直边界实现，
 * 与水平边界 {@link FloorCeiling} 对称：
 * <ul>
 *   <li>位置：X 坐标固定为区域的 left/right 边（由 {@link #right} 标志决定），
 *       纵向范围为区域的 top 到 bottom；</li>
 *   <li>移动：当区域尺寸或位置变化（{@link Area#set(java.awt.Rectangle)} 记录的 d* 位移）时，
 *       把附着在墙上的点一起带走 —— X 跟随墙的横向位移，Y 保持相对高度比例不变。</li>
 * </ul>
 * 典型用途：桌宠沿墙爬行、挂在墙上、转身撞墙判定等
 * </p>
 */
public class Wall implements Border {

	/** 所属区域（工作区/屏幕），墙的位置与位移全部取自该区域 */
	private final Area area;

	/** 是否为右墙：true=右墙（取区域的 right 边），false=左墙（取区域的 left 边） */
	private final boolean right;

	public Wall(final Area area, final boolean right) {
		this.area = area;
		this.right = right;
	}

	public Area getArea() {
		return this.area;
	}

	public boolean isRight() {
		return this.right;
	}

	/** 墙的 X 坐标：右墙取区域右边界，左墙取区域左边界 */
	public int getX() {
		return isRight() ? getArea().getRight() : getArea().getLeft();
	}

	/** 墙上边缘（与区域上边缘一致） */
	public int getTop() {
		return getArea().getTop();
	}

	/** 墙下边缘（与区域下边缘一致） */
	public int getBottom() {
		return getArea().getBottom();
	}

	/** 墙在 X 方向的位移量：右墙取 dright（新右边界-旧右边界），左墙取 dleft（新左边界-旧左边界） */
	public int getDX() {
		return isRight() ? getArea().getDright() : getArea().getDleft();
	}

	/** 墙上边缘的位移量（新 top - 旧 top） */
	public int getDTop() {
		return getArea().getDtop();
	}

	/** 墙下边缘的位移量（新 bottom - 旧 bottom） */
	public int getDBottom() {
		return getArea().getDbottom();
	}

	/** 墙的高度（= 区域高度） */
	public int getHeight() {
		return getArea().getHeight();
	}

	/**
	 * 判断点是否落在这面墙上：
	 * 区域可见，且 x 严格等于墙的 X 坐标，y 在 [top, bottom] 区间内。
	 * 与 {@link FloorCeiling#isOn} 对称（那里是 y 严格相等、x 在区间内）。
	 */
	@Override
	public boolean isOn(final Point location) {
		return getArea().isVisible() && (getX() == location.x) && (getTop() <= location.y)
				&& (location.y <= getBottom());
	}

	/**
	 * 墙移动时，把附着在墙上的点一起带走（例如窗口尺寸变化时，挂在墙上的桌宠跟随移动）。
	 * 映射规则：
	 * <ul>
	 *   <li>区域不可见或旧高度为 0 → 原样返回；</li>
	 *   <li>x = 原 x + 墙的横向位移 {@link #getDX()}；</li>
	 *   <li>y 保持相对高度比例不变：(y - 旧top) 按旧高度等比例映射到新高度区间；</li>
	 *   <li>横向或纵向位移达到 80px 以上视为异常跳变（如窗口跨屏移动），原样返回；</li>
	 * </ul>
	 */
	@Override
    public Point move(final Point location) {

		if (!getArea().isVisible()) {
			return location;
		}
		
		// 旧高度 = 旧bottom - 旧top（getTop()-getDTop() = 旧top，getBottom()-getDBottom() = 旧bottom）
		final int d = getBottom() - getDBottom() - (getTop() - getDTop());
		if ( d==0 ) {
			return location;
		}

		// y 按旧高度 → 新高度等比映射（保持相对位置比例），x 跟随墙的横向位移
		final Point newLocation = new Point(location.x + getDX(), (location.y - (getTop() - getDTop()))
				* (getBottom() - getTop()) / d + getTop());

		// 位移过大说明发生了异常跳变，保持原位不动
		if ((Math.abs(newLocation.x - location.x) >= 80) || (Math.abs(newLocation.y - location.y) >= 80)) {
			return location;
		}
		return newLocation;
	}
}
