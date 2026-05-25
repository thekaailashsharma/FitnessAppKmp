"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.generateMessage = generateMessage;
const GEMINI_API_KEY = "AIzaSyAIrxceccZ5tX880-v9q5rZ5_bMpMsOiYc";
const GEMINI_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`;
async function generateMessage(prompt) {
    var _a, _b, _c, _d, _e, _f;
    try {
        const response = await fetch(GEMINI_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                contents: [{ parts: [{ text: prompt }] }],
                generationConfig: {
                    maxOutputTokens: 150,
                    temperature: 0.8,
                },
            }),
        });
        if (!response.ok) {
            console.error("Gemini API error:", response.status);
            return "";
        }
        const data = await response.json();
        return ((_f = (_e = (_d = (_c = (_b = (_a = data.candidates) === null || _a === void 0 ? void 0 : _a[0]) === null || _b === void 0 ? void 0 : _b.content) === null || _c === void 0 ? void 0 : _c.parts) === null || _d === void 0 ? void 0 : _d[0]) === null || _e === void 0 ? void 0 : _e.text) === null || _f === void 0 ? void 0 : _f.trim()) || "";
    }
    catch (error) {
        console.error("Gemini call failed:", error);
        return "";
    }
}
//# sourceMappingURL=gemini.js.map