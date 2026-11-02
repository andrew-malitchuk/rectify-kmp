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

Rectify serves the Model Context Protocol itself, on the loopback interface. There is no second
process to install and nothing for you to launch.

```
Rectify ──writes──▶ ~/.rectify/handoff ──┐
                                         ├─ MCP endpoint ──HTTP──▶ Claude Code
                        (same process) ──┘   127.0.0.1:7391/mcp
```

1. **Rectify publishes.** The batch is written to a slot at `~/.rectify/handoff`, plus its bulk
   bytes under `~/.rectify/history/`.
2. **The endpoint reads that slot** and answers one tool, `get_latest_annotation`, over JSON-RPC
   on `http://127.0.0.1:7391/mcp`.
3. **Claude Code connects as a client.** It does not spawn anything; it opens an HTTP request
   when it needs the payload.

The slot still sits between the two halves even though they now share a process. It is what
makes a batch survive a restart, and what keeps the UI's send path independent of whether any
agent is listening.

**The app has to be open.** That is the one real cost of serving the protocol in-process, and it
replaced a worse one: before, you had to build a second binary, keep a JDK around for it, and
hand Claude Code an absolute path to it. The status bar now reads `MCP :7391` whenever the
endpoint is up, which is also the first thing to look at when the agent gets nothing.

**The endpoint binds to loopback only, and refuses cross-origin requests.** Anything else would
publish screenshots of whatever you are debugging to every page in your browser and every device
on your network.

## Using it from Claude Code

With the server registered — see [Installation](../installation/) — ask for the latest
annotation in whatever words you like. Claude Code calls `get_latest_annotation` and receives
the frame, the crops, the hierarchy and the prompt.

A useful habit is to let the prompt do the describing and add only what the boxes cannot say:

> Fix the annotations from the latest Rectify batch. The spacing one is the priority.

## Follow-ups

After a [Verify](../verify/) run finds something still off, the same button becomes **Send
follow-up** and re-stages just the annotations that failed — not the whole batch.
