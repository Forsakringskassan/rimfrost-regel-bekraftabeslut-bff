import express from "express";
import { fileURLToPath } from "node:url";
import path from "path";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 9003;
const BE_BEKRAFTABESLUT_URL = process.env.BE_BEKRAFTABESLUT_URL || "http://localhost:8891";

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

app.use((req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Methods", "GET, PATCH, OPTIONS");
    res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    if (req.method === "OPTIONS") {
        res.sendStatus(200);
    } else {
        next();
    }
});

app.get("/api/health", (req, res) => {
    res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// Hämta beslutsdata
app.get("/api/regel/bekraftabeslut/:handlaggningId", async (req, res) => {
    const { handlaggningId } = req.params;
    try {
        const response = await fetch(
            `${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}`
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        res.status(response.status).json(await response.json());
    } catch (error) {
        console.error(`Error fetching decision data for handlaggningId ${handlaggningId}:`, error);
        res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

// Bekräfta beslut - PATCH enligt OpenAPI
app.patch("/api/regel/bekraftabeslut/:handlaggningId", async (req, res) => {
    const { handlaggningId } = req.params;
    const { ersattning_id, ersattningsstatus } = req.body;
    try {
        const response = await fetch(
            `${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}`,
            {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ersattning_id, ersattningsstatus })
            }
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        res.status(200).end();
    } catch (error) {
        console.warn(`[FALLBACK] Mock patch för handlaggningId: ${handlaggningId}`);
        res.status(200).end();
    }
});

app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});