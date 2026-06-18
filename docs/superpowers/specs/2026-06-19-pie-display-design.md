# Pie Display — Design Spec

## Summary

Add a fourth metronome visualization, `DisplayMode.PIE`, alongside the existing
Clock (`CircularDisplay`), Conductor (`ConductorDisplay`), and Grid (`GridDisplay`)
modes. The pie display renders the current measure as a ring divided into equal
wedges — one per beat — that flash and fade in sequence as the metronome plays,
combining the ring layout of the Clock display with the discrete per-beat
lighting of the Grid display. Sequential lighting of adjacent wedges alone
creates the visual effect of motion traveling around the ring; no separate
traveling indicator (dot, sweep, etc.) is drawn.

## Motivation

The app currently has three metronome visualizations with distinct visual
languages: a continuous progress arc (Clock), a conductor's beat-pattern path
(Conductor), and a grid of independently flashing shapes (Grid). This adds a
fourth that sits between Clock and Grid: spatially arranged like a clock face,
but each beat is a discrete, independently animated wedge like a grid item.

## Scope

In scope:
- New `PieDisplay` page composable and `PieRing` component.
- Support for 1 or 2 enabled tracks, rendered as concentric rings.
- New `DisplayMode.PIE` enum value, string resource, and icon, wired into the
  existing display mode selector.

Out of scope:
- More than 2 tracks (Grid display already covers the "all tracks" case).
- A distinct traveling-light indicator between beats (explicitly rejected —
  sequential wedge lighting is sufficient).
- Any conductor-style "unsupported time signature" fallback — the pie layout
  works for any beat count, so no fallback message is needed.

## Components

### `pages/PieDisplay.kt`

Structurally mirrors `pages/CircularDisplay.kt`:

```kotlin
@Composable
fun PieDisplay(viewModel: MetronomeViewModel, tracks: List<MetronomeTrack>, modifier: Modifier = Modifier)
```

- `displayTracks = tracks.filter { it.enabled }`; return early (render nothing)
  if empty.
- Outer `Box(fillMaxSize)` containing:
  - A `Box(fillMaxHeight().aspectRatio(1f).align(Center))` holding
    `PieRing(displayTracks[0], ringSize = 6.dp.toPx(), trackPalette = displayTracks[0].color.getPalette())`.
  - If `displayTracks.size > 1`, a second `Box(fillMaxHeight().padding(24.dp).aspectRatio(1f).align(Center))`
    holding `PieRing(displayTracks[1], ringSize = 4.dp.toPx(), trackPalette = displayTracks[1].color.getPalette())`
    — same nesting/padding values `CircularDisplay` uses for its second ring,
    producing the concentric-ring effect.
  - A `Box(fillMaxHeight().padding(32.dp).aspectRatio(1f).align(Center))` holding
    the centered `TempoChanger`, identical to `CircularDisplay`'s and
    `GridDisplay`'s tempo control wiring (`onIncrement`/`onDecrement`/`onClick`
    calling `viewModel.setBpm` / `viewModel.setShowBpmDialog`).

### `components/PieRing.kt`

```kotlin
@Composable
fun BoxScope.PieRing(track: MetronomeTrack, ringSize: Float, trackPalette: TrackColorPalette)
```

Signature and internal data wiring mirror `CircularClock`:

- `rhythm` / `intervals` state, kept in sync via `track.editEvents` (same as
  `CircularClock`).
- `currentMeasure: Int` state, updated from `track.updateEvents`.
- Wedge count for the current measure:
  `intervals.filter { it.measure == currentMeasure }.size` — i.e. every
  rhythm element (notes and rests) in the measure, the same source
  `ClockBeats` already uses. This is **not** `timeSig.first`; compound/tuplet
  measures will show one wedge per element, consistent with how `ClockBeats`
  marks beats around the Clock display.
- Per-wedge animation state: a `List` (sized to the current wedge count,
  rebuilt via `remember(currentMeasure, wedgeCount)`) of:
  - `color: Animatable<Color>` — animates between `trackPalette.colorContainer`
    (inactive) and `trackPalette.color` (active), mirroring
    `GridDisplayItem.baseColor`.
  - `highlightAlpha: Animatable<Float>` — a brief flash overlay that snaps to
    visible and decays to `0f`, mirroring `GridDisplayItem.highlightAlpha`.
  - `highlightScale` is not needed for wedges (no shape morphing); the flash
    is communicated via a wider stroke width animatable (`strokeBoost: Animatable<Float>`)
    that snaps up on the beat and decays back to `0f`, added on top of the
    base `ringSize` when drawing — analogous role to `GridDisplayItem`'s
    `highlightScale` but expressed as stroke width since wedges are arcs, not
    polygons.

