package tn.pcecom.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import tn.pcecom.config.JwtConfig
import tn.pcecom.db.AdminUsers
import tn.pcecom.db.dbQuery
import tn.pcecom.model.MeResponse
import java.util.Date

class AuthService(private val cfg: JwtConfig) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    /** Creates the first admin from config if the table is empty. */
    fun bootstrapAdmin(email: String?, password: String?) = transaction {
        if (!AdminUsers.selectAll().empty()) return@transaction
        if (email == null || password == null) {
            log.warn("No admin account yet. Set ADMIN_EMAIL and ADMIN_PASSWORD to create one.")
            return@transaction
        }
        require(password.length >= 8) { "ADMIN_PASSWORD must be at least 8 characters" }
        AdminUsers.insert {
            it[AdminUsers.email] = email.trim().lowercase()
            it[AdminUsers.passwordHash] = hash(password)
        }
        log.info("Admin account created for {}", email)
    }

    /** Returns a JWT, or null if the credentials are wrong. */
    suspend fun login(email: String, password: String): String? {
        val row = dbQuery {
            AdminUsers.selectAll().where { AdminUsers.email eq email.trim().lowercase() }.singleOrNull()
        } ?: return null
        return if (verify(password, row[AdminUsers.passwordHash])) token(row[AdminUsers.id].value) else null
    }

    suspend fun me(adminId: Int): MeResponse? = dbQuery {
        AdminUsers.selectAll().where { AdminUsers.id eq adminId }.singleOrNull()
            ?.let { MeResponse(it[AdminUsers.id].value, it[AdminUsers.email]) }
    }

    /** Returns false if the current password is wrong. */
    suspend fun changePassword(adminId: Int, current: String, newPassword: String): Boolean {
        require(newPassword.length >= 8) { "password must be at least 8 characters" }
        val row = dbQuery {
            AdminUsers.selectAll().where { AdminUsers.id eq adminId }.singleOrNull()
        } ?: return false
        if (!verify(current, row[AdminUsers.passwordHash])) return false
        dbQuery {
            AdminUsers.update({ AdminUsers.id eq adminId }) { it[AdminUsers.passwordHash] = hash(newPassword) }
        }
        return true
    }

    private fun hash(password: String): String =
        BCrypt.withDefaults().hashToString(12, password.toCharArray())

    private fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified

    private fun token(adminId: Int): String = JWT.create()
        .withIssuer(cfg.issuer)
        .withAudience(cfg.audience)
        .withSubject(adminId.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + cfg.expiresHours * 3_600_000))
        .sign(Algorithm.HMAC256(cfg.secret))
}
