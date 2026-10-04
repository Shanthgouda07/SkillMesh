# SkillMesh

> **Offline-first Android app where nearby people find each other by skills, using on-device AI and Google Nearby Connections.** Built for the iQOO Hackathon 2026 (Community App track).

---

## 1. Product Overview

SkillMesh is a fully local, privacy-first mobile application that allows developers, designers, and hackers at hackathons, conferences, or campus events to discover collaborators without relying on internet connectivity, centralized servers, or user accounts.

- **On-Device LLM & Fallback AI**: Free-text biographies and search requirements are parsed locally into structured skills and experience levels using MediaPipe Tasks GenAI (Gemma). If the model file is not loaded, a deterministic 60+ token taxonomy and regex engine takes over seamlessly without crashes.
- **Transparent Scoring Engine**: Matches are evaluated with a documented, explainable formula:
  $$\text{Score} = 0.65 \times \text{Coverage} + 0.20 \times \text{Interests} + 0.10 \times \text{Availability} + 0.05 \times \text{Experience}$$
  Bidirectional partial credit is granted for related skills (e.g. Kotlin $\leftrightarrow$ Android).
- **Privacy-Preserving Nearby Discovery**: Utilizes Google Nearby Connections (`P2P_CLUSTER`) over Bluetooth and Wi-Fi. Profiles are never broadcast in plain air; discovery only reveals a short endpoint name, and profiles exchange strictly after mutual 6-digit cryptographic verification and user approval.
- **Project Rooms & Task Extraction**: Teams can create projects and automatically decompose required skills into milestone engineering tasks.

---

## 2. Architecture

```
ui/            Jetpack Compose (Material 3), Navigation, MVVM with StateFlow
    ├── home/          Status overview, quick radar search, demo mode toggle
    ├── discover/      Transparent match cards, score rings, explanation badges
    ├── projects/      Project rooms, AI task extraction, teammate suggestions
    ├── profile/       Local profile editor, AI skill analyzer, chip management
    ├── nearby/        Radar scanning animation, peer list, approval dialog
    └── components/    ScoreRing, SkillChip, StatusBadges, NearbyApprovalDialog
domain/        Pure Kotlin, zero Android dependencies, 100% unit-testable
    ├── Models         Profile, Requirement, MatchResult, Project, ProjectTask
    ├── SkillTaxonomy  60+ token canonical dictionary & bidirectional partial credit
    └── MatchEngine    Deterministic scoring heuristic & ranking
ai/            On-device intelligence layer with fallback resilience
    ├── LocalAIEngine  Interface for extraction, explanation, and task generation
    ├── MediaPipeEngine LlmInference wrapper with 12s timeout & local file loader
    ├── FallbackEngine Deterministic taxonomy & regex keyword extractor
    └── JsonSanitizer  Tolerant JSON parser with taxonomy canonicalization
nearby/        NearbyManager (Google Nearby Connections P2P_CLUSTER wrapper)
data/          Room Database, TypeConverters, DAOs, and Offline Repository
demo/          DemoData (10 seeded people, 3 projects, milestone tasks)
```

---

## 3. Two-Phone Demo Workflow (Airplane Mode)

1. **Activate Airplane Mode** on both phones.
2. Turn **Bluetooth** and **Wi-Fi** **ON** on both phones (no internet connection / no SIM card needed).
3. Open **SkillMesh** on both devices.
4. On **Phone A**:
   - Tap the **Radar** tab (or "Scan Nearby" on Home) and tap **Start Discovery**.
5. When Phone B is detected, an **Approval Dialog** pops up on both phones showing:
   - Peer display name
   - Mutual 6-digit confirmation token (e.g. `482910`)
6. Tap **Approve** on both phones.
   - The encrypted JSON profile payload is exchanged and cached into local Room storage.
   - The peer connection disconnects cleanly.
7. On **Phone A**, open **Discover**:
   - Enter: `"I need someone with Android and computer vision"`.
   - Tap **Search / AI Match**.
   - Phone B's profile appears with **90%+ match score**, green chips for **Android** and **Computer Vision**, and a clear explanation: `"Strong match: has Android and Computer Vision. Available and aligned with your team."`
8. Tap the candidate card $\rightarrow$ tap **Invite to Project Room**.

---

## 4. Single-Phone Hackathon Demo Mode

If you only have one device during presentation or evaluation:
- **Long-press** the **"SkillMesh"** title on the Home screen, or toggle **"Hackathon Demo Mode"** in the Profile screen.
- This immediately loads 10 realistic profiles (e.g. Ananya Sharma: Android + Computer Vision, Rahul Verma: Edge AI + IoT, Priya Patel: UI/UX, Devansh Joshi: Cybersecurity) and 3 active projects into Room.
- Searching for `"Android developer with computer vision"` immediately ranks **Ananya Sharma** first with 90%+ match score.

---

## 5. Setting up the On-Device Gemma Model

The app works out-of-the-box with `FallbackEngine`. To enable the on-device Gemma LLM:

1. Download a compatible quantized Gemma `.task` file (e.g., `gemma-2b-it-cpu-int4.task` or `gemma-2b-it-gpu-int4.task`).
2. Connect your device via USB with ADB debugging enabled.
3. Push the model to the app's external files directory:
```bash
adb shell "mkdir -p /sdcard/Android/data/com.example.skillmesh/files/models"
adb push gemma-2b-it-cpu-int4.task /sdcard/Android/data/com.example.skillmesh/files/models/gemma.task
```
4. Restart the app. The Home screen status badge will reflect **"AI: On-Device (Gemma)"**.
5. Every model inference has a 12-second timeout. If inference times out or fails, the app automatically falls back to `FallbackEngine` without crashing.

---

## 6. Build & Test Commands

### Run Unit Tests
```bash
./gradlew test
```
Tests pass for `MatchEngineTest`, `SkillTaxonomyTest`, and `JsonSanitizerTest`.

### Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install on Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
