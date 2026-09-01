package com.group_finity.mascot.action;

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 移动动作：在边界（BorderType 指定的地板/天花板/墙壁）约束下，向目标点 (TargetX, TargetY)
 * 方向持续移动。位移不是本类直接计算的，而是由动画帧 Pose 的 Velocity 属性驱动
 * （每帧 getAnimation().next() 移动锚点）；本类负责：
 * 1) 朝向管理：目标在锚点右侧则面向右，移动方向与当前朝向相反时先播放转身动画
 *    （动画标签 Turn="true"）再继续移动；
 * 2) 边界约束与 LostGround 检测：锚点越出边界即抛 LostGroundException，由上层切换到下落等动作；
 * 3) 终点吸附：锚点越过目标点时直接吸附回目标点，防止动画位移造成的超调/抖动。
 * </p>
 */
public class Move extends BorderedAction {

    private static final Logger log = Logger.getLogger( Move.class.getName( ) );

    // 目标点 X 坐标：可由 XML 属性或脚本变量 TargetX 提供（如 ScanMove 每帧动态写入），
    // 缺省 Integer.MAX_VALUE 表示该轴不参与移动（保持当前 x 不变）
    private static final String PARAMETER_TARGETX = "TargetX";

    private static final int DEFAULT_TARGETX = Integer.MAX_VALUE;

    // 目标点 Y 坐标，语义同 TargetX
    private static final String PARAMETER_TARGETY = "TargetY";

    private static final int DEFAULT_TARGETY = Integer.MAX_VALUE;
    
    // 是否正在播放转身动画：turning=true 期间不移动锚点，只播放 Turn="true" 的动画
    protected boolean turning = false;
    
    // 缓存本动作是否定义了转身动画（惰性求值，见 hasTurningAnimation()）
    private Boolean hasTurning = null;

    public Move( java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap context )
    {
        super( schema, animations, context );
    }

    /**
     * 是否还有下一帧：
     * 1) 父类条件成立（Duration 未耗尽且 Condition 表达式为真）
     * 2) 尚未到达目标，或正在播放转身动画
     * 注意：hasNotReached 命名有误导性——它实际表示"已有轴到达目标"：
     * 目标被显式设置（哨兵 Integer.MIN_VALUE，注意它不同于缺省值 Integer.MAX_VALUE；
     * 未设置时该子条件退化为 anchor.x == Integer.MAX_VALUE 恒为 false，等价于该轴不参与判定）
     * 且锚点坐标已等于目标时，该值为 true。
     * 两轴都到位（或都未设置）后动作本应结束，但若 turning=true（转身动画播放中）
     * 则继续返回 true，等待转身动画播完再结束。
     */
    @Override
    public boolean hasNext( ) throws VariableException
    {
        final int targetX = getTargetX( );
        final int targetY = getTargetY( );

        boolean hasNotReached = ( targetX != Integer.MIN_VALUE && getMascot( ).getAnchor( ).x == targetX ) ||
                                ( targetY != Integer.MIN_VALUE && getMascot( ).getAnchor( ).y == targetY );

        return super.hasNext( ) && ( !hasNotReached || turning );
    }

