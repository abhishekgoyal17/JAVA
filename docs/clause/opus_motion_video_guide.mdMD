# The Complete Guide: Making Motion Videos with Claude Opus 5.5
### From "what even is this" to building your own render pipeline

---

## Part 1 — The Core Idea (read this first)

Every viral "Claude made this video" clip on Twitter/X uses the **same trick**, and once you understand it, none of it feels like magic anymore.

> **Claude Opus 5.5 does not generate video pixels.** There is no diffusion model, no Sora/Runway/Kling-style frame synthesis happening. Opus **writes code** — usually a webpage with Canvas/SVG animation, or a script using a graphics library. A **headless browser plays that code back frame by frame**, screenshotting each frame. **ffmpeg** glues those frames + audio into an MP4.

So the real pipeline is:

```
   Idea / brief
        │
        ▼
  Claude Opus 5.5 writes a "director's treatment" (storyboard)
        │
        ▼
  Claude Opus 5.5 writes the film as CODE
  (HTML + Canvas/SVG + JS, often p5.js or GSAP)
  each scene = a pure function of time → draw(ctx, t)
        │
        ▼
  Headless Chrome (via Playwright) renders frame i
  by calling seek(i / fps) — a virtual clock, not real time
        │
        ▼
  Each frame is screenshotted (PNG/JPEG)
        │
        ▼
  ffmpeg stitches frames + audio → final .mp4
```

**Why this is a big deal / why it looks so good:**
- Because everything is *code*, not statistically generated pixels, there's no face-warping, no flickering inconsistency, no "AI slop" — a character drawn in frame 1 is *exactly* the same character in frame 500, because it's literally the same function.
- Because every frame is deterministic (`seek(t)`), rendering can be parallelized across workers, resumed if interrupted, and *retimed* by editing data instead of "reshooting."
- What's new in Opus 5.5 specifically is that it can hold a **whole film's structure in its head** and write it as one coherent program — consistent characters across scenes, transitions that connect, timing locked to music/voice — in very few (often one) prompts. Earlier models could animate a bouncing logo; this one can direct short films.

---

## Part 2 — Basic: Do It Yourself With Zero Setup

You don't need Claude Code or any of the repos below to *try* this. You can do a rough version directly in a Claude chat with code execution / artifacts:

1. Ask Claude (Opus 5.5) to build an **HTML animation** — e.g. "Build a 15-second animated explainer of X using HTML5 Canvas, make it self-contained in one file."
2. Claude can render it as a live **Artifact** you can preview directly in the browser — you'll see it play back in real time.
3. To turn that into an actual **MP4 file** you can post as a Reel, you need to record it. Two low-effort options:
   - **Screen-record the artifact** playing (OBS, QuickTime, or your phone) — genuinely how a lot of people do their first test.
   - **Use browser dev tools' frame-by-frame capture** or a simple Puppeteer/Playwright script (see Part 3) to render it properly frame-exact instead of screen-capturing in real time (screen recording drops frames; scripted rendering doesn't).

This tier is good for: testing the *idea*, seeing if Claude's animation code is any good, quick throwaway visuals. It is **not** how the viral high-production clips are made — those use a proper render pipeline (Part 3–4).

---

## Part 3 — Intermediate: A Proper Local Render Pipeline

This is the setup nearly every serious example uses. You need:

| Tool | Why |
|---|---|
| **Claude Code** (terminal/desktop) | Lets Opus 5.5 write *and iterate on* the animation code directly against your file system |
| **Node.js ≥ 18 (ideally ≥ 22)** | Runs the render script |
| **Google Chrome / Chromium** | Headless rendering target |
| **Playwright** (`npm i playwright`) | Drives headless Chrome, injects the virtual clock |
| **ffmpeg** | Encodes frames + audio into the final video |
| A JS animation library (optional but common): **p5.js**, **p5.brush**, or **GSAP** | Makes writing scene code faster/cleaner than raw Canvas calls |

### The core technique: `seek(t)`

The single most important pattern across every serious repo in this space is writing your animation as a **pure function of time**, not a real-time loop:

```js
// Instead of a real-time requestAnimationFrame loop...
function draw(ctx, t) {
  // t = seconds since the film started
  // draw EXACTLY what the film should look like at time t
  // no hidden state, no "current position" variables that drift
}

// A tiny seek function the renderer calls per-frame:
window.seek = (t) => draw(ctx, t);
```

Because `draw` only depends on `t`, you can render frame 4000 without rendering frames 1–3999 first. That's what makes parallel, resumable, frame-exact rendering possible.

