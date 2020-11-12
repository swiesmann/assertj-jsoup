package com.github.ulfs.assertj.jsoup

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

open class DocumentAssertions(
    actual: Document?
) : AbstractAssert<DocumentAssertions, Document>(actual, DocumentAssertions::class.java) {

    fun elementExists(cssSelector: String): DocumentAssertions = apply {
        isNotNull

        val selection = actual.selectFirst(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
        }
    }

    fun elementExists(cssSelector: String, count: Int): DocumentAssertions = apply {
        isNotNull

        val selection = actual.select(cssSelector)
        if (selection.size != count) {
            failWithMessage(
                "%nExpecting elements for%n" +
                        "  <%s>%n" +
                        "to have size of%n" +
                        "  <%s>%n" +
                        "but had%n" +
                        "  <%s>%n" +
                        "with elements:%n" +
                        "%s",
                cssSelector,
                count,
                selection.size,
                maskSelection(selection)
            )
        }
    }

    fun elementAttributeExists(cssSelector: String, attribute: String): DocumentAssertions = apply {
        isNotNull

        val selection = actual.select(cssSelector)
        if (selection.isEmpty()) {
            failWithElementNotFound(cssSelector)
        }

        if (!selection.hasAttr(attribute)) {
            failWithAttributeNotFound(attribute, cssSelector, selection)
        }
    }

    fun elementAttributeNotExists(cssSelector: String, attribute: String): DocumentAssertions = apply {
        isNotNull

        elementExists(cssSelector)
        val selection = actual.select(cssSelector)
        if (selection.hasAttr(attribute)) {
            failWithActualExpectedAndMessage(
                selection,
                null,
                "%nExpecting attribute%n" +
                        "  <%s>%n" +
                        "on element for%n" +
                        "  <%s>%n" +
                        "to be absent, but was%n" +
                        "  <%s>",
                attribute,
                cssSelector,
                selection
            )
        }
    }

    fun elementNotExists(cssSelector: String): DocumentAssertions = also {
        isNotNull

        val selection = actual.selectFirst(cssSelector)
        if (selection != null) {
            failWithActualExpectedAndMessage(
                selection,
                null,
                "%nExpecting element for%n" +
                        "  <%s>%n" +
                        "to be absent, but was%n" +
                        "%s",
                cssSelector,
                maskSelection(selection)
            )
        }
    }

    fun elementHasText(cssSelector: String, string: String) = apply {
        isNotNull

        val selection = actual.selectFirst(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        val text = selection.text()
        if (!text.contains(string)) {
            failWithActualExpectedAndMessage(
                text,
                string,
                "%nExpecting element for%n" +
                        "  <%s>%n" +
                        "to have text%n" +
                        "  <%s>%n" +
                        "but was%n" +
                        "<%s>",
                cssSelector,
                string,
                text
            )
        }
    }

    fun elementHasText(cssSelector: String, vararg strings: String) = apply {
        isNotNull

        val selection = actual.select(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        strings.zip(selection).onEachIndexed { index, matchPair ->
            val elementText = matchPair.second.text()
            val expectedText = matchPair.first
            if (elementText != expectedText) {
                failWithActualExpectedAndMessage(
                    elementText,
                    expectedText,
                    "%nExpecting element at position" +
                            " %s " +
                            "in list for%n" +
                            "<%s>%n" +
                            "to not have text%n" +
                            "  <%s>%n" +
                            "but was%n" +
                            "  <%s>",
                    index,
                    cssSelector,
                    expectedText,
                    elementText
                )
            }
        }

        if (selection.size < strings.size) {
            val rest = strings.drop(selection.size)
            failWithMessage(
                "%nExpecting" +
                        " <%s> remaining elements:%n" +
                        "  <%s>%n" +
                        "but was%n" +
                        "  <%s>" +
                        "for <%s>",
                rest.size,
                rest,
                selection,
                cssSelector
            )
        }
    }

    fun elementContainsText(cssSelector: String, substring: String) = apply {
        isNotNull

        val selection = actual.selectFirst(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        val text = selection.text()
        if (!text.contains(substring)) {
            failWithActualExpectedAndMessage(
                text,
                substring,
                "%nExpecting element for%n" +
                        "  <%s>%n" +
                        "not to contain text%n" +
                        "  <%s>%n" +
                        "but was%n" +
                        "  <%s>",
                cssSelector,
                substring,
                text
            )
        }
    }

    fun elementMatchesText(cssSelector: String, regex: Regex) = apply {
        isNotNull

        val selection = actual.selectFirst(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        val text = selection.text()
        if (!text.contains(regex)) {
            failWithActualExpectedAndMessage(
                text,
                regex,
                "%nExpecting element for%n" +
                        "  <%s>%n" +
                        "not to match regex%n" +
                        "  <%s>%n" +
                        "but was%n" +
                        "  <%s>",
                cssSelector,
                regex,
                text
            )
        }
    }

    fun elementAttributeHasText(cssSelector: String, attribute: String, text: String) = apply {
        isNotNull

        val selection = actual.select(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        if (!selection.hasAttr(attribute)) {
            failWithAttributeNotFound(cssSelector, attribute, selection)
            return this
        }

        val attrValue = selection.attr(attribute)
        if (attrValue != text) {
            failWithActualExpectedAndMessage(
                attrValue,
                text,
                "%nExpecting attribute%n" +
                        "  <%s>%n" +
                        "on element for%n" +
                        "  <%s>%n" +
                        "to be %n" +
                        "  <%s>%n" +
                        "but was <%s>",
                attribute,
                cssSelector,
                text,
                attrValue
            )
        }
    }

    fun elementAttributeHasText(cssSelector: String, attribute: String, vararg attrValues: String) = apply {
        isNotNull

        val selection = actual.select(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        attrValues.zip(selection).onEachIndexed { index, matchPair ->
            val element = matchPair.second

            // attribute not found
            if (!element.hasAttr(attribute)) {
                failWithActualExpectedAndMessage(
                    null,
                    attribute,
                    "%nExpecting element at position" +
                            " %s " +
                            "in list for%n" +
                            "<%s>%n" +
                            "to have attribute%n" +
                            "  <%s>%n" +
                            "but did not:%n" +
                            "  <%s>%n" +
                            "in list%n" +
                            "  <%s>",
                    index,
                    cssSelector,
                    attribute,
                    element,
                    selection
                )
            }

            // attribute value does not match
            val attrValue = element.attr(attribute)
            val expectedAttrValue = matchPair.first
            if (attrValue != expectedAttrValue) {
                failWithActualExpectedAndMessage(
                    attrValue,
                    expectedAttrValue,
                    "%nExpecting element at position" +
                            " %s " +
                            "in list for%n" +
                            "<%s>%n" +
                            "to have attribute value%n" +
                            "  <%s>%n" +
                            "but was%n" +
                            "  <%s>%n" +
                            "in list%n" +
                            "  <%s>",
                    index,
                    cssSelector,
                    expectedAttrValue,
                    attrValue,
                    selection
                )
            }
        }

        if (selection.size < attrValues.size) {
            val rest = attrValues.drop(selection.size)
            failWithMessage(
                "%nExpecting" +
                        " <%s> remaining elements:%n" +
                        "  <%s>%n" +
                        "but was%n" +
                        "  <%s>%n" +
                        "for%n" +
                        "  <%s>",
                rest.size,
                rest,
                selection,
                cssSelector
            )
        }
    }

    fun elementHasClass(cssSelector: String, className: String) = apply {
        isNotNull

        val selection = actual.selectFirst(cssSelector)
        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        if (!selection.hasAttr("class")) {
            failWithAttributeNotFound(cssSelector, "class", selection)
            return this
        }

        if (!selection.hasClass(className)) {
            failWithMessage(
                "%nExpecting element for%n" +
                        "  <%s>%n" +
                        "to include class%n" +
                        "  <%s>%n" +
                        "but found%n" +
                        "  <%s>",
                cssSelector,
                className,
                selection
            )
        }
    }

    fun elementNotHasClass(cssSelector: String, className: String) = apply {
        isNotNull

        val selection = actual.selectFirst(cssSelector)

        if (selection == null) {
            failWithElementNotFound(cssSelector)
            return this
        }

        if (!selection.hasAttr("class")) {
            failWithAttributeNotFound(cssSelector, "class", selection)
            return this
        }

        if (selection.hasClass(className)) {
            failWithMessage(
                "%nExpecting element for%n" +
                        "  <%s>%n" +
                        "to not include class%n" +
                        "  <%s>%n" +
                        "but was%n" +
                        "  <%s>",
                cssSelector,
                className,
                selection
            )
        }
    }

    private fun failWithElementNotFound(cssSelector: String) {
        failWithMessage(
            "%nExpecting element for%n" +
                    "  <%s>%n" +
                    "but found nothing",
            cssSelector
        )
    }

    private fun failWithAttributeNotFound(attribute: String, cssSelector: String, selections: Elements) {
        failWithMessage(
            "%nExpecting attribute%n" +
                    "  <%s>%n" +
                    "on elements for%n" +
                    "  <%s>%n" +
                    "but found%n" +
                    "  <%s>",
            attribute,
            cssSelector,
            selections
        )
    }

    private fun failWithAttributeNotFound(attribute: String, cssSelector: String, selection: Element) {
        failWithMessage(
            "%nExpecting attribute%n" +
                    "  <%s>%n" +
                    "on element for%n" +
                    "  <%s>%n" +
                    "but found%n" +
                    "  <%s>",
            attribute,
            cssSelector,
            selection
        )
    }

    companion object {
        @JvmStatic
        fun assertThat(actual: Document?): DocumentAssertions = DocumentAssertions(actual)

        @JvmStatic
        fun assertThatDocument(actual: String?): DocumentAssertions = DocumentAssertions(Jsoup.parse(actual))
            .also { assertThat(actual).withFailMessage("%nExpecting document but found%n  null").isNotNull }

        @JvmStatic
        fun qa(value: String): String = "*[data-qa=$value]"

        fun assertThatDocumentSpec(
            actual: String?,
            assert: DocumentAssertionsSpec.() -> DocumentAssertionsSpec
        ) = DocumentSoftAssertions.assertThatDocumentSpec(actual, false, assert)

        private fun maskSelection(selection: Elements) = selection.toString().prependIndent("  ")

        private fun maskSelection(selection: Element) = selection.toString().prependIndent("  ")
    }
}
