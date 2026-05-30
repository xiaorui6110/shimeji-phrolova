package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */
public interface Action {

	/**
     */
    void init(Mascot mascot) throws VariableException;

	/**
     */
    boolean hasNext() throws VariableException;
	
	/**
	 *
     */
    void next() throws LostGroundException, VariableException;
	
}
