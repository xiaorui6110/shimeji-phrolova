package com.group_finity.mascot.config;

import java.util.Map;

import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 * <p>
 * 动作构建器接口，包含验证、构建动作
 * </p>
 */

public interface IActionBuilder {

	void validate() throws ConfigurationException;

	Action buildAction(final Map<String, String> params) throws ActionInstantiationException;

}
