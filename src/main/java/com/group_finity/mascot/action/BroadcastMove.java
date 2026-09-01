package com.group_finity.mascot.action;

import java.util.List;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.script.VariableMap;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 广播移动动作类，用于向其他桌宠广播affordance信息（已废弃，主要原因是无使用需求）
 * </p>
 */
@Deprecated
public class BroadcastMove extends Move {

    public BroadcastMove( java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context )
    {
        super( schema, animations, context );
    }
}
