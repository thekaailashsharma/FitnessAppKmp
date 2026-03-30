const GEMINI_API_KEY = "AIzaSyDAAoM5EaGDHMXSqkwgALTJ0hbcnIYbuGc";
const GEMINI_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`;

export async function generateMessage(prompt: string): Promise<string> {
  try {
    const response = await fetch(GEMINI_URL, {
      method: "POST",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify({
        contents: [{parts: [{text: prompt}]}],
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

    const data = await response.json() as {
      candidates?: Array<{content?: {parts?: Array<{text?: string}>}}>;
    };
    return data.candidates?.[0]?.content?.parts?.[0]?.text?.trim() || "";
  } catch (error) {
    console.error("Gemini call failed:", error);
    return "";
  }
}
