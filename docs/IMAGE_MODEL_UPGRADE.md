# Image Model Upgrade + Image-Prompt Fixes

This branch upgrades both the text and image OpenAI models and cleans up the
wardrobe image-generation prompt. **No database migration, no frontend contract
change, no API/output structure change** — extraction JSON, image output format
(`low` quality, 1024×1024, base64 PNG), and the code flow are all unchanged.

Read this before merging / promoting.

---

## 1. What changed

| File | Change | Type |
|---|---|---|
| `application.yaml` | text model `gpt-4o-mini` → **`gpt-4.1-mini`** | config |
| `ChatGptText2ImageService.java` | image model `gpt-image-1` → **`gpt-image-2`** (still `low`, 1024×1024) | code (hardcoded) |
| `WardrobeItemAutoExtractService.java` | 3 image-prompt fixes (text only) | code |

### Why
- **`gpt-image-1` is deprecating Oct 23, 2026** — moving to `gpt-image-2` is required, not optional.
- `gpt-image-2` at `low` 1024² costs **~$0.005/image vs ~$0.011** for `gpt-image-1` low — ~55% cheaper, with better prompt adherence / photorealism.
- `gpt-4.1-mini` is cheaper and higher quality than `gpt-4o-mini` for the vision calls.

### The 3 image-prompt fixes (no structure/output change)
1. **Color rendering bug** — the prompt was injecting raw Java object text
   (`a [DetectedWardrobeItemColor[name=Navy, code=#000080], ...]`). Now renders
   clean color names (`a Navy, White`).
2. **Self-contradiction removed** — template said *"Surreal-style product image …
   no surreal elements"*; replaced with *"Clean, photorealistic studio product photo …"*.
3. **Template consistency** — the shoes and generic templates now share identical
   wording, differing only in `side view` vs `front-facing`.

---

## 2. Actions required before prod

- [ ] **🔴 Confirm prod OpenAI access to `gpt-image-2`.** It is the flagship image
      model and may require **org verification / a specific access tier**. If the
      prod key can't call it, image generation breaks. (`gpt-4.1-mini` is GA.)
- [ ] **🟠 Visual QA on generated images.** Fix #2 deliberately changes the *look*
      of wardrobe product images (surreal → photorealistic studio). Data shape is
      identical; the style is new. Eyeball a few generated items.
- [ ] **No `CHATGPT_VERSION` env override** — if the deploy injects it, the YAML
      `gpt-4.1-mini` is silently overridden. (Image model is hardcoded — no override risk.)
- [ ] **Build on JDK 17** — existing requirement (Spring Boot 3.4, toolchain 17); nothing new.
- [ ] **Smoke-test wardrobe auto-extract end-to-end** — extraction JSON parses
      (unchanged) and image generation returns a valid image on `gpt-image-2`.

---

## 3. Rollback

Config / single-string revert, no schema or data impact:
- Text model: set `chatgpt.version: gpt-4o-mini`.
- Image model: revert the model string in `ChatGptText2ImageService` to `gpt-image-1`.
- Prompt fixes: revert the `WardrobeItemAutoExtractService` change.

---

## 4. Coordination note

This branch overlaps with **PR #1** (`feature/llm-gpt-4.1-mini-prompt-temperature`):
both set the text model to `gpt-4.1-mini` (same value → no conflict). **PR #1 also
carries the temperature config + additional prompt fixes that this branch does NOT
include.** Decide merge order and make sure PR #1's work is not lost.
