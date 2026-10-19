package domain.usecase.impl.core

import domain.core.source.monad.Failure
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The one rule that is not about mapping: cancellation is not a failure.
 *
 * Every use case in this module funnels through [resultLauncher], so a swallowed
 * `CancellationException` here means a cancelled caller carries on past its own suspension
 * point everywhere at once — reducing state and writing to the database for a job somebody
 * already stopped (`review#COR-1`).
 */
class ResultLauncherTest {

    @Test
    fun `a cancellation unwinds instead of becoming a failed Result`() {
        runBlocking {
            assertFailsWith<CancellationException> {
                resultLauncher(errorMapper = Failure.Technical::Database) {
                    throw CancellationException("the caller went away")
                }
            }
        }
    }

    @Test
    fun `an ordinary throwable is still mapped through the errorMapper`() {
        runBlocking {
            val result = resultLauncher(errorMapper = Failure.Technical::Database) {
                throw RuntimeException("disk full")
            }

            assertIs<Failure.Technical.Database>(result.exceptionOrNull())
        }
    }

    @Test
    fun `a thrown domain Failure still arrives unmapped`() {
        runBlocking {
            val result = resultLauncher(errorMapper = Failure.Technical::Database) {
                throw Failure.Logic.NotFound
            }

            assertTrue(result.exceptionOrNull() === Failure.Logic.NotFound)
        }
    }
}
