package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceBeforeMe
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList

internal abstract class LayoutRule(
    id: String,
    runAfter: Set<String> = emptySet(),
    visitorModifiers: Set<Rule.VisitorModifier> = emptySet(),
) :
    Rule(
        ruleId = RuleId("matee:$id"),
        about = About(
            maintainer = "MateeDevs",
            repositoryUrl = "https://github.com/MateeDevs/matee-ruleset",
        ),
        visitorModifiers = visitorModifiers + runAfter.map {
            VisitorModifier.RunAfterRule(
                RuleId("matee:$it"),
                VisitorModifier.RunAfterRule.Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED,
            )
        }.toSet(),
        usesEditorConfigProperties = setOf(
            INDENT_SIZE_PROPERTY,
            MAX_LINE_LENGTH_PROPERTY,
        ),
    ),
    RuleAutocorrectApproveHandler {
    protected var indentSize = 4
    private var maxLineLength = 140

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentSize = editorConfig[INDENT_SIZE_PROPERTY]
        maxLineLength = editorConfig[MAX_LINE_LENGTH_PROPERTY]
    }

    protected fun wrapArguments(
        arguments: KtValueArgumentList,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val indentation = arguments.node.lineIndent()
        arguments.arguments.forEach { argument ->
            newlineBefore(
                argument.node,
                "$indentation${" ".repeat(indentSize)}",
                "Put each argument on a separate line",
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

    protected fun wrapChainCalls(
        node: ASTNode,
        calls: List<KtQualifiedExpression>,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val indentation = node.lineIndent() + " ".repeat(indentSize)
        calls.forEach { call ->
            newlineBefore(
                call.operationTokenNode,
                indentation,
                "Put each Compose chained call below its receiver",
                emit,
            )
        }
    }

    protected fun fitsOnLine(
        node: ASTNode,
        text: String,
        replacedEndOffset: Int = node.startOffset + node.textLength,
    ): Boolean {
        if (maxLineLength < 0) {
            return true
        }
        val source = node.psi.containingFile.text
        val prefix = source.substring(0, node.startOffset).substringAfterLast('\n')
        val remainder = source.substring(replacedEndOffset)
        val lineSuffix = remainder.substringBefore('\n')
        // Standard wrapping can pull a closing delimiter up once its expression becomes single-line.
        val following = remainder.trimStart()
        val suffix = if (lineSuffix.isBlank() && following.firstOrNull() in setOf(')', ']')) {
            following.substringBefore('\n')
        } else {
            lineSuffix
        }
        return prefix.length + text.length + suffix.length <= maxLineLength
    }

    protected fun newlineBefore(
        node: ASTNode,
        indentation: String,
        message: String,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        // The standard indent rule owns continuation indentation, including calls inside conditions.
        val previous = node.prevLeaf
        if (previous?.psi is PsiWhiteSpace && '\n' in previous.text) {
            return
        }
        whitespaceBefore(
            node,
            "\n$indentation",
            message,
            emit,
        )
    }

    protected fun whitespaceBefore(
        node: ASTNode,
        whitespace: String,
        message: String,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val previous = node.prevLeaf
        val existing = previous?.takeIf { it.psi is PsiWhiteSpace }?.text.orEmpty()
        if (existing != whitespace && emit(
                node.startOffset,
                message,
                true,
            ) == AutocorrectDecision.ALLOW_AUTOCORRECT
        ) {
            node.upsertWhitespaceBeforeMe(whitespace)
        }
    }
}
