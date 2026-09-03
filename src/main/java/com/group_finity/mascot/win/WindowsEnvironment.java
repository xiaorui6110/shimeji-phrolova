package com.group_finity.mascot.win;

import com.group_finity.mascot.Main;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.win.jna.Dwmapi;
import com.group_finity.mascot.win.jna.Gdi32;
import com.group_finity.mascot.win.jna.RECT;
import com.group_finity.mascot.win.jna.User32;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;

/**
 * Original Author: Yuki Yamada of Group Finity
 * (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */
class WindowsEnvironment extends Environment {
    private static final HashMap<Pointer, Boolean> ieCache = new LinkedHashMap<>();

    public static final Area workArea = new Area();

    public static final Area activeIE = new Area();

    private static Pointer activeIEobject = null;

    private static String[] windowTitles = null;
    private static String[] windowTitlesBlacklist = null;

    /**
     * 内置可互动（IE）窗口类名白名单。
     * 标题匹配无法覆盖中文标题/动态标题（如"下载 - 文件资源管理器"），
     * 故按窗口类名识别常见可互动窗口。可按需扩充。
     */
    private static final String[] DEFAULT_IE_CLASS_NAMES = {
            "CabinetWClass", // 文件资源管理器 / File Explorer
            "Chrome_WidgetWin_1", // Chrome / Edge（Chromium）
            "Chrome_WidgetWin_0", // 旧版 Chrome
            "IEFrame", // Internet Explorer / 系统 Web 宿主
            "Notepad", // 记事本
            "MozillaWindowClass", // Firefox
            "ApplicationFrameWindow" // UWP 应用宿主窗口
    };

    private enum IEResult {
        INVALID, NOT_IE, IE_OUT_OF_BOUNDS, IE
    }

    private static boolean isIE(final Pointer ie) {
        final Boolean cachedValue = ieCache.get(ie);
        if (cachedValue != null)
            return cachedValue;

        final char[] title = new char[1024];

        final int titleLength = User32.INSTANCE.GetWindowTextW(ie, title, 1024);

        final String ieTitle = new String(title, 0, titleLength);

        // 窗口类名：用于识别标题为中文/动态的常见可互动窗口
        final char[] classNameBuf = new char[256];
        final int classNameLength = User32.INSTANCE.GetClassNameW(ie, classNameBuf, 256);
        final String className = new String(classNameBuf, 0, classNameLength);
        final String classNameLower = className.toLowerCase(java.util.Locale.ROOT);

        // optimisation to remove empty windows from consideration without the loop.
        // Program Manager hard coded exception as there's issues if we mess with it
        if (ieTitle.isEmpty() || ieTitle.equals("Program Manager")) {
            ieCache.put(ie, false);
            return false;
        }

        // blacklist takes precedence over whitelist；条目同时匹配标题与类名
        boolean blacklistInUse = false;
        if (windowTitlesBlacklist == null) {
            windowTitlesBlacklist = Main.getInstance().getProperties().getProperty("InteractiveWindowsBlacklist", "").split("/");
        }
        for (String entry : windowTitlesBlacklist) {
            if (!entry.trim().isEmpty()) {
                blacklistInUse = true;
                if (matchesWindow(ieTitle, classNameLower, entry)) {
                    ieCache.put(ie, false);
                    return false;
                }
            }
        }

        // 内置窗口类名白名单优先（不依赖标题语言，可识别资源管理器/浏览器等）
        for (String ieClass : DEFAULT_IE_CLASS_NAMES) {
            if (className.equals(ieClass)) {
                ieCache.put(ie, true);
                return true;
            }
        }

        // whitelist：InteractiveWindows 配置条目同时匹配标题与类名，作为补充白名单
        boolean whitelistInUse = false;
        if (windowTitles == null) {
            windowTitles = Main.getInstance().getProperties().getProperty("InteractiveWindows", "").split("/");
        }

        for (String entry : windowTitles) {
            if (!entry.trim().isEmpty()) {
                whitelistInUse = true;
                if (matchesWindow(ieTitle, classNameLower, entry)) {
                    // log.log( Level.INFO, String.format( "value %s is ie", new String( title, 0,
                    // titleLength ) ) );
                    ieCache.put(ie, true);
                    return true;
                }
            }
        }

        if (whitelistInUse || !blacklistInUse) {
            // log.log( Level.INFO, String.format( "value %s is not ie", new String( title,
            // 0, titleLength ) ) );
            ieCache.put(ie, false);
            return false;
        } else {
            ieCache.put(ie, true);
            return true;
        }
    }

