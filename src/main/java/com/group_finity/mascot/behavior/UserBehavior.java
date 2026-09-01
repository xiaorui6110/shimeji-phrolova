package com.group_finity.mascot.behavior;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.action.ActionBase;
//import com.group_finity.mascot.action.Dragged;
//import com.group_finity.mascot.action.Regist;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.hotspot.Hotspot;

/**
 * Simple Sample Behavior.
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 行为（Behavior）= 一个动作（Action）+ 一整套鼠标交互与约束处理的状态机：
 * - 每帧由 next() 推进：驱动动作播放、处理按住鼠标期间新出现的热点、屏幕出界重投、动作结束后的行为切换；
 * - 鼠标按下/释放由 mousePressed()/mouseReleased() 处理：热点点击、拖拽启动/禁用、松手抛出；
 * - 执行中丢失地面（LostGroundException）统一回退到 Fall 行为。
 * </p>
 */
public class UserBehavior implements Behavior {

    private static final Logger log = Logger.getLogger( UserBehavior.class.getName() );

    public static final String BEHAVIOURNAME_FALL = "Fall";

    public static final String BEHAVIOURNAME_DRAGGED = "Dragged";

    public static final String BEHAVIOURNAME_THROWN = "Thrown";

    private enum HotspotResult { INACTIVE, ACTIVE_NULL, ACTIVE }

    private final String name;

    private final Configuration configuration;

    private final Action action;

    private Mascot mascot;

    public UserBehavior( final String name, final Action action, final Configuration configuration )
    {
        this.name = name;
        this.configuration = configuration;
        this.action = action;
    }

    @Override
    public String toString( )
    {
        return "Behavior(" + getName( ) + ")";
    }

