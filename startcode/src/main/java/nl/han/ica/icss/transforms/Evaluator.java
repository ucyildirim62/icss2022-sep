package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Expression>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();

        transformSheet(ast.root);
    }

    /**
     * transformSheet(Stylesheet sheet): Deze methode doorloopt de stylesheet en voert transformaties uit op variabele toewijzingen en style rules.
     * Het initialiseert ook de variabele variableValues en houdt bij welke knopen verwijderd moeten worden.
     **/
    private void transformSheet(Stylesheet sheet) {
        variableValues.addFirst(new HashMap<>());
        ArrayList<ASTNode> toRemove = new ArrayList<>();
        for (int i = 0; i < sheet.getChildren().size(); i++) {
            if (sheet.getChildren().get(i) instanceof VariableAssignment) {
                addVar((VariableAssignment) sheet.getChildren().get(i));
                toRemove.add(sheet.getChildren().get(i));
            }

            if (sheet.getChildren().get(i) instanceof Stylerule) {
                transformStyleRule((Stylerule) sheet.getChildren().get(i));
            }
        }

        for (ASTNode astNode : toRemove) {
            sheet.removeChild(astNode);
        }

        variableValues.removeFirst();
    }

    /**
     addVar(VariableAssignment var): Deze methode voegt een variabele toe aan de lijst van variabele waarden en controleert of deze al bestaat in de huidige scope.
     */

    private void addVar(VariableAssignment var) {

        boolean exists = false;

        if (variableValues.get(0).get(var.name.name) != null) {
            variableValues.get(0).replace(var.name.name, var.expression);
            exists = true;
        }

        if (!exists) {
            variableValues.getFirst().put(var.name.name, var.expression);
        }
    }

    /**
     transformStyleRule(Stylerule rule): Deze methode transformeert een stylerule door de variabelen toe te voegen aan de lijst met variabele waarden en de
     declaraties in de body te transformeren.
     */
    private void transformStyleRule(Stylerule rule) {
        ArrayList<ASTNode> transformedNodes = new ArrayList<>();
        variableValues.addFirst(new HashMap<>());

        for (int i = 0; i < rule.getChildren().size(); i++) {
            if (rule.getChildren().get(i) instanceof Declaration) {
                transformedNodes.add(transformDeclaration((Declaration) rule.getChildren().get(i)));
            }
            if (rule.getChildren().get(i) instanceof IfClause) {
                transformIfClause((IfClause) rule.getChildren().get(i));
                ArrayList<ASTNode> ifBody = transformIfClause((IfClause) rule.getChildren().get(i));
                transformedNodes.addAll(ifBody);
            }
            if (rule.getChildren().get(i) instanceof VariableAssignment) {
                addVar((VariableAssignment) rule.getChildren().get(i));
                //toRemove.add(rule.getChildren().get(i));
            }
        }

        rule.body = transformedNodes;
        variableValues.removeFirst();
    }

    /**
     transformDeclaration(Declaration declaration): Deze methode transformeert een declaratie door de expressie in de declaratie te evalueren.
     */
    private Declaration transformDeclaration(Declaration declaration) {
        declaration.expression = transformExpression(declaration.expression);

        return declaration;
    }

    /**
     * TR01 - Evalueer expressies. Schrijf een transformatie in Evaluator die alle Expression knopen in de AST
     * door een Literal knoop met de berekende waarde vervangt:
     * <p>
     * Deze methode evalueert een expressie en vervangt deze door een Literal knoop met de berekende waarde.
     * Hiermee wordt voldaan aan eis TR01.
     */
    private Literal transformExpression(Expression expression) {
        if (expression instanceof Operation) {
            return transformOperation((Operation) expression);
        }

        if (expression instanceof VariableReference) {
            return transformVarReference((VariableReference) expression);
        }

        if (expression instanceof Literal) {
            return (Literal) expression;
        }

        return null;
    }

    /**
     transformOperation(Operation operation): Deze methode evalueert een operatie en retourneert een Literal knoop met het resultaat van de operatie.
     */
    private Literal transformOperation(Operation operation) {
        Expression left = operation.lhs;
        Expression right = operation.rhs;

        int leftValue = 0;
        int rightValue = 0;

        if (left instanceof Operation) {
            left = transformOperation((Operation) left);
        } else if (left instanceof VariableReference) {
            left = transformVarReference((VariableReference) left);
        }

        if (left instanceof PixelLiteral) {
            leftValue = ((PixelLiteral) left).value;
        } else if (left instanceof PercentageLiteral) {
            leftValue = ((PercentageLiteral) left).value;
        } else if (left instanceof ScalarLiteral) {
            leftValue = ((ScalarLiteral) left).value;
        }

        if (right instanceof Operation) {
            right = transformOperation((Operation) right);
        } else if (right instanceof VariableReference) {
            right = transformVarReference((VariableReference) right);
        }

        if (right instanceof PixelLiteral) {
            rightValue = ((PixelLiteral) right).value;
        } else if (right instanceof PercentageLiteral) {
            rightValue = ((PercentageLiteral) right).value;
        } else if (right instanceof ScalarLiteral) {
            rightValue = ((ScalarLiteral) right).value;
        }

        if (operation instanceof AddOperation) {
            return operation((Literal) left, leftValue + rightValue);
        } else if (operation instanceof MultiplyOperation) {
            return operation((Literal) left, leftValue * rightValue);
        } else if (operation instanceof SubtractOperation) {
            return operation((Literal) left, leftValue - rightValue);
        } else {
            return null;
        }
    }

    /**
     De methode operation maakt een nieuwe Literal-knoop met een gegeven waarde,
     en zorgt ervoor dat het juiste type wordt toegekend aan de nieuwe Literal op basis van het oorspronkelijke type van de Literal en de gegeven waarde.
     Het resultaat is een nieuwe Literal met dezelfde waarde maar het juiste type.
     */
    private Literal operation(Literal literal, int value) {
        if (literal instanceof PercentageLiteral) {
            return new PixelLiteral(value);
        } else if (literal instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (literal instanceof ScalarLiteral) {
            return new ScalarLiteral(value);
        } else {
            return null;
        }
    }

    /**
     transformVarReference(VariableReference reference): Deze methode zoekt naar de waarde van een variabele in de lijst van variabele waarden en evalueert deze.
     */
    private Literal transformVarReference(VariableReference reference) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            if (variableValues.get(i).get(reference.name) != null) {
                return transformExpression(variableValues.get(i).get(reference.name));
            }
        }

        return null;
    }

    /**
     * TR02 - Evalueer if/else expressies. Schrijf een transformatie in Evaluator die alle IfClauses uit de AST
     * verwijdert. Wanneer de conditie van de IfClause TRUE is wordt deze vervangen door de body van het if-statement.
     * Als de conditie FALSE is dan vervang je de IfClause door de body van de ElseClause. Als er geen ElseClause is bij een
     * negatieve conditie dan verwijder je de IfClause volledig uit de AST:
     * <p>
     * Door IfClauses op deze manier te evalueren en te transformeren, wordt voldaan aan eis TR02, waarbij de AST wordt
     * aangepast op basis van de condities van de IfClauses.
     */
    private ArrayList<ASTNode> transformIfClause(IfClause clause) {
        BoolLiteral conditional = (BoolLiteral) transformExpression(clause.conditionalExpression);
        ArrayList<ASTNode> bodyNodes = new ArrayList<>();

        if (conditional.value) {
            clause.elseClause = null;
        } else if (clause.elseClause != null) {
            clause.body = clause.elseClause.body;
        } else {
            return new ArrayList<>();
        }

        for (int i = 0; i < clause.body.size(); i++) {
            if (clause.body.get(i) instanceof Declaration) {
                bodyNodes.add(transformDeclaration((Declaration) clause.body.get(i)));
            }
            if (clause.body.get(i) instanceof IfClause) {
                ArrayList<ASTNode> ifBody = transformIfClause((IfClause) clause.body.get(i));
                bodyNodes.addAll(ifBody);
            }
            if (clause.body.get(i) instanceof VariableAssignment) {
                addVar((VariableAssignment) clause.body.get(i));
            }
        }

        return bodyNodes;
    }
}