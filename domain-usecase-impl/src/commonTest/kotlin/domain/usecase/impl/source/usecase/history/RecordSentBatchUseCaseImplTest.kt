package domain.usecase.impl.source.usecase.history

import domain.core.source.model.history.BatchStatusModel
import domain.core.source.model.history.HistoryEntryModel
import domain.core.source.model.history.SentBatchModel
import domain.core.source.monad.Failure
import domain.repository.api.source.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * [HistoryRepository.record] throws on failure by contract. T-9 relies on a failed history
 * write never failing a send, which only holds if that throw arrives as a [Failure] inside
 * [Result] instead of escaping. The thrown type is a plain [RuntimeException] because
 * `domain-usecase-impl` cannot see data-layer exception types and `resultLauncher` has to
 * handle any unexpected throwable anyway.
 */
class RecordSentBatchUseCaseImplTest {

    @Test
    fun `invoke maps a thrown repository exception into a Technical Database Failure`() {
        runBlocking {
            val useCase = RecordSentBatchUseCaseImpl(
                ThrowingHistoryRepository(RuntimeException("disk full")),
            )

            val result = useCase(SentBatchModel(id = "s-1", sentAtMs = 0L, deviceLabel = "Pixel", prompt = "p"))

            assertIs<Failure.Technical.Database>(result.exceptionOrNull())
        }
    }
}

/** A [HistoryRepository] whose [record] always throws [toThrow]. */
private class ThrowingHistoryRepository(private val toThrow: Throwable) : HistoryRepository {

    override suspend fun record(batch: SentBatchModel): Unit = throw toThrow

    override fun observe(): Flow<List<HistoryEntryModel>> = emptyFlow()
    override suspend fun updateStatus(batchId: String, status: BatchStatusModel): Unit =
        throw NotImplementedError()
}
