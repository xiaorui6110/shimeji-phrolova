package com.group_finity.mascot.config;

import com.group_finity.mascot.Main;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;

import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.action.Animate;
import com.group_finity.mascot.action.Move;
import com.group_finity.mascot.action.Select;
import com.group_finity.mascot.action.Sequence;
import com.group_finity.mascot.action.Stay;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.AnimationInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 动作（Action）构建器：把 actions.xml 中的一个 <Action> 节点解析为运行时 Action 对象。
 * 一个动作由三要素构成：参数变量（Variables）、动画列表（Animations）、子动作（Child Actions）。
 * 通过 Type 属性决定实例化方式：Embedded（反射创建自定义动作类）或内置类型
 * （Move/Stay/Animate/Sequence/Select）。动作之间通过 ActionReference 延迟引用连接。
 * </p>
 */
public class ActionBuilder implements IActionBuilder {

	private static final Logger log = Logger.getLogger( ActionBuilder.class.getName( ) );
	private final String type;
	private final String name;
	private final String className;
	private final Map<String, String> params = new LinkedHashMap<>();
	private final List<AnimationBuilder> animationBuilders = new ArrayList<>();
	private final List<IActionBuilder> actionRefs = new ArrayList<>();
	private final ResourceBundle schema;

	/**
	 * 解析动作节点：
	 * 1) 读取 Name/Type/Class 属性（属性名经 schema 本地化映射）；
	 * 2) 节点全部属性收集进 params，作为动作的参数变量；
	 * 3) 子元素 <Animation> -> AnimationBuilder（立即解析动画帧）；
	 * 4) 子元素 <ActionReference> -> ActionRef（只记录动作名，构建时由 Configuration 按名查找，
	 *    即"延迟引用"）；嵌套 <Action> -> 递归 ActionBuilder（立即解析）。
	 */
	public ActionBuilder( final Configuration configuration, final Entry actionNode, final String imageSet ) throws IOException
        {
            schema = configuration.getSchema( );
            name = actionNode.getAttribute( schema.getString( "Name" ) );
            type = actionNode.getAttribute( schema.getString( "Type" ) );
            className = actionNode.getAttribute( schema.getString( "Class" ) );
            
            log.log( Level.INFO, "Read Start Operation({0})", this );

            // 节点全部属性 -> 参数表（如 Move 的 TargetX/TargetY、表达式类型的 XML 属性均在此）
            getParams( ).putAll( actionNode.getAttributes( ) );
            for( final Entry node : actionNode.selectChildren( schema.getString( "Animation" ) ) )
            {
                getAnimationBuilders( ).add( new AnimationBuilder( schema, node, imageSet ) );
            }

            for( final Entry node : actionNode.getChildren( ) )
            {
                if( node.getName( ).equals( schema.getString( "ActionReference" ) ) )
                {
                    getActionRefs( ).add( new ActionRef( configuration, node ) );
                }
                else if( node.getName( ).equals( schema.getString( "Action" ) ) )
                {
                    getActionRefs( ).add( new ActionBuilder( configuration, node, imageSet ) );
                }
            }

            log.log( Level.INFO, "Actions Finished Loading" );
	}

	@Override
	public String toString( )
        {
	    return "Action(" + getName( ) + "," + getType( ) + "," + getClassName( ) + ")";
	}