- `LaunchedEffect(track) { track.updateEvents.collect { beat -> ... } }`:
  - Apply `Settings.VISUAL_LATENCY.get()` delay, bail if `!metronome.playing`
    or the timestamp changed (race with a new beat), exactly as
    `CircularClock` and `ConductorDisplay` do.
  - Call `track.vibrate(beat)`.
  - Update `currentMeasure = beat.measure`.
  - Resolve the wedge index for this beat as `beat.index` into the current
    wedge-state list.
  - Snap that wedge's `color` to `trackPalette.color`, `highlightAlpha` to a
    starting value (e.g. `0.5f`), and `strokeBoost` to a starting value (e.g.
    `ringSize * 0.5f`); then animate all three back down
    (`trackPalette.colorContainer`, `0f`, `0f`) using the same
    `MaterialTheme.motionScheme` spec `GridDisplayItem` uses
    (`slowSpatialSpec`/`slowEffectsSpec` as appropriate per type).
- `LaunchedEffect(track) { track.pauseEvents.collect { playing -> ... } }`:
  - When transitioning to not-playing, reset `currentMeasure = 0` and snap
    every wedge's `color`/`highlightAlpha`/`strokeBoost` back to inactive,
    matching `CircularClock`'s pause handling.

Drawing (`Canvas` inside the ring's `Box`):
- Compute `radius` and `center` the same way `CircularClock` does
  (`size.minDimension / 2 - ringSize / 2`, box center).
- For each wedge `i` in `0 until wedgeCount`:
  - `gapDegrees` = a small fixed angular gap (e.g. `4f`, consistent across all
    wedge counts — at high wedge counts this shrinks the visible arc length
    per wedge but stays visually distinct).
  - `sweep = 360f / wedgeCount - gapDegrees`
  - `start = -90f + i * (360f / wedgeCount) + gapDegrees / 2f`
  - `drawArc(color = wedgeColor[i].value, startAngle = start, sweepAngle = sweep, useCenter = false, style = Stroke(width = ringSize + strokeBoost[i].value))`
- Wedges are stroked arcs at a single radius (not center-anchored pie sectors),
  so two tracks can nest as concentric rings without overlapping — same
  geometry role as `CircularClock`'s progress arc, just segmented and
  per-segment animated instead of one continuous sweep.

## Display mode wiring

- `MetronomeViewModel.kt`: add `PIE` to the `DisplayMode` enum (after `GRID`,
  preserving existing ordinal order for `CLOCK`, `CONDUCTOR`, `GRID`).
- `MetronomeDisplay.kt`:
  - `when(displayMode)` gains `DisplayMode.PIE -> PieDisplay(viewModel, tracks)`.
  - `DisplaySelector`'s `modes` list gains
    `R.string.metronome_display_pie to R.drawable.outline_pie_chart_24`,
    appended after the Grid entry.
- `strings.xml`: add `<string name="metronome_display_pie">Pie</string>`
  next to the other `metronome_display_*` strings.
- `res/drawable/outline_pie_chart_24.xml`: new vector icon, sourced from the
  Material Symbols "pie_chart" outline glyph, matching the existing
  960×960-viewport outline style used by the other display-mode icons
  (`outline_timelapse_24`, `outline_person_24`, `outline_apps_24`).

## Edge cases

- **Single enabled track:** only the outer ring renders; identical condition
  to `CircularDisplay`'s `displayTracks.size > 1` check.
- **No enabled tracks:** `PieDisplay` returns early and renders nothing, same
  as `CircularDisplay` and `GridDisplay`.
- **Time signature / measure changes mid-play:** wedge list is rebuilt
  (`remember(currentMeasure, wedgeCount)`) whenever `currentMeasure` or the
  computed wedge count changes, so a tuplet-heavy or irregular measure simply
  produces a different wedge count on the next measure — no fallback/"not
  supported" UI is needed (unlike Conductor).
- **Pause/stop:** all wedges reset to inactive state; `currentMeasure` resets
  to `0`, matching `CircularClock`'s pause handling.
- **High beat counts:** the fixed angular gap means wedges shrink but remain
  individually visible down to reasonably high counts; no special-casing is
  planned since Conductor/Clock don't special-case this either.

## Testing

- Manual verification via the `run` skill: switch to Pie mode for tracks with
  simple meters (4/4), compound/odd meters (5/8, 7/8), and tuplets, with 1 and
  2 enabled tracks, confirming wedges light up in beat order, fade correctly,
  and reset on pause/stop.
- No new unit-testable logic beyond what's already covered by existing
  `MetronomeTrack`/`Rhythm` tests; this is a purely presentational addition.