### The render script (conceptually — see PDoomVideo repo for the real one)

```bash
node render.mjs --frames=0:20 --workers=4   # paints every frame to out/frames/, resumable
node render.mjs --encode --out=out/final.mp4  # stitches frames + audio with ffmpeg
```

Under the hood, `render.mjs` typically:
1. Launches N parallel headless Chrome instances (Playwright)
2. Each loads your HTML page and calls `window.seek(frameIndex / fps)`
3. Screenshots the canvas → saves as `out/frames/00001.png`, etc.
4. Once all frames exist, calls ffmpeg to combine them with your audio track into an MP4

### Workflow with Claude Code

1. **Brief Claude**: describe the video (topic, length, tone, target platform — 9:16 for Reels/Shorts, 16:9 for YouTube).
2. **Let it plan first**: ask it to write a storyboard/treatment *before* writing code — this is what separates good output from chaotic output. (See "director's treatment" pattern in Part 5.)
3. **Approve the treatment**, then let it write the actual animation code.
4. **Render and review**: have it render a handful of test frames first (contact sheet / frame strip) rather than the whole video, to catch problems early — cheaper to fix a treatment than to re-render 20 seconds of video.
5. **Iterate in plain language** — "the text is too subtle," "slow down scene 2" — and let Claude treat each note as a code change.

---

## Part 4 — Advanced: What the Best Examples Are Doing

Once the basic pipeline works, the advanced techniques people are using to get the really polished results:

- **Audio as the master clock.** Instead of timing visuals arbitrarily, the audio track drives everything. Voiceover is transcribed with **Whisper** for word-level timestamps; music is analyzed for tempo/beat/onsets so cuts land exactly on the beat.
- **Multiple sound-source routes**: your own audio file, a YouTube link (fetched via `yt-dlp`), fully synthesized music/SFX via the **Web Audio API**, or AI voiceover (ElevenLabs / OpenAI TTS).
- **Parallel subagents for long films.** For anything more than ~30 seconds, one Opus instance plans the whole film and writes a style/animation guide, then spins up **subagents** that each build individual scenes/chapters against that shared guide — this is literally how the P(doom) music video's 9 chapters were built.
- **Self-review loop.** After rendering, Opus reads back its own output — contact sheets, frame-by-frame strips, even 1:1 crops of faces/hands — and fixes what's visually wrong before calling it done. This is a big part of why quality is so much higher than a single-shot generation.
- **3D when needed.** Most of this is 2D Canvas/SVG (fastest to get right), but three.js is used for cases needing real 3D — rigs, instancing, shadows, depth of field, exploded models.
- **WebGL for effects at scale**: film grain, chromatic aberration, motion blur (accumulating multiple sub-frames per output frame, e.g. 8 sub-frames across a simulated 180° shutter), thousands of particles/tiles via **instancing**.
- **A dedicated QA pass.** Before posting, decode the actual rendered MP4 (not the source code) and check for dropped/repeated/jumped frames — see the `jankscan` tool below.

---

## Part 5 — GitHub Repositories (in order of "start here" → "go deep")

### 1. [JohnHeibel/PDoomVideo](https://github.com/JohnHeibel/PDoomVideo) — ⭐ start here
The most transparent, fully-documented real example. Full source for a **156.6-second music video**, entirely built by Opus 5.5 in Claude Code across two generations.
- `studio.html` — the page every frame is painted into, using **p5.js + p5.brush**
- `render.mjs` — renders frames in headless Chrome, encodes with ffmpeg
- `STORYBOARD.md` — Opus's own shot-by-shot plan (written by the model itself)
- `ANIMATION_GUIDE.md` — the style/code guide Opus wrote to brief its own parallel subagents
- `src/ch/` — nine chapter files, `legacy/` — the first (weaker) generation, for comparison
- **To render it yourself:**
  ```bash
  npm install
  node render.mjs --frames=0:156.6 --workers=4   # paints frames, resumable
  node render.mjs --encode --out=out/pdoom.mp4   # combines frames + audio
  ```
