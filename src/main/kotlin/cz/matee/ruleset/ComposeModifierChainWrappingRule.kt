package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal class ComposeModifierChainWrappingRule : LayoutRule("compose-modifier-chain-wrapping") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val expression = node.psi as? KtQualifiedExpression ?: return
        if (!expression.isInComposeFunction()) {
            return
        }
        val chain = expression.callChain() ?: return
        if (!chain.isModifier || chain.calls.isEmpty()) {
            return
        }
        wrapChainCalls(
            node,
            chain.calls,
            emit,
        )
    }
}
