package ai.create.photo.data.supabase

import ai.create.photo.data.supabase.model.UserFile
import ai.create.photo.data.supabase.model.UserGeneration
import co.touchlab.kermit.Logger
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body

object SupabaseFunction {

    suspend fun trainAiModel() = retryWithBackoff {
        Logger.i("trainAiModel")
        Supabase.supabase.functions.invoke(
            function = "training",
        )
    }

    suspend fun generatePhoto(trainingId: String, prompt: String, parentGenerationId: String?) =
        retryWithBackoff {
        Logger.i("generatePhoto trainingId: $trainingId, parentGenerationId: $parentGenerationId, prompt: $prompt")
        Supabase.supabase.functions.invoke(
            function = "generate",
            body = mapOf(
                "training_id" to trainingId,
                "prompt" to prompt,
                "parent_generation_id" to parentGenerationId,
            )
        )
    }

    suspend fun deleteUser() = retryWithBackoff {
        Logger.i("deleteUser")
        Supabase.supabase.functions.invoke(function = "delete-user")
    }

    suspend fun analyzePhoto(fileId: String): UserFile = retryWithBackoff {
        Logger.i("analyzePhoto fileId: $fileId")
        val response = Supabase.supabase.functions.invoke(
            function = "analyze-selfie",
            body = mapOf("file_id" to fileId)
        )
        return@retryWithBackoff response.body<UserFile>()
    }

    suspend fun generatePersonDescription(trainingId: String) = retryWithBackoff {
        Logger.i("generatePersonDescription, trainingId: $trainingId")
        Supabase.supabase.functions.invoke(
            function = "generate-person-description",
            body = mapOf("training_id" to trainingId)
        )
    }

    suspend fun surpriseMe(): String = retryWithBackoff {
        Logger.i("surpriseMe")
        val response = Supabase.supabase.functions.invoke(function = "surprise-me")
        return@retryWithBackoff response.body<String>()
    }

    suspend fun translate(prompt: String): String = retryWithBackoff {
        Logger.i("translate prompt: $prompt")
        val response = Supabase.supabase.functions.invoke(
            function = "translate",
            body = mapOf("prompt" to prompt)
        )
        return@retryWithBackoff response.body<String>()
    }

    suspend fun enhancePrompt(prompt: String): String = retryWithBackoff {
        Logger.i("enhancePrompt, prompt: $prompt")
        val response = Supabase.supabase.functions.invoke(
            function = "enhance-prompt",
            body = mapOf("prompt" to prompt)
        )
        return@retryWithBackoff response.body<String>()
    }

    suspend fun pictureToPrompt(url: String): String = retryWithBackoff {
        Logger.i("pictureToPrompt url: $url")
        val response = Supabase.supabase.functions.invoke(
            function = "picture-to-prompt",
            body = mapOf("url" to url)
        )
        return@retryWithBackoff response.body<String>()
    }

    suspend fun applyPromoCode(code: String): Boolean = retryWithBackoff {
        Logger.i("enterPromoCode: $code")
        val response = Supabase.supabase.functions.invoke(
            function = "promo-code",
            body = mapOf("promo_code" to code)
        )
        return@retryWithBackoff response.body<Boolean>()
    }

    suspend fun sendSlackError(error: String) = retryWithBackoff {
        Logger.i("sendSlackError: $error")
        Supabase.supabase.functions.invoke(
            function = "slack-error",
            body = mapOf("error" to error)
        )
    }

    suspend fun verifyAndroidPurchase(productId: String, purchaseToken: String): Boolean =
        retryWithBackoff {
        Logger.i("verifyAndroidPurchase: $productId, $purchaseToken")
        val response = Supabase.supabase.functions.invoke(
            function = "verify-android-purchase",
            body = mapOf("product_id" to productId, "purchase_token" to purchaseToken)
        )
            return@retryWithBackoff response.body<Boolean>()
    }

    suspend fun verifyIosPurchase(
        productId: String,
        receipt: String,
        transactionId: String
    ): Boolean = retryWithBackoff {
        Logger.i("verifyIosPurchase: $productId, $receipt")
        val response = Supabase.supabase.functions.invoke(
            function = "verify-ios-purchase",
            body = mapOf(
                "product_id" to productId,
                "receipt" to receipt,
                "transaction_id" to transactionId
            )
        )
        return@retryWithBackoff response.body<Boolean>()
    }

    suspend fun getGeneration(id: String): UserGeneration? = retryWithBackoff {
        Logger.i("getGeneration by id: $id")
        val response = Supabase.supabase.functions.invoke(
            function = "get-generation?id=$id"
        )
        return@retryWithBackoff response.body<UserGeneration?>()
    }
}
