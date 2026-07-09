# Tajly Revamp v2 — Master TODO (from detailed feedback)

Nothing here is coded yet. This captures **every** point from your feedback, organized by area, with PM rationale, bug flags 🐞, decisions ❓, and asset/research needs. Backend stays untouched (same rule). Items marked ❓ need your call (asked separately). Three research streams (payments/compliance, liquid-glass+assets, community/home/paywall UX) are running and will add implementation detail — coverage below is already complete.

Legend: `[ ]` todo · 🐞 bug · ❓ decision · ⭐ priority · 🎨 needs premium asset/backdrop.

---

## 0. GLOBAL — PURE LIQUID GLASS EVERYWHERE ⭐ (priority)
- [ ] Adopt **real liquid glass app-wide** (backdrop blur + refraction + specular), not faux — via Haze (verify CMP 1.9.3 version) with faux fallback on old Android. Ref: **image 6 (WhatsApp glass tab bar)**, **image 15 (selected pill)**.
- [ ] Bottom bar 🐞: remove hard **borders + outer margins** that make it look detached; must **blend perfectly** with the UI as one glass surface (image 6). Selected tab = **glass pill highlight** (image 15 style).
- [ ] Apply glass to: bottom nav, top bars/headers, sheets, dialogs, key cards, search, chips — tastefully (perf budget: ≤ a few live-blur surfaces per screen, never blur inside scrolling lists).
- [ ] Premium asset pass 🎨: current 3D icons look **cartoonish** in places — source **premium 3D icons + premium logos** and use restraint (3D only where it elevates, not everywhere). Add **cinematic fitness backdrops** (minor hero imagery, not on every screen).

---

