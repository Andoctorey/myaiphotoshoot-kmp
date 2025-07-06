package ai.create.photo.ui.main

import ai.create.photo.ui.generate.Prompt

data class PromptState(
    val prompt: Prompt? = null,
    val version: Int = 0
) {
    fun update(prompt: Prompt?): PromptState = copy(prompt = prompt, version = version + 1)
} 