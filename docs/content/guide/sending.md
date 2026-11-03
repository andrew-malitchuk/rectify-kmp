# Sending to Claude Code

## Resolve and Send

The toolbar's primary action, or <kbd>⌘↵</kbd>. It is disabled while nothing is staged and
while a send is already in flight.

The status line reports each step as it happens:

```
● Streaming  ›  ● Frozen  ›  ● Dumping hierarchy  ›  ● Sent
```

`Dumping hierarchy` is the slow one — roughly 2.7 seconds on a typical handset — because it
shells out to the device and waits for a full view tree.

On success a toast confirms what went where, with the session id and an **Open session** action
that copies it. The mirror resumes live and the staged counter returns to zero.

## What is in the payload

| Part | Detail |
|---|---|
| **Frame** | The full frozen screenshot, at device resolution. |
| **Crops** | One per annotation, with the red outline baked in. |
| **Hierarchy** | The view-hierarchy XML dump taken at freeze time. |
| **Prompt** | Generated text describing each annotation, including any <kbd>⌥</kbd> measurement. |

The crops exist because an agent reading a 1080×2400 frame has to find your region in it first.
The hierarchy exists because *the price label is misaligned* is a different instruction from
*the view with id `item_price` is misaligned*.

## How the handoff actually works

Rectify does not talk to Claude Code. The two never share a process, a socket or a lifetime.

```
Rectify  ──writes──▶  ~/.rectify/handoff  ◀──reads──  rectify-mcp  ──stdio──▶  Claude Code
```

1. **Rectify publishes.** The batch is written to a slot at `~/.rectify/handoff`, plus its bulk
   bytes under `~/.rectify/history/`.
2. **Claude Code spawns the server.** `rectify-mcp` is a separate, deliberately tiny JVM
   process. Claude Code starts and stops it; you never run it yourself.
3. **The server reads the slot** and answers one tool, `get_latest_annotation`, over JSON-RPC.

The indirection is the point. Rectify can be closed, restarted or crash, and the last batch is
still there to be read. Claude Code can be started after the fact and still get the payload.
Neither has to be running when the other is.

It also explains the MCP server's unusually small dependency list — no Compose, no Koin, no
graphics stack. Claude Code pays its JVM startup cost on every tool call, so the classpath is
pinned to the handoff contract and a JSON parser.

## Using it from Claude Code

With the server registered — see [Installation](../installation/) — ask for the latest
annotation in whatever words you like. Claude Code calls `get_latest_annotation` and receives
the frame, the crops, the hierarchy and the prompt.

A useful habit is to let the prompt do the describing and add only what the boxes cannot say:

> Fix the annotations from the latest Rectify batch. The spacing one is the priority.

## Follow-ups

After a [Verify](../verify/) run finds something still off, the same button becomes **Send
follow-up** and re-stages just the annotations that failed — not the whole batch.
