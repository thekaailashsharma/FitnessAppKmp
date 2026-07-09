# Tajly — Premium UX Overhaul TODO

**Goal:** Re-skin and re-think the entire app to feel District/CREW-premium while keeping the **gold** theme.
**Locked:** keep gold (#C9A84C family) · deepen warm darks · add District-style section colors · real glass via **Haze** + faux fallback · lots of 3D · local-HTML mockups first, then Kotlin.
**Principles:** quiet canvas / loud objects · gold = "earned" only (~10%) · one reactive glass material · motion as physics (<300ms, springs, stagger) · big tabular numerals · rationed delight.

Legend: `[ ]` todo · `[~]` in progress · `[x]` done. Each screen ends with a **Definition of done**.

---

## PHASE 0 — Foundations & infrastructure (do first; everything depends on it)

- [ ] **0.1 Tokens in `theme/Color.kt` + new `theme/Tokens.kt`**: warm near-black surface ladder (bg `#0A0908`, s0–s4), text tiers (#F5F0E8/#A9A294/#6E685C), gold ramp (keep existing) + metallic gradient brush, District section-accent palette (violet/green/blue/coral/teal/pink), gold-glow shadow tokens.
- [ ] **0.2 Typography `theme/Type.kt`**: display weights + negative tracking; add a **tabular/lining-figures** text style for all numerals; hero-number style (big) + quiet-unit style.
- [ ] **0.3 Add Haze** (`dev.chrisbanes.haze:haze:1.7.2` + `haze-materials`) to `commonMain`; verify CMP version (bump to 1.8+ if shared-element transitions wanted, else add skydoves/Orbital).
- [ ] **0.4 `GlassSurface` composable** (commonMain) + `FauxGlassSurface` + `rememberGlassTier()` expect/actual (Android: API≥31 && !lowRam → LIVE; iOS: LIVE). One `HazeState` per screen scaffold.
- [ ] **0.5 Motion utils**: standard spring specs, `pressScale` modifier (.96/130ms), staggered-entrance helper, count-up numeral animation, reduced-motion guard.
- [ ] **0.6 3D asset pipeline**: move curated PNGs from `design/assets/` into `composeResources/drawable/` (namespaced e.g. `ic3d_fire`), generate `Res` accessors; build a `Asset3D` glow-tile composable.
- [ ] **0.7 Haptics**: expect/actual `haptic(type)` (light/medium/success); wire to press, set-complete, milestone.
- [ ] **0.8 Add compottie** (`io.github.alexzhirkevich:compottie:2.2.4`) for milestone Lottie bursts; create a `Celebration` overlay (gold confetti, fired once).
- [ ] **0.9 Core component kit** (rebuild existing `ui/components`): `GoldButton` (metallic gradient + glow + pressScale), `GlassCard`, `BentoTile` (section-tinted radial glow + 3D icon), `StatRing` (conic gold, tonal track), `GlassTopBar`/`GlassBottomBar` (progressive blur), `GlassChip`, `Numeral`, `SectionHeader`, `GlassBottomSheet`, `EmptyState` (3D-asset hero), `SkeletonShimmer`.
- [ ] **0.10 Film-grain + aurora background** composable (subtle gold/section radial glows + noise overlay) used app-wide behind content.
- **DoD:** a sample screen renders with glass bars, gold button, a bento tile, a stat ring, grain — at 60fps on a mid device.

---

## PHASE 1 — Splash  (`SplashScreen.kt`)
- [ ] Warm near-black + gold radial glow + grain.
- [ ] Gold metallic wordmark with **handwriting/shimmer reveal** (keep existing TajlyHandwriting, upgrade to gold gradient + specular sweep); 3D bicep/logo mark with scale-in spring.
- [ ] 3 pulse dots while auth/subscription check runs; premium retry state (glass card) instead of plain text.
- **DoD:** sub-2s, no jank, routes correctly (Auth/Paywall/Onboarding/Main).

## PHASE 2 — Auth  (`AuthScreen.kt`)
- [ ] Cinematic dark bg (gradient + grain), glass sign-in card floating.
- [ ] `GlassTextField` (focus glow, gold caret), `GoldButton` primary, ghost-glass secondary.
- [ ] Sign-in/up segmented toggle (glass, gold active), inline animated error banner (coral), forgot-password glass bottom sheet.
- [ ] Microcopy warm; loading = button spinner; success → existing routing.
- **DoD:** all states (error/loading/reset) styled; validation visuals on gold/coral.

## PHASE 3 — Paywall + Purchase Success  (`PaywallScreen.kt`, `PurchaseSuccessScreen.kt`)
- [ ] **CREW-style**: cinematic top (gradient + 3D hero objects: trophy/crown/sparkles/gem) **melting into dark** via gradient mask + grain; frosted gold "TAJLY PREMIUM" badge on the seam.
- [ ] Headline light-weight + restrained; feature rows with small 3D icons (AI coach, meals, challenges, insights).
- [ ] Glass plan cards (Annual = gold-rimmed "best value" + strike-through old price; Monthly); gold CTA "Start 7-day free trial"; ghost "Enter referral code"; restore/terms demoted.
- [ ] Auto-dismiss error/success states; keep EN/NL toggle.
- [ ] **PurchaseSuccess**: gold celebration (Lottie burst + success haptic), "PREMIUM UNLOCKED" badge, then route via `navigateAfterPremium()`.
- [ ] **Fix terms link** (currently no-op) → open URL.
- **DoD:** feels like a club, not a transaction; gold reserved for badge + CTA + best-value.

## PHASE 4 — Onboarding + Goals  (`OnboardingScreen.kt`, `UserFitnessGoalsScreen.kt`)
- [ ] Re-skin 5 steps: glass cards, 3D icon per goal/level, gold selection state + checkmark, progress as gold dots/bar with spring.
- [ ] Pre-permission "why" framing; warm copy; staggered option entrance.
- [ ] Day-count step: big tabular numeral + gold accent; commitment message.
- [ ] (Optional) surface the unused "specific requirements" field; consider folding age/weight/height here for plan quality.
- **DoD:** each step delights on select (haptic + scale), completion triggers plan-gen with a graceful loading state.

## PHASE 5 — Home dashboard  (`home/HomeScreen.kt`, `HomeViewModel`)
- [ ] Greeting + avatar header; **hero gold StatRing card** ("on track", rings/calories, streak chip).
- [ ] **District bento grid** of section tiles (Workout/Meals/Calories/Challenge) — section-tinted radial glow + 3D icon + live value; size by importance.
- [ ] "Today's workout" + "Today's meals" glass cards with progress; wellness tips as glass carousel.
- [ ] Wire in the **unused** `BuddyMotivationCard` + `DailyCheckInBanner` (premium glass versions).
- [ ] Glass bottom nav (progressive blur) + Buddy FAB (gold, glow); pull-to-refresh.
- [ ] Numbers count-up on load; staggered card entrance.
- **DoD:** the home screen is the "wow" — content-forward, gold ring hero, glass nav, 3D tiles.

## PHASE 6 — Workouts  (`WorkoutScreen.kt`, `WorkoutSchedulerScreen.kt`, completion sheet)
- [ ] **Active logging = Hevy/Fitbod model**: exercise glass cards, set rows with **ghost previous values**, big **gold checkmark** to complete (large tap target), auto-start **gold rest-timer ring**, focus mode (hide nav).
- [ ] Big tabular stats strip (volume/elapsed/sets); gold for the active set/row/number only.
- [ ] Plan carousel + day selector re-skin; AI-generate flow with premium loading.
- [ ] **Completion celebration**: gold confetti (Lottie) + success haptic + shareable summary card + PR as gold badge; "Chat with Coach" CTA.
- [ ] Scheduler: glass calendar, section-colored events, glass add/edit sheet.
- [ ] (Backlog) wire real progress analytics (currently random) + exercise 3D/video.
- **DoD:** logging feels fast (tap-to-confirm) and finishing feels earned.

## PHASE 7 — Meals & meal plans  (`MealScreen`, `MealDetailScreen`, `MealPlanSetupScreen`, `MyPlansScreen`, `ShoppingListScreen`, sheets)
- [ ] Day selector + glass `MacroSummaryCard` with gold progress ring/bars (tabular numerals).
- [ ] Meal slot sections: section-colored accents + 3D food icons; glass meal cards; gold completion checkmark.
- [ ] AI plan generation: premium stepped loading (analyze→generate→balance→save) with 3D motion.
- [ ] Meal detail: hero, macro pills, ingredient checklist, swap (gold), mark-eaten.
- [ ] Shopping list: glass grouped sections, checkable items.
- [ ] Remove/retire unused legacy `MealViewModel`/`MealRepository` path or hide it.
- **DoD:** plan + logging consistent with workout language; food 3D icons carry color.

## PHASE 8 — Calories & tracking  (`CalorieCalculatorScreen.kt`, `CalorieViewModel`)
- [ ] Re-skin calculator inputs (glass fields, gold chips for gender/activity/goal); result cards for BMR/TDEE/target with big numerals + citations.
- [ ] Weight + measurement tracking: premium line/area charts (gold accent line, soft range band), AI insight card.
- [ ] **Decide CaloriesTab fate** (currently orphaned) — either remove or surface via Home.
- **DoD:** charts read premium-not-clinical; numbers tabular; gold only on the live line/value.

## PHASE 9 — Fitness Buddy (AI)  (`avatar/AvatarScreen.kt`, `AvatarSelectionScreen.kt`, components)
- [ ] **Chat redesign**: coach replies **full-width, no bubble** (avatar + label); user messages = small gold-tinted bubbles; typing indicator.
- [ ] **Inline generative cards** in replies (suggested workout/meal/stat → actionable gold button) — the centerpiece move.
- [ ] Quick-reply chips (glass) per turn + modifiers (Shorter/Why?); empty state = greeting + 4 capability cards.
- [ ] Avatar selection: premium carousel; **upgrade emoji avatars → 3D rendered avatar** (use 3D assets / generate); persist selection.
- [ ] Add "AI" transparency tag; (backlog) persist conversation history; per-avatar personality.
- [ ] Buddy authors celebration/kudos copy app-wide.
- **DoD:** chat feels like a premium coach with living, actionable replies.

## PHASE 10 — Community  (`community/*`, `CommunityViewModel`)
- [ ] Feed: glass filter chips (For You/Following/You), premium post cards (avatar row → content → media → stat row → kudos/comment), **Strava-style one-tap kudos** with haptic + pop.
- [ ] Create post: glass composer, 3D stat chips (calories/steps/duration), image picker.
- [ ] Post detail, profile, edit-profile sheet, find-friends (glass tabs + user cards with streak/level), activity feed with section-colored notification icons + gold unread badge.
- [ ] Implement the **no-op share button**; consider realtime (later).
- **DoD:** social surfaces feel alive; gold only on kudos/active/badges.

## PHASE 11 — Challenges & leaderboards  (`ChallengesScreen`, `ChallengeDetailScreen`, cards)
- [ ] Hero challenge card (3D icon + gold progress ring + countdown); glass active/available lists with gold "Join".
- [ ] Detail: progress ring, auto-tracking explainer, **gold completion celebration**, leaderboard rows (rank/avatar/value), rewards (gold XP), tips.
- [ ] **Badges done right (WOW + bronze→silver→gold)**: 3D medal assets, locked=desaturated, unlock reveal; reserve **gold for apex tier + milestones only**.
- [ ] (Backlog) make levels/XP/streak real (currently hardcoded heuristics).
- **DoD:** earning reads as prestige; gold rationed to apex.

## PHASE 12 — Discover  (`DiscoverScreen.kt`, video, articles)
- [ ] **Wire it into nav** (currently orphaned) — as a Home/Profile entry or section.
- [ ] Re-skin: liquid-glass search, glass filter chips, hero + compact article cards (section-colored category tags), 3D empty state.
- [ ] Decide WebView (currently commented out): keep browser-open or revisit when CMP allows; integrate video player tastefully if used.
- **DoD:** reachable, premium, consistent.

## PHASE 13 — Profile & Settings  (`ProfileScreen.kt`, i18n)
- [ ] Profile header (avatar, name, gamification stats with 3D + gold), glass settings sections.
- [ ] Theme toggle, language selector, avatar nav, **subscription/premium status card** (currently missing), delete/logout.
- [ ] Fix hardcoded strings ("Progress", XP/Streak/Level labels) → StringKey; add new keys to EN/NL.
- [ ] (Backlog) RTL readiness.
- **DoD:** settings feel intentional; gold on premium badge + active states only.

---

## PHASE 14 — Cross-cutting polish
- [ ] Shared-element morphs (card → detail) on key flows (workout, meal, challenge, post).
- [ ] Consistent skeleton/shimmer loaders (no <1s spinners); optimistic updates where possible.
- [ ] Celebration system reused (workout/challenge/streak/purchase) — rationed, gold, haptic.
- [ ] Empty states everywhere with 3D-asset heroes + warm copy.
- [ ] Tasteful gamification: streak counts rest days, grace/freeze, "progress" not "points", milestone-only big celebration.

## PHASE 15 — QA / perf / a11y
- [ ] Glass perf budget (1–3 live surfaces/screen; no blur inside lists; snapshot static blurs); test low-end Android fallback.
- [ ] Contrast: gold text ≥4.5:1 per elevation (re-check on raised cards); chart lines/rings ≥3:1; never color-alone signaling.
- [ ] Reduced-motion + reduce-transparency paths.
- [ ] Verify on Android (API 31+ and ≤30 fallback) and iOS.

---

### Open decisions to confirm with user/manager
- Hero photography for Paywall (CREW uses photos; we currently fake with 3D — OK or source licensed imagery?).
- CaloriesTab + Discover: remove vs. resurface.
- Bump CMP to 1.8+ for native shared-element transitions (vs. Orbital lib)?
- Generate custom 3D avatars for the Buddy (AI gen) vs. use downloaded 3D objects.
