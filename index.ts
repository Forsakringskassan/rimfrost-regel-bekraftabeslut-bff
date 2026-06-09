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

// Referensdata endpoints - must be registered before /:handlaggningId to avoid route conflict
const referensdataRoutes = ["avslutstyp", "beslutstyp", "beslutsutfallstyp", "yrkandestatus"];
for (const path of referensdataRoutes) {
    app.get(`/api/regel/bekraftabeslut/${path}`, async (_req, res) => {
        try {
            const response = await fetch(`${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${path}`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            res.json(await response.json());
        } catch (error) {
            console.error(`Error fetching referensdata ${path}:`, error);
            res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
        }
    });
}

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
app.patch("/api/regel/bekraftabeslut/:handlaggningId", async (req, res) => {
    const { handlaggningId } = req.params;
    const { ersattningar, beslut } = req.body;
    if (!Array.isArray(ersattningar) || ersattningar.length === 0) {
        res.status(400).json({ error: "Bad request", message: "ersattningar must be a non-empty array" });
        return;
    }
    if (!beslut || typeof beslut !== "object" || !beslut.avslutstyp || !beslut.beslutstyp || !beslut.beslutsutfall) {
        res.status(400).json({ error: "Bad request", message: "beslut must include avslutstyp, beslutstyp, and beslutsutfall" });
        return;
    }
    try {
        const response = await fetch(
            `${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/${handlaggningId}`,
            {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(req.body)
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

app.get("/api/uppgiftsbeskrivning", async (_req, res) => {
    const backendUrl = `${BE_BEKRAFTABESLUT_URL}/regel/bekraftabeslut/utokadUppgiftsbeskrivning`;
    try {
        const response = await fetch(backendUrl);
        if (!response.ok) {
            const errorText = await response.text();
            return res.status(response.status).json({ error: "Failed to fetch from backend", details: errorText });
        }
        res.json(await response.json());
    } catch (error) {
        res.status(502).json({ error: "Backend service unavailable", message: error instanceof Error ? error.message : String(error) });
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