# Empty Cards Timeline Design (Android)

This document covers the empty placeholder cards shown on the Timeline when the user has no followed shows (onboarding state).

---

## When Empty Cards Appear

Empty cards are shown **only during onboarding** when the user has no followed shows. They demonstrate what the Timeline will look like once shows are followed.

---

## Two Card Types

| State | Card Type | Dimensions |
|-------|-----------|------------|
| **Expanded** | Landscape (EmptySlotCard) | Width: fills available, Height: 175dp fixed |
| **Collapsed** | Portrait (EmptyPortraitCard) | Width: 230dp, Height: 310dp |

---

## Sizing Strategy: Fixed Height, Not Aspect Ratio

To prevent cards from becoming square, use **fixed dimensions** instead of aspect ratios:

```kotlin
// CORRECT - Fixed height keeps landscape shape
Modifier
    .fillMaxWidth()      // Flexible width
    .height(175.dp)      // Locked height

// WRONG - Aspect ratio can cause issues
Modifier
    .aspectRatio(16f/9f) // May not match actual show cards
```

---

## Color Palette

### Premiering Soon Style
| Element | Color |
|---------|-------|
| Accent (border, text) | `#73E6B3` at 40% opacity → `#73E6B366` |
| Border opacity | 50% of accent |
| Countdown text | White |
| Countdown label | White 70% |

### Anticipated (TBD) Style
| Element | Color |
|---------|-------|
| Accent (border, text) | `#4D4D4D` (30% white) |
| Border opacity | 50% of accent |
| Countdown text | Accent color |
| Countdown label | Accent 70% |

### Common Colors
| Element | Color |
|---------|-------|
| "EMPTY" / "EMPTY SLOTS" text | `#595959` (35% white) |
| Background | Black (`#000000`) |

---

## Expanded State: Landscape Card (EmptySlotCard)

### Layout
```
┌────────────────────────────────────────────────────────────────┐
│  ┌──────┐  ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐   │
│  │  --  │  │                                              │   │
│  │ DAYS │  │            EMPTY SLOTS                       │   │
│  │      │  │                                              │   │
│  └──────┘  └─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘   │
│     ↑                      ↑                              ↑   │
│   80dp                fills width                       24dp  │
│                       height: 175dp                    trailing│
└────────────────────────────────────────────────────────────────┘
```

### Specifications

| Element | Value |
|---------|-------|
| **Total card height** | 190dp (container) |
| **Countdown area width** | 80dp |
| **Countdown vertical padding** | 8dp |
| **Countdown horizontal padding** | 12dp |
| **Placeholder area height** | 175dp (FIXED) |
| **Placeholder area width** | Fills available (`maxWidth: .infinity`) |
| **Corner radius** | 24dp |
| **Border style** | Dashed (6dp dash, 4dp gap) |
| **Border width** | 1dp |
| **Trailing padding** | 24dp |

### Countdown Text

| Style | Text | Label | Font Size |
|-------|------|-------|-----------|
| Premiering | `--` | `DAYS` | 36sp |
| Anticipated | `TBD` | `DATE` | 24sp |

### Typography
| Element | Font |
|---------|------|
| Countdown number | System Heavy Rounded, 36sp (or 24sp for "TBD") |
| Countdown label | System Heavy, 9sp, tracking 1sp |
| "EMPTY SLOTS" text | System Medium, 11sp, tracking 1.5sp |

---

## Collapsed State: Portrait Card (EmptyPortraitCard)

### Layout
```
┌────────────────────────────────────────────────────────────────┐
│  ┌──────┐                              ┌─ ─ ─ ─ ─ ─ ─ ─ ─┐    │
│  │  --  │                              │                 │    │
│  │ DAYS │                              │                 │    │
│  │      │                              │     EMPTY       │    │
│  └──────┘                              │                 │    │
│     ↑                                  │                 │    │
│   80dp                                 └─ ─ ─ ─ ─ ─ ─ ─ ─┘    │
│                                              ↑            ↑   │
│                                         230 x 310dp     24dp  │
└────────────────────────────────────────────────────────────────┘
```

### Specifications

| Element | Value |
|---------|-------|
| **Total card height** | 320dp (container) |
| **Countdown area width** | 80dp |
| **Poster placeholder width** | 230dp (FIXED) |
| **Poster placeholder height** | 310dp (FIXED) |
| **Corner radius** | 15dp |
| **Border style** | Dashed (6dp dash, 4dp gap) |
| **Border width** | 1dp |
| **Trailing padding** | 24dp |
| **Poster alignment** | Right (end) |

