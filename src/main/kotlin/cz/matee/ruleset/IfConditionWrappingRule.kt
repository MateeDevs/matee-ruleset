package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtIfExpression

internal class IfConditionWrappingRule : LayoutRule(
    id = "if-condition-wrapping",
    runAfter = setOf("comparison-wrapping"),
) {
    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val expression = node.psi as? KtIfExpression ?: return
        val condition = expression.condition ?: return
        if (expression.hasComments() || '\n' in condition.text || !fitsOnLine(
                expression.node,
                "if (${condition.text})",
            )
        ) {
            return
        }
        if (condition.node.treePrev?.psi is PsiWhiteSpace) {
            whitespaceBefore(
                condition.node,
                "",
                "Keep a short if condition beside its parentheses",
                emit,
            )
        }
        expression.rightParenthesis?.let { closing ->
            if (closing.node.treePrev?.psi is PsiWhiteSpace) {
                whitespaceBefore(
                    closing.node,
                    "",
                    "Keep a short if condition beside its parentheses",
                    emit,
                )
            }
        }
    }
}
