package com.group_finity.mascot.win.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Original Author: Yuki Yamada of Group Finity
 * (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public interface Gdi32 extends StdCallLibrary {

	Gdi32 INSTANCE = Native.load("Gdi32", Gdi32.class);

	Pointer CreateCompatibleDC(Pointer HDC);

	Pointer SelectObject(Pointer HDC, Pointer HGDIOBJ);

	void DeleteDC(Pointer hdc);

	int DIB_RGB_COLORS = 0;

	Pointer CreateDIBSection(Pointer hdc, BITMAPINFOHEADER pbmi, int iUsage, Pointer ppvBits, Pointer hSection,
			int dwOffset);

	void GetObjectW(Pointer hgdiobj, int cbBuffer, BITMAP lpvObject);

	void DeleteObject(Pointer hObject);

	Pointer CreateRectRgn(
			int nLeftRect,
			int nTopRect,
			int nRightRect,
			int nBottomRect);

	void GetRgnBox(Pointer hrgn, RECT lprc);
}
