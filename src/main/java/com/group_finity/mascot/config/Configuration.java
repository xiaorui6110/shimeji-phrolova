package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.behavior.UserBehavior;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;
import com.joconner.i18n.Utf8ResourceBundleControl;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 图像集配置的加载与构建总入口（每个图像集一个实例）：
 * - load()：解析图像集的 actions.xml/behaviors.xml，填充常量表、动作构建器表、行为构建器表与图像集信息；
 * - validate()：加载完成后统一校验动作/行为引用是否有效；
 * - buildAction()/buildBehavior()/buildNextBehavior()：运行时按名构建动作与行为、
 *   并按下一次行为候选池做加权随机选择（含互斥替换与兜底 Fall）。
 * </p>
 */
public class Configuration {

    private static final Logger log = Logger.getLogger( Configuration.class.getName( ) );
    // 全局常量表（<Constant> 节点）：构建变量上下文时最先放入，保证不被 mascot 等运行值覆盖
    private final Map<String, String> constants = new LinkedHashMap<>(2);
    // 动作构建器表（按动作名索引）：由 load() 填充，buildAction() 按名查找
    private final Map<String, ActionBuilder> actionBuilders = new LinkedHashMap<>();
    // 行为构建器表（按行为名索引）：由 load() 填充，构建/选择行为时按名查找
    private final Map<String, BehaviorBuilder> behaviorBuilders = new LinkedHashMap<>();
    // 图像集信息（<Information> 节点）：名称/预览图/作者等，供设置界面展示
    private final Map<String, String> information = new LinkedHashMap<>(8);
    // 属性名语言映射（schema_en_US/schema_ja_JP）：随 XML 标签语言自动选择
    private ResourceBundle schema;

    /**
     * 加载图像集配置（整个加载过程在启动期一次性完成）：
     * 1) 选择 schema：检测 XML 是否使用日文标签（動作リスト/行動リスト），
     *    以此决定用 ja-JP 还是 en-US 的属性名映射，实现两种语言的 actions.xml 兼容；
     * 2) 读取 <Constant> 常量表（可被脚本变量上下文引用，如 ${someConstant}）；
     * 3) 读取 <ActionList> 下全部 <Action> -> ActionBuilder，动作名重复则抛异常；
     * 4) 读取 <BehaviourList> 下行为 -> BehaviorBuilder（递归支持嵌套 Condition）；
     * 5) 读取 <Information> 图像集信息。
     */
    public void load( final Entry configurationNode, final String imageSet ) throws IOException, ConfigurationException
    {
        log.log( Level.INFO, "Start Reading Configuration File..." );
        
        // prepare schema
        ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl( false );
        Locale locale;

        // check for Japanese XML tag and adapt locale accordingly
        if( configurationNode.hasChild( "動作リスト" ) ||
            configurationNode.hasChild( "行動リスト" ) )
        {
            log.log( Level.INFO, "Using ja-JP schema" );
            locale = Locale.forLanguageTag( "ja-JP" );
        }
        else
        {
            log.log( Level.INFO, "Using en-US schema" );
            locale = Locale.forLanguageTag( "en-US" );
        }
        
        schema = ResourceBundle.getBundle( "schema", locale, utf8Control );
        
        // 常量表：name -> value，供脚本表达式引用
        for( Entry constant : configurationNode.selectChildren( schema.getString( "Constant" ) ) )
        {
            getConstants( ).put( constant.getAttribute( schema.getString( "Name" ) ),
                                 constant.getAttribute( schema.getString( "Value" ) ) );
        }

        // 动作表：每个 <Action> 解析为 ActionBuilder（含动画/子动作），重名直接抛错
        for( final Entry list : configurationNode.selectChildren( schema.getString( "ActionList" ) ) )
        {
            log.log( Level.INFO, "Action List..." );

            for( final Entry node : list.selectChildren( schema.getString( "Action" ) ) )
            {
                final ActionBuilder action = new ActionBuilder( this, node, imageSet );

                if( getActionBuilders( ).containsKey( action.getName( ) ) )
                {
                    throw new ConfigurationException( Main.getInstance( ).getLanguageBundle( ).getString( "DuplicateActionErrorMessage" ) + ": " + action.getName( ) );
                }

                getActionBuilders( ).put( action.getName( ), action );
            }
        }

        // 行为表：<BehaviourList> 下的行为与条件嵌套统一由 loadBehaviors 递归处理
        for( final Entry list : configurationNode.selectChildren( schema.getString( "BehaviourList" ) ) )
        {
            log.log( Level.INFO, "Behavior List..." );

            loadBehaviors( list, new ArrayList<>() );
        }
        
        // 图像集信息：名称/预览图/作者等，供设置界面展示
        for( final Entry list : configurationNode.selectChildren( schema.getString( "Information" ) ) )
        {
            log.log( Level.INFO, "Information List..." );
            
            loadInformation( list );
        }

        log.log( Level.INFO, "Configuration loaded successfully" );
    }

