package tn.pcecom.service

import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import tn.pcecom.db.Categories
import tn.pcecom.db.dbQuery
import tn.pcecom.model.CategoryDto
import tn.pcecom.model.CategoryInput

/** Two levels only: top-level categories and their subcategories. */
class CategoryService {

    private fun toDto(r: ResultRow) = CategoryDto(
        id = r[Categories.id].value,
        name = r[Categories.name],
        slug = r[Categories.slug],
        parentId = r[Categories.parentId],
        position = r[Categories.position],
        visible = r[Categories.visible],
    )

    suspend fun tree(includeHidden: Boolean): List<CategoryDto> {
        val all = dbQuery {
            Categories.selectAll()
                .orderBy(Categories.position to SortOrder.ASC, Categories.id to SortOrder.ASC)
                .map(::toDto)
        }.filter { includeHidden || it.visible }
        val byParent = all.groupBy { it.parentId }
        return (byParent[null] ?: emptyList()).map { it.copy(children = byParent[it.id] ?: emptyList()) }
    }

    suspend fun create(input: CategoryInput): CategoryDto = dbQuery {
        val name = input.name.trim()
        require(name.isNotEmpty()) { "name is required" }
        val parentSlug = input.parentId?.let { parentSlugFor(it) }
        val slug = input.slug?.let(::slugify)?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(parentSlug, slugify(name)).joinToString("-")

        val newId = Categories.insertAndGetId {
            it[Categories.name] = name
            it[Categories.slug] = slug
            it[Categories.parentId] = input.parentId
            it[Categories.position] = input.position
            it[Categories.visible] = input.visible
        }
        toDto(Categories.selectAll().where { Categories.id eq newId }.single())
    }

    suspend fun update(categoryId: Int, input: CategoryInput): CategoryDto = dbQuery {
        val current = Categories.selectAll().where { Categories.id eq categoryId }.singleOrNull()
            ?: throw NotFoundException("category not found")
        val name = input.name.trim()
        require(name.isNotEmpty()) { "name is required" }
        if (input.parentId != null) {
            require(input.parentId != categoryId) { "a category cannot be its own parent" }
            val hasChildren = Categories.selectAll().where { Categories.parentId eq categoryId }.count() > 0
            require(!hasChildren) { "a category with subcategories cannot become a subcategory" }
            parentSlugFor(input.parentId)
        }
        val slug = input.slug?.let(::slugify)?.takeIf { it.isNotEmpty() } ?: current[Categories.slug]

        Categories.update({ Categories.id eq categoryId }) {
            it[Categories.name] = name
            it[Categories.slug] = slug
            it[Categories.parentId] = input.parentId
            it[Categories.position] = input.position
            it[Categories.visible] = input.visible
        }
        toDto(Categories.selectAll().where { Categories.id eq categoryId }.single())
    }

    /** Fails with 409 (via StatusPages) if products or subcategories still use it. */
    suspend fun delete(categoryId: Int) = dbQuery {
        val deleted = Categories.deleteWhere { Categories.id eq categoryId }
        if (deleted == 0) throw NotFoundException("category not found")
    }

    private fun parentSlugFor(parentId: Int): String {
        val parent = Categories.selectAll().where { Categories.id eq parentId }.singleOrNull()
            ?: throw BadRequestException("parent category not found")
        require(parent[Categories.parentId] == null) { "only two category levels are supported" }
        return parent[Categories.slug]
    }
}
