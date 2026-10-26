package common.core.source.mapper

/**
 * Transforms an input of type [I] into an output of type [O].
 *
 * A single-method functional interface that serves as the canonical mapping
 * contract across data, domain, and presentation layers. Implement as a
 * lambda or a named class depending on complexity.
 *
 * @param I The input type to map from.
 * @param O The output type to map to.
 */
public fun interface Mapper<I, O> {

    /**
     * Maps [input] to an instance of [O].
     *
     * @param input The object to transform.
     * @return The transformed result.
     */
    public fun map(input: I): O
}
