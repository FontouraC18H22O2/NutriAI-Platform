import express from 'express';
import multer from 'multer';
import dotenv from 'dotenv';
import { GoogleGenAI } from '@google/genai';

dotenv.config();

const app = express();
const port = 8000;

// Configuração do Multer para receber a foto e guardá-la temporariamente na pasta 'uploads/'
const upload = multer({ dest: 'uploads/' });

// Inicializa o cliente oficial da SDK da Gemini (Google Gen AI)
const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

app.use(express.json());

// O endpoint exato que mapeaste no RetrofitClient do Android
app.post('/api/meals/analyze', upload.single('photo'), async (req, res) => {
    try {
        const file = req.file;
        if (!file) {
            return res.status(400).json({ error: 'Nenhuma foto foi enviada.' });
        }

        console.log(`[Backend] Foto recebida com sucesso: ${file.originalname} -> Guardada em: ${file.path}`);

        // 1. Converter o ficheiro local guardado pelo multer para o formato binário que a SDK do Gemini exige
        const fs = await import('fs');
        const imageBuffer = fs.readFileSync(file.path);
        const imagePart = {
            inlineData: {
                data: imageBuffer.toString("base64"),
                mimeType: "image/jpeg"
            },
        };

        // 2. Criar o prompt de Engenharia para guiar a IA a extrair os dados nutricionais exatos
        const prompt = `
            Analisa esta imagem de uma refeição. Identifica o tipo de refeição, estima as calorias totais, 
            as proteínas (em gramas), os carbohidratos (em gramas), as gorduras (em gramas) e faz uma lista 
            dos alimentos individuais detetados.
            
            Deves responder OBRIGATORIAMENTE apenas com um objeto JSON válido, sem qualquer formatação markdown (não uses aspas triplas de código), 
            seguindo rigorosamente esta estrutura:
            {
                "id": 1,
                "meal_type": "Nome da Refeição (ex: Pequeno Almoço Saudável)",
                "total_calories": 450,
                "protein": 25.5,
                "carbs": 40.0,
                "fats": 12.3,
                "detected_foods": ["Alimento 1", "Alimento 2"]
            }
        `;

        console.log("[Backend] A enviar imagem para o modelo Gemini...");

        // 3. Chamar o modelo multimodal (gemini-2.5-flash é ideal para velocidade e custo zero em dev)
        const response = await ai.models.generateContent({
            model: 'gemini-2.5-flash',
            contents: [prompt, imagePart],
        });

        const responseText = response.text.trim();
        console.log("[Backend] Resposta bruta da IA:", responseText);

        // 4. Parsear a string devolvida pela IA num JSON real e enviar de volta para o Android
        const cleanJson = JSON.parse(responseText);
        
        // Apaga o ficheiro temporário da pasta uploads para não encher o disco do PC
        fs.unlinkSync(file.path);

        return res.json(cleanJson);

    } catch (error) {
        console.error("[Backend] Erro fatal no processamento:", error);
        return res.status(500).json({ error: 'Erro interno ao analisar a refeição com a IA.' });
    }
});

app.listen(port, () => {
    console.log(`🚀 Servidor do NutriAI a rodar perfeitamente em http://localhost:${port}`);
});