### Typography
| Element | Font |
|---------|------|
| Countdown number | System Heavy Rounded, 36sp (or 24sp for "TBD") |
| Countdown label | System Heavy, 9sp, tracking 1sp |
| "EMPTY" text | System Medium, 11sp, tracking 1.5sp |

---

## Section Layout

### Expanded Section (3 cards)
```kotlin
Column(verticalArrangement = Arrangement.spacedBy(30.dp)) {
    repeat(3) { index ->
        EmptySlotCard(
            style = style,
            modifier = Modifier.height(190.dp)
        )
    }
}
```

### Collapsed Section (3 cards)
```kotlin
Column(verticalArrangement = Arrangement.spacedBy(30.dp)) {
    repeat(3) { index ->
        EmptyPortraitCard(
            style = style,
            modifier = Modifier.height(320.dp)
        )
    }
}
```

---

## Android Implementation

### Landscape Empty Card (Expanded)

```kotlin
@Composable
fun EmptySlotCard(
    style: TimelineCardStyle,
    modifier: Modifier = Modifier
) {
    val accentColor = when (style) {
        TimelineCardStyle.Premiering -> Color(0x6673E6B3) // 40% opacity
        TimelineCardStyle.Anticipated -> Color(0xFF4D4D4D)
    }

    val (countdownText, countdownLabel) = when (style) {
        TimelineCardStyle.Premiering -> "--" to "DAYS"
        TimelineCardStyle.Anticipated -> "TBD" to "DATE"
    }

    Row(
        modifier = modifier.padding(end = 24.dp)
    ) {
        // Countdown area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(80.dp)
                .background(Color.Black)
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Text(
                text = countdownText,
                style = TextStyle(
                    fontSize = if (countdownText == "TBD") 24.sp else 36.sp,
                    fontWeight = FontWeight.Black,
                    // Use rounded font if available
                ),
                color = if (style == TimelineCardStyle.Premiering) Color.White else accentColor
            )
            Text(
                text = countdownLabel,
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = if (style == TimelineCardStyle.Premiering)
                    Color.White.copy(alpha = 0.7f) else accentColor.copy(alpha = 0.7f)
            )
        }

        // Placeholder area - FIXED HEIGHT
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)  // FIXED - prevents square
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp),
                    // Dashed: use PathEffect.dashPathEffect(floatArrayOf(6.dp, 4.dp))
                )
        ) {
            Text(
                text = "EMPTY SLOTS",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                ),
                color = Color(0xFF595959)
            )
        }
    }
}
```

### Portrait Empty Card (Collapsed)

```kotlin
@Composable
fun EmptyPortraitCard(
    style: TimelineCardStyle,
    modifier: Modifier = Modifier
) {
    val accentColor = when (style) {
        TimelineCardStyle.Premiering -> Color(0x6673E6B3)
        TimelineCardStyle.Anticipated -> Color(0xFF4D4D4D)
    }

    val (countdownText, countdownLabel) = when (style) {
        TimelineCardStyle.Premiering -> "--" to "DAYS"
        TimelineCardStyle.Anticipated -> "TBD" to "DATE"
    }

    Row(
        modifier = modifier.padding(end = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Countdown area (left)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(80.dp)
                .background(Color.Black)
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            Text(
                text = countdownText,
                style = TextStyle(
                    fontSize = if (countdownText == "TBD") 24.sp else 36.sp,
                    fontWeight = FontWeight.Black,
                ),
                color = if (style == TimelineCardStyle.Anticipated) accentColor else Color.White
            )
            Text(
                text = countdownLabel,
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = if (style == TimelineCardStyle.Anticipated)
                    accentColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Portrait placeholder (right) - FIXED DIMENSIONS
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(230.dp)   // FIXED
                .height(310.dp)  // FIXED
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(15.dp),
                    // Dashed border
                )
        ) {
            Text(
                text = "EMPTY",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                ),
                color = Color(0xFF595959)
            )
        }
    }
}
```

---

## Dashed Border in Compose

```kotlin
// Create dashed border using DrawScope
fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: Shape,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
) = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply { addOutline(outline) }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx())
            )
        )
    )
}
```

---

## Key Takeaway: Why Fixed Height Works

```kotlin
// Landscape card sizing
Modifier
    .fillMaxWidth()    // Width adapts to screen
    .height(175.dp)    // Height LOCKED → always landscape

// Portrait card sizing
Modifier
    .width(230.dp)     // Width LOCKED
    .height(310.dp)    // Height LOCKED → always portrait (2:3 ratio)
```

By fixing the height (landscape) or both dimensions (portrait), the cards maintain their intended shape regardless of screen size. This matches the actual show cards exactly.
