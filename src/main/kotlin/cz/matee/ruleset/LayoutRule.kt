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
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtAnnotatedExpression
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

internal abstract class LayoutRule(id: String) :
    Rule(
        ruleId = RuleId("mate:$id"),
        about = About(maintainer = "MateeDevs"),
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

    protected fun fitsOnLine(
        node: ASTNode,
        text: String,
    ): Boolean =
        maxLineLength < 0 || node.psi.containingFile.text
            .substring(
                0,
                node.startOffset,
            )
            .substringAfterLast('\n')
            .length + text.length <= maxLineLength

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

internal fun ASTNode.lineIndent(): String =
    psi.containingFile.text
        .substring(
            0,
            startOffset,
        )
        .substringAfterLast('\n')
        .takeWhile { it == ' ' || it == '\t' }

internal fun PsiElement.hasComments(): Boolean =
    PsiTreeUtil.findChildOfType(
        this,
        PsiComment::class.java,
    ) != null

// Stop at ordinary function boundaries: a file or enclosing composable does not make every helper UI.
internal fun PsiElement.isInComposeFunction(): Boolean {
    generateSequence(parent) { it.parent }.forEach { owner ->
        when (owner) {
            is KtNamedFunction -> return owner.annotationEntries.any { it.isComposeAnnotation() }
            is KtAnnotatedExpression ->
                if (owner.annotationEntries.any { it.isComposeAnnotation() }) {
                    return true
                }
            is KtProperty ->
                if (owner.typeReference?.annotationEntries.orEmpty().any { it.isComposeAnnotation() }) {
                    return true
                }
        }
    }
    return false
}

private fun KtAnnotationEntry.isComposeAnnotation(): Boolean {
    val name = shortName?.asString() ?: return false
    return name == "Composable" || containingKtFile.importDirectives.any {
        it.aliasName == name && it.importedFqName?.asString() == "androidx.compose.runtime.Composable"
    }
}
