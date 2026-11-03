# Annotating a frame

## Drag to freeze

Click and drag anywhere on the live mirror. While you drag you get a dashed red outline, a live
size readout in device pixels, and ruler guides along the edges so you can line the box up
against something real.

**Releasing the drag freezes the frame.** The exact pixels under your box at that instant become
the frozen frame, and the box becomes annotation number 1.

Freezing is a *render* decision, not a transport one. The capture loop keeps running behind the
frozen frame, which is why **Resume live** costs nothing — there is no reconnection to pay for.

## Write the comment

Each annotation gets a popover with a numbered badge matching its box, an autofocused comment
field, a size and position line, and a delete control.

- <kbd>↵</kbd> saves the comment.
- <kbd>Esc</kbd> dismisses the popover, keeping the annotation.
- The delete control removes the annotation entirely.

The placeholder asks *What's wrong here?* because that is the question the payload is answering.
A comment is optional — an annotation with none is still a region the agent can look at — but a
sentence of intent is usually worth more than the crop.

## More than one

Keep dragging. Everything outside the annotation boxes dims, so the frozen frame reads as a set
of marked regions rather than a screenshot with boxes on it.

When popovers would overlap each other, they relocate to a gutter beside the frame and connect
back to their boxes with leader lines, rather than stacking on top of each other over the
content you are trying to describe.

The toolbar's **staged** counter tracks how many you have. **Clear** removes all of them at
once — but it is disabled for exactly the window during which a send is assembling the batch,
so you cannot wipe a batch out from under its own send.

## The side panel

Three tabs, switchable with <kbd>⌘1</kbd> <kbd>⌘2</kbd> <kbd>⌘3</kbd>:

| Tab | Shows |
|---|---|
| **Notes** | Your annotations, in order, with their comments. |
| **Payload** | Exactly what will be sent — see below. |
| **History** | Every batch you have ever sent. See [Verify and history](../verify/). |

The panel only appears once a device is selected. With nothing to annotate, annotation surfaces
would be advertising actions that cannot be taken.

You can drag the panel's leading edge to resize it between 260 and 480 dp. The width is
remembered across launches.

## The Payload tab

This is the review step. Nothing here is a summary — it is the actual content of the send:

- **Prompt** — the generated text, with a token estimate and an **Edit** affordance if you want
  to say something the boxes cannot.
- **Crops** — a three-column grid of per-annotation cropped images, each with the red outline
  baked in, captioned with the frame they came from.
- **Hierarchy** — the view-hierarchy dump as an indented monospace tree, with matched and
  flagged nodes marked.
- **Footer** — a one-line summary of contents and total size, e.g.
  `frame.png · 3 crops · hierarchy.xml`.

Reading the Payload tab before sending is the cheapest way to notice that a box landed on the
wrong view.

## Resuming

**Resume live** unfreezes and goes back to the stream. A successful send does it automatically
and resets the staged counter to zero — see [Sending to Claude Code](../sending/).
