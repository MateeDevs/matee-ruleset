package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression

internal class ComparisonWrappingRule : LayoutRule("comparison-wrapping") {
    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val expression = node.psi as? KtBinaryExpression ?: return
        if (expression.operationReference.text !in setOf(
                "==",
                "!=",
                "===",
                "!==",
                "<",
                ">",
                "<=",
                ">=",
            )
        ) {
            return
        }
        val left = expression.left ?: return
        val right = expression.right ?: return
        val compact = "${left.text} ${expression.operationReference.text} ${right.text}"
        if ('\n' in left.text || '\n' in right.text || expression.hasComments() || !fitsOnLine(
                expression.node,
                compact,
            )
        ) {
            return
        }
        whitespaceBefore(
            expression.operationReference.node,
            " ",
            "Keep a short comparison on one line",
            emit,
        )
        whitespaceBefore(
            right.node,
            " ",
            "Keep a short comparison on one line",
            emit,
        )
    }
}
