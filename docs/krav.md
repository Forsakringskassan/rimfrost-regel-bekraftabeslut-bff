# Krav — Bekräfta Beslut BFF (BEKB)

## Bakgrund och syfte

Bekräfta Beslut BFF är backend-för-frontend för Bekräfta Beslut-mikrofrontenden. Den
förmedlar handläggarens hämtning av beslutsunderlag och referensdata, bekräftelse av
ersättningsbeslut och slutförande av uppgiften, mot den bakomliggande bekräftabeslut-tjänsten.
Den existerar för att mikrofrontenden ska kunna anropa en enda, stabil tjänst utan
kännedom om regeltjänstens kontrakt.

---

## Intressenter och aktörer

| Aktör | Roll |
|---|---|
| Bekräfta Beslut Frontend | Anropar BFF:n för att hämta beslutsunderlag och bekräfta beslut |
| Bekräftabeslut-regeltjänsten | Bakomliggande tjänst som äger beslutsunderlag och ersättningsdata |

---

## Funktionella krav

### BEKB-FR-01 — Hämta beslutsunderlag

- **BEKB-FR-01.1** BFF:n ska kunna hämta beslutsunderlag för en given handläggning från
  bakomliggande regeltjänst och returnera det oförändrat till frontend.

### BEKB-FR-02 — Hämta referensdata

- **BEKB-FR-02.1** BFF:n ska kunna hämta referensdata för typerna `avslutstyp`,
  `beslutstyp`, `beslutsutfallstyp` och `yrkandestatus` från bakomliggande regeltjänst.
- **BEKB-FR-02.2** BFF:n ska returnera 404 om en okänd referensdatatyp efterfrågas.

### BEKB-FR-03 — Hämta uppgiftsbeskrivning

- **BEKB-FR-03.1** BFF:n ska kunna hämta en utökad uppgiftsbeskrivning från bakomliggande
  regeltjänst och returnera den oförändrad till frontend.

### BEKB-FR-04 — Bekräfta beslut

- **BEKB-FR-04.1** BFF:n ska ta emot en icke-tom lista av ersättningsuppdateringar och ett
  beslutsobjekt från frontend och vidarebefordra dem till bakomliggande regeltjänst.
- **BEKB-FR-04.2** BFF:n ska validera att listan av ersättningsuppdateringar inte är tom
  innan vidare behandling.
- **BEKB-FR-04.3** BFF:n ska validera att beslutsobjektet innehåller avslutstyp, beslutstyp
  och beslutsutfall, och att ingen av dessa är tom.

### BEKB-FR-05 — Slutföra handläggning

- **BEKB-FR-05.1** BFF:n ska ta emot ett handläggnings-id från frontend och markera
  handläggningen som slutförd hos bakomliggande regeltjänst.
- **BEKB-FR-05.2** BFF:n ska validera att handläggnings-id inte är tomt innan vidare
  behandling.

### BEKB-FR-06 — Felhantering vid integration mot regeltjänsten

- **BEKB-FR-06.1** Om regeltjänsten svarar med ett felstatus ska BFF:n returnera samma
  HTTP-statuskod till frontend.
- **BEKB-FR-06.2** Om regeltjänsten inte går att nå, inklusive vid avbrutna
  nätverksanslutningar, ska BFF:n returnera en statuskod som tydligt anger att bakomliggande
  tjänst är otillgänglig.

---

## Icke-funktionella krav

### BEKB-NFR-01 — Observerbarhet

- **BEKB-NFR-01.1** BFF:n ska exponera en hälsokontroll för sin egen driftstatus.
- **BEKB-NFR-01.2** Beredskapskontroll ska proba bakomliggande regeltjänst för att avgöra om
  BFF:n är redo att ta emot trafik.
- **BEKB-NFR-01.3** BFF:n ska inkludera handläggnings-id i loggkontext för alla anrop som
  involverar ett handläggnings-id.

### BEKB-NFR-02 — Säkerhet

- **BEKB-NFR-02.1** BFF:n ska vidarebefordra anropande handläggares auktoriseringsuppgifter
  till bakomliggande regeltjänst oförändrade.

---

## API-gränssnitt (översikt)

| API | Målgrupp | Specifikationsartefakt |
|---|---|---|
| Bekräfta Beslut BFF REST-API | Bekräfta Beslut Frontend | `rimfrost-regel-bekraftabeslut-openapi` |
| Bekräftabeslut-regeltjänstens API | Denna BFF | `rimfrost-regel-bekraftabeslut-openapi` |

---

## Integration med Bekräftabeslut-regeltjänsten

BFF:n är en synkron REST-till-REST-integration mot regeltjänsten. BFF:n lagrar inget
tillstånd själv och publicerar inga event.
