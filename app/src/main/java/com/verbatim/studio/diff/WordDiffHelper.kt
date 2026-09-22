package com.verbatim.studio.diff

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.verbatim.studio.theme.*
import kotlin.math.max

object WordDiffHelper {

    private sealed class DiffPart(val text: String) {
        class Equal(text: String) : DiffPart(text)
        class Insert(text: String) : DiffPart(text)
        class Delete(text: String) : DiffPart(text)
    }

    fun buildDiffAnnotatedString(
        oldStr: String,
        newStr: String,
        isDark: Boolean
    ): AnnotatedString {
        if (oldStr.isEmpty()) {
            return AnnotatedString(newStr)
        }
        if (newStr.isEmpty()) {
            return AnnotatedString("")
        }

        val oldTokens = splitIntoTokens(oldStr)
        val newTokens = splitIntoTokens(newStr)

        val m = oldTokens.size
        val n = newTokens.size
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 1..m) {
            for (j in 1..n) {
                if (oldTokens[i - 1] == newTokens[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        var i = m
        var j = n
        val diffList = mutableListOf<DiffPart>()

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && oldTokens[i - 1] == newTokens[j - 1]) {
                diffList.add(0, DiffPart.Equal(oldTokens[i - 1]))
                i--
                j--
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                diffList.add(0, DiffPart.Insert(newTokens[j - 1]))
                j--
            } else {
                diffList.add(0, DiffPart.Delete(oldTokens[i - 1]))
                i--
            }
        }

        val addedBg = if (isDark) DiffAddedBgDark else DiffAddedBgLight
        val addedText = if (isDark) DiffAddedTextDark else DiffAddedTextLight
        val removedBg = if (isDark) DiffRemovedBgDark else DiffRemovedBgLight
        val removedText = if (isDark) DiffRemovedTextDark else DiffRemovedTextLight

        return buildAnnotatedString {
            for (part in diffList) {
                when (part) {
                    is DiffPart.Equal -> {
                        append(part.text)
                    }
                    is DiffPart.Insert -> {
                        withStyle(
                            SpanStyle(
                                background = addedBg,
                                color = addedText
                            )
                        ) {
                            append(part.text)
                        }
                    }
                    is DiffPart.Delete -> {
                        withStyle(
                            SpanStyle(
                                background = removedBg,
                                color = removedText,
                                textDecoration = TextDecoration.LineThrough
                            )
                        ) {
                            append(part.text)
                        }
                    }
                }
            }
        }
    }

    private fun splitIntoTokens(text: String): List<String> {
        val tokens = mutableListOf<String>()
        val regex = Regex("(\\s+|[^\\s]+)")
        for (match in regex.findAll(text)) {
            tokens.add(match.value)
        }
        return tokens
    }
}
