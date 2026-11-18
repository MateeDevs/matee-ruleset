package cz.matee.ruleset

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtAnnotatedExpression
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

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

internal fun KtExpression.isUiCall(): Boolean =
    this is KtCallExpression &&
        calleeExpression?.text?.firstOrNull()?.isUpperCase() == true