## 1. PAYWALL (image 1) ⭐ — most important page; currently Apple-rejectable
**Compliance / correctness**
- [ ] 🐞 Show **full plan details** (price, billing period, what's included, renewal = auto-renew, trial terms) — Apple rejects paywalls without this (guideline 3.1.2).
- [ ] **14-day free trial** (change from 7-day) — configure store intro offer + copy.
- [ ] Pricing display: annual shown as **monthly-equivalent** ("€6.67/mo, billed annually €79.99"), **strikethrough** original + **% off** ("~~€9.99/mo~~ save 33%"). Standard SaaS pattern.
- [ ] ❓ Monthly plan **only after scroll** (if App-Review-compliant — confirm via research).
- [ ] Replace **"TAJLY" text badge → Tajly LOGO**.
- [ ] **Terms of Service + Privacy Policy → open in IN-APP browser** (iOS SFSafariViewController / Android Chrome Custom Tabs) via expect/actual — not external. Add **Restore Purchases** (already present, keep).
**Real transactions**
- [ ] ⭐ Wire **real RevenueCat purchases** (currently fake/test button): configure products + entitlements (RevenueCat dashboard), App Store Connect subscription group + StoreKit products, Google Play subscriptions; client: fetch offerings → purchase package → check entitlement → restore. (Store/dashboard config is a prerequisite — will document exactly.)
- [ ] ⭐ **Sandbox simulation**: StoreKit Configuration file (Xcode) + sandbox testers + Play license testers + RevenueCat sandbox → run the **entire real purchase→entitlement flow for free**. Keep a dev toggle that mirrors production exactly (replaces the flaky "test valid purchase").
**Product / conversion (this page must move metrics)**
- [ ] Redesign as a **conversion machine**: clear value prop, benefit framing, social proof/trust, urgency, plan comparison, "best value / most popular" tags. UI/UX inspiration from **image 16 (District Pass)** + **image 17 (Elite/GPT paywall)** — layout/UX only, NOT their content or colors (keep Tajly gold).
- [ ] 🎨 Cinematic **elegant fitness backdrop** (real gym, tasteful, not cartoon).

---

## 2. PURCHASE SUCCESS (post-purchase)
- [ ] 🐞 **Intermittent navigation**: screen doesn't always appear after a valid purchase — fix the success→route reliability.
- [ ] Replace the icon → **confetti + real premium animation** (Lottie), celebratory.
- [ ] 🎨 Real **gym backdrop**.

---

## 3. ONBOARDING
- [ ] 🎨 Add an **elegant cinematic backdrop** (like the paywall but different, very elegant).
- [ ] 🐞 **Identity bug**: onboarding asks the user's name, but the app shows an email-derived handle ("nandinisingh0212122"). Reconcile — the entered name must be used consistently everywhere.
- [ ] ⭐ **Set up the community profile DURING onboarding** (your preference): collect handle/display name, photo, bio (+ optional social links) elegantly, so the community identity exists from day one. UX must be very elegant.

---

## 4. IDENTITY — one profile across the app ⭐
- [ ] The **app Profile** and the **Community Profile** must be **one connected identity** (not two disconnected things). Single source of truth; edits sync both ways.

---

## 5. HOME (images 2/3) ⭐ — biggest problem: disconnected + dummy data
- [ ] ❓ **Direction decision** (your call): Home = **(A) clean action hub** OR **(B) a connected STORY** that links the 15+ features (today's plan → workout → meal → calories → streak → challenge → community → buddy). Currently it feels disconnected — pick one identity.
- [ ] ⭐ **Connect the 15+ features into one coherent narrative/flow** — no orphaned widgets.
- [ ] 🐞 **Kill dummy data** — every number/section must be real state.
- [ ] Remove the redundant big **"Chat with Buddy"** button (a Buddy **FAB** already exists) and the confusing small **"How are you feeling / check-in"** button — consolidate (one clear entry, not three).
- [ ] **Streak**: make it **functional**, or replace with a working **motivational hero banner** (the best-looking, working element in the app). Add **minor** hero images (not everywhere).
- [ ] 🐞 **Profile shown in 2+ places** (bottom bar + top-right avatar) — single entry.
- [ ] **Daily wellness tips → dedicated screen**: half-screen **hero image** + half **advice**, **swipe next**, **no refresh button**, genuinely engaging (think beyond a refresh carousel).
- [ ] Home design inspiration: **image 10/11** (bold hero backdrop + big primary action) — UX only, keep gold.

---

## 6. PROFILE (image 4) — "eww", needs real redesign
- [ ] 🐞 Shows the **AI buddy's face** instead of the **user's own info/photo** — fix (trust).
- [ ] **Sync with community profile** (see §4) — same screen/identity.
- [ ] **Premium/subscription card** 🎨: redesign to a **premium glassy color-backdrop** (not cartoon); motivational fitness imagery. Current "Tajly Premium" block reads cartoonish.
- [ ] 🐞 **Progress (XP / Streak / Level)** and **Fitness Stats (BMR / TDEE / Goal)** appear non-functional/"--" — make them real, or hide until data exists.
- [ ] **Settings** redesign: the dark-theme toggle icons look bad; use **premium 3D icons / logos** selectively (not everywhere); premium feel.
- [ ] **Demote Logout + Delete Account 3 levels deep** (retention — don't make leaving/cancelling easy). Same for "change plan".
- [ ] ⭐ **NEW: Subscription / Billing screen** (separate) — current plan, payment method + button, change/upgrade plan, **download invoices**, manage. Entry via a **premium icon** in Profile → opens a screen with a **beautiful backdrop** + all required actions.

---

## 7. BOTTOM BAR (images 5/6/15)
- [ ] ⭐ **Liquid glass** that blends (no detached border/margins), **selected = pill highlight** (image 15).
- [ ] ❓ **5th / empty slot**: decide what fills it as a senior PM (e.g., central **Post/Create**, **Discover**, or **Buddy**). Currently 4 tabs (Home/Train/Community/Profile).

---

## 8. COMMUNITY FEED (image 7) — "piece of hell"
- [ ] Reduce **big cartoony boxes + oversized avatars**; tighten post cards; add tasteful **backdrops/visual interest**.
- [ ] **Collapsing header**: composer ("share your win") + For You/Following/You tabs + top actions **hide on scroll up** (community is scroll-heavy — give the feed room).
- [ ] ❓ **FAB**: decide keep or remove (redundant with a top compose entry?).
- [ ] **Kudos micro-interaction** 🐞: the popup heart looks bad → **real heart + Lottie** animation (tasteful, rationed).
- [ ] ⭐ **Post-from-anywhere**: a **reusable "Post to Community" bottom sheet** available across the app (workout finish, challenge win, PR, meal). One **elegant Tajly share button** (not cringe) everywhere.
- [ ] Achievements/challenge **wins look great as posts** (celebratory post templates).
- [ ] **First-time community**: an onboarding screen in the style of **image 9** (create-profile CTA with a people arc) — your preference over image 8. Rethink the CTA copy/placement.
- [ ] **"Arc of most-active people"** component (like image 10/11 messages arc) on Community; **tap → community profile**. Reuse the same arc in the **Challenges leaderboard** (links to profiles).
- [ ] Research the **best community-profile UX** (web) — running.

---

## 9. COMMUNITY PROFILE (images 12/13/14)
- [ ] 🐞 **Bio edit**: keyboard **covers the input** — redesign as **full-screen or large keyboard-aware bottom sheet**. Add **social links** fields.
- [ ] ❓ **Cover/banner**: add an **editable banner** (currently not editable). For simplicity, offer **free curated banner options** that fit.
- [ ] 🐞 **Photo edit**: the **camera icon on the face** looks bad → redesign avatar-edit UX.
- [ ] Replicate the **elegant profile design of image 14** somewhere appropriate.
- [ ] Sync with app profile (§4).

---

## 10. CHALLENGES
- [ ] **Winning/achievement moment** looks great (celebratory) + **shareable to community** (reuse §8 sheet).
- [ ] **Leaderboard**: add the **active-people arc**; entries **link to community profiles**.

---

## 11. TIPS / DISCOVER
- [ ] New **engaging tips experience** (half hero + half advice, swipe), **no refresh** (folds into §5 tips screen).

---

## 12. CROSS-CUTTING / INFRA
- [ ] **In-app browser** expect/actual (Terms/Privacy, article links).
- [ ] **Lottie (compottie)** for confetti/celebration/heart.
- [ ] **Premium asset pipeline** 🎨: premium 3D icons, premium logo(s), cinematic fitness backdrops, free banners.
- [ ] **Onboarding theme-aware** retrofit (still always-dark).
- [ ] Full **simulator run + smoke test** of every flow after each batch; zero backend break.

---

## ✅ LOCKED DECISIONS
1. **Home = story-led hub** (priority 1 > 2 > 3): lead with the connected story, keep some clean-hub actions. Time-aware daily narrative (below).
2. **Bottom bar = 5 tabs with a CENTER Post/Create action**: `Home · Train · [＋ Post] · Community · Profile`. The center ＋ opens the reusable "Post to Community" sheet (quick-log/share).
3. **Community FAB removed** — the center Post tab is the single compose entry.
Already decided: community profile **at onboarding** · first-time community = **image-9 style** · app+community profiles **synced** · **14-day** trial · **in-app** browser · **logout/delete demoted**.

---

## 🔬 RESEARCH-HARDENED SPECS (folded in)
**Liquid glass (§0):** use **Haze `1.7.0`** (the version built for CMP 1.9.3 — 2.0.x needs CMP 1.11+, would crash). One `rememberHazeState()`/screen; `Modifier.hazeSource` on the scroll body, `Modifier.hazeEffect(style=HazeMaterials.ultraThin/thin)` on bars/sheets/cards. The "blends, no hard border" look = blur 24–34dp + small `noiseFactor` (0.05) + real `backgroundColor` + a **white→transparent gradient border** (fake specular). Selected nav **pill** = a 2nd `hazeEffect` layer + gold tint + `spring` offset (liquid slide). Perf: never blur inside lazy lists; ≤2 live-glass surfaces/screen; `inputScale` on big surfaces; `expect/actual` tier → FULL on iOS + Android 31+, SOLID scrim < API 31. Optional true **refraction** via **KMPLiquidGlass** (alpha) on 1–3 marquee surfaces only (kudos card, nav pill) — pin-test on a branch first.
**Motion:** **compottie 2.x** (pin to the release bundling CMP 1.9.x) for confetti / celebration / animated kudos heart.
**Premium assets (kills the cartoon look):** re-render **3dicons.co** CC0 sources in a **brushed-gold PBR material** (Blender) for a bespoke on-brand 3D set; cinematic dark-gym backdrops from **Unsplash/Pexels** (free commercial, no attribution); free banners from same.
**Home story architecture (Whoop/Oura/Fitbod/Nike + NN/g):** one **functional hero** (time-of-day-aware status + one primary CTA — not a mood photo), then a **sectioned bento** (tile size = priority) grouped Train/Nutrition/Progress/Community, each with **"See all"**; **every module shows real state or a *teaching* empty state** (icon + one line + one CTA) — never a dummy chart; a module with nothing to say **collapses**. One **semantic status palette** learned once; one card-emphasis style. **Community intro** = lower-scroll empty-state upsell → contextual coach-mark after 1st workout/streak → full intro only on tap (never a first-run blocker). Streaks framed **gently** (rest counts), not punitive.

---

## 🔬 RESEARCH-HARDENED SPECS — part 2 (payments · community · tips · identity)

**PAYWALL conversion + compliance (§1).** Blueprint (RevenueCat/Superwall/Adapty/Growth.Design): value **before** price (benefit stack + free-vs-Pro table); a **trust band** (star rating + review count, "N+ users", one testimonial, "Cancel anytime"); **annual defaulted + visually dominant** (gold border + "Best Value" + "Save 33%" + **per-week/per-month equivalent** with strikethrough — worth +20–30%); **1–3 plans max**; **single-screen with a sticky CTA**; **trial on the annual plan** with a visible **"how the trial works" 3-step timeline** + an **opt-in "remind me before it ends" toggle** (Blinkist: +23% signups, −55% complaints); **CTA = "Start Free Trial"**; a **visible, honest close button** (hiding it = Apple dark-pattern rejection + user reactance) and **no asterisk fine print**; animated/glass paywalls convert ~2–3× static. Fitness is trial-positive; annual take-rate ~61–68%. **Note on trial length:** you chose **14-day** — research says ≤7-day trials convert better right now; flagging as a data point, your call stands (14-day is the plan unless you say otherwise). Post-purchase **aftercare** screen reinforces benefits.
**REAL payments + sandbox (§1).** RevenueCat: configure products + `premium` entitlement (dashboard) → App Store Connect subscription group + StoreKit products → Google Play subscriptions; client = get offerings → purchase package → check entitlement → restore. 14-day trial = store **intro offer**. Compliance must-haves: real price/currency from the store, billing period, auto-renew disclosure, trial terms, **Terms(EULA)+Privacy links**, **Restore**. **Sandbox** = StoreKit Configuration file (Xcode sim) + App Store sandbox testers + Play license testers + RevenueCat sandbox → run the **full real purchase→entitlement flow for free**; a dev toggle mirrors production (replaces the flaky "test valid purchase" + fixes the intermittent PurchaseSuccess nav).
**Payments build-order & compliance gotchas (critical):** (1) create store products first — App Store Connect **Subscription Group** + product with a **"Free / 2 Weeks" intro offer** (= 14-day); Google Play **subscription → base plan → 14-day free-trial offer** (RC product id maps to the **base plan**: `sub_id:base_plan_id`). (2) Import to RevenueCat → attach to `premium` entitlement → Default offering with `$rc_annual`/`$rc_monthly`. (3) Client (RevenueCat **KMP 3.0.0** — matches our stack; no CocoaPods; min SDK 23; drop `purchases-kmp-datetime`): `configure` → `awaitOfferings` → `awaitPurchase` → `awaitRestore` → `PurchasesDelegate`. ⚠️ **Do NOT use a trial TOGGLE** — Apple began **rejecting toggle-trial paywalls (Feb 2026)** as a dark pattern. ⚠️ The **full billed price must be the MOST PROMINENT** pricing element; the per-month equivalent must be **subordinate** in size/position (Apple "billing amount" rule) — so show "€79.99/yr" prominently with "≈€6.67/mo" secondary, not the reverse. Reviewer must be able to complete a sandbox purchase (products must load) or it's rejected. Use `pricePerMonth`/`formattedPricePerMonth` + `relative_discount` for the strikethrough/savings.
**IN-APP BROWSER (§12).** expect/actual → iOS `SFSafariViewController` (walk to top-most VC so it presents over the paywall modal), Android Chrome Custom Tabs (`androidx.browser:browser`), or drop-in `KInAppBrowser`. For Terms/Privacy/articles. Also mirror Privacy Policy + EULA in App Store Connect metadata.
**COMMUNITY (§8–9).** Header = **`enterAlways`** collapse (composer + For You/Following/You hide on scroll-down, return on scroll-up; keep a slim pinned element so it never fully vanishes; profile hero uses `exitUntilCollapsed`). Kill "cartoony boxes": **flat/tonal elevation, hairline dividers, edge-to-edge media, 12–16dp radii, 36–40dp avatars, icon-only actions**, strict 4:5 / 16:9 crops with a max height. **Compose = center Post tab** (your decision) opening **one reusable `ShareToCommunitySheet(context)`** used everywhere (workout finish, PR, challenge win, meal) — in-app compose for the feed, **native OS share sheet** for outbound. **Kudos** = `KudosButton` outline→gold-filled heart + one-shot Lottie burst (~150–250ms fill, ~300–400ms burst) + one light haptic; only on affirmative kudos, rationed; cross-fade under Reduce-Motion. **Active-people ring** = one `RingedAvatar(user,tier)` atom → horizontal rail in feed + ranked/podium **leaderboard**; ring gradient = tier; tap → community profile. **Profile edit = full-screen route** (not a bottom sheet — fixes the keyboard-covers-bio bug): single-column, `imePadding`, Cancel/Save bar; **banner + avatar = full-screen crop editors** (avatar edit affordance on the **ring**, not a camera icon on the face); add **social-links** fields. Editable **banner** with free curated options.
**TIPS deck (§11).** Replace the refresh-carousel with a full-screen **`TipDeck`**: top ~50% hero image + bottom ~50% advice, **user-swiped (no auto-advance, no refresh)**, segmented "story-bar" progress, terminal "all caught up" card, long-press = save. (Auto-rotating carousels get ~1% engagement; user-paced decks fix it.)
**UNIFIED IDENTITY (§3–4).** One `Identity` record backs both app + community profiles (community profile is a *view*, not a 2nd account) — fixes the name/handle bug. **Progressive profiling**: minimal signup (auth + first name), collect community fields **just-in-time** (name+avatar before first post, @handle when mentionable, bio on first profile visit), each skippable. Add a private **Profile-Strength ring** (LinkedIn-style, gold) to drive completion with a one-line "why" per field. Community profile set up during onboarding per your call, but as a fast/skippable step, not a wall.

---

## Notes on scope/sequencing (proposed)
Phase A (foundation): real liquid glass (Haze) app-wide + premium assets + in-app browser + Lottie.
Phase B (money): Paywall rebuild + real RevenueCat + sandbox + Billing screen + PurchaseSuccess fix.
Phase C (identity): unified profile, onboarding community setup, identity bug.
Phase D (home): Home story/hub rebuild + tips screen + kill dummy data.
Phase E (community): feed declutter + collapsing header + post-anywhere sheet + kudos Lottie + profile edit + arc + first-time screen.
Phase F: challenges celebration/share + leaderboard arc.
Each phase: compile + dual-platform verify + smoke test.
