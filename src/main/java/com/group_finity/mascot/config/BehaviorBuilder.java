package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.behavior.UserBehavior;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 行为（Behavior）构建器：把 conf/behaviors.xml 中的一个 <Behavior> 节点解析为行为定义。
 * 行为 = 一个动作（Action 引用）+ 生效条件链（继承父级 + 自身）+ 选择频率 + 下一行为候选列表
 * （NextBehaviourList）+ 交互属性（Hidden/Toggleable）+ 附加参数。
 * 运行时由 Configuration.buildNextBehavior 按频率加权随机选择下一个行为，
 * 由 buildBehavior 创建 UserBehavior 实例。
 * </p>
 */
public class BehaviorBuilder {

	private static final Logger log = Logger.getLogger(BehaviorBuilder.class.getName());

	private final Configuration configuration;

	private final String name;

	private final String actionName;

	private final int frequency;

	private final List<String> conditions;

	private final boolean hidden;
        
	private final boolean toggleable;

	private final boolean nextAdditive;

	private final List<BehaviorBuilder> nextBehaviorBuilders = new ArrayList<>();

	private final Map<String, String> params = new LinkedHashMap<>();

	/**
	 * 解析行为节点：
	 * 1) 读取 Name/Action（缺省等于行为名）/Frequency/Hidden，并把继承条件 + 自身 Condition
	 *    组成条件链；
	 * 2) 确定 toggleable：Fall/Thrown/Dragged 三个系统必需行为强制不可禁用，其余按 Toggleable 属性；
	 * 3) 收集附加参数：全部属性剔除 Name/Action/Frequency/Hidden/Condition/Toggleable 后剩余项；
	 * 4) 遍历 <NextBehaviourList>：Add 属性决定下一行为候选是否互斥（false 时在
	 *    Configuration.buildNextBehavior 中清空既有候选池），并递归 loadBehaviors 解析候选。
	 */
	public BehaviorBuilder(final Configuration configuration, final Entry behaviorNode, final List<String> conditions) {
		this.configuration = configuration;
		this.name = behaviorNode.getAttribute( configuration.getSchema( ).getString( "Name" ) );
		this.actionName = behaviorNode.getAttribute( configuration.getSchema( ).getString( "Action" ) ) == null ? getName( ) : behaviorNode.getAttribute( configuration.getSchema( ).getString( "Action" ) );
		this.frequency = Integer.parseInt( behaviorNode.getAttribute( configuration.getSchema( ).getString( "Frequency" ) ) );
                this.hidden = Boolean.parseBoolean( behaviorNode.getAttribute( configuration.getSchema( ).getString( "Hidden" ) ) );
		this.conditions = new ArrayList<>(conditions);
		this.getConditions().add(behaviorNode.getAttribute( configuration.getSchema( ).getString( "Condition" ) ) );
                
                // override of toggleable state for required fields
                // （拖拽/抛出/下落是系统运行必需，若被禁用会导致桌宠无法交互/落地）
                if( name.equals( UserBehavior.BEHAVIOURNAME_FALL ) ||
                    name.equals( UserBehavior.BEHAVIOURNAME_THROWN ) ||
                    name.equals( UserBehavior.BEHAVIOURNAME_DRAGGED ) )
                {
                    toggleable = false;
                }
                else
                {
                    toggleable = Boolean.parseBoolean( behaviorNode.getAttribute( configuration.getSchema( ).getString( "Toggleable" ) ) );
                }
                
		log.log(Level.INFO, "Start Reading({0})", this);

		// 附加参数 = 全部属性 - 已用于本行为的属性（剩余项构建动作时传入，
		// 如 Move 的 TargetX/TargetY 等可在行为级覆盖）
		this.getParams( ).putAll( behaviorNode.getAttributes( ) );
		this.getParams( ).remove( configuration.getSchema( ).getString( "Name" ) );
		this.getParams( ).remove( configuration.getSchema( ).getString( "Action" ) );
		this.getParams( ).remove( configuration.getSchema( ).getString( "Frequency" ) );
		this.getParams( ).remove( configuration.getSchema( ).getString( "Hidden" ) );
		this.getParams( ).remove( configuration.getSchema( ).getString( "Condition" ) );
                this.getParams( ).remove( configuration.getSchema( ).getString( "Toggleable" ) );

		boolean nextAdditive = true;

		// 每个 <NextBehaviourList> 提供一组下一行为候选；多个列表时最后的 Add 值生效：
		// Add=true（缺省）追加到既有候选池，Add=false 互斥替换（见 Configuration.buildNextBehavior）
		for( final Entry nextList : behaviorNode.selectChildren( configuration.getSchema( ).getString( "NextBehaviourList" ) ) )
                {
			log.log(Level.INFO, "Lists the Following Behaviors...");

			nextAdditive = Boolean.parseBoolean( nextList.getAttribute( configuration.getSchema( ).getString( "Add"  ) ) );

			loadBehaviors(nextList, new ArrayList<>());
		}
		
		this.nextAdditive = nextAdditive;

		log.log(Level.INFO, "Behaviors have finished loading({0})", this);

	}

