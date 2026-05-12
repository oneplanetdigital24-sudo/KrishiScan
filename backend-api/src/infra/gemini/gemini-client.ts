import { config } from '../../common/config/env';
import { ApiError } from '../../common/errors/api-error';

type GeminiResponse = {
  candidates?: Array<{
    content?: { parts?: Array<{ text?: string }> };
  }>;
};

export class GeminiClient {
  private readonly base = 'https://generativelanguage.googleapis.com/v1beta';

  private async callGemini(systemInstruction: string, userText: string): Promise<string> {
    const url = `${this.base}/models/${config.geminiModel}:generateContent?key=${config.geminiApiKey}`;
    const body = {
      system_instruction: { parts: [{ text: systemInstruction }] },
      contents: [{ role: 'user', parts: [{ text: userText }] }],
      generationConfig: { temperature: 0.4, maxOutputTokens: 512, topP: 0.9 },
    };

    const resp = await fetch(url, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify(body),
    });

    if (!resp.ok) {
      if (resp.status === 429) throw new ApiError('RATE_LIMITED', 'AI rate limited', 429);
      throw new ApiError('EXTERNAL_SERVICE_ERROR', 'AI service unavailable', 503);
    }

    const json = (await resp.json()) as GeminiResponse;
    const text = json.candidates?.[0]?.content?.parts?.[0]?.text?.trim();
    if (!text) throw new ApiError('EXTERNAL_SERVICE_ERROR', 'AI returned empty output', 503);
    return text;
  }

  async generateTreatment(prompt: string): Promise<string> {
    return this.callGemini(
      'You are an agricultural expert for Indian farmers. Keep reply practical and simple. 4 parts: disease, chemical treatment, organic remedy, prevention.',
      prompt,
    );
  }

  async generateChatReply(prompt: string): Promise<string> {
    return this.callGemini(
      'You are KrishiBot. Reply only about farming topics in concise farmer-friendly language.',
      prompt,
    );
  }
}
