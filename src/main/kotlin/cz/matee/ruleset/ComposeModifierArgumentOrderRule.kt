package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression

internal class ComposeModifierArgumentOrderRule : LayoutRule("compose-modifier-argument-order") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val call = node.psi as? KtCallExpression ?: return
        if (!call.isInComposeFunction()) {
            return
        }
        if (!call.isUiCall()) {
            return
        }
        val arguments = call.valueArgumentList?.arguments ?: return
        val index = arguments.indexOfFirst { it.getArgumentName()?.asName?.asString() == "modifier" }
        if (index >= 0 && index != arguments.lastIndex) {
            // Reordering arguments can change evaluation order, so this check deliberately has no autofix.
            emit(
                arguments[index].asElement().textOffset,
                "Put modifier last inside the Compose call parentheses",
                false,
            )
        }
    }
}
