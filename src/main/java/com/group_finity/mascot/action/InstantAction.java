package com.group_finity.mascot.action;

import java.util.ArrayList;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */
public abstract class InstantAction extends ActionBase {

	public InstantAction( java.util.ResourceBundle schema, final VariableMap params) {
		super( schema, new ArrayList<>(), params);

	}

	@Override
	public final void init(final Mascot mascot) throws VariableException {
		super.init(mascot);

		if (super.hasNext()) {
			apply();
		}
	}

	protected abstract void apply() throws VariableException;

	@Override
	public final boolean hasNext() throws VariableException {
        super.hasNext();
        return false;
	}

	@Override
	protected final void tick() {
	}
}
