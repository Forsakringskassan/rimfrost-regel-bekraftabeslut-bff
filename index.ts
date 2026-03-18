import express from "express";
import { fileURLToPath } from "node:url";
import path from "path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 9003;
const BEKRAFTABESLUT_URL = process.env.BEKRAFTABESLUT_URL || "http://localhost:8891";

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

app.use((req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
    res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    if (req.method === "OPTIONS") {
        res.sendStatus(200);
    } else {
        next();
    }
});

function getMockBeslutsdata(handlaggningId: string) {
    return {
        handlaggning_id: handlaggningId,
        kund: {
            fornamn: "Lisa",
            efternamn: "Tass",
            kon: "KVINNA",
            anstallning: {
                organisationsnamn: "Mock AB",
                arbetstid_procent: 100,
                lon: {
                    lonesumma: 40000,
                }
            }
        },
        ersattning: [
            {
                ersattning_id: `ers-${handlaggningId}-1`,
                ersattningstyp: "HUNDBIDRAG",
                omfattning_procent: 100,
                belopp: 40000,
                berakningsgrund: 40000,
                beslutsutfall: "FU",
                avslagsanledning: "",
                from: "2025-01-10T00:00:00Z",
                tom: "2025-01-10T23:59:59Z",
            }
        ]
    };
}

app.get("/api/health", (req, res) => {
    res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// Hämta beslutsdata
app.get("/api/regel/bekraftabeslut/:handlaggningId", async (req, res) => {
    const { handlaggningId } = req.params;
    try {
        const response = await fetch(
            `${BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}`
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        res.status(response.status).json(await response.json());
    } catch (error) {
        console.warn(`[FALLBACK] Using mock data for handlaggningId: ${handlaggningId}`);
        res.json(getMockBeslutsdata(handlaggningId));
    }
});

// Bekräfta beslut (done)
app.post("/api/regel/bekraftabeslut/:handlaggningId/done", async (req, res) => {
    const { handlaggningId } = req.params;
    try {
        const response = await fetch(
            `${BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}/done`,
            { method: "POST", headers: { "Content-Type": "application/json" } }
        );
        res.status(response.status).end();
    } catch (error) {
        console.warn(`[FALLBACK] Mock done for handlaggningId: ${handlaggningId}`);
        res.status(200).end();
    }
});

// Uppdatera ersättning
app.patch("/api/regel/bekraftabeslut/:handlaggningId/ersattning/:ersattningId", async (req, res) => {
    const { handlaggningId, ersattningId } = req.params;
    try {
        const response = await fetch(
            `${BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}/ersattning/${ersattningId}`,
            { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify(req.body) }
        );
        res.status(response.status).end();
    } catch (error) {
        console.warn(`[FALLBACK] Mock patch ersattning ${ersattningId}`);
        res.status(200).end();
    }
});

app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});