    /**
     * 行为初始化：绑定桌宠并初始化内部动作。
     * 若动作一帧都无法播放（hasNext() 为 false，如 Condition 恒为假），
     * 则直接按 NextBehavior 链切换到下一个行为，本行为不进入播放。
     */
    @Override
    public synchronized void init( final Mascot mascot ) throws CantBeAliveException
    {
        this.setMascot( mascot );

        log.log( Level.INFO, "Default Behavior({0},{1})", new Object[ ]
        {
             this.getMascot( ), this
        } );

        try
        {
            getAction( ).init( mascot );
            if( !getAction( ).hasNext( ) )
            {
                try
                {
                    mascot.setBehavior( this.getConfiguration( ).buildNextBehavior( getName( ), mascot ) );
                }
                catch( final BehaviorInstantiationException e )
                {
                    throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedInitialiseFollowingBehaviourErrorMessage" ), e );
                }
            }
        }
        catch( final VariableException e )
        {
            throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "VariableEvaluationErrorMessage" ), e );
        }
    }

    private Configuration getConfiguration( )
    {
        return this.configuration;
    }

    private Action getAction( )
    {
        return this.action;
    }

    private String getName( )
    {
        return this.name;
    }

    /**
     * On Mouse Pressed. Start dragging.
     * <p>
     * 左键按下处理（按优先级）：
     * 1) 热点命中（几何包含 + 行为未被 DisabledBehaviours.{ImageSet} 禁用）-> 记录光标并切换热点行为；
     * 2) 否则若当前动作声明不可拖拽（isDraggable() 为 false）-> 本次按下不进入拖拽；
     * 3) 否则进入拖拽：切换到 Dragged 行为。
     * @ Throws CantBeAliveException
     */
    @Override
    public synchronized void mousePressed(final MouseEvent event ) throws CantBeAliveException
    {
        if( SwingUtilities.isLeftMouseButton( event ) )
        {
            // handled=true 表示本次按下已被处理（热点激活或动作不可拖拽），不再进入默认拖拽
            boolean handled = false;

            // check for hotspots
            if( !mascot.getHotspots( ).isEmpty( ) )
            {
                for( final Hotspot hotspot : mascot.getHotspots( ) )
                {
                    if( hotspot.contains( mascot, event.getPoint( ) ) &&
                        Main.getInstance( ).getConfiguration( mascot.getImageSet( ) ).isBehaviorEnabled( hotspot.behaviour( ), mascot ) )
                    {
                        // activate hotspot
                        handled = true;
                        try
                        {
                            // 记录光标位置：isHotspotClicked() = cursor != null，按住期间据此做热点再检
                            getMascot( ).setCursorPosition( event.getPoint( ) );
                            // 行为为 null 的热点仅拦截拖拽（handled 已置 true），不切换行为
                            if( hotspot.behaviour( ) != null ) {
                                getMascot( ).setBehavior( configuration.buildBehavior( hotspot.behaviour( ), mascot ) );
                            }
                        }
                        catch( final BehaviorInstantiationException e )
                        {
                            throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedInitialiseFollowingBehaviourErrorMessage" ) + " " + hotspot.behaviour(), e );
                        }
                        break;
                    }
                }
            }

            // check if this action has dragging disabled
            if( !handled && action != null && action instanceof ActionBase )
            {
                try
                {
                    // 动作标记为不可拖拽时视为已处理：桌宠不进入拖拽（留在当前动作继续播放）
                    handled = !( (ActionBase)action ).isDraggable( );
                }
                catch( VariableException ex )
                {
                    throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedDragActionInitialiseErrorMessage" ), ex );
                }
            }

            if( !handled )
            {
                // Begin dragging
                try
                {
                    getMascot( ).setBehavior( configuration.buildBehavior( configuration.getSchema( ).getString( BEHAVIOURNAME_DRAGGED ) ) );
                }
                catch( final BehaviorInstantiationException e )
                {
                    throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedDragActionInitialiseErrorMessage" ), e );
                }
            }
        }
    }

    /**
     * On Mouse Release. End dragging.
     * <p>
     * 左键释放处理：
     * 1) 若此前是热点点击（isHotspotClicked()），先清除光标结束点击状态；
     * 2) 若正处于拖拽中（isDragging()），结束拖拽并切换到 Thrown 行为（甩出桌宠）。
     * @ Throws CantBeAliveException
     */
    @Override
    public synchronized void mouseReleased(final MouseEvent event ) throws CantBeAliveException
    {
        if( SwingUtilities.isLeftMouseButton( event ) )
        {
            if( getMascot( ).isHotspotClicked( ) ) {
                getMascot( ).setCursorPosition( null );
            }

            // check if we are in the middle of a drag, otherwise we do nothing
            if( getMascot( ).isDragging( ) )
            {
                try
                {
                    getMascot( ).setDragging( false );
                    getMascot( ).setBehavior( configuration.buildBehavior( configuration.getSchema( ).getString( BEHAVIOURNAME_THROWN ) ) );
                }
                catch( final BehaviorInstantiationException e )
                {
                    throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedDropActionInitialiseErrorMessage" ), e );
                }
            }
        }
    }

    /**
     * 单帧推进（由桌宠线程每帧调用），流程：
     * 1) 动作还有帧则推进一帧（可能抛 LostGroundException，统一切 Fall）；
     * 2) 若鼠标处于按住状态（isHotspotClicked()），对按住期间新出现/移入的热点再检：
     *    命中并切换行为 -> ACTIVE（本帧不再处理动作）；命中但行为为 null -> ACTIVE_NULL；
     *    未命中 -> INACTIVE（清除光标）；
     * 3) 未切换热点行为时：
     *    a. 动作仍在播放：检查桌宠是否完全移出屏幕（左/右/下边界），出界则随机重投到
     *       屏幕（或工作区）顶部上方 256px，切 Fall 让其落回；
     *    b. 动作已结束：按 NextBehavior 频率表构建下一个行为并切换；
     * 4) LostGroundException：清除光标/拖拽状态后切 Fall；
     *    VariableException：转为致命异常 CantBeAliveException。
     */
    @Override
    public synchronized void next( ) throws CantBeAliveException
    {
        try
        {
            if( getAction( ).hasNext( ) )
            {
                getAction( ).next( );
            }

            HotspotResult hotspotIsActive = HotspotResult.INACTIVE;
            if( getMascot( ).isHotspotClicked( ) )
            {
                // activate any hotspots that emerge while mouse is held down
                // （按住期间光标移入新出现的 Hotspot 区域同样触发）
                if( !mascot.getHotspots( ).isEmpty( ) )
                {
                    for( final Hotspot hotspot : mascot.getHotspots( ) )
                    {
                        if( hotspot.contains( mascot, mascot.getCursorPosition( ) ) )
                        {
                            // activate hotspot
                            hotspotIsActive = HotspotResult.ACTIVE_NULL;
                            try
                            {
                                // no need to set cursor position, it's already set
                                if( hotspot.behaviour( ) != null )
                                {
                                    hotspotIsActive = HotspotResult.ACTIVE;
                                    getMascot( ).setBehavior( configuration.buildBehavior( hotspot.behaviour( ), mascot ) );
                                }
                            }
                            catch( final BehaviorInstantiationException e )
                            {
                                throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedInitialiseFollowingBehaviourErrorMessage" ) + " " + hotspot.behaviour(), e );
                            }
                            break;
                        }
                    }
                }

                // 按住期间未命中任何热点 -> 清除光标，结束点击状态
                if( hotspotIsActive == HotspotResult.INACTIVE )
                {
                    getMascot( ).setCursorPosition( null );
                }
            }

            // 已通过热点切换了行为则本帧不再推进动作/检查边界
            if( hotspotIsActive != HotspotResult.ACTIVE )
            {
                if( getAction( ).hasNext( ) )
                {
                    if( ( getMascot( ).getBounds( ).getX( ) + getMascot( ).getBounds( ).getWidth( )
                            <= getEnvironment( ).getScreen( ).getLeft() )
                            || ( getEnvironment( ).getScreen( ).getRight( ) <= getMascot( ).getBounds( ).getX( ) )
                            || ( getEnvironment( ).getScreen( ).getBottom( ) <= getMascot( ).getBounds( ).getY( ) ) )
                    {
                        log.log( Level.INFO, "Out of the screen bounds({0},{1})", new Object[ ] { getMascot( ), this } );

                        // Multiscreen=true（默认）：随机重投到桌宠当前所在屏幕；
                        // false：重投到所有屏幕并集的工作区内（防止桌宠落到另一块屏幕上）
                        if( Boolean.parseBoolean( Main.getInstance( ).getProperties( ).getProperty( "Multiscreen", "true" ) ) )
                        {
                            // 屏幕内随机 x，y 固定为屏幕顶部上方 256px（随后由 Fall 落回屏幕）
                            getMascot( ).setAnchor( new Point( (int)( Math.random() * ( getEnvironment( ).getScreen( ).getRight( ) - getEnvironment( ).getScreen( ).getLeft( ) ) ) + getEnvironment( ).getScreen( ).getLeft( ),
                                                              getEnvironment( ).getScreen( ).getTop( ) - 256 ) );
                        }
                        else
                        {
                            // 工作区内随机 x，y 固定为工作区顶部上方 256px
                            getMascot( ).setAnchor( new Point( (int)( Math.random() * ( getEnvironment( ).getWorkArea( ).getRight( ) - getEnvironment( ).getWorkArea( ).getLeft( ) ) ) + getEnvironment( ).getWorkArea( ).getLeft( ),
                                                              getEnvironment( ).getWorkArea( ).getTop( ) - 256 ) );
                        }

                        try
                        {
                            getMascot( ).setBehavior( this.getConfiguration( ).buildBehavior( configuration.getSchema( ).getString( BEHAVIOURNAME_FALL ) ) );
                        }
                        catch( final BehaviorInstantiationException e )
                        {
                            throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedFallingActionInitialiseErrorMessage" ), e );
                        }
                    }
                }
                else
                {
                    log.log( Level.INFO, "Completed Behavior ({0},{1})", new Object[ ]
                     {
                         getMascot( ), this
                    } );

                    try
                    {
                        getMascot( ).setBehavior( this.getConfiguration( ).buildNextBehavior( getName( ), getMascot( ) ) );
                    }
                    catch( final BehaviorInstantiationException e )
                    {
                        throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedInitialiseFollowingActionsErrorMessage" ), e );
                    }
                }
            }
        }
        catch( final LostGroundException e )
        {
            log.log( Level.INFO, "Lost Ground ({0},{1})", new Object[]
             {
                 getMascot( ), this
            } );

            try
            {
                getMascot( ).setCursorPosition( null );
                getMascot( ).setDragging( false );
                getMascot( ).setBehavior( configuration.buildBehavior( configuration.getSchema( ).getString( BEHAVIOURNAME_FALL ) ) );
            }
            catch( final BehaviorInstantiationException ex )
            {
                throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedFallingActionInitialiseErrorMessage" ), e );
            }
        }
        catch( final VariableException e )
        {
            throw new CantBeAliveException( Main.getInstance( ).getLanguageBundle( ).getString( "VariableEvaluationErrorMessage" ), e );
        }
    }

    private void setMascot( final Mascot mascot )
    {
        this.mascot = mascot;
    }

    private Mascot getMascot( )
    {
        return this.mascot;
    }

    // 桌宠所处的屏幕/工作区环境（出界重投与边界判断用）
    protected MascotEnvironment getEnvironment( )
    {
        return getMascot( ).getEnvironment( );
    }
}
