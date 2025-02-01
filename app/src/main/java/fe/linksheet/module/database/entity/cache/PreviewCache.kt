package fe.linksheet.module.database.entity.cache

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "preview_cache",
    foreignKeys = [
        ForeignKey(entity = UrlEntity::class, parentColumns = arrayOf("id"), childColumns = arrayOf("id"))
    ]
)
data class PreviewCache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String?,
    val description: String?,
    val faviconType: String?,
    val faviconUrl: ByteArray?,
    val thumbnailType: String?,
    val thumbnail: ByteArray?
) {

}
