# Image Generation Upgrade (gpt-image-2) + Image-Prompt Cleanup

Branched off latest `main` (after PR #1 was merged). This branch adds **only the
image-generation pieces** — the text/vision models and temperature work are
already in `main`.

**No database migration, no frontend contract change, no API/output structure
change.** Image output stays `low` quality, 1024×1024, base64 PNG; the extraction
JSON and code flow are unchanged.

---

## 1. Scope of this branch

| Area | Model / change | In this branch? |
|---|---|---|
| Wardrobe **detail extraction** (auto + manual) | `gpt-4.1-mini` | ❌ already in `main` (PR #1) — unchanged |
| Single-image feedback + comparison | `gpt-4.1-mini` + temperature | ❌ already in `main` (PR #1) — unchanged |
| **Image generation** (product shots) | `gpt-image-1` → **`gpt-image-2`** | ✅ **this branch** |
| Image-generation **prompt** | 3 text-only fixes | ✅ **this branch** |

## 2. What changed (2 files)

### `ChatGptText2ImageService.java`
- Image model `gpt-image-1` → **`gpt-image-2`** (still `low`, 1024×1024, b64 PNG).
- Why: `gpt-image-1` deprecates **2026-10-23**; `gpt-image-2` at `low`/1024² is
  **~55% cheaper** (~$0.005 vs ~$0.011/image) with better prompt adherence.

### `WardrobeItemAutoExtractService.java` — `generateImageGenerationPrompts()` (prompt text only)
1. **Color rendering bug fixed** — the prompt was injecting raw Java object text
   (`a [DetectedWardrobeItemColor[name=Navy, code=#000080], ...]`); now renders
   clean names (`a Navy, White`).
2. **Self-contradiction removed** — `"Surreal-style product image ... no surreal
   elements"` → `"Clean, photorealistic studio product photo ..."`.
3. **Template consistency** — shoes & generic templates now share identical wording,
   differing only in `side view` vs `front-facing`.

## 3. Actions required before prod

- [ ] **🔴 Confirm prod OpenAI access to `gpt-image-2`.** It may require org
      verification / a specific access tier. If the prod key can't call it, image
      generation breaks.
- [ ] **🟠 Visual QA.** Fix #2 deliberately changes the *look* of generated wardrobe
      images (surreal → photorealistic studio). Data shape is identical; the style
      is new. Eyeball a few items.
- [ ] **Smoke-test wardrobe auto-extract end-to-end** — extraction JSON parses
      (unchanged) and image generation returns a valid image on `gpt-image-2`.
- [ ] **Build on JDK 17** — existing requirement; nothing new.

## 4. Rollback

Single-string revert, no schema/data impact:
- Revert the image model in `ChatGptText2ImageService` to `gpt-image-1`.
- Revert the `WardrobeItemAutoExtractService` prompt change if needed.
