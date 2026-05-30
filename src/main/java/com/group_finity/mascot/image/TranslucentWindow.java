package com.group_finity.mascot.image;

import java.awt.Component;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public interface TranslucentWindow
{
    Component asComponent();

    void setImage(NativeImage image);

    void updateImage();
    
    void dispose();
    
    void setAlwaysOnTop(boolean onTop);
}
