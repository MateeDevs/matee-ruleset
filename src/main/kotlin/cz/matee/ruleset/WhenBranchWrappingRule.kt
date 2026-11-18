package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtWhenEntry

internal class WhenBranchWrappingRule : LayoutRule("when-branch-wrapping") {
    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val entry = node.psi as? KtWhenEntry ?: return
        val expression = entry.expression as? KtIfExpression ?: return
        if ('\n' in expression.text) {
            newlineBefore(
                expression.node,
                "${entry.node.lineIndent()}${" ".repeat(indentSize)}",
                "Start a multiline if branch below the when arrow",
                emit,
            )
        }
    }
}
