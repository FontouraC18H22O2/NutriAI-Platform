# 🥑 NutriAI Platform

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

O **NutriAI Platform** é um ecossistema nutricional premium, minimalista e intuitivo com tema escuro nativo. A aplicação combina inteligência artificial avançada com uma arquitetura robusta para fornecer tracking de macros, classificação de biotipo corpóreo e análise de refeições por imagem em tempo real.

---

## 🚀 Funcionalidades Principais

* **🔐 Sistema de Autenticação Híbrido:** Gestão de sessões locais e remotas com onboarding interativo.
* **🧬 Classificador de Somatotipo IA:** Questionário inteligente para identificar o biotipo (Ectomorfo, Mesomorfo, Endomorfo) e gerar planos de macros personalizados.
* **📊 Tracker de Métricas Diárias:** Painel visual para monitorização de calorias, água e peso.
* **📸 Análise de Alimentos via Câmera (Gemini API):** Captura de fotos com CameraX e extração automatizada de valores nutricionais usando IA.
* **📜 Histórico Premium:** Linha do tempo dinâmica e detalhada com o registo de todas as atividades passadas.

---

## 🛠️ Arquitetura e Tech Stack

A aplicação foi desenhada seguindo as melhores práticas de desenvolvimento Android moderno (MAD):

* **Framework de UI:** 100% Jetpack Compose com Material 3 (Premium Dark Mode).
* **Padrão Arquitetural:** MVVM (Model-View-ViewModel) com uma camada estrita de *Repository Pattern*.
* **Camada de Persistência Híbrida:** * Bases de dados locais com **Room Database** para funcionamento offline.
    * Sincronização remota via API REST usando **Retrofit** com backend MySQL/MariaDB.
* **Concorrência:** Kotlin Coroutines & Flow.

