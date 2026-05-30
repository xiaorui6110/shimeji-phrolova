package com.group_finity.mascot.image;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public class ImagePair {

	/**
	 */
	private final MascotImage leftImage;

	/**
	 */
	private final MascotImage rightImage;

	/**
	 */
	public ImagePair(
			final MascotImage leftImage, final MascotImage rightImage) {
		this.leftImage = leftImage;
		this.rightImage = rightImage;
	}

	/**
	 */
	public MascotImage getImage(final boolean lookRight) {
		return lookRight ? this.getRightImage() : this.getLeftImage();
	}

	private MascotImage getLeftImage() {
		return this.leftImage;
	}
	
	private MascotImage getRightImage() {
		return this.rightImage;
	}
}
