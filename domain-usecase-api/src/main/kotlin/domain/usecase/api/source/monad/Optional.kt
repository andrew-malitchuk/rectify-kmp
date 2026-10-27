package domain.usecase.api.source.monad

/**
 * Represents the result of an operation that returns no specific value ([Unit]),
 * but can fail with a [domain.core.source.monad.Failure].
 *
 * This is a typealias for [Result]<[Unit]>.
 */
public typealias Optional = Result<Unit>