    /**
     * 判断窗口是否命中配置条目：标题包含（区分大小写，兼容旧配置）或 类名包含（不区分大小写）。
     */
    private static boolean matchesWindow(final String title, final String classNameLower, final String entry) {
        if (title.contains(entry)) {
            return true;
        }
        return classNameLower.contains(entry.toLowerCase(java.util.Locale.ROOT));
    }

    private static IEResult isViableIE(Pointer ie) {
        if (User32.INSTANCE.IsWindowVisible(ie) != 0) {
            // metro apps can be closed or minimised and still be considered "visible" by
            // User32
            // have to consider the new cloaked variable instead
            LongByReference flagsRef = new LongByReference();
            NativeLong result = Dwmapi.INSTANCE.DwmGetWindowAttribute(ie, Dwmapi.DWMWA_CLOAKED, flagsRef, 8);
            if (result.longValue() != 0x80070057 && (result.longValue() != 0 || flagsRef.getValue() != 0)) // unsupported
                                                                                                           // on 7 so
                                                                                                           // skip the
                                                                                                           // check
            {
                return IEResult.NOT_IE;
            }

            // int flags = User32.INSTANCE.GetWindowLongW( ie, User32.GWL_STYLE );
            // if( ( flags & User32.WS_MAXIMIZE ) != 0 )
            // return IEResult.INVALID;

            if (User32.INSTANCE.IsZoomed(ie) != 0) {
                return IEResult.INVALID;
            }

            if (isIE(ie) && (User32.INSTANCE.IsIconic(ie) == 0)) {
                Rectangle ieRect = getIERect(ie);
                if (ieRect.intersects(getScreenRect())) {
                    return IEResult.IE;
                } else {
                    return IEResult.IE_OUT_OF_BOUNDS;
                }
            }
        }

        return IEResult.NOT_IE;
    }

    private static Pointer findActiveIE() {
        activeIEobject = null;

        User32.INSTANCE.EnumWindows((ie, data) -> switch (isViableIE(ie)) {
            case IE -> {
                activeIEobject = ie;
                yield false;
            }
            case IE_OUT_OF_BOUNDS, NOT_IE -> // Valid window but not interactive according to user settings
                    true; // Something invalid is the foreground object
            default -> {
                activeIEobject = null;
                yield false;
            }
        }, null);

        return activeIEobject;

        // Pointer ie = User32.INSTANCE.GetWindow(
        // User32.INSTANCE.GetForegroundWindow(), User32.GW_HWNDFIRST );
        // Boolean continueFlag = true;
        //
        // while( continueFlag && User32.INSTANCE.IsWindow( ie ) != 0 )
        // {
        // switch( isViableIE( ie ) )
        // {
        // case IE:
        // return ie;
        //
        // case IE_OUT_OF_BOUNDS:
        // case NOT_IE: // Valid window but not interactive according to user settings
        // ie = User32.INSTANCE.GetWindow( ie, User32.GW_HWNDNEXT );
        // break;
        //
        // case INVALID: // Something invalid is the foreground object
        // continueFlag = false;
        // break;
        // }
        // }
        //
        // return null;
    }

    private static Rectangle getIERect(Pointer ie) {
        final RECT out = new RECT();
        User32.INSTANCE.GetWindowRect(ie, out);
        final RECT in = new RECT();
        if (getWindowRgnBox(ie, in) == User32.ERROR) {
            // log.log( Level.INFO, "getWindowRgnBox == User32.ERROR" );
            in.left = 0;
            in.top = 0;
            in.right = out.right - out.left;
            in.bottom = out.bottom - out.top;
        }
        return new Rectangle(out.left + in.left, out.top + in.top, in.Width(), in.Height());
    }

    private static int getWindowRgnBox(final Pointer window, final RECT rect) {

        Pointer hRgn = Gdi32.INSTANCE.CreateRectRgn(0, 0, 0, 0);
        try {
            if (User32.INSTANCE.GetWindowRgn(window, hRgn) == User32.ERROR) {
                return User32.ERROR;
            }
            Gdi32.INSTANCE.GetRgnBox(hRgn, rect);
            return 1;
        } finally {
            Gdi32.INSTANCE.DeleteObject(hRgn);
        }
    }