    /**
     * 单帧推进：
     * 1) 边界约束 + LostGround 检测（锚点被边界 move() 限制后若仍不在边界上，说明已脱离地面）
     * 2) 朝向与转身动画管理：目标在右则面向右；移动方向与当前朝向相反且存在转身动画时，
     *    先播转身动画（turning=true），播完后再恢复移动
     * 3) 推进当前动画帧（帧内 Pose 的 Velocity 属性实际驱动锚点位移）
     * 4) 终点吸附：锚点已到达/越过目标轴时吸附回目标坐标，防止动画位移造成的抖动
     */
    @Override
    protected void tick( ) throws LostGroundException, VariableException
    {
        // 先用边界对象把锚点限制在边界内（如沿地板/天花板/墙壁滑动）并推进边界状态
        super.tick( );

        // LostGround 检测：有边界约束但锚点已不在边界上（如被拖拽出边界/越界），
        // 视为"丢失地面"，抛出异常由上层切换到下落等动作
        if( ( getBorder( ) != null ) && !getBorder( ).isOn( getMascot( ).getAnchor( ) ) )
        {
            log.log( Level.INFO, "Lost Ground ({0},{1})", new Object[] { getMascot( ), this } );
            throw new LostGroundException( );
        }

        int targetX = getTargetX( );
        int targetY = getTargetY( );

        // down 标志：目标点在锚点下方（用于垂直方向终点吸附时的方向判断）
        boolean down = false;

        if( targetX != DEFAULT_TARGETX )
        {
            if( getMascot( ).getAnchor( ).x != targetX )
            {
                // activate turn animation if we change directions
                // 转身激活：移动方向（anchor.x < targetX，即目标在锚点右侧）与当前朝向相反时
                // 置 turning=true，之后只播放转身动画不再移动；若已在转身中则保持，避免反复切换。
                // anchor.x == targetX 时不动（无方向变化，无需转身）
                turning = hasTurningAnimation( ) && ( turning || getMascot( ).getAnchor( ).x < targetX != getMascot( ).isLookRight( ) );
                // 朝向与移动方向保持一致（目标在右 -> 面向右）
                getMascot( ).setLookRight( getMascot( ).getAnchor( ).x < targetX );
            }
        }
        if( targetY != DEFAULT_TARGETY )
        {
            down = getMascot( ).getAnchor( ).y < targetY;
        }

        // check if turning animation has finished
        // 转身动画结束判定：动作累计时间达到转身动画总时长后关闭 turning，
        // 恢复播放普通动画并继续移动（注意 getTime() 是动作累计时间而非转身开始时间）
        if( turning && getTime( ) >= getAnimation( ).getDuration( ) )
        {
            turning = false;
        }

        // 推进当前选中动画的一帧：turning=true 时 getAnimation() 选中的是 Turn="true" 的转身动画，
        // 否则为普通动画；锚点位移由帧内 Pose 的 Velocity 属性完成
        getAnimation( ).next( getMascot( ), getTime( ) );

        // 水平终点吸附：面向右时锚点 x 已到/超过目标，或面向左时锚点 x 已到/低于目标，
        // 即本帧位移越过了目标点 -> 直接吸附回 targetX（防止超调后在目标点附近反复横跳）
        if( targetX != DEFAULT_TARGETX )
        {
            if( ( getMascot( ).isLookRight( ) && ( getMascot( ).getAnchor( ).x >= targetX ) ) || 
                ( !getMascot( ).isLookRight( ) && ( getMascot( ).getAnchor( ).x <= targetX ) ) )
            {
                getMascot( ).setAnchor( new Point( targetX, getMascot( ).getAnchor( ).y ) );
            }
        }
        // 垂直终点吸附：按 down 方向（向下/向上）判断是否已越过目标 y，越过后吸附回 targetY
        if( targetY != DEFAULT_TARGETY )
        {
            if( ( down && ( getMascot( ).getAnchor( ).y >= targetY ) ) ||
                ( !down && ( getMascot( ).getAnchor( ).y <= targetY ) ) )
            {
                getMascot( ).setAnchor( new Point( getMascot( ).getAnchor( ).x, targetY ) );
            }
        }
    }
    
    /**
     * 选择当前帧要播放的动画。重写父类的原因：父类只按 Condition 表达式选择动画，
     * 无法区分转身状态。这里额外要求"转身状态与动画的 Turn 标志匹配"：
     * turning=true 时只选 Turn="true" 的转身动画，否则只选普通动画，
     * 从而实现"先转身、后移动"的两阶段播放。无匹配时返回 null（需保证存在匹配项）。
     */
    @Override
    protected Animation getAnimation( ) throws VariableException
    {
        List<Animation> animations = super.getAnimations( );
        for (Animation animation : animations) {
            if (animation.isEffective(getVariables()) &&
                    turning == animation.isTurn()) {
                return animation;
            }
        }

        return null;
    }

    /**
     * 本动作是否定义了转身动画（任一动画的 Turn 标志为 true）。
     * 结果惰性缓存到 hasTurning 字段，首次调用后不再重复扫描动画列表。
     */
    protected boolean hasTurningAnimation( )
    {
        if( hasTurning == null )
        {
            hasTurning = false;
            List<Animation> animations = super.getAnimations( );
            for (Animation animation : animations) {
                if (animation.isTurn()) {
                    hasTurning = true;
                    break;
                }
            }
        }
        return hasTurning;
    }

    protected boolean isTurning( )
    {
        return turning;
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
