package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Refactored, clean and idiomatic CSS generator.
 * GE01 – Zet AST om naar CSS2-compatibele string.
 * GE02 – Twee spaties inspringing per scopeniveau.
 */
public class Generator {

	private final StringBuilder out = new StringBuilder(256);

	public Generator() { }

	public String generate(AST ast) {
		Objects.requireNonNull(ast, "AST must not be null");
		Objects.requireNonNull(ast.root, "AST root must not be null");

		visit(ast.root, 0);

		if (out.length() == 0 || out.charAt(out.length() - 1) != '\n') out.append('\n');
		return out.toString();
	}

	private void visit(ASTNode node, int level) {
		if (node == null) return;

		if (node instanceof Stylerule) {
			writeStyleRule((Stylerule) node, level);
			return;
		}

		var children = node.getChildren();
		if (children == null || children.isEmpty()) return;
		for (ASTNode child : children) {
			visit(child, level);
		}
	}

	private void writeStyleRule(Stylerule rule, int level) {
		String selectorList = rule.selectors.stream()
				.map(Object::toString)
				.collect(Collectors.joining(", "));

		indent(level).append(selectorList).append(" {\n");

		for (ASTNode child : rule.getChildren()) {
			if (child instanceof Declaration) {
				writeDeclaration((Declaration) child, level + 1);
			}
		}

		indent(level).append("}\n");
	}

	private void writeDeclaration(Declaration decl, int level) {
		indent(level)
				.append(decl.property.name)
				.append(": ")
				.append(expressionToString(decl.expression))
				.append(";\n");
	}

	private String expressionToString(Expression expression) {
		if (expression == null) return "";
		if (expression instanceof PercentageLiteral) return ((PercentageLiteral) expression).value + "%";
		if (expression instanceof PixelLiteral) return ((PixelLiteral) expression).value + "px";
		if (expression instanceof ColorLiteral) return ((ColorLiteral) expression).value;


		return String.valueOf(expression);
	}

	private StringBuilder indent(int level) {
		// Twee spaties per niveau (GE02)
		return out.append("  ".repeat(Math.max(0, level)));
	}
}
