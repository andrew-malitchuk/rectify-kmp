package data.repository.impl.core.mapper.base

import common.core.source.mapper.Mapper

/**
 * Bidirectional mapper between a domain model type [MODEL] and a data resource type [RESOURCE].
 *
 * No upper-bound constraints are placed on the type parameters so that plain types
 * (e.g. [String], nullable types) can be used as [MODEL] without requiring them to
 * implement a marker interface.
 *
 * Usage example:
 * ```kotlin
 * object UserMapper : ModelResourceMapper<UserModel, UserDto> {
 *     override val toModel = Mapper<UserDto, UserModel> { dto ->
 *         UserModel(id = dto.id, email = dto.email)
 *     }
 *     override val toResource = Mapper<UserModel, UserDto> { model ->
 *         UserDto(id = model.id, email = model.email)
 *     }
 * }
 * ```
 *
 * @param MODEL The domain-side type.
 * @param RESOURCE The data-layer type.
 */
internal interface ModelResourceMapper<MODEL, RESOURCE> {
    /**
     * Mapper to convert a [RESOURCE] to a [MODEL].
     */
    val toModel: Mapper<RESOURCE, MODEL>

    /**
     * Mapper to convert a [MODEL] to a [RESOURCE].
     */
    val toResource: Mapper<MODEL, RESOURCE>
}
