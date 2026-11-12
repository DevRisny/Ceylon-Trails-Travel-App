# Ceylon Trails: Travel App

An Android travel guide to **Sri Lanka**, plus a quick reference for **every country in the world**. Browse 27 hand-picked destinations as photo cards, filter by beaches, heritage, hill country, wildlife, adventure or cities, see which places are **in season this month**, open a place for its story, a 12-month season strip and **live weather**, and save favourites.

> Pynexia Tech · Android Development Internship · **Week 3: Travel App**
> Built by **Junaideen Risny Suha** · Kotlin · XML layouts · Material 3 · MVVM

## Features

| | |
|---|---|
| 🗺️ **Explore Sri Lanka** | 27 curated places (Sigiriya, Ella, Galle Fort, Mirissa, Yala, Jaffna, Arugam Bay, Adam's Peak…) as CardView grid tiles with photos. |
| 🔎 **Filters** | Live search (also matches highlights, so "nine arch" finds Ella), category chips, an **In season** toggle, and sorting: recommended (in season first), A to Z, nearest to Colombo, or north to south. |
| 🌦️ **Seasons that make sense** | Sri Lanka has two monsoons, so the south-west coast is best around December-April and the east coast around May-September. Every place has a season strip, and the home screen says how many places are at their best this month. |
| 📖 **Place details** | Collapsing photo header, Wikipedia summary and link, highlights, distance from Colombo, **Directions** (opens Maps), **Share**. |
| ☀️ **Live weather** | Current conditions and a 7-day forecast from Open-Meteo, with a travel tip (rain jacket, mist at viewpoints, thunderstorms on hikes). |
| 🌍 **World** | All countries from REST Countries: flag, capital, region, population, currencies, languages, time zones. Search, region chips, sort by name or population, pull to refresh, and a Wikipedia summary for each country. |
| ❤️ **Saved** | Favourite places and countries, kept on the device. |
| 📶 **Offline-friendly** | An OkHttp disk cache (20 MB) and an in-memory cache, so pages you've seen open again without a connection. Each section has its own loading, error and retry state. |

## APIs (all free, no API key)

| API | Used for | Call |
|---|---|---|
| [Wikipedia REST](https://en.wikipedia.org/api/rest_v1/) | Descriptions, photos, article links | `GET page/summary/{title}` |
| [Open-Meteo](https://open-meteo.com/) | Current weather + 7-day forecast | `GET v1/forecast?latitude=…&longitude=…&current=…&daily=…` |
| [REST Countries](https://restcountries.com/) | Country facts and flags | `GET v3.1/all?fields=name,cca2,capital,…` |

Requests go through **Retrofit + Gson** with a shared **OkHttp** client that adds a descriptive User-Agent (Wikimedia asks for one). Images load with **Glide** using the same header. Country names that land on a Wikipedia disambiguation page (e.g. Georgia) automatically retry as "Georgia (country)".

## Architecture

```
:core  (pure Kotlin, unit-tested on the JVM)
  Destination, Destinations     curated places, categories, best months, highlights
  Explore, Seasons              filtering, sorting, distances, season labels ("Dec – Apr")
  Weather, Wiki, Countries      API response models (Gson) and mapping to app models
  Favorites                     saved keys, encoded for storage
  TravelRepository              caching, weather TTL, friendly error messages (TravelApi interface)

:app   (Android)
  data/Network                  Retrofit services for the three APIs, OkHttp cache
  data/Images                   Glide loading
  data/FavoritesStore           SharedPreferences + StateFlow
  ui/*ViewModel                 StateFlow UI state, coroutines
  ui/*Fragment, *Activity       Explore, World, Saved tabs; place and country details
  ui/*Adapter                   ListAdapter + DiffUtil, payload updates for the heart icon
```

- **MVVM**: ViewModels expose `StateFlow`s; screens collect them with `repeatOnLifecycle(STARTED)`.
- **Lazy photos**: a card asks for its photo only when it's bound, and recycled cards ignore late results.
- **ViewBinding** everywhere, Material 3 components, accessible content descriptions, and 2 or 3 grid columns depending on screen width.
- `java.time` works on Android 7 through core library desugaring (minSdk 24).

## Run it

Open the project in **Android Studio** (Koala or newer, JDK 17) and run the `app` configuration, or:

```bash
./gradlew :core:test            # 13 unit tests: filters, seasons, API parsing, repository
./gradlew :app:assembleDebug    # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:connectedDebugAndroidTest   # UI tests on a device or emulator
```

## Tests

- **Core (JUnit)**: curated data is valid, category, province, season and text filters, recommended ordering, distances, season labels across the new year, season status, parsing real-shaped Wikipedia, Open-Meteo and REST Countries responses (including missing fields), number and flag formatting, favourites encoding, and repository caching (weather expires after 15 minutes, disambiguation retry, offline message).
- **Instrumented (Espresso)**: the list shows every destination, category chips filter it, an empty search shows the empty state and *Clear filters* restores it, opening a card shows its details, and saving a place shows it in the Saved tab.
- **CI (GitHub Actions)**: runs the core tests, builds the APK, then runs the UI tests on an Android emulator. A screenshot walkthrough with live data (Explore, place detail, World, country, Saved) is uploaded as the `emulator-screenshots` artifact.

## Data notes

Seasons are typical months from the monsoon pattern, not guarantees. Distances are straight-line from Colombo. Text and photos are from Wikipedia (CC BY-SA), weather from Open-Meteo, and country data from REST Countries; the app credits them on every detail screen.

## License

MIT © 2026 Junaideen Risny Suha
