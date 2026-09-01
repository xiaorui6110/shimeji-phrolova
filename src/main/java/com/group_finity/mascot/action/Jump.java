package com.group_finity.mascot.action;

import java.awt.Point;
import java.util.List;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 跳跃动作：把桌宠从当前位置沿目标方向匀速直线移动到 XML 指定的目标点
 * (TargetX, TargetY)，每帧位移固定为 VelocityParam 指定的速度（像素/帧）。
 * 本类只负责"位移"，不含重力/空气阻力/碰撞检测（那是 Fall 的职责）；
 * 跳跃的抛物线观感由目标方向修正（垂直距离扣除 |dx|/2）+ 终点吸附共同实现。
 * </p>
 */
public class Jump extends ActionBase {

    public static final String PARAMETER_TARGETX = "TargetX";

    private static final int DEFAULT_TARGETX = 0;

    public static final String PARAMETER_TARGETY = "TargetY";

    private static final int DEFAULT_TARGETY = 0;

    //A Pose Attribute is already named Velocity
    public static final String PARAMETER_VELOCITY = "VelocityParam";

    private static final double DEFAULT_VELOCITY = 20.0;

    public static final String VARIABLE_VELOCITYX = "VelocityX";

    public static final String VARIABLE_VELOCITYY = "VelocityY";

    public Jump( java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context )
    {
        super( schema, animations, context );
    }

    /**
     * 是否还有下一帧：
     * 1) 父类条件成立（Duration 未耗尽且 Condition 表达式为真）
     * 2) 尚未到达目标点（修正后的直线距离不为 0）
     */
    @Override
    public boolean hasNext( ) throws VariableException
    {
        final int targetX = getTargetX( );
        final int targetY = getTargetY( );

        // 到目标点的水平位移
        final double distanceX = targetX - getMascot( ).getAnchor( ).x;
        // 垂直位移扣除水平距离的一半：让速度方向偏向水平，使跳跃轨迹呈平缓弧线而非直线平移
        final double distanceY = targetY - getMascot( ).getAnchor( ).y - Math.abs( distanceX ) / 2;

        // 修正后目标点的直线距离（作为方向归一化的模）
        final double distance = Math.sqrt( distanceX * distanceX + distanceY * distanceY );

        return super.hasNext( ) && ( distance != 0 );
    }

    /**
     * 单帧推进：
     * 1) 按目标点水平位置刷新朝向
     * 2) 将速度向量沿"指向目标的方向"归一化分解，移动锚点并推进动画帧
     * 3) 剩余距离不足一帧速度时吸附到目标点，结束动作
     */
    @Override
    protected void tick( ) throws LostGroundException, VariableException
    {
        final int targetX = getTargetX( );
        final int targetY = getTargetY( );

        // 目标点在锚点右侧则面向右，否则面向左（跳跃方向与朝向保持一致）
        getMascot( ).setLookRight( getMascot( ).getAnchor( ).x < targetX );

        final double distanceX = targetX - getMascot( ).getAnchor( ).x;

        // 与 hasNext() 相同的高度修正：垂直距离扣除 |dx|/2，速度向量偏向水平（起跳段轨迹平缓，接近目标时再由吸附逻辑快速到位）
        final double distanceY = targetY - getMascot( ).getAnchor( ).y - Math.abs( distanceX ) / 2;

        // 到修正后目标点的直线距离，作为速度方向归一化的模
        final double distance = Math.sqrt( distanceX * distanceX + distanceY * distanceY );

        // 每帧移动速度（像素/帧），可由 XML 的 VelocityParam 覆盖（默认 20）
        final double velocity = getVelocity( );

        if( distance != 0 )
        {
            // 速度按单位方向向量分解：水平/垂直分量 = velocity * (dx/distance, dy/distance)
            final int velocityX = (int)( velocity * distanceX / distance );
            final int velocityY = (int)( velocity * distanceY / distance );

            // 将分解后的速度（浮点精度）写入 VelocityX/VelocityY 变量，供动画帧 Pose 的表达式读取（如位移量、帧选择等）
            putVariable( getSchema( ).getString( VARIABLE_VELOCITYX ), velocity * distanceX / distance );
            putVariable( getSchema( ).getString( VARIABLE_VELOCITYY ), velocity * distanceY / distance );

            // 按本帧速度移动锚点，并推进当前有效动画帧
            getMascot( ).setAnchor( new Point( getMascot( ).getAnchor( ).x + velocityX, 
                                               getMascot( ).getAnchor( ).y + velocityY ) );
            getAnimation( ).next( getMascot( ), getTime( ) );
        }

        // 剩余距离不足一帧速度时直接吸附到真正的目标点 (targetX, targetY)，防止位移量超过剩余距离导致在目标点附近来回震荡（超调）
        if( distance <= velocity )
        {
            getMascot( ).setAnchor( new Point( targetX, targetY ) );
        }
    }

    private double getVelocity( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_VELOCITY ), Number.class, DEFAULT_VELOCITY ).doubleValue( );
    }

    private int getTargetX( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_TARGETX ), Number.class, DEFAULT_TARGETX ).intValue( );
    }

    private int getTargetY( ) throws VariableException
    {
        return eval( getSchema( ).getString( PARAMETER_TARGETY ), Number.class, DEFAULT_TARGETY ).intValue( );
    }
}
