# HA Companion for Wear OS

A Home Assistant client for Wear OS watches that mirrors one of your dashboards live, in a round-screen, HA-styled UI. Built and used daily on a Fossil Gen 6 (Wear OS 3); works over the watch's Wi‑Fi or the phone's Bluetooth.

## What it does

- **Home:** four sections, Favoritos / Menús / Áreas / Resúmenes (crown scrolls).
- **Favoritos:** the entities under the `Favoritos` heading of your dashboard, 3 per page, swipe left/right. Everyday controls first, then cameras, then protected switches.
- **Menús:** every heading after an `Apps` heading becomes a menu (e.g. one per server, listing its container switches).
- **Áreas:** the area cards under an `Áreas` heading open their subviews; each subview's headed sections become sub-menus (single-item sections are pooled into "Más").
- **Resúmenes:** a home summary (alarm, lights on, weather, away mode) and arc gauges (CPU / RAM / disk, temperature in the middle) per server from `config.json`.
- **Cameras:** full-screen picture; tap to capture and re-fetch (made for snapshot cameras such as Verisure).
- **Looks like your dashboard:** card names, HA theme colors, MDI icons and app logos (PNG) from the cards.
- **Safety:** kill switches (`*kill_switch*`) and locks ask ✗ / ✓ first; the alarm arms only from its own ALARMA prompt.
- **Alarm:** arms total (`alarm_arm_away`); if the panel rejects it because zones are open and Verisure OWA's `verisure_owa.force_arm` is available, force-arms skipping those zones and shows which.
- **Tile:** round tile with a center button (opens Favoritos; optional logo) and 8 buttons around it, one every 45°, showing live state; configured in `config.json` (`ring`).
- **Connectivity:** one WebSocket while the app is open, subscribed only to the entities shown; tries the home address, then the remote one, and remembers which worked.

## Setup

1. Copy `watch/config.example.json` to `watch/app/src/main/assets/config.json` and fill in your addresses, dashboard url_path, entities, servers and ring buttons.
2. Optional: put a square image at `watch/app/src/main/res/drawable-nodpi/m_logo.jpg` for the tile's center (★ without it).
3. Build: `cd watch && ./gradlew assembleDebug` (needs JDK 17+ and the Android SDK).
4. Install with ADB over Wi‑Fi: `adb connect <watch-ip>:5555` then `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
5. Create a long-lived access token in HA (Profile → Security) and run `watch/set-token.ps1 -Watch <watch-ip>:5555`. The script uses `run-as`, so it needs the debug build; afterwards you can install the optimized `assembleRelease` build over it (`install -r` keeps the token).
6. Add the tile: long-press a tile → + → HA Companion.

## Dashboard conventions

The app reads the `sections` view with path `overview` in the configured dashboard and splits it by heading cards: `Favoritos`, `Áreas` (area cards with `navigation_path` to subviews), `Apps` (every following heading is a menu). Visibility conditions and tap actions are ignored when collecting entities.

## Notes

- UI text is in Spanish.
- A rejected token stops reconnect attempts, so it can't get the watch's IP banned by `http.ip_ban`.
- Tested on a Fossil Gen 6 (416×416, Android 11 / API 30).
