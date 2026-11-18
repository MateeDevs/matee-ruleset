package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtWhenEntry

internal class CallLayoutRule : LayoutRule("call-layout") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val arguments = node.psi as? KtValueArgumentList ?: return
        val call = arguments.parent as? KtCallExpression
        val block = call?.parent as? KtBlockExpression
        val standaloneUi = call?.isUiCall() == true && call.isInComposeFunction() && block?.text?.contains('\n') == true
        if (arguments.arguments.isEmpty() || arguments.arguments.size < 2 && !standaloneUi) {
            return
        }

        val indentation = node.lineIndent()
        arguments.arguments.forEach { argument ->
            newlineBefore(
                argument.node,
                "$indentation${" ".repeat(indentSize)}",
                "Put each argument of a multi-argument call or standalone UI element on a separate line",
                emit,
            )
        }
        arguments.rightParenthesis?.let { closing ->
            newlineBefore(
                closing.node,
                indentation,
                "Put the closing parenthesis on a separate line",
                emit,
            )
        }
    }

    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        when (val element = node.psi) {
            is KtValueArgumentList -> compactSingleArgument(
                element,
                emit,
            )
            is KtValueArgument -> compactArgumentValue(
                element,
                emit,
            )
            is KtBinaryExpression -> compactComparison(
                element,
                emit,
            )
            is KtIfExpression -> compactCondition(
                element,
                emit,
            )
            is KtWhenEntry -> wrapWhenBranch(
                element,
                emit,
            )
            is KtProperty -> compactInitializerReceiver(
                element,
                emit,
            )
        }
    }

    private fun compactInitializerReceiver(
        property: KtProperty,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val initializer = property.initializer ?: return
        if (initializer !is KtCallExpression && initializer !is KtBinaryExpression) {
            return
        }
        val gap = initializer.node.treePrev ?: return
        val equals = gap.treePrev ?: return
        if (gap.psi is PsiWhiteSpace && '\n' in gap.text && equals.text == "=" &&
            fitsOnLine(
                equals,
                "= ${initializer.text.lineSequence().first()}",
            )
        ) {
            whitespaceBefore(
                initializer.node,
                " ",
                "Keep the initializer receiver beside '=' when it fits",
                emit,
            )
        }
    }

    private fun compactSingleArgument(
        arguments: KtValueArgumentList,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
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

    private fun compactArgumentValue(
        argument: KtValueArgument,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
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

    private fun compactComparison(
        expression: KtBinaryExpression,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
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

    private fun compactCondition(
        expression: KtIfExpression,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
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

    private fun wrapWhenBranch(
        entry: KtWhenEntry,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
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
