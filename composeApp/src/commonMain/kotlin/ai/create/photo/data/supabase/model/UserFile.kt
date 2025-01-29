package ai.create.photo.data.supabase.model

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames

@Serializable
data class UserFile(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("file_name") val fileName: String,
    @SerialName("signed_url") val signedUrl: String,
    @SerialName("analysis") val analysis: String?,
    @SerialName("analysis_status") val analysisStatus: AnalysisStatus?,
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val columns = serializer().descriptor.elementNames.toList()
    }
}

enum class AnalysisStatus {
    @SerialName("approved")
    APPROVED,

    @SerialName("declined")
    DECLINED,

    @SerialName("processing")
    PROCESSING,
}