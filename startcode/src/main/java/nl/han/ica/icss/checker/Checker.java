package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * CH01, CH02, CH03, CH04, CH05, CH06.
 */
public class Checker {

    private final IHANLinkedList<HashMap<String, Expression>> variableTypes = new HANLinkedList<>();

    public void check(AST ast) {
        if (ast == null) throw new IllegalArgumentException("AST must not be null");
        if (ast.root == null) throw new IllegalArgumentException("AST root must not be null");


        pushScope();
        try {
            visit(ast.root);
        } finally {
            popScope();
        }
    }

    private void visit(ASTNode node) {
        if (node == null) return;

        if (node instanceof Declaration declaration) {
            checkDeclaration(declaration);
        } else if (node instanceof Operation operation) {
            checkOperation(operation);
        } else if (node instanceof IfClause ifClause) {
            checkIfClause(ifClause);
        } else if (node instanceof VariableAssignment assignment) {
            currentScope().put(assignment.name.name, assignment.expression);
        } else if (node instanceof VariableReference reference) {
            checkScope(reference);
        }

        if (!node.getChildren().isEmpty()) {
            pushScope();
            try {
                for (ASTNode child : node.getChildren()) {
                    visit(child);
                }
            } finally {
                popScope();
            }
        }
    }

    private void checkScope(VariableReference reference) {
        if (!isDefined(reference.name)) {
            reference.setError("variable out of scope");
        }
    }

    private boolean isDefined(String name) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(name)) return true;
        }
        return false;
    }

    private Expression resolveVariable(VariableReference reference) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            Expression found = variableTypes.get(i).get(reference.name);
            if (found != null) return found;
        }
        return null;
    }

    private Literal resolveToLiteral(Expression expression) {
        if (expression == null) return null;
        if (expression instanceof Literal literal) return literal;
        if (expression instanceof VariableReference ref) return resolveToLiteral(resolveVariable(ref));
        if (expression instanceof Operation op) return checkOperation(op);
        return null;
    }

    private void checkDeclaration(Declaration declaration) {
        Expression raw = declaration.expression;
        Literal value = resolveToLiteral(raw);

        String property = declaration.property.name;
        switch (property) {
            case "color", "background-color" -> {
                if (!(value instanceof ColorLiteral)) {
                    declaration.setError(property + " value can only be color literal");
                }
            }
            case "width", "height" -> {
                boolean ok = value instanceof PixelLiteral || value instanceof PercentageLiteral;
                if (!ok) ok = raw instanceof Operation; // operatie mag ook, mits elders gevalideerd
                if (!ok) declaration.setError(property + " must have pixel or percentage value");
            }
            default -> declaration.setError("unknown property name");
        }
    }

    private enum Kind { PIXEL, PERCENTAGE, SCALAR, COLOR, BOOL, UNKNOWN }

    private Kind kindOf(Literal lit) {
        if (lit instanceof PixelLiteral) return Kind.PIXEL;
        if (lit instanceof PercentageLiteral) return Kind.PERCENTAGE;
        if (lit instanceof ScalarLiteral) return Kind.SCALAR;
        if (lit instanceof ColorLiteral) return Kind.COLOR;
        if (lit instanceof BoolLiteral) return Kind.BOOL;
        return Kind.UNKNOWN;
    }

    /**
     * CH03: geen kleuren of booleans in operaties (recursief).
     * CH02: +/−: beide operanden zelfde numerieke soort (px/px, %/% of scalar/scalar);
     *      × : minstens één operand moet scalar zijn.
     * Retourneert een representatieve Literal voor het resultaat, of null als onbekend.
     */
    private Literal checkOperation(Operation operation) {
        Expression left = unwrap(operation.lhs);
        Expression right = unwrap(operation.rhs);

        if (containsType(left, BoolLiteral.class) || containsType(right, BoolLiteral.class)) {
            operation.setError("operation cant contain boolean");
            return null;
        }
        if (containsType(left, ColorLiteral.class) || containsType(right, ColorLiteral.class)) {
            operation.setError("operation cant contain color");
            return null;
        }

        if (left instanceof Operation lop) left = checkOperation(lop);
        if (right instanceof Operation rop) right = checkOperation(rop);

        Literal lLit = resolveToLiteral(left);
        Literal rLit = resolveToLiteral(right);
        Kind lk = kindOf(lLit);
        Kind rk = kindOf(rLit);


        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            boolean sameNumeric = (lk == rk) && (lk == Kind.PIXEL || lk == Kind.PERCENTAGE || lk == Kind.SCALAR);
            if (!sameNumeric) {
                operation.setError("plus/min operands must have same unit or both scalar");
            }
            return lLit != null ? lLit : rLit;

        } else if (operation instanceof MultiplyOperation) {
            boolean oneScalar = (lk == Kind.SCALAR) || (rk == Kind.SCALAR);
            if (!oneScalar) {
                operation.setError("multiply requires at least one scalar operand");
            }
            if (lk == Kind.SCALAR && (rk == Kind.PIXEL || rk == Kind.PERCENTAGE)) return rLit;
            if (rk == Kind.SCALAR && (lk == Kind.PIXEL || lk == Kind.PERCENTAGE)) return lLit;

            return lLit != null ? lLit : rLit;
        }


        if (lLit instanceof PixelLiteral || lLit instanceof PercentageLiteral || lLit instanceof ScalarLiteral) {
            return lLit;
        }
        return rLit;
    }



    private Expression unwrap(Expression expr) {
        if (expr instanceof VariableReference ref) {
            Expression resolved = resolveVariable(ref);
            return resolved != null ? resolved : expr;
        }
        return expr;
    }

    private boolean containsType(Expression expr, Class<? extends Literal> literalClass) {
        if (expr == null) return false;
        if (literalClass.isInstance(expr)) return true;
        if (expr instanceof VariableReference ref) return containsType(resolveVariable(ref), literalClass);
        if (expr instanceof Operation op) return containsType(op.lhs, literalClass) || containsType(op.rhs, literalClass);
        return false;
    }

    // ------------------------------------------------------------
    // CH05 - If-clause is boolean
    // ------------------------------------------------------------

    private void checkIfClause(IfClause clause) {
        Literal lit = resolveToLiteral(clause.conditionalExpression);
        if (!(lit instanceof BoolLiteral)) {
            clause.setError("conditional expression is not boolean");
        }
    }

    // ------------------------------------------------------------
    // Scope helpers (HANLinkedList)
    // ------------------------------------------------------------

    private void pushScope() {
        variableTypes.addFirst(new HashMap<>());
    }

    private void popScope() {
        if (variableTypes.getSize() > 0) {
            variableTypes.removeFirst();
        }
    }

    private Map<String, Expression> currentScope() {
        if (variableTypes.getSize() == 0) {
            variableTypes.addFirst(new HashMap<>());
        }
        return variableTypes.getFirst();
    }
}