	/**
	 * 递归解析行为列表：<Condition> 把条件追加进条件链并继续向下递归
	 * （嵌套的 <Condition>/<Behaviour> 共享祖先条件）；<Behaviour> 创建
	 * BehaviorBuilder 并按名登记到行为表。
	 */
	private void loadBehaviors( final Entry list, final List<String> conditions )
        {
            for( final Entry node : list.getChildren( ) )
            {
                if( node.getName( ).equals( schema.getString( "Condition" ) ) )
                {
                    final List<String> newConditions = new ArrayList<>(conditions);
                    newConditions.add( node.getAttribute( schema.getString( "Condition" ) ) );

                    loadBehaviors(node, newConditions);
                }
                else if( node.getName( ).equals( schema.getString( "Behaviour" ) ) )
                {
                    final BehaviorBuilder behavior = new BehaviorBuilder( this, node, conditions );
                    this.getBehaviorBuilders( ).put( behavior.getName( ), behavior );
                }
            }
	}

	/**
	 * 按动作名构建运行时动作：从动作构建器表取出 ActionBuilder，
	 * 传入附加参数（调用方/行为级参数）构建；动作不存在时抛异常。
	 */
	public Action buildAction(final String name, final Map<String, String> params) throws ActionInstantiationException {

		final ActionBuilder factory = this.actionBuilders.get(name);
		if (factory == null) {
			throw new ActionInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "NoCorrespondingActionFoundErrorMessage" ) + ": " + name);
		}

		return factory.buildAction( params );
	}
        
    /**
     * 解析图像集信息：
     * - <Name>/<PreviewImage>/<SplashImage>：以节点名（元素名，如 "PreviewImage"）为键存文本；
     * - <Artist>/<Scripter>/<Commissioner>/<Support>：带 Name/URL 属性时以
     *   "节点名+Name"/"节点名+URL" 为组合键存姓名与链接。
     */
    private void loadInformation( final Entry list )
    {
        for( final Entry node : list.getChildren( ) )
        {
            if( node.getName( ).equals( schema.getString( "Name" ) ) ||
                node.getName( ).equals( schema.getString( "PreviewImage" ) ) ||
                node.getName( ).equals( schema.getString( "SplashImage" ) ) )
            {
                information.put( node.getName( ), node.getText( ) );
            }
            else if( node.getName( ).equals( schema.getString( "Artist" ) ) ||
                     node.getName( ).equals( schema.getString( "Scripter" ) ) || 
                     node.getName( ).equals( schema.getString( "Commissioner" ) ) ||
                     node.getName( ).equals( schema.getString( "Support" ) ) )
            {
                String nameText = node.getAttribute( schema.getString( "Name" ) ) != null ? node.getAttribute( schema.getString( "Name" ) ) : null;
                String linkText = node.getAttribute( schema.getString( "URL" ) ) != null ? node.getAttribute( schema.getString( "URL" ) ) : null;
                
                if( nameText != null )
                {
                    information.put( node.getName( ) + schema.getString( "Name" ), nameText );
                    if( linkText != null ) {
                        information.put( node.getName( ) + schema.getString( "URL" ), linkText );
                    }
                }
            }
        }
    }

	/**
	 * 加载完成后统一校验：递归校验所有动作（子动作/动作引用）与行为（动作存在性）。
	 * 任一引用无效即抛配置异常终止启动。
	 */
	public void validate() throws ConfigurationException{

		for(final ActionBuilder builder : getActionBuilders().values()) {
			builder.validate();
		}
		for(final BehaviorBuilder builder : getBehaviorBuilders().values()) {
			builder.validate();
		}
	}

    /**
     * 为桌宠选择下一个行为（上一行为结束后调用），流程：
     * 1) 构建变量上下文：常量表 + mascot（常量先放，保证不被运行值覆盖）；
     * 2) 收集全行为表中"条件为真且未被黑名单禁用"的行为作为候选，累加总频率；
     * 3) 若有上一行为（previousName）：若其 NextBehaviourList 标记为互斥（isNextAdditive()==false），
     *    清空候选池，再追加其 NextBehaviourList 中有效的候选——实现"行为链"的定向切换；
     * 4) 总频率为 0（无可选行为）时：把桌宠重投到屏幕/工作区顶部上方 256px，强制切 Fall 落回；
     * 5) 加权随机：random = Math.random()*总频率，逐个减去候选频率，首个 random<0 的行为被选中。
     */
    public Behavior buildNextBehavior( final String previousName, final Mascot mascot ) throws BehaviorInstantiationException
    {
        final VariableMap context = new VariableMap( );
        context.putAll( getConstants( ) ); // put first so they can't override mascot
        context.put( "mascot", mascot );

        // 候选池与总频率：先收集全部启用的行为作为默认候选
        final List<BehaviorBuilder> candidates = new ArrayList<>();
        long totalFrequency = 0;
        for( final BehaviorBuilder behaviorFactory : this.getBehaviorBuilders( ).values( ) )
        {
            try
            {
                if( behaviorFactory.isEffective( context ) && isBehaviorEnabled( behaviorFactory, mascot ) )
                {
                    candidates.add( behaviorFactory );
                    totalFrequency += behaviorFactory.getFrequency( );
                }
            }
            catch( final VariableException e )
            {
                log.log( Level.WARNING, "An error occurred calculating the frequency of the action", e );
            }
        }

        // 上一行为指定了互斥的 NextBehaviourList 时：清空全局候选，只从该列表中选择
        if( previousName != null )
        {
            final BehaviorBuilder previousBehaviorFactory = this.getBehaviorBuilders( ).get( previousName );
            if( !previousBehaviorFactory.isNextAdditive( ) )
            {
                totalFrequency = 0;
                candidates.clear( );
            }
            // 追加上一行为的定向候选（如 Jumping 之后必接 Falling）
            for( final BehaviorBuilder behaviorFactory : previousBehaviorFactory.getNextBehaviorBuilders( ) )
            {
                try
                {
                    if( behaviorFactory.isEffective( context ) && isBehaviorEnabled( behaviorFactory, mascot ) )
                    {
                        candidates.add( behaviorFactory );
                        totalFrequency += behaviorFactory.getFrequency( );
                    }
                }
                catch( final VariableException e )
                {
                    log.log( Level.WARNING, "An error occurred calculating the frequency of the behavior", e );
                }
            }
        }

        // 无可选行为（如全部被禁用或条件不满足）：重投屏幕上方并强制下落兜底
        if( totalFrequency == 0 )
        {
            if( Boolean.parseBoolean( Main.getInstance( ).getProperties( ).getProperty( "Multiscreen", "true" ) ) )
            {
                mascot.setAnchor( new Point( (int)( Math.random( ) * ( mascot.getEnvironment( ).getScreen( ).getRight( ) - mascot.getEnvironment( ).getScreen( ).getLeft( ) ) ) + mascot.getEnvironment( ).getScreen( ).getLeft( ),
                                             mascot.getEnvironment( ).getScreen( ).getTop( ) - 256 ) );
            }
            else
            {
                mascot.setAnchor( new Point( (int)( Math.random( ) * ( mascot.getEnvironment( ).getWorkArea( ).getRight( ) - mascot.getEnvironment( ).getWorkArea( ).getLeft( ) ) ) + mascot.getEnvironment( ).getWorkArea( ).getLeft( ),
                                             mascot.getEnvironment( ).getWorkArea( ).getTop( ) - 256 ) );
            }
            return buildBehavior( schema.getString( UserBehavior.BEHAVIOURNAME_FALL ) );
        }

        // 加权随机：frequency 越大的行为被选中的概率越高
        double random = Math.random( ) * totalFrequency;

        for( final BehaviorBuilder behaviorFactory : candidates )
        {
            random -= behaviorFactory.getFrequency( );
            if( random < 0 )
            {
                return behaviorFactory.buildBehavior( );
            }
        }

        return null;
    }

    /**
     * 按行为名构建行为（带桌宠上下文，供 UserBehavior.next() 等按名切换行为时使用）：
     * 1) 行为存在且未被黑名单禁用 -> 构建返回；
     * 2) 行为存在但被禁用 -> 把桌宠重投到屏幕/工作区顶部上方 256px，强制切 Fall（防卡死）；
     * 3) 行为不存在 -> 抛异常。
     */
    public Behavior buildBehavior( final String name, final Mascot mascot ) throws BehaviorInstantiationException
    {
        if( behaviorBuilders.containsKey( name ) )
        {
            if( isBehaviorEnabled( name, mascot ) )
            {
                return getBehaviorBuilders( ).get( name ).buildBehavior( );
            }
            else
            {
                // 被禁用的行为被请求时，重投屏幕上方并以 Fall 兜底，避免桌宠进入无法交互的状态
                if( Boolean.parseBoolean( Main.getInstance( ).getProperties( ).getProperty( "Multiscreen", "true" ) ) )
                {
                    mascot.setAnchor( new Point( (int)( Math.random( ) * ( mascot.getEnvironment( ).getScreen( ).getRight( ) - mascot.getEnvironment( ).getScreen( ).getLeft( ) ) ) + mascot.getEnvironment( ).getScreen( ).getLeft( ),
                                                 mascot.getEnvironment( ).getScreen( ).getTop( ) - 256 ) );
                }
                else
                {
                    mascot.setAnchor( new Point( (int)( Math.random( ) * ( mascot.getEnvironment( ).getWorkArea( ).getRight( ) - mascot.getEnvironment( ).getWorkArea( ).getLeft( ) ) ) + mascot.getEnvironment( ).getWorkArea( ).getLeft( ),
                                                 mascot.getEnvironment( ).getWorkArea( ).getTop( ) - 256 ) );
                }
                return buildBehavior( schema.getString( UserBehavior.BEHAVIOURNAME_FALL ) );
            }
        }
        else {
            throw new BehaviorInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "NoBehaviourFoundErrorMessage" ) + " (" + name + ")" );
        }
    }

    /**
     * 按行为名构建行为（无桌宠上下文，仅校验存在性，供启动期/无桌宠场景使用）
     */
    public Behavior buildBehavior( final String name ) throws BehaviorInstantiationException
    {
        if( behaviorBuilders.containsKey( name ) ) {
            return getBehaviorBuilders( ).get( name ).buildBehavior( );
        } else {
            throw new BehaviorInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "NoBehaviourFoundErrorMessage" ) + " (" + name + ")" );
        }
    }
    
    /**
     * 行为是否可用：仅 toggleable 行为受黑名单控制——在设置中
     * DisabledBehaviours.{ImageSet}（"/" 分隔的行为名列表）命中即禁用；
     * 非 toggleable（Fall/Thrown/Dragged）恒可用。
     */
    public boolean isBehaviorEnabled( final BehaviorBuilder builder, final Mascot mascot )
    {
        if( builder.isToggleable( ) )
        {
            for( String behaviour : Main.getInstance( ).getProperties( ).getProperty( "DisabledBehaviours." + mascot.getImageSet( ), "" ).split( "/" ) )
            {
                if( behaviour.equals( builder.getName( ) ) ) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 行为名版本：行为存在时委托给 BehaviorBuilder 版本，不存在视为不可用
     */
    public boolean isBehaviorEnabled( final String name, final Mascot mascot )
    {
        if( behaviorBuilders.containsKey( name ) ) {
            return isBehaviorEnabled( getBehaviorBuilders( ).get( name ), mascot );
        } else {
            return false;
        }
    }
    
    /**
     * 行为是否在右键菜单中隐藏（供菜单构建查询）
     */
    public boolean isBehaviorHidden( final String name )
    {
        if( behaviorBuilders.containsKey( name ) ) {
            return getBehaviorBuilders( ).get( name ).isHidden( );
        } else {
            return false;
        }
    }
    
    /**
     * 行为是否可被禁用（toggleable，供设置界面显示禁用复选框）
     */
    public boolean isBehaviorToggleable( final String name )
    {
        if( behaviorBuilders.containsKey( name ) ) {
            return getBehaviorBuilders( ).get( name ).isToggleable( );
        } else {
            return false;
        }
    }

    private Map<String, String> getConstants( )
    {
        return constants;
    }

    Map<String, ActionBuilder> getActionBuilders( )
    {
        return actionBuilders;
    }

    private Map<String, BehaviorBuilder> getBehaviorBuilders( )
    {
        return behaviorBuilders;
    }

    public java.util.Set<String> getBehaviorNames( )
    {
        return behaviorBuilders.keySet( );
    }

    public boolean containsInformationKey( String key )
    {
        return information.containsKey( key );
    }

    public String getInformation( String key )
    {
        return information.get( key );
    }

    public java.util.ResourceBundle getSchema( )
    {
        return schema;
    }
}
