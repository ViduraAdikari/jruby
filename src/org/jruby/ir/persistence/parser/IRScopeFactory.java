package org.jruby.ir.persistence.parser;

import java.util.List;

import org.jruby.ir.IRClassBody;
import org.jruby.ir.IRManager;
import org.jruby.ir.IRMetaClassBody;
import org.jruby.ir.IRMethod;
import org.jruby.ir.IRModuleBody;
import org.jruby.ir.IRScope;
import org.jruby.ir.IRScopeType;
import org.jruby.ir.IRScriptBody;
import org.jruby.ir.instructions.Instr;
import org.jruby.parser.IRStaticScope;
import org.jruby.parser.IRStaticScopeFactory;
import org.jruby.parser.IRStaticScopeType;
import org.jruby.parser.StaticScope;

public enum IRScopeFactory {
    INSTANCE;

    private static final String SCRIPT_BODY_PSEUDO_CLASS_NAME = "_file_";

    public IRScope createScope(IRScopeType type, IRScope lexicalParent, String name, String lineNumberString, IRStaticScope staticScope) {
        IRManager manager = IRParsingContext.INSTANCE.getIRManager();
        int lineNumber = Integer.parseInt(lineNumberString);
        IRScope scope;
        switch (type) {
        case CLASS_BODY:
            scope = new IRClassBody(manager, lexicalParent, name, lineNumber, staticScope);
            break;
        case METACLASS_BODY:
            scope = new IRMetaClassBody(manager, lexicalParent, name, lineNumber, staticScope);
            break;
        case METHOD:
            // FIXME: not instance methods are not supported so far 
            boolean isInstanceMethod = true;
            scope = new IRMethod(manager, lexicalParent, name, isInstanceMethod, lineNumber, staticScope);
            break;
        case MODULE_BODY:
            scope = new IRModuleBody(manager, lexicalParent, name, lineNumber, staticScope);
            break;
        case SCRIPT_BODY:
            scope = new IRScriptBody(manager, SCRIPT_BODY_PSEUDO_CLASS_NAME, name, staticScope);
            break;

        case CLOSURE:
        case EVAL_SCRIPT:
        default:
            throw new UnsupportedOperationException();
        }
        IRParsingContext.INSTANCE.addToScopes(scope);
        
        return scope;
    }
    
    public IRScope findLexicalParent(String name) {
        IRScope parent = IRParsingContext.INSTANCE.getScopeByName(name);
        // Its a side effect
        IRParsingContext.INSTANCE.setCurrentScope(parent);
        return parent;
    }
    
    public IRStaticScope buildStaticScope(IRStaticScopeType type, String[] names ) {
        // Use a side effect form 'findLexicalParent'
        IRScope currentScope = IRParsingContext.INSTANCE.getCurrentScope();
        StaticScope parent = null;
        if(currentScope != null) {
            parent = currentScope.getStaticScope();
        }
        return IRStaticScopeFactory.newStaticScope(parent, type, names);
    }
    
    public IRScope addToScope(IRScope scope, List<Instr> instrs) {        
        for (Instr instr : instrs) {
            scope.addInstr(instr);
        }
        
        return scope;
    }
    
    public IRScope enterScope(String name) {
        IRScope scope = IRParsingContext.INSTANCE.getScopeByName(name);
        IRParsingContext.INSTANCE.setCurrentScope(scope);
        return scope;
    }

}
