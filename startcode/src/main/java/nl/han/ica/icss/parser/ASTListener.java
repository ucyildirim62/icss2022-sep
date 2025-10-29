package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import nl.han.ica.datastructures.HANStack;

/**
 * Extracts the ICSS Abstract Syntax Tree from the Antlr parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	private final AST ast;
	private final IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

	public AST getAST() {return ast;}

	@Override public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		ast.setRoot(stylesheet);
		currentContainer.push(stylesheet);
	}

	@Override public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterSelector(ICSSParser.SelectorContext ctx) {
		Selector selector;
		String selectorText = ctx.getText();

		if(ctx.idSelector() != null){
			selector = new IdSelector(selectorText);
		} else if(ctx.classSelector() != null) {
			selector = new ClassSelector(selectorText);
		} else {
			selector = new TagSelector(selectorText);
		}
		currentContainer.peek().addChild(selector);
	}

	@Override public void enterLiteral(ICSSParser.LiteralContext ctx) {
		if (ctx.PIXELSIZE() != null) {
			currentContainer.peek().addChild(new PixelLiteral(ctx.getText()));
			return;
		}
		if (ctx.PERCENTAGE() != null) {
			currentContainer.peek().addChild(new PercentageLiteral(ctx.getText()));
			return;
		}
		if (ctx.COLOR() != null) {
			currentContainer.peek().addChild(new ColorLiteral(ctx.getText()));
			return;
		}
		if (ctx.SCALAR() != null) {
			currentContainer.peek().addChild(new ScalarLiteral(ctx.getText()));
			return;
		}
		if (ctx.boolLiteral() != null) {
			currentContainer.peek().addChild(new BoolLiteral(ctx.getText()));
        }
	}

	@Override public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		VariableReference variableReference = new VariableReference(ctx.getText());
		currentContainer.peek().addChild(variableReference);
	}


	@Override public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();
		currentContainer.peek().addChild(variableAssignment);
		currentContainer.push(variableAssignment);
	}

	@Override public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		if(ctx.ifClause() != null || ctx.variableAssignment() != null){
			return;
		}
		Declaration declaration = new Declaration(ctx.property().getText());
		currentContainer.peek().addChild(declaration);
		currentContainer.push(declaration);
	}

	@Override public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		if(ctx.ifClause() != null || ctx.variableAssignment() != null){
			return;
		}
		currentContainer.pop();
	}

	@Override public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		IfClause ifClause = new IfClause();
		currentContainer.peek().addChild(ifClause);
		currentContainer.push(ifClause);
	}

	@Override public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		ElseClause elseClause = new ElseClause();
		currentContainer.peek().addChild(elseClause);
		currentContainer.push(elseClause);
	}

	@Override public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
		Stylerule stylerule = new Stylerule();
		currentContainer.peek().addChild(stylerule);
		currentContainer.push(stylerule);
	}

	@Override public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterAddOperation(ICSSParser.AddOperationContext ctx) {
		AddOperation addOperation = new AddOperation();
		currentContainer.peek().addChild(addOperation);
		currentContainer.push(addOperation);

	}

	@Override public void exitAddOperation(ICSSParser.AddOperationContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterMulOperation(ICSSParser.MulOperationContext ctx) {
		MultiplyOperation multiplyOperation = new MultiplyOperation();
		currentContainer.peek().addChild(multiplyOperation);
		currentContainer.push(multiplyOperation);
	}

	@Override public void exitMulOperation(ICSSParser.MulOperationContext ctx) {
		currentContainer.pop();
	}

	@Override public void enterMinOperation(ICSSParser.MinOperationContext ctx) {
		SubtractOperation subtractOperation = new SubtractOperation();
		currentContainer.peek().addChild(subtractOperation);
		currentContainer.push(subtractOperation);
	}

	@Override public void exitMinOperation(ICSSParser.MinOperationContext ctx) {
		currentContainer.pop();
	}

}
