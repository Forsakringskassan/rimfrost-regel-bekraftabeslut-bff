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
        res.status(response.status).json(await response.json());
    } catch (error) {
        console.error("Error fetching beslutsdata:", error);
        res.status(500).json({ error: "Internal server error" });
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
        console.error("Error posting done:", error);
        res.status(500).json({ error: "Internal server error" });
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
        console.error("Error patching ersattning:", error);
        res.status(500).json({ error: "Internal server error" });
    }
});

app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});