	/**
	 * 构建运行时 Action 对象：
	 * 1) 先构建三要素：参数变量（本节点属性 + 调用方传入参数，后者覆盖前者，值经 Variable.parse
	 *    解析为表达式/常量）、动画列表、子动作列表（以空参数构建）；
	 * 2) 按 Type 分派实例化：
	 *    - Embedded：反射创建自定义动作类，依次尝试三个构造器
	 *      (schema, animations, variables) -> (schema, variables) -> 无参，逐级降级；
	 *      反射失败按异常类型（实例化失败/无访问权限/类不存在）转成对应的本地化消息异常；
	 *    - Move/Stay/Animate：需要动画列表与参数表；
	 *    - Sequence/Select：需要参数表与子动作数组（依次执行/条件分支）；
	 *    - 其他：抛"未知动作类型"异常。
	 * 3) 动画/参数构建阶段抛出的异常统一转换为 ActionInstantiationException。
	 *
	 * @param params 调用方（上级动作/行为）传入的附加参数，可覆盖本节点 XML 属性
	 */
	@Override
    @SuppressWarnings("unchecked")
	public Action buildAction( final Map<String, String> params) throws ActionInstantiationException {

		try {
			// Create Variable Map
			final VariableMap variables = createVariables(params);

			// Create Animations
			final List<Animation> animations = createAnimations();

			// Create Child Actions
			final List<Action> actions = createActions( );

			// 反射实例化自定义动作类（<Action Type="Embedded" Class="...">）
			if( this.type.equals( schema.getString( "Embedded" ) ) )
                        {
				try {
					final Class<? extends Action> cls = (Class<? extends Action>) Class.forName(this.getClassName());
					try {

						try {
							// 优先：带动画列表与参数表的构造器（内置动作如 Move/Animate 的签名）
							return cls.getConstructor( ResourceBundle.class, List.class, VariableMap.class ).newInstance( schema, animations, variables);
						} catch (final Exception e) {
							// NOTE There's no constructor
						}

						// 其次：仅带参数表的构造器（如 Mute 等无动画的一次性动作）
						return cls.getConstructor( ResourceBundle.class, VariableMap.class ).newInstance( schema, variables );
					} catch (final Exception e) {
						// NOTE There's no constructor
					}

					// 兜底：无参构造器
					return cls.getDeclaredConstructor().newInstance();
				} catch (final InstantiationException | InvocationTargetException | NoSuchMethodException e) {
					throw new ActionInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedClassActionInitialiseErrorMessage" ) + "(" + this + ")", e);
				} catch (final IllegalAccessException e) {
					throw new ActionInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "CannotAccessClassActionErrorMessage" ) + "(" + this + ")", e);
				} catch (final ClassNotFoundException e) {
					throw new ActionInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "ClassNotFoundErrorMessage" ) + "(" + this + ")", e);
				}

                        } else if( this.type.equals( schema.getString( "Move" ) ) ) {
                            return new Move( schema, animations, variables );
			} else if( this.type.equals( schema.getString( "Stay" ) ) ) {
                            return new Stay( schema, animations, variables);
			} else if( this.type.equals( schema.getString( "Animate" ) ) ) {
                            return new Animate( schema, animations, variables);
			} else if( this.type.equals( schema.getString( "Sequence" ) ) ) {
                            return new Sequence( schema, variables, actions.toArray(new Action[0]));
			} else if( this.type.equals( schema.getString( "Select" ) ) ) {
                            return new Select( schema, variables, actions.toArray(new Action[0]));
			} else {
                            throw new ActionInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "UnknownActionTypeErrorMessage" ) + "(" + this + ")");
			}

		} catch (final AnimationInstantiationException e) {
			throw new ActionInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedCreateAnimationErrorMessage" ) + "(" + this + ")", e);
		} catch (final VariableException e) {
			throw new ActionInstantiationException( Main.getInstance( ).getLanguageBundle( ).getString( "FailedParameterEvaluationErrorMessage" ) + "(" + this + ")", e);
		}
	}

	/**
	 * 递归校验子动作引用：ActionRef 会检查目标动作是否存在于 Configuration 的动作表，
	 * 嵌套 ActionBuilder 递归自身校验。由 Configuration.validate() 在全部加载完成后调用。
	 */
	@Override
    public void validate() throws ConfigurationException {

		for (final IActionBuilder ref : this.getActionRefs()) {
			ref.validate();
		}
	}
	
	// 构建子动作列表：ActionReference 在此按名解析为真实动作（延迟引用的落地点），
	// 嵌套 Action 直接递归构建；子动作均以空参数构建
	private List<Action> createActions( ) throws ActionInstantiationException {
		final List<Action> actions = new ArrayList<>();
		for (final IActionBuilder ref : this.getActionRefs()) {
			actions.add( ref.buildAction(new HashMap<>() ) );
		}
		return actions;
	}

	// 构建动画帧列表（AnimationBuilder 已解析好图片/位移/时长等）
	private List<Animation> createAnimations() throws AnimationInstantiationException {
		final List<Animation> animations = new ArrayList<>();
		for (final AnimationBuilder animationFactory : this.getAnimationBuilders()) {
			animations.add(animationFactory.buildAnimation());
		}
		return animations;
	}

	// 构建参数变量表：先装入本节点 XML 属性，再用调用方传入的参数覆盖同名项（后者优先级高）；
	// 每个值经 Variable.parse 解析：${...} 视为表达式，其余视为字符串常量
	private VariableMap createVariables(final Map<String, String> params) throws VariableException {
		final VariableMap variables = new VariableMap();
		for (final Map.Entry<String, String> param : this.getParams().entrySet()) {
			variables.put(param.getKey(), Variable.parse(param.getValue()));
		}
		for (final Map.Entry<String, String> param : params.entrySet()) {
			variables.put(param.getKey(), Variable.parse(param.getValue()));
		}
		return variables;
	}

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	private String getClassName() {
		return this.className;
	}

	private Map<String, String> getParams() {
		return this.params;
	}

	private List<AnimationBuilder> getAnimationBuilders() {
		return this.animationBuilders;
	}

	private List<IActionBuilder> getActionRefs() {
		return this.actionRefs;
	}


}
