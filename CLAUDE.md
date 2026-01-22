---

## Slot Machine Countdown Component

**CRITICAL DIRECTION RULE**: The numbers always go from LEFT to RIGHT, lowest to highest.
- Lowest numbers on the LEFT (0, 1, 2...)
- Highest numbers on the RIGHT (...98, 99, TBD)
- TBD is at position 100 (far RIGHT, highest position)
- When countdown decreases, numbers scroll LEFT (revealing lower numbers)
- When value is nil or invalid, animate to TBD (scroll RIGHT to position 100)

---

## Documentation First

**ALWAYS check official documentation before implementing solutions for third-party libraries and frameworks.**

- Do NOT assume how APIs work based on naming or intuition
- Do NOT try multiple guessed approaches - look up the correct solution first
- Use Context7 or web search to find official documentation
- This applies to: Retrofit, Room, Hilt, Coil, any Gradle dependencies, Android/Jetpack APIs, etc.

---

## Bug Fixing Workflow

**For small bugs** (typos, simple logic errors, obvious fixes): Just fix them.

**For big issues** (architectural changes, changes that affect multiple components, non-obvious fixes):
1. **Explain what's wrong** - describe the issue clearly so the user understands it
2. **Propose a fix** - explain what you're planning to do and why
3. **Wait for approval** - do NOT touch any code until the user approves the approach

If unsure whether something is a small bug or big issue, ask first.

---

## Implementation Plan Requirement

**For any new feature or significant implementation**, ALWAYS provide an implementation plan BEFORE writing any code. The plan should include:

1. **Files to Create** - List all new files with their purpose
2. **Files to Modify** - List existing files that need changes
3. **Data/State Management** - How state will be stored and shared
4. **Navigation Flow** - How screens connect and pass data
5. **Key Components** - Reusable components to build or leverage
6. **Implementation Order** - Numbered steps in dependency order

Wait for user approval of the plan before proceeding with implementation.

---

## Phase Execution Workflow

**At the start of each build plan phase:**

1. **Present a summary** - Explain what the phase covers and its goals
2. **Provide an implementation plan** with:
   - Files to create (with purpose)
   - Files to modify
   - Implementation order (numbered steps in dependency order)
   - Any decisions or clarifications needed
3. **Wait for approval** - Do NOT begin coding until the user approves the plan
4. **Execute autonomously** - Once approved, implement the phase without asking for confirmation on individual steps
5. **End with phase completion** - Print "PHASE COMPLETE — READY FOR REVIEW" and play notification sound

---

## Countdown2Binge Project Rules (Android)

- The product specification in `/docs` is the source of truth for all product behavior.
- Do not invent features or flows not described in the spec.
- Android API 24+ only.
- Kotlin + Jetpack Compose only.
- Room for persistence, Coroutines + Flow for async.
- Core logic and tests must exist before UI work begins.

---

## Deprecated APIs to Avoid

**Do NOT use deprecated Android APIs.** Common ones to avoid:
- `AsyncTask` - use Kotlin Coroutines instead
- `LocalBroadcastManager` - use Flow or LiveData instead
- `startActivityForResult` - use Activity Result APIs instead
- `onActivityResult` - use registerForActivityResult instead

---

When making code changes, ALWAYS run `./gradlew build` or `./gradlew assembleDebug` unless explicitly instructed to skip testing. This applies when implementing a feature or fix, modifying data layer, ViewModels, shared logic, or making multi-screen updates, or when the user says things like "test this" or "let's see if it works." Do NOT run builds for single-file UI tweaks (use Compose Previews instead), when actively iterating on the same file, or when changes are limited to comments, formatting, or naming. Rely on compiler output and test results only. Do not modify signing, provisioning, credentials, or upload builds; this tool is execution-only and should surface errors clearly and immediately.

When a phase completes successfully:
- Print the line exactly: PHASE COMPLETE — READY FOR REVIEW
- Run: `afplay /System/Library/Sounds/Glass.aiff && osascript -e 'display notification "Ready for review" with title "Phase Complete"'`
- Do not begin the next phase automatically

Always use the design skill when changing the UI

After completing a task that involves tool use, provide a quick summary of the work you've done

<execution_mode>
When a product specification or build plan exists, treat it as explicit authorization to implement tasks described within it.

Do not ask for confirmation for individual files or steps that are clearly defined by:
- The product specification
- The approved build plan
- The current active phase

Pause and ask for clarification only when:
- The specification is ambiguous or contradictory
- A decision would alter architecture or product behavior
- A phase boundary has been reached

Proceed autonomously within a phase. Stop only at phase completion or when blocked.
</execution_mode>
