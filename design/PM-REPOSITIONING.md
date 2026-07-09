# Tajly — PM Repositioning Map

How we reposition every feature for a premium experience **without changing any behavior**. This is the contract the implementation follows.

---

## 0. Backend-preservation contract (the non-negotiable)

**NEVER touched (zero changes):**
- `viewmodel/*` — no change to any public function signature or `StateFlow` exposed to the UI.
- `repository/*`, `network/*` — no change to any Firestore REST call, endpoint, field name, auth flow, RevenueCat call, or Gemini prompt/parser.
- `model/*` — no change to any domain/Firestore model.
- `data/UserSettings.kt` — existing keys/getters/setters unchanged. (New keys may be **added** only; additive, never renaming/removing.)
- `navigation/RootRoute`, Voyager `Screen` graph, `ViewModelStore` wiring.
- All `expect/actual` platform contracts (KtorClient, ImagePicker, VideoPlayer, StatusBar, RevenueCat init, DeepLink).
- `RevenueCatKeys`, `AppConfig`, Gemini keys.

**ONLY touched (additive/visual):**
- `ui/**` (screens & components) — same VM calls + same state, new pixels.
- `theme/**` — extend tokens (keep existing names + the `GreenAccent = GoldPrimary` alias so all 163 call-sites keep compiling).
- New files: `ui/components/glass/*`, `ui/components/foundation/*`, `theme/Tokens.kt`, `theme/Motion.kt`.
- `composeResources/` — add 3D assets, hero photos.
- `build.gradle.kts` — add ONLY Haze (version verified against CMP 1.9.3) + maybe compottie. No change to existing deps.

**Rule:** every reskinned screen renders the exact same ViewModel state it does today. If a screen reads `state.caloriesGoal`, it still reads `state.caloriesGoal`. We restyle the container, never the data.

**Stack reality (verified):** CMP 1.9.3, Kotlin 2.3.20, Material3, Voyager 1.1.0-beta03, RevenueCat KMP 3.0.0, min SDK 24, iOS X64/Arm64/SimArm64 static framework. Newer CMP = native `SharedTransitionLayout` available.

---

## 1. New information architecture (placement only — routes unchanged)

```
Splash ─ District-style draw-on logo
  └ Auth ─ glass sign-in over cinematic image
     └ Paywall ─ CREW-style (hero photo → dark, glass plans)
        └ Onboarding ─ pure-glass steps over imagery
           └ Main (4 tabs + Buddy FAB)
              ├ HOME  ── premium dashboard (now surfaces Calorie, Discover, Buddy card, Check-in)
              ├ TRAIN ── [Workouts · Meals · Challenges]  (sub-tabs unchanged)
              ├ COMMUNITY ── feed / post / profile / activity / find friends
              └ PROFILE ── user + gamification + SUBSCRIPTION card + settings + entries
```

**Buddy FAB** stays global (all tabs except Community), still `navigator.push(AvatarScreen(MANUAL))`.

**Fixes (orphans → reachable), placement only:**
- **Calorie Calculator** (today only reachable from Home) → keep Home entry + add a **Profile → "Calculator & body stats"** entry. (Dead `CaloriesTab` object stays unused, untouched.)
- **Discover** (built, fully functional, currently unreachable) → surface as a **Home "Discover" content rail** + a **Profile → Discover** entry. No 5th tab. (`LocalArticleViewModel` already provided in `MainScreen`.)
- **BuddyMotivationCard** + **DailyCheckInBanner** (built, unused) → wire into **Home**.

---

## 2. Per-feature repositioning

