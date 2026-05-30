package com.group_finity.mascot.script;

import com.group_finity.mascot.Main;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import java.util.Map;

import com.group_finity.mascot.exception.VariableException;

/**
 * Original Author: Yuki Yamada of Group Finity (<a href="http://www.group-finity.com/Shimeji/">...</a>)
 * Currently developed by Shimeji-ee Group.
 */

public class Script extends Variable {

	private static final ContextFactory contextFactory = new ContextFactory() {
		@Override
		protected Context makeContext() {
			Context cx = super.makeContext();
			// Configure security and performance settings
			cx.setOptimizationLevel(-1); // Always use interpretive mode for security
			cx.setLanguageVersion(Context.VERSION_ES6); // Support modern JavaScript
			cx.setInstructionObserverThreshold(10000); // Prevent long-running scripts
			return cx;
		}
		
		@Override
		protected void observeInstructionCount(Context cx, int instructionCount) {
			// Prevent infinite loops or excessive computation
			if (instructionCount > 100000) {
				throw new RuntimeException("Script execution timeout - too many instructions");
			}
		}
		
		@Override
		protected boolean hasFeature(Context cx, int featureIndex) {
			// Disable potentially dangerous features
            return switch (featureIndex) {
                case Context.FEATURE_DYNAMIC_SCOPE -> false;
                case Context.FEATURE_STRICT_VARS -> true; // Enable strict variable handling
                case Context.FEATURE_STRICT_EVAL -> true; // Enable strict eval
                default -> super.hasFeature(cx, featureIndex);
            };
		}
	};

    private final String source;
	
	private final boolean clearAtInitFrame;
	
	private final org.mozilla.javascript.Script compiled;
	
	private Object value;
	
	public Script(final String source, final boolean clearAtInitFrame)  throws VariableException {
            
		this.source = source;
		this.clearAtInitFrame = clearAtInitFrame;
		
		Context cx = contextFactory.enterContext();
		try {
			// Context is already configured by our custom ContextFactory
			
			// Compile the script
			this.compiled = cx.compileString(this.source, "<script>", 1, null);
		} catch (final RhinoException e) {
			throw new VariableException( Main.getInstance( ).getLanguageBundle( ).getString( "ScriptCompilationErrorMessage" ) + ": "+this.source, e);
		} finally {
			Context.exit();
		}
	}

	@Override
	public String toString() {
		return this.isClearAtInitFrame() ? "#{"+this.getSource()+"}" : "${"+this.getSource()+"}";
	}
	
	@Override
	public void init() {
		setValue(null);
	}
	
	@Override
	public void initFrame() {
		if ( this.isClearAtInitFrame() ) {
			setValue(null);
		}
	}
	
	@Override
	public synchronized Object get(final VariableMap variables)  throws VariableException {
			
		if ( getValue()!=null ) {
			return getValue();
		}

		Context cx = contextFactory.enterContext();
		try {
			// Context is already configured by our custom ContextFactory
			
			// Create a new scope for this execution
			Scriptable scope = cx.initStandardObjects();
			
			// Populate scope with variables more efficiently
			populateScope(cx, scope, variables);
			
			// Execute the compiled script
			Object result = getCompiled().exec(cx, scope);
			
			// Convert result back to Java object with better type handling
			Object javaResult = convertJSToJava(result);
			setValue(javaResult);
			
		} catch (final RhinoException e) {
			throw new VariableException( Main.getInstance( ).getLanguageBundle( ).getString( "ScriptEvaluationErrorMessage" ) + ": "+this.source, e);
		} catch (final RuntimeException e) {
			// Handle potential circular reference or recursion issues
			throw new VariableException( Main.getInstance( ).getLanguageBundle( ).getString( "ScriptEvaluationErrorMessage" ) + ": "+this.source, e);
		} finally {
			Context.exit();
		}

		return getValue();
	}
	
	/**
	 * Efficiently populate the JavaScript scope with variables from VariableMap
	 * while avoiding recursion issues
	 */
	private void populateScope(Context cx, Scriptable scope, VariableMap variables) {
		// Use getRawMap() to avoid triggering Variable.get() which causes recursion
		if (variables == null || variables.getRawMap() == null) {
			return;
		}
		for (Map.Entry<String, Variable> entry : variables.getRawMap().entrySet()) {
			String key = entry.getKey();
			Variable variable = entry.getValue();
			
			try {
				Object value = getVariableValueSafely(variable, variables);
				if (value != null) {
					// Convert Java objects to JavaScript-compatible objects with better type mapping
					Object jsValue = convertJavaToJS(value, scope);
					ScriptableObject.putProperty(scope, key, jsValue);
				}
			} catch (Exception e) {
				// Log warning but continue with other variables
				// Could add logging here if needed
            }
		}
	}
	
	/**
	 * Safely get variable value without causing recursion
	 */
	private Object getVariableValueSafely(Variable variable, VariableMap variables) throws VariableException {
		if (variable instanceof Script scriptVar) {
			// For Script variables, use their cached value if available to prevent recursion
            return scriptVar.getValue();
		} else {
			// Safe to evaluate non-Script variables (Constant variables)
			return variable.get(variables);
		}
	}
	
	/**
	 * Convert Java objects to JavaScript with better type handling
	 */
	private Object convertJavaToJS(Object value, Scriptable scope) {
		if (value == null) {
			return null;
		}
		
		// Handle common types more efficiently
		if (value instanceof String || 
			value instanceof Number || 
			value instanceof Boolean) {
			return value; // These types are directly compatible
		}
		
		// Use Rhino's conversion for complex objects
		return Context.javaToJS(value, scope);
	}
	
	/**
	 * Convert JavaScript result to Java with better type handling
	 */
	private Object convertJSToJava(Object result) {
		if (result == null || result == org.mozilla.javascript.Undefined.instance) {
			return null;
		}
		
		// Handle primitive types directly
		if (result instanceof String || 
			result instanceof Number || 
			result instanceof Boolean) {
			return result;
		}
		
		// Convert complex objects
		return Context.jsToJava(result, Object.class);
	}

	private void setValue(final Object value) {
		this.value = value;
	}

	private Object getValue() {
		return this.value;
	}
	
	private boolean isClearAtInitFrame() {
		return this.clearAtInitFrame;
	}
	
	private org.mozilla.javascript.Script getCompiled() {
		return this.compiled;
	}
	
	private String getSource() {
		return this.source;
	}
}
