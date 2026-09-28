package tn.pcecom.service

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import tn.pcecom.db.Categories
import tn.pcecom.db.ContactClicks
import tn.pcecom.db.Products
import tn.pcecom.db.dbQuery
import tn.pcecom.model.CategoryCountDto
import tn.pcecom.model.DailyCountDto
import tn.pcecom.model.ProductClicks
import tn.pcecom.model.StatsDto
import tn.pcecom.model.Status
import java.time.LocalDateTime

class StatsService {

    /** productId = null means the general (floating) WhatsApp button. */
    suspend fun recordClick(productId: Int?) = dbQuery {
        val valid = productId == null || !Products.selectAll()
            .where { (Products.id eq productId) and (Products.status neq Status.HIDDEN) }
            .empty()
        if (valid) {
            ContactClicks.insert {
                it[ContactClicks.productId] = productId?.let { id -> EntityID(id, Products) }
            }
        }
    }

    suspend fun stats(): StatsDto = dbQuery {
        val now = LocalDateTime.now()
        val monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        val since = now.minusDays(30)

        val available = Products.selectAll().where { Products.status eq Status.AVAILABLE }.count()
        val soldThisMonth = Products.selectAll()
            .where { (Products.status eq Status.SOLD) and (Products.soldAt greaterEq monthStart) }
            .count()
        val clicks = ContactClicks.selectAll().where { ContactClicks.clickedAt greaterEq since }.count()

        val clickCount = ContactClicks.id.count()
        val top = (ContactClicks innerJoin Products)
            .select(Products.id, Products.brand, Products.model, clickCount)
            .where { ContactClicks.clickedAt greaterEq since }
            .groupBy(Products.id, Products.brand, Products.model)
            .orderBy(clickCount to SortOrder.DESC)
            .limit(5)
            .map { ProductClicks(it[Products.id].value, it[Products.brand], it[Products.model], it[clickCount]) }

        val revenueSum = Products.priceTnd.sum()
        val revenueThisMonth = Products.select(revenueSum)
            .where { (Products.status eq Status.SOLD) and (Products.soldAt greaterEq monthStart) }
            .single()[revenueSum]?.toLong() ?: 0L
        val revenueAllTime = Products.select(revenueSum)
            .where { Products.status eq Status.SOLD }
            .single()[revenueSum]?.toLong() ?: 0L

        val seriesStart = now.toLocalDate().minusDays(29)
        val soldDates = Products.select(Products.soldAt)
            .where { (Products.status eq Status.SOLD) and (Products.soldAt greaterEq seriesStart.atStartOfDay()) }
            .mapNotNull { it[Products.soldAt]?.toLocalDate() }
        val countsByDate = soldDates.groupingBy { it }.eachCount()
        val salesLast30Days = (0..29).map { offset ->
            val date = seriesStart.plusDays(offset.toLong())
            DailyCountDto(date.toString(), (countsByDate[date] ?: 0).toLong())
        }

        val categoryProductCount = Products.id.count()
        // categoryId isn't declared as an Exposed reference() (see Tables.kt), so the join
        // condition must be given explicitly — plain `innerJoin` can't infer it.
        val topCategories = Products.join(Categories, JoinType.INNER, Products.categoryId, Categories.id)
            .select(Categories.id, Categories.name, categoryProductCount)
            .where { Products.status eq Status.AVAILABLE }
            .groupBy(Categories.id, Categories.name)
            .orderBy(categoryProductCount to SortOrder.DESC)
            .limit(5)
            .map { CategoryCountDto(it[Categories.id].value, it[Categories.name], it[categoryProductCount]) }

        StatsDto(
            productsAvailable = available,
            soldThisMonth = soldThisMonth,
            clicksLast30Days = clicks,
            topClicked = top,
            revenueThisMonthTnd = revenueThisMonth,
            revenueAllTimeTnd = revenueAllTime,
            salesLast30Days = salesLast30Days,
            topCategories = topCategories,
        )
    }
}
