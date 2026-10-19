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
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * TAC-1 — the only behaviour in this use case worth a test.
 *
 * [HistoryRepository.get] answers `null` for both "no such batch" and "the frame is gone from
 * disk", and this is the single place that absence becomes an error value. The verify run
 * turns that failure into `VerifyError.NotOnDisk`, so a silent `Result.success(null)` here
 * would strand the caller with nothing to report. The sibling branch — a database read that
 * throws — must still arrive as `Failure.Technical.Database`, not as `NotFound`.
 */
class GetSentBatchUseCaseImplTest {

    @Test
    fun `invoke maps a null repository result to a Logic NotFound Failure`() {
        runBlocking {
            val useCase = GetSentBatchUseCaseImpl(StubHistoryRepository(answer = { null }))

            val result = useCase("s-missing")

            assertEquals(Failure.Logic.NotFound, result.exceptionOrNull())
        }
    }

    @Test
    fun `invoke maps a thrown repository exception into a Technical Database Failure`() {
        runBlocking {
            val useCase = GetSentBatchUseCaseImpl(
                StubHistoryRepository(answer = { throw RuntimeException("database is locked") }),
            )

            val result = useCase("s-1")

            assertIs<Failure.Technical.Database>(result.exceptionOrNull())
        }
    }

    @Test
    fun `invoke passes a found batch straight through`() {
        runBlocking {
            val batch = SentBatchModel(id = "s-1", sentAtMs = 0L, deviceLabel = "Pixel", prompt = "p")
            val useCase = GetSentBatchUseCaseImpl(StubHistoryRepository(answer = { batch }))

            val result = useCase("s-1")

            assertEquals(batch, result.getOrNull())
        }
    }
}

/** A [HistoryRepository] whose [get] returns whatever [answer] decides. */
private class StubHistoryRepository(
    private val answer: () -> SentBatchModel?,
) : HistoryRepository {

    override suspend fun get(batchId: String): SentBatchModel? = answer()

    override fun observe(): Flow<List<HistoryEntryModel>> = emptyFlow()
    override suspend fun record(batch: SentBatchModel): Unit = throw NotImplementedError()
    override suspend fun updateStatus(batchId: String, status: BatchStatusModel): Unit =
        throw NotImplementedError()
}
