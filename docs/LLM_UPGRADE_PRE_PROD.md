# LLM Upgrade — Pre-Prod Checklist

This branch upgrades the OpenAI model, tunes the AI prompts, and makes the
generation temperature configurable. **No database migration and no frontend
contract change** — the JSON returned to the app is byte-identical in shape.

Read this before merging to `main` / promoting to production.

---

## 1. What changed

### Model
- `chatgpt.version`: **`gpt-4o-mini` → `gpt-4.1-mini`** (in base `application.yaml`, so it applies to **all** profiles: dev / test / production).
- Why: for the image-heavy vision calls, `gpt-4.1-mini` is both **higher quality** and **~40% cheaper per call** than `gpt-4o-mini` (4o-mini encodes a 1024×1024 image as ~25,500 tokens vs ~1,660 for 4.1-mini).

### Temperature (new, configurable)
Added `chatgpt.temperature.*` and wired it per task type:

| Operation | Temperature | Reason |
|---|---|---|
| Single-image feedback | `feedback` = **0.5** | Warm, natural voice while respecting strict word limits |
| Outfit comparison | `feedback` = **0.5** | Same |
| Wardrobe auto-extract | `extraction` = **0.0** | Deterministic, reliable JSON + labels |
| Wardrobe manual-extract | `extraction` = **0.0** | Same |

Code services read these via `@Value` **with inline defaults** (`:0.5` / `:0.0`), so the app still starts even if the keys are ever missing from a config source.

### Prompt fixes (quality only — output keys unchanged)
- **Single-image** (`SingleImageSubmissionPrompt`): clear goal/method, fixed the "10 sections vs 8" miscount, removed the ghost `<weather>` variable (now inferred from location + date), added explicit `compliment` guidance.
- **Comparison** (`ComparisonPrompt`): added the two sections for `areasForImprovement` and `finalCompliment` — these fields are consumed by the backend but previously had no instruction, so **they were always null**. They will now populate.
- **Manual-extract** (`WardrobeItemManualExtractionPrompt`): replaced an unrendered Python f-string (`{', '.join(self.big_labels)}`) with the real category list, fixed copy-paste labels, unified season vocab (`Fall` → `Autumn`), fixed double-brace JSON.

---

## 2. Build requirement (unchanged — not new to this branch)

This service **already** requires **JDK 17** (Spring Boot 3.4, Gradle toolchain pinned to 17, records + text blocks). This branch does **not** change that.

```bash
# CI / local build
export JAVA_HOME=/path/to/jdk-17
./gradlew clean build
```

If a build agent only has Java 8/11, it could never have built this repo — install JDK 17 (Eclipse Temurin or Microsoft OpenJDK).

---

## 3. Pre-prod checklist

- [ ] **Build passes on JDK 17** — `./gradlew clean build` (CI).
- [ ] **OpenAI access to `gpt-4.1-mini`** — confirm the production API key/account can call `gpt-4.1-mini`. (GA model; normally yes.)
- [ ] **No env-var override of the model** — if the deploy injects `CHATGPT_VERSION` (Spring relaxed binding for `chatgpt.version`), the YAML change is silently overridden and prod stays on `gpt-4o-mini`. Remove/update it.
- [ ] **Temperature config present** — base `application.yaml` supplies `chatgpt.temperature.feedback` / `.extraction`; inline `@Value` defaults cover any gap. Override per environment only if desired.
- [ ] **Smoke test each AI path on a staging build:**
  - [ ] Single-image feedback → valid JSON, all sections populated, word limits respected.
  - [ ] Outfit comparison → `areasForImprovement` and `finalCompliment` now **non-null** (expected new behavior).
  - [ ] Wardrobe auto-extract → items detected, valid JSON.
  - [ ] Wardrobe manual-extract → single-item validation works, valid JSON.
- [ ] **Cost/latency check** — sample a few hundred calls; confirm spend is in line with the ~40% per-call reduction and latency is acceptable.
- [ ] **Frontend regression** — confirm feedback screens render unchanged (the `OutfitAnalysis` / `ComparisonAnalysis` JSON shape is identical; only previously-null comparison fields now carry text).

---

## 4. Rollback

Low-risk, fully config-driven:
- **Revert the model:** set `chatgpt.version: gpt-4o-mini` (config or `CHATGPT_VERSION` env var) and redeploy — no code change needed.
- **Revert temperatures:** adjust `chatgpt.temperature.*` in config.
- **Full revert:** revert the branch merge; no schema/data changes were introduced.

---

## 5. Security note (pre-existing, not introduced here)

`src/main/resources/application.yaml` already contains plaintext third-party keys
(e.g. the weather `api-key`) committed before this branch. This change does not
add secrets, but it's worth moving those into the existing secrets mechanism
(the OpenAI key already comes from `secretProperties`, not YAML).
