package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList

internal class ComposeSingleArgumentCallRule : LayoutRule("compose-single-argument-call") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val arguments = node.psi as? KtValueArgumentList ?: return
        val call = arguments.parent as? KtCallExpression ?: return
        val block = call.parent as? KtBlockExpression ?: return
        if (arguments.arguments.size == 1 && call.isUiCall() && call.isInComposeFunction() && '\n' in block.text) {
            wrapArguments(
                arguments,
                emit,
            )
        }
    }
}
