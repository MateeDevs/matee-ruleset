package cz.matee.ruleset

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression

internal data class CallChain(
    val receiver: KtExpression,
    val calls: List<KtQualifiedExpression>,
) {
    val isModifier: Boolean
        get() = receiver.text == "Modifier" || receiver.text == "modifier"

    val callCount: Int
        get() = if (receiver is KtCallExpression) {
            calls.size + 1
        } else {
            calls.size
        }
}

internal fun KtQualifiedExpression.callChain(): CallChain? {
    if (parent is KtQualifiedExpression && (parent as KtQualifiedExpression).receiverExpression == this) {
        return null
    }
    val expressions = generateSequence(this) { it.receiverExpression as? KtQualifiedExpression }.toList().asReversed()
    return CallChain(
        receiver = expressions.first().receiverExpression,
        calls = expressions.filter { it.selectorExpression is KtCallExpression },
    )
}
