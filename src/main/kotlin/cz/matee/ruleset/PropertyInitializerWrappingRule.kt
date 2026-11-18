package cz.matee.ruleset

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtProperty

internal class PropertyInitializerWrappingRule : LayoutRule("property-initializer-wrapping") {
    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (Int, String, Boolean) -> AutocorrectDecision,
    ) {
        val property = node.psi as? KtProperty ?: return
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
}
