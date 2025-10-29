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

		// Bezoek de boom en render alle stylerules (waar ze ook staan)
		visit(ast.root, 0);

		// Zorg voor exact één trailing newline
		if (out.isEmpty() || out.charAt(out.length() - 1) != '\n') out.append('\n');
		return out.toString();
	}

	private void visit(ASTNode node, int level) {
		if (node == null) return;

		if (node instanceof Stylerule rule) {
			writeStyleRule(rule, level);
			return; // stylerule schrijft zelf zijn children (declarations)
		}

		// Standaard: doorloop kinderen om eventuele stylerules te vinden
		var children = node.getChildren();
		if (children == null || children.isEmpty()) return;
		for (ASTNode child : children) {
			visit(child, level);
		}
	}

	private void writeStyleRule(Stylerule rule, int level) {
		// Selectors: "a, b, .class"
		String selectorList = rule.selectors.stream()
				.map(Object::toString)
				.collect(Collectors.joining(", "));

		indent(level).append(selectorList).append(" {\n");

		// Declarations (twee spaties per niveau: GE02)
		for (ASTNode child : rule.getChildren()) {
			if (child instanceof Declaration decl) {
				writeDeclaration(decl, level + 1);
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
		if (expression instanceof PercentageLiteral p) return p.value + "%";
		if (expression instanceof PixelLiteral px) return px.value + "px";
		if (expression instanceof ColorLiteral c) return c.value; // waarde bevat al #RRGGBB

		// Fallback: laat het model zelf renderen indien mogelijk
		return String.valueOf(expression);
	}

	private StringBuilder indent(int level) {
		// Twee spaties per niveau (GE02)
		return out.append("  ".repeat(Math.max(0, level)));
	}
}
