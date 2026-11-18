package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtValueArgumentList

internal class MultilineCallArgumentsRule : LayoutRule("multiline-call-arguments") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val arguments = node.psi as? KtValueArgumentList ?: return
        if (arguments.arguments.size >= 2 && arguments.arguments.any { it.isNamed() }) {
            wrapArguments(
                arguments,
                emit,
            )
        }
    }
}
