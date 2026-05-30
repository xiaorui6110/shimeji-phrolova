package com.group_finity.mascot.win;

import com.group_finity.mascot.Main;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.win.jna.BITMAP;
import com.group_finity.mascot.win.jna.BITMAPINFOHEADER;
import com.group_finity.mascot.win.jna.Gdi32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * (@link WindowsTranslucentWindow) a value that can be used with images.
 * <p>
 * {@link WindowsTranslucentWindow} is available because only Windows bitmap
 * {@link BufferedImage} existing copy pixels from a Windows bitmap.
 * <p>
 * Original Author: Yuki Yamada of Group Finity
 * (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */
class WindowsNativeImage implements NativeImage {

	/**
	 * Windows to create a bitmap.
	 * @ Param width width of the bitmap.
	 * @ Param height the height of the bitmap.
	 * @ Return the handle of a bitmap that you create.
	 */
	private static Pointer createNative(final int width, final int height) {

		final BITMAPINFOHEADER bmi = new BITMAPINFOHEADER();
		bmi.biSize = 40;
		bmi.biWidth = width;
		bmi.biHeight = height;
		bmi.biPlanes = 1;
		bmi.biBitCount = 32;

        return Gdi32.INSTANCE.CreateDIBSection(
                Pointer.NULL, bmi, Gdi32.DIB_RGB_COLORS, Pointer.NULL, Pointer.NULL, 0);
	}

	/**
	 * {@link BufferedImage} to reflect the contents of the bitmap.
	 * 
	 * @param nativeHandle bitmap handle.
	 * @param rgb          ARGB of the picture.
	 */
	private static void flushNative(final Pointer nativeHandle, final int[] rgb) {

		final BITMAP bmp = new BITMAP();
		Gdi32.INSTANCE.GetObjectW(nativeHandle, Main.getInstance().getPlatform().getBitmapSize() + Native.POINTER_SIZE,
				bmp);

		// Copy at the pixel level. These dimensions are already scaled
		int width = bmp.bmWidth;
		int height = bmp.bmHeight;
		final int destPitch = ((bmp.bmWidth * bmp.bmBitsPixel) + 31) / 32 * 4;
		long destIndex = destPitch * (height - 1L);

		// 优化：预处理RGB数据，处理透明度问题
		// UpdateLayeredWindow FFFFFF RGB value has the bug that it ignores the value of a,
		// Photoshop is where a is an RGB value of 0 have the property value to 0.
		for (int i = 0; i < rgb.length; i++) {
			if ((rgb[i] & 0xFF000000) == 0) {
				rgb[i] = 0;
			}
		}

		for (int y = 0; y < height; y++) {
			int srcRowIndex = y * width;
			bmp.bmBits.write(destIndex, rgb, srcRowIndex, width);
			destIndex -= destPitch;
		}

	}

	/**
	 * Java Image object.
	 */
	private final BufferedImage managedImage;

	/**
	 * Windows Bittomappuhandoru.
	 */
	private final Pointer nativeHandle;

	public WindowsNativeImage(final BufferedImage image) {

		this.managedImage = image;
		this.nativeHandle = createNative(image.getWidth(), image.getHeight());

		int[] rbgValues = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

		flushNative(this.getNativeHandle(), rbgValues);
	}

	/**
	 * Changes to be reflected in the Windows bitmap image.
	 */
	public void update() {
		// this isn't used
	}

	public void flush() {
		managedImage.flush();
	}

	public Graphics getGraphics() {
		return managedImage.createGraphics();
	}

	public Pointer getHandle() {
		return nativeHandle;
	}

	public int getHeight() {
		return managedImage.getHeight();
	}

	public int getWidth() {
		return managedImage.getWidth();
	}

	public int getHeight(final ImageObserver observer) {
		return managedImage.getHeight(observer);
	}

	public Object getProperty(final String name, final ImageObserver observer) {
		return managedImage.getProperty(name, observer);
	}

	public ImageProducer getSource() {
		return managedImage.getSource();
	}

	public int getWidth(final ImageObserver observer) {
		return managedImage.getWidth(observer);
	}

	private Pointer getNativeHandle() {
		return nativeHandle;
	}
}
