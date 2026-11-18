package cz.matee.ruleset

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil

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