    private static void moveIE(final Pointer ie, final Rectangle rect) {

        if (ie == null) {
            return;
        }

        final RECT out = new RECT();
        User32.INSTANCE.GetWindowRect(ie, out);
        final RECT in = new RECT();
        if (getWindowRgnBox(ie, in) == User32.ERROR) {
            // log.log( Level.INFO, "getWindowRgnBox == User32.ERROR" );
            in.left = 0;
            in.top = 0;
            in.right = out.right - out.left;
            in.bottom = out.bottom - out.top;
        }

        User32.INSTANCE.MoveWindow(ie, rect.x - in.left, rect.y - in.top, rect.width + out.Width() - in.Width(),
                rect.height + out.Height() - in.Height(), 1);

    }

    private static void restoreAllIEs() {
        User32.INSTANCE.EnumWindows(new User32.WNDENUMPROC() {
            int offset = 25;

            @Override
            public boolean callback(Pointer ie, Pointer data) {
                IEResult result = isViableIE(ie);
                if (result == IEResult.IE_OUT_OF_BOUNDS) {
                    final RECT workArea = new RECT();
                    User32.INSTANCE.SystemParametersInfoW(User32.SPI_GETWORKAREA, 0, workArea, 0);
                    final RECT rect = new RECT();
                    User32.INSTANCE.GetWindowRect(ie, rect);

                    rect.OffsetRect(workArea.left + offset - rect.left, workArea.top + offset - rect.top);
                    User32.INSTANCE.MoveWindow(ie, rect.left, rect.top, rect.Width(), rect.Height(), 1);
                    User32.INSTANCE.BringWindowToTop(ie);

                    offset += 25;
                }

                return true;
            }
        }, null);
    }

    @Override
    public void tick() {
        super.tick();
        workArea.set(getWorkAreaRect());

        final Rectangle ieRect = getIERect(findActiveIE());
        activeIE.setVisible(ieRect.intersects(getScreen().toRectangle()));
        activeIE.set(ieRect);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void moveActiveIE(final Point point) {
        moveIE(findActiveIE(), new Rectangle(point.x, point.y, activeIE.getWidth(), activeIE.getHeight()));
    }

    @Override
    public void restoreIE() {
        restoreAllIEs();
    }

    @Override
    public Area getWorkArea() {
        return workArea;
    }

    @Override
    public Area getActiveIE() {
        return activeIE;
    }

    @Override
    public String getActiveIETitle() {
        final Pointer ie = findActiveIE();

        final char[] title = new char[1024];

        final int titleLength = User32.INSTANCE.GetWindowTextW(ie, title, 1024);

        return new String(title, 0, titleLength);
    }

    private static Rectangle getWorkAreaRect() {
        final RECT rect = new RECT();
        User32.INSTANCE.SystemParametersInfoW(User32.SPI_GETWORKAREA, 0, rect, 0);
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    @Override
    public void refreshCache() {
        ieCache.clear(); // will be repopulated next isIE call
        windowTitles = null;
    }

    @Override
    public long getActiveWindowId() {
        if (activeIEobject != null) {
            return Pointer.nativeValue(activeIEobject);
        }
        return 0;
    }

    // private void dumpWindowInformation( )
    // {
    // final StringBuilder text = new StringBuilder( );
    // final char[] title = new char[ 1024 ];
    // User32.INSTANCE.EnumWindows( new User32.WNDENUMPROC( )
    // {
    // @Override
    // public boolean callback( Pointer ie, Pointer data )
    // {
    // int titleLength = User32.INSTANCE.GetWindowTextW( ie, title, 1024 );
    //
    // String ieTitle = new String( title, 0, titleLength );
    //
    // text.append( ieTitle ).append( " " ).append( isViableIE( ie ) ).append(
    // "\r\n" );
    // return true;
    // }
    // }, null );
    //
    // try
    // {
    // PrintWriter out = new PrintWriter( "window-debug-information.txt" );
    // out.println( text.toString( ) );
    // out.close( );
    // }
    // catch( Exception e )
    // {
    // }
    // }
}
