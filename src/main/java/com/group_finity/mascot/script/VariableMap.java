package com.group_finity.mascot.script;

import com.group_finity.mascot.Main;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import com.group_finity.mascot.exception.VariableException;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public class VariableMap extends AbstractMap<String, Object> implements Bindings {

	private final Map<String, Variable> rawMap = new LinkedHashMap<>();

	public Map<String, Variable> getRawMap() {
		return this.rawMap;
	}

	public void init() {
		// Use forEach with method reference for cleaner code
		this.getRawMap().values().forEach(Variable::init);
	}

	public void initFrame() {
		// Use forEach with method reference for cleaner code
		this.getRawMap().values().forEach(Variable::initFrame);
	}

	private final Set<Map.Entry<String, Object>> entrySet = new AbstractSet<>() {

        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {

            return new Iterator<>() {

                private final Iterator<Map.Entry<String, Variable>> rawIterator = VariableMap.this.getRawMap().entrySet()
                        .iterator();

                @Override
                public boolean hasNext() {
                    return this.rawIterator.hasNext();
                }

                @Override
                public Map.Entry<String, Object> next() {
                    final Map.Entry<String, Variable> rawKeyValue = this.rawIterator.next();
                    final Variable value = rawKeyValue.getValue();

                    return new Map.Entry<>() {

                        @Override
                        public String getKey() {
                            return rawKeyValue.getKey();
                        }

                        @Override
                        public Object getValue() {
                            try {
                                return value.get(VariableMap.this);
                            } catch (final VariableException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        @Override
                        public Object setValue(final Object value) {
                            throw new UnsupportedOperationException(Main.getInstance().getLanguageBundle().getString("SetValueNotSupportedErrorMessage"));
                        }

                    };
                }

                @Override
                public void remove() {
                    this.rawIterator.remove();
                }

            };
        }

        @Override
        public int size() {
            return VariableMap.this.getRawMap().size();
        }

    };

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return this.entrySet;
	}

	@Override
	public Object put(final String key, final Object value) {
		Object result;
		
		if (value instanceof Variable) {
			result = this.getRawMap().put(key, (Variable)value);
		} else {
			result = this.getRawMap().put(key, new Constant(value));
		}

		return result;

	}

}
