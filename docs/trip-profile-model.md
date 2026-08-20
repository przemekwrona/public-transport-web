# Model Trip + TripProfile

**Public Transport Web — propozycja modelu danych**

Data: 17 sierpnia 2026

---

## 1. Cel

Oddzielenie **struktury kursu** (przystanki, geometria, wariant trasy) od **wariantów czasowych** (normalny przejazd, korki, inne warunki ruchu).

---

## 2. Diagram relacji

```
Route
  -> Trip (struktura kursu)
       -> TripProfile (NORMAL | TRAFFIC)
            -> StopTime (czasy na przystanku)
Stop -> StopTime (wiele)
```

---

## 3. Kluczowe zmiany vs. model obecny

| Aspekt | Było | Jest |
|--------|------|------|
| Tożsamość trip | `(route, variantName, variantMode, trafficMode)` | `(route, variantName, variantMode)` |
| NORMAL / TRAFFIC | osobny wiersz w `trip` | osobny wiersz w `trip_profile` |
| StopTime PK | `(trip_id, stop_sequence)` | `(profile_id, stop_sequence)` |
| Czas jazdy, prędkość, is_customized | na `trip` | na `trip_profile` |
| Odległość, geometria, headsign | na `trip` | nadal na `trip` (wspólne) |
| BrigadeEvent | tylko `trip_id` | `trip_id` + `profile_id` |
| API TripId | bez zmian | nadal ma `trafficMode`, ale wskazuje **profil**, nie trip |

---

## 4. Encje

### 4.1 Trip — struktura kursu

| Kolumna | Opis |
|---------|------|
| trip_id | PK |
| route_id | FK do route |
| variant_name, variant_mode | FRONT / BACK |
| headsign | tablica kierunkowa |
| origin_stop_id/name, destination_stop_id/name | |
| distance_in_meters | łączna odległość |
| geometry | polyline trasy |
| is_main_variant, variant_designation, variant_description | |
| trip_code, trip_sequence | |
| created_at, updated_at | |

**Usunięte z trip:** `traffic_mode`, `travel_time_in_seconds`, `calculated_communication_velocity`, `customized_communication_velocity`, `is_customized`

### 4.2 TripProfile — wariant czasowy

| Kolumna | Opis |
|---------|------|
| profile_id | PK |
| trip_id | FK do trip |
| traffic_mode | NORMAL \| TRAFFIC |
| travel_time_in_seconds | |
| calculated_communication_velocity | |
| customized_communication_velocity | |
| is_default | zwykle TRUE dla NORMAL |
| is_customized | |

**Unikalność:** `(trip_id, traffic_mode)`

### 4.3 StopTime — czasy per profil

| Kolumna | Opis |
|---------|------|
| profile_id, stop_sequence | PK |
| stop_id | FK do stop |
| calculated_time_seconds | czas skumulowany / arrival |
| customized_time_seconds | czas po korekcie / departure |
| break_seconds | postój / przerwa |
| distance_meters | odległość od początku trasy |

### 4.4 Stop — bez zmian

| Kolumna | Opis |
|---------|------|
| stop_id | PK |
| name, lat, lon, … | |

---

## 5. Relacje

- `Trip` 1 — * `TripProfile` 1 — * `StopTime` * — 1 `Stop`
- `Trip` 1 — * `BrigadeTrip`
- `BrigadeEvent` * — 1 `Trip`
- `BrigadeEvent` * — 1 `TripProfile`

---

## 6. Reguły biznesowe

1. **Inny kierunek / inna trasa przystanków** → nowy `Trip` (FRONT vs BACK, inny wariant nazwy).
2. **Ten sam kurs, inne czasy (korki)** → nowy `TripProfile` pod istniejącym `Trip`.
3. **Tworzenie trip** → 1 `trip` + 1 profil `NORMAL`; opcjonalnie skopiowany profil `TRAFFIC`.
4. **Edycja przystanków / geometrii** → aktualizacja `trip` (+ ewentualnie synchronizacja sekwencji we wszystkich profilach).
5. **Edycja czasów** → tylko wybrany `TripProfile` (po `trafficMode` z API).
6. **Usunięcie** → usunięcie profilu TRAFFIC zostawia trip; usunięcie ostatniego profilu = usunięcie trip.
7. **Brygada / rozkład** → event wskazuje `profile_id`, czas trwania z `profile.travel_time_in_seconds`.

---

## 7. Przykład

Trip „L5 → Centrum”, ten sam układ przystanków:

| Profil | A→B | postój B | B→C | łącznie |
|--------|-----|----------|-----|---------|
| NORMAL | 180s | 30s | 240s | 450s |
| TRAFFIC | 300s | 30s | 420s | 750s |

Jeden wiersz w `trip`, dwa w `trip_profile`, dwa zestawy w `stop_time`.

---

## 8. API (kompatybilność wsteczna)

```yaml
TripId:
  routeId
  variantName
  variantMode
  trafficMode    # wybiera TripProfile, nie Trip
```

**Lookup:**

```
Trip     = route + variantName + variantMode
Profile  = Trip + trafficMode
StopTime = Profile + stopSequence
```

---

## 9. Migracja danych

1. Dla kazdego starego `trip` - wstaw `trip_profile` z jego `traffic_mode` i czasami.
2. `stop_time.trip_id` na `stop_time.profile_id`.
3. Zduplikowane tripy (NORMAL + TRAFFIC, ten sam wariant) - scal w jeden `trip`, dwa profile.
4. `brigade_event.profile_id` z profilu powiazanego z `trip_id`.
5. Usuń z `trip`: `traffic_mode`, kolumny czasowe.

---

## 10. Podsumowanie

| Pojęcie | Gdzie |
|---------|-------|
| Inne przystanki / geometria | nowy **Trip** |
| Te same przystanki, inne czasy | nowy **TripProfile** |
| arrival / departure / break | **StopTime** pod profilem |

**Esencja:** `Trip` = *co jedzie*, `TripProfile` = *jak długo*, `StopTime` = *kiedy na każdym przystanku*.
