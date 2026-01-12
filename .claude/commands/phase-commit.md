# Phase Commit

Commit and push the current phase's work.

## Instructions

1. Read the build plan at `/docs/build-plan.md`
2. Ask me which phase number was just completed
3. Find that phase's title in the build plan
4. Run:

```bash
git add -A && git commit -m "Phase {PHASE_NUMBER}: {PHASE_TITLE}" && git push
```

## Example

If I say "21", find "Phase 21" in the build plan (e.g., "Timeline Polish") and run:

```bash
git add -A && git commit -m "Phase 21: Timeline Polish" && git push
```
