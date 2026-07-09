# Tajly — Real Payments Setup (what YOU configure; the app code can't)

Real in-app purchases need store + dashboard configuration that only an account owner can do. The app is wired to RevenueCat; these steps make the buttons charge real money (and let us test the full flow for free in sandbox). Do them in this order.

## 0. Prereqs
- RevenueCat account + the app's public SDK keys already in `RevenueCatKeys` (verify they're the real project's keys, not placeholders).
- Apple: paid Apple Developer account + **Paid Apps agreement signed** in App Store Connect (Business → Agreements) — subscriptions won't load until this is active.
- Google: Play Console with a merchant/payments profile set up.

## 1. App Store Connect (iOS)
1. Monetization → **Subscriptions** → create a **Subscription Group** (e.g. "Tajly Premium").
2. Add two subscriptions in that group (a user can hold only one per group; set level order — level 1 = best):
   - **Annual** — Product ID `tajly_premium_annual` (immutable, used in code), duration 1 year, price (e.g. €79.99).
   - **Monthly** — Product ID `tajly_premium_monthly`, duration 1 month, price (e.g. €9.99).
3. For **Annual**, Subscription Prices → **Set up Introductory Offer** → type **Free** → duration **2 Weeks** (= 14-day trial). (Intro offers can't be edited — delete/recreate to change. One intro offer per group, ever.)
4. Fill each subscription's localization, review screenshot, and a review note. Add the app's **Privacy Policy URL** (App Privacy) and an EULA/License Agreement (App Information → License Agreement) — the paywall links to these.
5. Metadata can take ~1h to reach sandbox.

## 2. Google Play Console (Android)
1. Monetize → Products → **Subscriptions** → create a subscription (e.g. `tajly_premium`).
2. Add **base plans** (auto-renewing): `annual-autorenewing` and `monthly-autorenewing`, set price/period, **activate** them.
3. On the annual base plan, add an **Offer** with a **Free trial phase of 14 days**.
   - ⚠️ In RevenueCat, the Android product ID is the **base plan**, format `subscription_id:base_plan_id` → e.g. `tajly_premium:annual-autorenewing` and `tajly_premium:monthly-autorenewing`.
4. **Service account link** (so RC reads Play): Google Cloud → enable *Play Android Developer API* + *Play Developer Reporting API* → create a service account → grant *Pub/Sub Editor* + *Monitoring Viewer* → download JSON key → in Play Console invite the service-account email with the 3 financial/orders permissions → upload the JSON to RevenueCat. Propagation can take up to **36 hours**.

## 3. RevenueCat dashboard
1. Product catalog → **Products** → Import Products (pulls the App Store + Play products created above).
2. Product catalog → **Entitlements** → create id **`premium`** → attach all four products (both stores × annual/monthly).
3. **Offerings** → create the Default offering → add packages using standard ids **`$rc_annual`** and **`$rc_monthly`**, each attaching the matching-duration product per store. Mark it **Default** (the app reads `offerings.current`).

## 4. Test the WHOLE flow for FREE (sandbox — identical to production)
- **iOS, fast inner loop**: Xcode → File → New → **StoreKit Configuration File**; add the two subs with matching Product IDs; Edit Scheme → Run → Options → StoreKit Configuration → select it. Buy in the Simulator, time-warp renewals in the Transaction Manager. (To have these reach RevenueCat, upload the config's public cert to RC + mirror products; otherwise it's local-only.)
- **iOS, production-identical (hits Apple + RC)**: App Store Connect → Users and Access → **Sandbox** → create a Sandbox Apple Account → on a real device, Settings → Developer → Sandbox Apple Account → run the app → purchases show "[Environment: Sandbox]", $0 charge, accelerated renewals.
- **Android, production-identical**: Play Console → Settings → **License testing** → add tester emails; publish the app to the **internal** track; testers install via the internal opt-in link → purchases are free "test card" through real Play billing. ⚠️ Being on the internal track alone is NOT enough — the account must ALSO be a license tester.
- **RevenueCat sandbox** is auto-detected from the receipt (no code flag). Enable **"View Sandbox Data"** in the dashboard to watch test transactions land.

## 5. App code (already handled / to verify on our side)
- Client wiring: `configure` → `awaitOfferings` → show packages → `awaitPurchase(package)` → check `entitlements.active["premium"]` → `awaitRestore`; a `PurchasesDelegate` re-checks on updates. (RevenueCat KMP 3.0.0 matches our stack; min SDK 23; no CocoaPods.)
- The paywall shows the store price/period, 14-day trial terms, auto-renew + cancel copy, in-app Terms/Privacy, and Restore — for App Review compliance (guideline 3.1.2).
- ⚠️ No trial **toggle** (Apple rejects since Feb 2026) — plans are tappable cards. The full billed price is the most prominent element; monthly-equivalent is secondary.
- Any existing dev "test valid purchase" shortcut is replaced by the real sandbox flow above; keep it only behind a debug flag if useful.

## Reviewer note (to avoid rejection)
Apple's reviewer must be able to complete a sandbox purchase — if products don't load, it's an automatic reject. Verify offerings load in a fresh sandbox account before submitting, and that Terms/Privacy links + Restore are visible and functional in the app UI.
