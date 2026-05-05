import { transformBackendResponse } from '#utils/transformBackendResponse.js';
import express from 'express';

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
    res.header("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
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
app.post("/api/regel/bekraftabeslut", async (req, res) => {
    const { handlaggningId } = req.body;
    try {
        const response = await fetch(
            `${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}`
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = await response.json();
        res.json(transformBackendResponse(data));
    } catch (error) {
        console.error(`Error fetching decision data for handlaggningId ${handlaggningId}:`, error);
        res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

// Bekräfta beslut - PATCH enligt OpenAPI
app.patch("/api/regel/bekraftabeslut", async (req, res) => {
    const { handlaggningId, ersattningId, yrkandestatus } = req.body;
    const patchBody = JSON.stringify({ ersattning_id: ersattningId, yrkandestatus });
    try {
        const response = await fetch(
            `${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}`,
            {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: patchBody
            }
        );
        if (!response.ok) {
            const errorBody = await response.text();
            console.error(`Backend returned ${response.status}: ${errorBody}`);
            throw new Error(`HTTP ${response.status}`);
        }
        res.status(200).end();
    } catch (error) {
        console.error(`Error patching decision data for handlaggningId ${handlaggningId}:`, error);
        res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

app.post("/api/regel/bekraftabeslut/done", async (req, res) => {
    const { handlaggningId } = req.body;
    try {
        const response = await fetch(
            `${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}/done`,
            {
                method: "POST",
                headers: {
                    ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
                },
            }
        );
        if (!response.ok) {
            const errorBody = await response.text();
            console.error(`Backend /done returned ${response.status}: ${errorBody}`);
            throw new Error(`HTTP ${response.status}`);
        }
        res.status(204).end();
    } catch (error) {
        console.error(`Error calling /done for handlaggningId ${handlaggningId}:`, error);
        res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});