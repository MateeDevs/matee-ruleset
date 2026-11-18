package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList

internal class SingleArgumentCallRule : LayoutRule("single-argument-call") {
    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val arguments = node.psi as? KtValueArgumentList ?: return
        val argument = arguments.arguments.singleOrNull() ?: return
        val call = arguments.parent as? KtCallExpression
        if (call?.parent is KtBlockExpression && call.isInComposeFunction()) {
            return
        }
        if ('\n' in argument.text || arguments.hasComments() || !fitsOnLine(
                arguments.node,
                "(${argument.text})",
            )
        ) {
            return
        }
        whitespaceBefore(
            argument.node,
            "",
            "Keep a simple single argument beside its opening parenthesis",
            emit,
        )
        // A trailing comma keeps this deliberately multiline list multiline in the standard rules.
        val comma = arguments.node.getChildren(null).firstOrNull { it.text == "," }
        if (comma != null && emit(
                comma.startOffset,
                "Remove the trailing comma from a compact single-argument call",
                true,
            ) ==
            AutocorrectDecision.ALLOW_AUTOCORRECT
        ) {
            arguments.node.removeChild(comma)
        }
        arguments.rightParenthesis?.let { closing ->
            whitespaceBefore(
                closing.node,
                "",
                "Keep a simple single argument beside its closing parenthesis",
                emit,
            )
        }
    }
}
