package domain.core.source.model.history

import domain.core.source.model.base.Model

/**
 * Lifecycle state of a batch of annotations that was sent to the coding agent.
 *
 * Only [Sent] is ever written today — the remaining four are persisted and rendered so the
 * verify-and-rediff loop can advance a batch without a schema change.
 */
public enum class BatchStatusModel : Model {
    /** Handed to the agent; nothing has come back yet. */
    Sent,

    /** The agent is working on the batch. */
    InProgress,

    /** The agent reports a fix; the batch can be verified. */
    Fixed,

    /** The fix was verified against a fresh frame. */
    Verified,

    /** A follow-up batch was sent because verification failed. */
    FollowUpSent,
}
