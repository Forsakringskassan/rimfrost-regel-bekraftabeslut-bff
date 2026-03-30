# Rimfrost Regel Bekrafta Beslut BFF

Backend for Frontend for bekrafta beslut-regeln.

Denna service exponerar API-endpoints som frontend använder och proxar anrop mot backend-tjansten for bekrafta beslut.

## Features

- Express-baserad BFF i TypeScript
- Request logging for alla inkommande anrop
- CORS med stod for `GET`, `PATCH` och `OPTIONS`
- Health endpoint for runtime-checks
- Proxy mot backend for hamtning och bekraftelse av beslut
- Fallback-beteende for PATCH (returnerar `200` vid backendfel)

## Forutsattningar

- Node.js 24+
- npm

## Kom igang

1. Installera beroenden:

```bash
npm install
```

2. Skapa `.env` (du kan utga fran `.env.example`) och satt variabler:

```env
PORT=9003
BE_BEKRAFTABESLUT_URL=http://localhost:8891
```

3. Starta utvecklingsserver:

```bash
npm run dev
```

Servern startar pa `http://localhost:9003` om `PORT` inte satts.

## Scripts

- `npm run dev` - Startar med hot reload och lasning av `.env`
- `npm run build` - Bygger TypeScript till `dist/`
- `npm run start` - Startar byggd applikation med `.env`
- `npm run type-check` - TypeScript-kontroll utan build
- `npm run lint` - Kor ESLint
- `npm run lint:fix` - Fixa lint-fel automatiskt
- `npm run format` - Formatera med Prettier
- `npm run format:check` - Verifiera formatering

## API Endpoints

### `GET /api/health`

Returnerar status:

```json
{
  "status": "ok",
  "timestamp": "2026-03-30T12:34:56.789Z"
}
```

### `GET /api/regel/bekraftabeslut/:handlaggningId`

Hamtar beslutsdata fran backend:

`GET {BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/:handlaggningId`

Felhantering:

- Returnerar `500` vid backendfel eller kommunikationsfel

### `PATCH /api/regel/bekraftabeslut/:handlaggningId`

Skickar bekraftelse till backend med body:

```json
{
  "ersattning_id": "...",
  "ersattningsstatus": "..."
}
```

Anropar:

`PATCH {BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/:handlaggningId`

Felhantering:

- Vid backendfel loggas fallback-varning och endpointen svarar med `200`

## Miljovariabler

| Variabel | Beskrivning | Default i kod |
|----------|-------------|----------------|
| `PORT` | Port som servern lyssnar pa | `9003` |
| `BE_BEKRAFTABESLUT_URL` | Bas-URL till backend for bekrafta beslut | `http://localhost:8891` |

## Projektstruktur

```
rimfrost-regel-bekraftabeslut-bff/
|- index.ts
|- package.json
|- tsconfig.json
|- eslint.config.js
|- .env.example
```

## Implementation Notes

- Aktiva routes finns i `index.ts`.
- Nuvarande `.env.example` anvander template-namn (`BACKEND_BASE_URL`) medan koden laser `BE_BEKRAFTABESLUT_URL`.
- For korrekt drift ska `BE_BEKRAFTABESLUT_URL` finnas i din `.env`.

## License

ISC