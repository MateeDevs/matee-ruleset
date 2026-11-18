package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal class ComposeLayoutRule : LayoutRule("compose-layout") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        if (!node.psi.isInComposeFunction()) {
            return
        }
        when (val expression = node.psi) {
            is KtQualifiedExpression -> wrapChain(
                expression,
                emit,
            )
            is KtCallExpression -> checkModifierOrder(
                expression,
                emit,
            )
        }
    }

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

    private fun wrapChain(
        expression: KtQualifiedExpression,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        if (expression.parent is KtQualifiedExpression && (expression.parent as KtQualifiedExpression).receiverExpression == expression) {
            return
        }
        val chain = generateSequence(expression) { it.receiverExpression as? KtQualifiedExpression }.toList().asReversed()
        val receiver = chain.first().receiverExpression
        val calls = chain.filter { it.selectorExpression is KtCallExpression }
        val modifier = receiver.text in setOf(
            "Modifier",
            "modifier",
        )
        if (!modifier && calls.size + (if (receiver is KtCallExpression) 1 else 0) < 2) {
            return
        }
        val indentation = expression.node.lineIndent() + " ".repeat(indentSize)
        calls.forEach { call ->
            newlineBefore(
                call.operationTokenNode,
                indentation,
                "Put each Compose chained call below its receiver",
                emit,
            )
        }
    }

    private fun checkModifierOrder(
        call: KtCallExpression,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
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

internal fun KtExpression.isUiCall(): Boolean =
    this is KtCallExpression &&
        calleeExpression?.text?.firstOrNull()?.isUpperCase() == true