	@Override
	public String toString() {
		return "Behavior(" + getName() + "," + getFrequency() + "," + getActionName() + ")";
	}

	/**
	 * 递归解析 NextBehaviourList 的子节点：
	 * <Condition> 子元素把条件追加进条件链并继续向下递归（形成条件嵌套，同一分支
	 * 下的行为共享全部祖先条件）；<BehaviourReference> 子元素创建一个嵌套的
	 * BehaviorBuilder 作为本行为的下一行为候选。
	 */
	private void loadBehaviors(final Entry list, final List<String> conditions) {
		
		for (final Entry node : list.getChildren()) {

			if( node.getName( ).equals( configuration.getSchema( ).getString( "Condition" ) ) )
                        {

				final List<String> newConditions = new ArrayList<>(conditions);
				newConditions.add( node.getAttribute( configuration.getSchema( ).getString( "Condition" ) ) );

				loadBehaviors(node, newConditions);

			}
                        else if( node.getName( ).equals( configuration.getSchema( ).getString( "BehaviourReference" ) ) )
                        {
				final BehaviorBuilder behavior = new BehaviorBuilder(getConfiguration(), node, conditions);
				getNextBehaviorBuilders().add(behavior);
			}
		}
	}

	/**
	 * 校验：本行为绑定的动作必须存在于配置的动作表中。
	 * 由 Configuration.validate() 在加载完成后统一触发，缺失即抛配置异常终止启动。
	 */
	public void validate() throws ConfigurationException {
		
		if ( !getConfiguration().getActionBuilders().containsKey(getActionName()) ) {
			log.log(Level.SEVERE, "There is no corresponding action(" + this + ")");			
			throw new ConfigurationException( Main.getInstance( ).getLanguageBundle( ).getString( "NoActionFoundErrorMessage" ) + "("+this+")");
		}
	}

	/**
	 * 构建运行时行为：以行为名、绑定的动作（buildAction 传入附加参数，可覆盖动作级参数）
	 * 与配置创建 UserBehavior 实例。动作构建失败时转换为 BehaviorInstantiationException。
	 */
	public Behavior buildBehavior() throws BehaviorInstantiationException {

		try {
			return new UserBehavior(getName(),
						getConfiguration().buildAction(getActionName(), 
								getParams()), getConfiguration() );
		} catch (final ActionInstantiationException e) {
			log.log(Level.SEVERE, "Failed to initialize the corresponding action("+this+")");				
			throw new BehaviorInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedInitialiseCorrespondingActionErrorMessage" ) + "("+this+")", e);
		}
	}

	/**
	 * 本行为当前是否可被选中（由 Configuration.buildNextBehavior 调用）：
	 * 1) frequency==0 直接不可选（频率为 0 表示禁用）；
	 * 2) 条件链中所有非 null 条件在给定变量上下文中求值均为 true。
	 */
    public boolean isEffective(final VariableMap context) throws VariableException
    {
        if( frequency == 0 ) {
            return false;
        }

        for( final String condition : getConditions( ) )
        {
            if( condition != null )
            {
                if( !(Boolean)Variable.parse( condition ).get( context ) )
                {
                    return false;
                }
            }
        }

        return true;
    }
	
	public String getName() {
		return this.name;
	}

	public int getFrequency() {
		return this.frequency;
	}

    // 是否在右键菜单中隐藏（供 Configuration.isBehaviorHidden 查询）
    public boolean isHidden( )
    {
        return hidden;
    }

    // 是否受黑名单 DisabledBehaviours.{ImageSet} 控制（供 Configuration.isBehaviorEnabled 查询）
    public boolean isToggleable( )
    {
        return toggleable;
    }

	private String getActionName() {
		return this.actionName;
	}
	
	private Map<String, String> getParams() {
		return this.params;
	}
	
	private List<String> getConditions() {
		return this.conditions;
	}
	
	private Configuration getConfiguration() {
		return this.configuration;
	}

	// 下一行为候选是否"追加"：false 表示本列表互斥（buildNextBehavior 会清空既有候选池）
	public boolean isNextAdditive() {
		return this.nextAdditive;
	}

	// 下一行为候选列表（buildNextBehavior 按频率加权随机选取其一）
	public List<BehaviorBuilder> getNextBehaviorBuilders() {
		return this.nextBehaviorBuilders;
	}
}
