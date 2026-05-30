package com.group_finity.mascot.exception;

import java.io.Serial;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public class ActionInstantiationException extends Exception{

	/**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 1L;

	public ActionInstantiationException(final String message) {
		super(message);
	}

	public ActionInstantiationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
