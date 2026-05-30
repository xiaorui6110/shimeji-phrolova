package com.group_finity.mascot.action;

import java.util.List;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

/**
 * 广播动作类，用于向其他桌宠广播affordance信息
 * 
 * 这是一个类似于Animate的动作，但会在执行时设置桌宠的affordance属性，
 * 让其他桌宠能够通过ScanMove找到并与之交互。
 * 
 * Original Author: Yuki Yamada of Group Finity
 * Currently developed by Shimeji-ee Group.
 */
public class Broadcast extends Animate {
    
    private boolean affordanceSet = false;
    
    /**
     * 构造函数
     */
    public Broadcast(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context) {
        super(schema, animations, context);
    }
    
    @Override
    public void init(Mascot mascot) throws VariableException {
        super.init(mascot);
        
        // 设置桌宠的affordance，让其他桌宠能够找到它
        String affordance = getAffordance();
        if (affordance != null && !affordance.trim().isEmpty() && !affordanceSet) {
            getMascot().setAffordance(affordance);
            affordanceSet = true;
        }
    }
    
    @Override
    public boolean hasNext() throws VariableException {
        boolean hasNext = super.hasNext();
        
        // 如果动画结束，清理affordance
        if (!hasNext && affordanceSet) {
            String affordance = getAffordance();
            getMascot().removeAffordance(affordance);
            affordanceSet = false;
        }
        
        return hasNext;
    }
}