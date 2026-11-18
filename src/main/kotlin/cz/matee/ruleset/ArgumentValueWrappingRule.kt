package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtValueArgument

internal class ArgumentValueWrappingRule : LayoutRule(
    id = "argument-value-wrapping",
    runAfter = setOf("single-argument-call"),
) {
    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val argument = node.psi as? KtValueArgument ?: return
        val expression = argument.getArgumentExpression() ?: return
        if (!argument.isNamed() || '\n' in expression.text || argument.hasComments() || !fitsOnLine(
                argument.node,
                "${argument.getArgumentName()?.asName?.asString()} = ${expression.text}",
            )
        ) {
            return
        }
        whitespaceBefore(
            expression.node,
            " ",
            "Keep a short argument value beside '='",
            emit,
        )
    }
}
