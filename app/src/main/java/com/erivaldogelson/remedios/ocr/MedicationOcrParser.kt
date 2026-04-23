package com.erivaldogelson.remedios.ocr

import com.erivaldogelson.remedios.domain.model.OcrSuggestion

class MedicationOcrParser {

    fun parse(rawText: String): OcrSuggestion {
        val lines = rawText.lines()
            .map(String::trim)
            .filter(String::isNotBlank)

        val dosageRegex = Regex("""(\d+\s?(mg|g|ml|mcg|ui|IU))""", RegexOption.IGNORE_CASE)
        val manufacturerHints = listOf("laboratório", "lab", "fabricado", "fabricante", "pharma", "farma")

        val suggestedName = lines.firstOrNull {
            it.any(Char::isLetter) && !dosageRegex.containsMatchIn(it)
        }.orEmpty()

        val suggestedDosage = lines.firstNotNullOfOrNull { line ->
            dosageRegex.find(line)?.value
        }.orEmpty()

        val suggestedManufacturer = lines.firstOrNull { line ->
            manufacturerHints.any { hint -> line.contains(hint, ignoreCase = true) }
        } ?: lines.firstOrNull { line ->
            line.contains("ltda", ignoreCase = true) || line.contains("s.a", ignoreCase = true)
        }.orEmpty()

        val suggestedInstructions = lines.firstOrNull { line ->
            line.contains("uso", ignoreCase = true) ||
                line.contains("tomar", ignoreCase = true) ||
                line.contains("via oral", ignoreCase = true)
        }.orEmpty()

        return OcrSuggestion(
            rawText = rawText,
            suggestedName = suggestedName,
            suggestedDosage = suggestedDosage,
            suggestedManufacturer = suggestedManufacturer,
            suggestedInstructions = suggestedInstructions,
        )
    }
}