| Feature (files) | Backend it uses | PM repositioning (visual/placement only) | Safety |
|---|---|---|---|
| **Splash** | AuthVM.checkAuthState, SubscriptionRepo.checkPremiumAccess, UserSettings | District-style draw-on **TAJLY** logo (mix: clean wordmark + construction-line draw + fill) → same routing logic | same VM calls |
| **Auth** | AuthVM (signIn/up/reset), DeepLink email | Cinematic dark bg, glass card, gold CTA, glass forgot-password sheet; same fields/validation/states | same state machine |
| **Paywall** | SubscriptionVM (offerings/purchase/restore), RootRoute | **CREW-style**: hero photo melts to dark, frosted TAJLY badge, glass plan cards (annual best-value), gold CTA, referral/restore; **fix Terms no-op → open URL** | same VM, same RevenueCat |
| **PurchaseSuccess** | navigateAfterPremium | Gold Lottie celebration + success haptic → same route | additive |
| **Onboarding** | UserSettings (name/goal/level/days) + WorkoutVM.saveUserGoals | Pure-glass steps over imagery, 3D goal icons, gold selection, progress dots. **PM add:** optional age/weight/height/gender steps writing **new** UserSettings keys + feeding existing `CalorieViewModel.calculateCalories` so the calorie engine has inputs | additive keys only; reuses existing calc fn |
| **Home** (HomeVM) | userProfile, caloriesGoal, bmr, tdee, scheduledWorkoutsToday, upcomingWorkout, dailyTips + MealPlanVM active plan + completions | Premium dashboard: greeting, **gold rings/streak hero**, **Daily Check-in banner** (wire unused), bento stat tiles (colorful 3D), today's workout + meals, **Buddy motivation card** (wire unused), **Discover rail** (wire Article VM), wellness tips, glass nav + FAB | renders same state; only adds views that read already-available VMs |
| **Workouts** (WorkoutVM) | plans/exercises CRUD, setExerciseCompleted, recordWorkoutCompleted, challenge auto-progress | Active-logging = ghost previous values, **gold checkmark** completion, **gold rest-timer ring**, focus mode, big tabular stats; AI-generate premium loading; **completion celebration** (existing sheet → richer) | same completion + streak + challenge calls |
| **WorkoutScheduler** | UserSettings.workoutSchedules CRUD | Glass calendar, section-colored events, glass add/edit sheet | same local store ops |
| **Calorie + tracking** (CalorieVM) | calculateCalories (Gemini), weighIns/measurements, analyzeMeasurements | Glass inputs, gold result cards (BMR/TDEE/target, tabular), premium weight/measurement charts (gold line + range band), AI insight card; reachable from Home + Profile | same Gemini + UserSettings |
| **Meals + plans** (MealPlanVM) | generate/swap (Gemini), plans CRUD, toggleMealCompletion, challenge auto-progress | Glass macro ring card, section-colored slot sections + colorful 3D food icons, gold completion; premium stepped generation; detail/shopping reskin. Retire dead `MealViewModel` path (hide, not delete) | same VM calls |
| **Buddy / Avatar** (AvatarVM) | sendUserMessage (Gemini), triggers, daily check-in, avatar selection | Full-width **coach** replies (no bubble), gold mascot, **inline generative cards**, glass quick-reply chips + composer; selection carousel; **AI tag** | same VM; conversation still in-memory |
| **Community** (CommunityVM) | feed/likes/comments/follow/profile/activity/post + image upload | Glass filter chips, premium post cards, **one-tap kudos** w/ haptic, glass composer + 3D stat chips, activity w/ section-colored icons + gold unread badge; **implement no-op share** | same VM/repo |
| **Challenges** (ChallengesVM) | challenges/join/progress/leaderboard, auto-progress | Hero gold progress-ring card, glass lists, gold "Join", detail celebration, **3D medal badges (bronze→silver→gold, gold=apex)**, leaderboard rows | same VM; badge tiers are display-only |
| **Discover** (ArticleVM) | RSS fetch, 24h cache | Liquid-glass search (subtle color), hero+compact article cards w/ section tags, 3D empty state; **now reachable** | same VM/repo |
| **Profile + i18n** | UserSettings, LanguageVM, AuthVM (logout/delete), Subscription | Glass header + gamification (3D + gold), **subscription/premium status card (new, data exists)**, theme/lang/avatar, entries → Calculator/Discover/Scheduler/Plans; **fix hardcoded strings → StringKey (+EN/NL)** | additive string keys; same VM calls |

---

## 3. Image system (the "more images" ask)

- **Hero photography** (free-commercial, Pexels): Paywall, Auth, Onboarding, section empty-states, Profile header backdrop, celebration. Stored in `composeResources/files/photos/`.
- **3D objects** (CC0, colorful, white-bg stripped): category tiles, stats, achievements, empty states. `composeResources/drawable/`.
- **Gradient + grain** ambient layer behind content (code, no asset).
- Backend has **no image URLs** for meals/exercises → use 3D category icons + (optional) curated stock by category; never block on missing content images.
- A `RemoteImage` loader (Ktor `get` → `decodeImageBitmap`, the existing expect/actual) handles network images already used by community.

---

## 4. Logo decision (your note: "mix earlier + current")
Splash logo = **clean legible wordmark** (filled, refined) **+** the **construction-line draw-on + outline-then-fill choreography** from v2. Legible like v1, dramatic like District. In Compose: animate an `androidx.compose.ui.graphics.Path` via `PathMeasure` (smoother than the CSS stroke trick) + animated line strokes.

---

## 5. Implementation sequence (each step backend-safe)

1. **Phase 0a** — `theme/Tokens.kt` (extend, keep aliases), `theme/Motion.kt`, faux-glass component kit (`GlassCard`, `GoldButton`, `GlassChip`, `StatRing`, `BentoTile`, `GlassBottomBar`, `Numeral`, `SectionHeader`, `EmptyState`, `Celebration`). **No new deps.**
2. **Phase 0b** — verify Haze version for CMP 1.9.3 → add → upgrade glass to real blur (hybrid + faux fallback via `expect/actual rememberGlassTier`).
3. **Phase 0c** — import 3D assets + photos into `composeResources`, generate `Res` accessors.
4. **Phases 1–13** — reskin screens in priority order, **building + smoke-testing Android + iOS after each batch**.
5. Each PR-sized batch: confirm the screen still calls the same VM functions and renders the same state (diff review) → **0% functional change**.

---

## 6. Verification (every batch)
- `./gradlew :composeApp:compileKotlinIosSimulatorArm64` + `:composeApp:assembleDebug` (Android) must stay green.
- Run Android emulator + iOS Simulator; smoke-test the touched flow end-to-end (auth, paywall purchase path in sandbox, generate workout/meal, log set/meal, post, join challenge, chat) to confirm behavior identical.
- Glass perf: ≤3 live blur surfaces/screen; faux fallback on low-end Android.