- Bonus: the same author published [**JohnHeibel/ClaudeAnimationBase**](https://github.com/JohnHeibel/ClaudeAnimationBase) — a cleaner, more general-purpose starting template for making your *own* Claude-driven animations (built after learning from the P(doom) project).

### 2. [klsoen/opus-js-animations](https://github.com/klsoen/opus-js-animations) — the "try it yourself" tool
This is the one to actually **install and use**, not just read. It's a **Claude Code skill + plugin** that behaves like a film director rather than a code generator you have to prompt carefully:

**Install:**
```bash
# As a Claude Code plugin
/plugin marketplace add klsoen/opus-js-animations
/plugin install opus-js-animations@opus-js-animations

# Or as a personal skill
git clone https://github.com/klsoen/opus-js-animations
cp -r opus-js-animations/skills/opus-js-animations ~/.claude/skills/
```

**Then just ask for a film**, e.g.:
> *"Make a 20-second reel of this hadith recitation: [youtube link]"*
> *"An explainer on compound interest with a calm male voiceover (I have an ElevenLabs key)"*

**What it does — five conversations before writing a single frame:**
1. **Brief** — describes back what you asked for, measures any reference clip
2. **Sound** — asks where audio comes from: your file, a YouTube link (via `yt-dlp`), generated music (Web Audio), or AI voiceover (ElevenLabs/OpenAI)
3. **Listening** — analyzes length, loudness, pauses, tempo; transcribes/aligns voice with Whisper
4. **Direction** — asks about visual style, 2D vs 3D, format (9:16/16:9), on-screen text
5. **Treatment** — gives you a full director's overview; **nothing is built until you approve it**

After you approve, it builds, checks its own frames (purity tests, contact sheets, face/hand crops), then renders master + platform-specific copies. Revisions are plain-language notes ("let the pieces stay visible") that it treats as design changes.

**Requirements:** Claude Opus 5.5 in Claude Code, Node ≥ 22, Chrome/Chromium, ffmpeg, Python 3 + numpy (+ `openai-whisper` for voice alignment), `yt-dlp`, optionally an ElevenLabs/OpenAI key for TTS.

**Worth reading inside the repo:** `skills/opus-js-animations/references/` has genuinely excellent short docs on styles, camera/composition, shaders (skies, water, mist), rendering thousands of particles via WebGL2 instancing, and three.js usage — a good design reference even if you don't use the tool itself.

### 3. [SuhaasNv/claude-opus5.5-video](https://github.com/SuhaasNv/claude-opus5.5-video) — technical deep dive
Shows how far the "pure function of time" idea can be pushed:
- Every scene is `draw(ctx, t)` with **no hidden state** — frames render in any order, on any worker, which is also how the montage can "re-shoot" earlier scenes by just calling them with an old `t`
- **Real motion blur**: 8 sub-frames per output frame across a simulated 180° shutter, accumulated in a half-float WebGL2 target (7,200 sub-frames total for the full piece) with dithering to avoid banding
- A lens pass adds impact-driven chromatic aberration, barrel warp, flash, vignette, and per-frame film grain
- Cuts are locked to a 120 BPM grid, with the camera shutter timed so no frame straddles a cut

Good repo to study if you want cinematic polish, not just "an animation that plays."

### 4. [siyuanfeng636-cpu/agentic-motion-graphics](https://github.com/siyuanfeng636-cpu/agentic-motion-graphics) — packaged production engine
A full "no video editor needed" engine: HTML5/CSS3/SVG/GSAP for visuals, Whisper for word-level narration timestamps, Python-synthesized music beds and SFX (whooshes, risers) locked to a BPM grid, and headless frame-by-frame rendering via the **HyperFrames CLI**. Explicitly positioned as a Premiere/After Effects/CapCut replacement.

### 5. [diggerhq/shipvideo](https://github.com/diggerhq/shipvideo) — the productized version
Shows what this looks like as an actual **tool**, not a repo you hand-run: paste a URL or a prompt, get a 20–40s launch video. Opus 5.5 writes a single HTML "film," a serverless agent on **OpenComputer** renders it frame-by-frame in headless Chromium (Playwright, under an injected virtual clock controlling `requestAnimationFrame`/timers/`Date`/CSS animations via `__seek(t)`), pipes JPEG frames into `ffmpeg` (libx264, crf 18, yuv420p, faststart), and uploads the result to Vercel Blob. Rendering runs at roughly real time (a 30s film takes 30–40s to render). One-click deploy is included in the repo.

### 6. [stas4000/jankscan](https://github.com/stas4000/jankscan) — QA tool for your renders
Not a generator — a checker. Decodes your *actual rendered MP4* (not the source) and reports what a viewer would actually feel but a file inspector wouldn't catch:
- `held` — a frame repeated while everything around it is moving (dropped render)
- `jump` — a frame that moves ~2x as far as its neighbors (a skipped frame)
- `freeze` — the longest run of repeated frames inside motion

```bash
python3 jankscan.py video.mp4
python3 jankscan.py renders/          # scans every mp4/mov/webm/mkv in a folder
python3 jankscan.py video.mp4 --json
```
One Python file, MIT license, just needs the `ffmpeg` binary. Worth running on anything before you post it.

---

## Part 6 — Distilbook: A Different Category (worth knowing, not the same technique)

[**distilbook.com**](https://distilbook.com/) solves a related but different problem, and it's worth including in a reel as a **contrast point**, not lumped in with the code-rendering trend above.

**What it is:** turns a *document* (PDF, DOCX, TXT, article link, or a typed prompt) into a **narrated, hand-illustrated-style explainer video** — no coding involved at all, it's a finished SaaS product.

**How it works:**
1. Drop in a source (PDF/DOCX/TXT/image/link)
2. Pick a template + color palette
3. DistilBook's AI agent writes a **narration script you review and approve first** (this is the one human-in-the-loop checkpoint — narration locks once generation starts)
4. It draws **original artwork per scene** (not stock footage, not a synthetic presenter) and animates it stroke-by-stroke in sync with narration
5. You get an editable timeline to fine-tune text, visuals, and timing

**Specs:**
- 15 visual styles (isometric illustration, sketch notebook, Bauhaus poster, cut-paper collage, inkwash journal, editorial flat vector, etc.), each suited to different content types (technical explainers, compliance training, onboarding, book summaries)
- Outputs **16:9 or 9:16** — directly usable for Reels/Shorts/TikTok as well as course/training content
- Your logo + brand colors on every paid plan
- Narration in 20+ languages
- Export up to 1440p MP4
- Pricing: 30 free credits on signup (≈ one 3-slide video, no card required), then $15/$35/$75/month for 170/450/1,000 credits; PDFs capped at 10/30/999 pages depending on plan
- Typical timeline: script ready in a couple of minutes, first narrated slide in ~6 minutes, a full 8–10 slide video in ~35 minutes

**The key distinction for your reel:** DistilBook is a **packaged product** for turning existing documents into explainer videos — you're not writing or reviewing any code, and you don't control the render pipeline. The Opus 5.5 trend above is **raw model capability** — people using Claude Code directly to have the model *write the entire render pipeline itself* from scratch, with full control over every frame. One is "give me a video from my content," the other is "give me full creative + technical control and I'll build the machine that makes the video."

---

## Part 7 — Suggested Reel Structure

Given the systems/engineering angle that fits your explainer format:

**Hook:** *"AI video didn't get better — it stopped being AI video."*

1. **The reveal** — show a viral clip, then reveal: no diffusion model, it's rendered from *code*.
2. **The pipeline, visually** — Claude writes JS → headless Chrome plays it back frame-by-frame → ffmpeg stitches it. (This maps naturally onto a systems diagram, which fits your usual format.)
3. **Why it doesn't look like typical "AI slop"** — because it's deterministic code, not statistically sampled pixels, so characters/brand elements stay perfectly consistent frame to frame.
4. **Show the receipts** — pull frames/GIFs from PDoomVideo's actual public repo as proof, not just a rendered final clip.
5. **CTA** — "here's the repo, here's the plugin, go try it" (link `klsoen/opus-js-animations` since it's the most installable one).

---

## Quick Reference — All Links

| Resource | Type | Link |
|---|---|---|
| PDoomVideo | Full example, MIT-adjacent, most documented | https://github.com/JohnHeibel/PDoomVideo |
| ClaudeAnimationBase | Clean starter template | https://github.com/JohnHeibel/ClaudeAnimationBase |
| opus-js-animations | Installable Claude Code skill/plugin | https://github.com/klsoen/opus-js-animations |
| claude-opus5.5-video | Advanced technique reference (motion blur, WebGL2) | https://github.com/SuhaasNv/claude-opus5.5-video |
| agentic-motion-graphics | Packaged production engine (Whisper, GSAP, HyperFrames) | https://github.com/siyuanfeng636-cpu/agentic-motion-graphics |
| shipvideo | Productized one-click tool | https://github.com/diggerhq/shipvideo |
| jankscan | Post-render QA / stutter detection | https://github.com/stas4000/jankscan |
| DistilBook | Document → explainer video SaaS (contrast point) | https://distilbook.com/ |

### Core tools you'll need regardless of which repo you start from
- Claude Code (terminal, desktop, or VS Code/JetBrains extension) with Opus 5.5
- Node.js ≥ 22
- Google Chrome or Chromium (Playwright can install its own if needed)
- ffmpeg
- Python 3 + numpy (for audio analysis / Whisper alignment, if doing voice-synced work)
- `yt-dlp` (only if pulling reference audio from YouTube)
