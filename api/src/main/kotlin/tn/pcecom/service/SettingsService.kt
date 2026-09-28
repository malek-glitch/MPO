package tn.pcecom.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import tn.pcecom.db.StoreSettings
import tn.pcecom.db.dbQuery
import tn.pcecom.model.StoreSettingsDto
import tn.pcecom.model.StoreSettingsInput

/** Single-row table: one store per installation. */
class SettingsService(private val storage: PhotoStorage) {

    suspend fun get(): StoreSettingsDto {
        val r = dbQuery { StoreSettings.selectAll().where { StoreSettings.id eq 1 }.single() }
        return StoreSettingsDto(
            name = r[StoreSettings.name],
            logoUrl = r[StoreSettings.logoKey]?.let { storage.url(it, "md") },
            whatsappNumber = r[StoreSettings.whatsappNumber],
            phone = r[StoreSettings.phone],
            address = r[StoreSettings.address],
            facebookUrl = r[StoreSettings.facebookUrl],
            instagramUrl = r[StoreSettings.instagramUrl],
            tiktokUrl = r[StoreSettings.tiktokUrl],
            whatsappTemplate = r[StoreSettings.whatsappTemplate],
            deliveryInfo = r[StoreSettings.deliveryInfo],
        )
    }

    suspend fun update(i: StoreSettingsInput): StoreSettingsDto {
        require(i.name.isNotBlank()) { "store name is required" }
        val number = normalizeWhatsapp(i.whatsappNumber)
        dbQuery {
            StoreSettings.update({ StoreSettings.id eq 1 }) {
                it[StoreSettings.name] = i.name.trim()
                it[StoreSettings.whatsappNumber] = number
                it[StoreSettings.phone] = i.phone.cleanOrNull()
                it[StoreSettings.address] = i.address.cleanOrNull()
                it[StoreSettings.facebookUrl] = i.facebookUrl.cleanOrNull()
                it[StoreSettings.instagramUrl] = i.instagramUrl.cleanOrNull()
                it[StoreSettings.tiktokUrl] = i.tiktokUrl.cleanOrNull()
                it[StoreSettings.whatsappTemplate] = i.whatsappTemplate.cleanOrNull()
                it[StoreSettings.deliveryInfo] = i.deliveryInfo.cleanOrNull()
            }
        }
        return get()
    }

    suspend fun setLogo(bytes: ByteArray): StoreSettingsDto {
        val old = dbQuery {
            StoreSettings.selectAll().where { StoreSettings.id eq 1 }.single()[StoreSettings.logoKey]
        }
        val key = withContext(Dispatchers.IO) { storage.upload("store", bytes) }
        dbQuery { StoreSettings.update({ StoreSettings.id eq 1 }) { it[StoreSettings.logoKey] = key } }
        if (old != null) withContext(Dispatchers.IO) { storage.delete(old) }
        return get()
    }
}
