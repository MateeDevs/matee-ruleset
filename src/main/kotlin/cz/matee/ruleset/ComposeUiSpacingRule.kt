package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAsLateAsPossible
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtProperty

internal class ComposeUiSpacingRule : LayoutRule(
    id = "compose-ui-spacing",
    visitorModifiers = setOf(RunAsLateAsPossible),
) {
    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val block = node.psi as? KtBlockExpression ?: return
        if (!block.isInComposeFunction()) {
            return
        }
        block.statements.zipWithNext().forEach { (previous, current) ->
            val uiPair = previous.isUiCall() && current.isUiCall()
            val stateAndUi = previous is KtProperty && current.isUiCall() || previous.isUiCall() && current is KtProperty
            val gap = generateSequence(previous.nextSibling) { it.nextSibling }
                .takeWhile { it != current }
                .toList()
            if ((uiPair || stateAndUi) && ('\n' in previous.text || '\n' in current.text) &&
                gap.all { it is PsiWhiteSpace }
            ) {
                whitespaceBefore(
                    current.node,
                    "\n\n${current.node.lineIndent()}",
                    "Separate multiline UI elements and state/rendering steps with a blank line",
                    emit,
                )
            }
        }
    }
}
