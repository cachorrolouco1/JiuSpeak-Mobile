# JIUSPEAK MOBILE V4 — BLUEPRINT ESTRATÉGICO DE CRESCIMENTO E ARQUITETURA
## SISTEMA DE RETENÇÃO, ENGAJAMENTO, GAMIFICAÇÃO E EXPANSÃO GLOBAL

Este documento define a arquitetura técnica completa, o banco de dados local/remoto, a especificação de APIs, o fluxo de Socket.IO, a estrutura Android/Web e o Roadmap de crescimento internacional para as novas funcionalidades de Gamificação e Retenção do JiuSpeak.

---

## 1. ARQUITETURA DE SISTEMAS (MÉTODO LIMPO / CLEAN MVVM)

Seguindo o padrão de design e a base técnica atual da plataforma JiuSpeak, o fluxo de dados respeitará a separação hermética de responsabilidades entre Interface de Usuário, Repositórios e Serviços de Dados.

```
       [ JETPACK COMPOSE UI LAYER ]
                   │
           [ VIEWMODEL LAYER ]  ◄── (StateFlow & UI States)
                   │
         [ REPOSITORY PATTERN ]
             /           \
  [ LOCAL ROOM DB ]    [ RETROFIT / OKHTTP API CLIENT ]
```

---

## 2. ESQUEMA DE BANCO DE DADOS LOCAL (ROOM DATABASE ENTITIES)

Toda a persistência local necessária para suportar o funcionamento offline e o cache rápido no celular físico será implementada no banco de dados local.

### 2.1 Entidade de Temporada (Season Pass)

```kotlin
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "seasons")
data class SeasonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val themeColorHex: String,
    val bannerUrl: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val maxLevel: Int,
    val currentLevel: Int,
    val currentXp: Int,
    val requiredXpNextLevel: Int,
    val hasVipPass: Boolean,
    val hasProPass: Boolean
) : Serializable
```

### 2.2 Entidade de Recompensa de Nível (Season Rewards)

```kotlin
@Entity(tableName = "season_rewards")
data class SeasonRewardEntity(
    @PrimaryKey val id: String,
    val seasonId: String,
    val level: Int,
    val type: String, // "AVATAR", "FRAME", "MEDAL", "JIU_TICKETS", "BOOSTER", "TITLE"
    val name: String,
    val isPremium: Boolean, // Requere VIP ou PRO
    val rewardsValue: Int,
    val isClaimed: Boolean,
    val imageUrl: String
)
```

### 2.3 Entidade de Clã/Equipe (Clans)

```kotlin
@Entity(tableName = "clans")
data class ClanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logoUrl: String,
    val country: String,
    val city: String,
    val gymName: String,
    val masterName: String,
    val description: String,
    val memberCount: Int,
    val maxMembers: Int,
    val totalXp: Int,
    val rankPosition: Int,
    val myRole: String // "LEADER", "CO_LEADER", "CAPTAIN", "MEMBER", "NONE"
)
```

### 2.4 Entidade de Liga Mundial (League Status)

```kotlin
@Entity(tableName = "league_status")
data class LeagueEntity(
    @PrimaryKey val id: String,
    val currentElo: Int,
    val division: String, // "BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "LEGEND"
    val subDivision: Int, // 1, 2, 3, 4
    val promotionGoalElo: Int,
    val rebaixamentoThresholdElo: Int,
    val globalRank: Int,
    val countryRank: Int,
    val winsCount: Int,
    val lossesCount: Int
)
```

### 2.5 Entidade de Conquistas e Medalhas (Achievements)

```kotlin
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // "STUDY", "PVP", "COMMUNITY", "MARKETPLACE", "SEASONS"
    val rarity: String, // "COMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC"
    val progress: Int,
    val targetProgress: Int,
    val isCompleted: Boolean,
    val xpReward: Int,
    val jiuTicketsReward: Int,
    val iconUrl: String,
    val unlockedAtTimestamp: Long
)
```

### 2.6 Entidade de Reels de Técnica

```kotlin
@Entity(tableName = "reels_videos")
data class ReelsEntity(
    @PrimaryKey val id: String,
    val videoUrl: String,
    val title: String,
    val description: String,
    val userName: String,
    val userAvatar: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean,
    val techniqueCategory: String // "TAKEDOWNS", "PASSING", "GUARDS", "SUBMISSIONS", "VOCABULARY", "PRONUNCIATION"
)
```

---

## 3. ESQUEMA DE APIS E DTOS DE REDE (RETROFIT SERVICE BLUEPRINT)

Todas as requisições à nuvem serão feitas de forma assíncrona utilizando `Retrofit` e injetadas de forma limpa nos novos flows reativos.

### 3.1 DTOs das APIs

```kotlin
package com.example.data.network.dto

// Season Pass Schemas
data class SeasonDto(
    val id: String,
    val name: String,
    val description: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val rewards: List<RewardDto>
)

data class RewardDto(
    val id: String,
    val level: Int,
    val name: String,
    val type: String,
    val isPremium: Boolean,
    val isClaimed: Boolean
)

// Clans Schemas
data class ClanDto(
    val id: String,
    val name: String,
    val logoUrl: String,
    val gymName: String,
    val memberCount: Int,
    val totalXp: Int
)

data class CreateClanRequest(
    val name: String,
    val description: String,
    val gymName: String,
    val country: String,
    val city: String
)

// World League Schemas
data class LeagueProgressDto(
    val elo: Int,
    val division: String,
    val rank: Int
)

// Reels Schemas
data class ReelsVideoDto(
    val id: String,
    val title: String,
    val videoUrl: String,
    val likes: Int,
    val author: String
)
```

### 3.2 Novo Endpoints definidos em `JiuSpeakApi`

```kotlin
interface JiuSpeakApiV4 {

    // === SEASON PASS ===
    @GET("api/seasons/active")
    suspend fun getActiveSeason(@Header("Authorization") token: String): SeasonDto

    @POST("api/seasons/claim-reward")
    suspend fun claimReward(
        @Header("Authorization") token: String, 
        @Body request: ClaimRewardRequest
    ): Response<Void>


    // === CLANS / EQUIPES ===
    @GET("api/clans/list")
    suspend fun getClans(@Header("Authorization") token: String): List<ClanDto>

    @POST("api/clans/create")
    suspend fun createClan(
        @Header("Authorization") token: String,
        @Body request: CreateClanRequest
    ): ClanDto

    @POST("api/clans/{clanId}/join")
    suspend fun joinClan(
        @Header("Authorization") token: String,
        @Path("clanId") clanId: String
    ): Response<Void>


    // === WORLD LEAGUE ===
    @GET("api/leagues/status")
    suspend fun getLeagueStatus(@Header("Authorization") token: String): LeagueProgressDto


    // === REELS VIDEOS ===
    @GET("api/reels")
    suspend fun getReels(@Header("Authorization") token: String): List<ReelsVideoDto>

    @POST("api/reels/{id}/like")
    suspend fun likeReel(
        @Header("Authorization") token: String, 
        @Path("id") reelId: String
    ): Response<Void>
}
```

---

## 4. FLUXO REAL-TIME MULTIPLAYER (SOCKET.IO PROTOCOL)

O módulo de batalhas interativas PVP, Chats Coletivos e Convites no celular físico será orquestrado assincronamente através de Socket.IO conectado a `https://www.jiuspeak.com.br`.

### 4.1 Eventos Recebidos (Listen)

| Evento de Rede | Payload JSON | finalidade |
| :--- | :--- | :--- |
| `pvp:match_found` | `{ "opponentName": "Charles", "matchType": "Vocabulary", "questions": [...] }` | Dispara tela de batalha em tempo real |
| `pvp:invite_received`| `{ "host": "Mestre_Lacerda", "matchType": "Grammar" }` | Emite notificação push em tempo real de desafio |
| `clan:chat_message` | `{ "userId": "123", "userName": "Leo", "text": "Quem vai treinar hoje?" }` | Sincroniza o chat coletivo interno do clã |
| `leaderboard:update`| `{ "userId": "321", "newXp": 8900 }` | Atualiza o ranking dinamicamente sem forçar reload |

### 4.2 Eventos Enviados (Emit)

```javascript
// Localizar partida na Arena de Lutas
socket.emit("pvp:join_queue", { matchType: "Vocabulary", belt: "BROWN" });

// Submeter resposta certa para aplicar dano/pontuação ao oponente
socket.emit("pvp:submit_answer", { isCorrect: true, responseTimeMs: 1200 });

// Enviar desafio direto a uma academia rival
socket.emit("clan:challenge_gym", { targetClanId: "clan_456", format: "5v5" });
```

---

## 5. ESTRUTURA WEB E PAINEL ADMINISTRATIVO FOR GROWTH CONTROL

O administrador da plataforma JiuSpeak controla toda a economia, modera canais e agenda mentorias diretamente do painel corporativo em ReactJS, que sincroniza em tempo real via PostgreSQL. Let's design the React interface endpoints:

*   **Painel de Temporadas:** CRUD completo de temporadas que insere bônus de XP sazonais diretamente no banco.
*   **Gerenciador de Clãs:** Moderação, verificação de academias legítimas com selo oficial JiuSpeak e estatísticas agregadas de XP coletivo.
*   **Moderação de Reels:** Player interativo para o administrador aprovar ou banir vídeos enviados por professores.
*   **Editor de Notificações Push:** Filtro avançado para segmentar e engajar usuários com base no tempo de inatividade.

---

## 6. FLUXOS E MECÂNICAS DE RETENÇÃO E GROWTH

Para que o aplicativo alcance níveis superiores de engajamento diário (DAU) e tempo médio de sessão no celular físico, as seguintes mecânicas foram projetadas estruturalmente:

```
[ RETENÇÃO INICIAL ] ──► Login Diário + Multiplicador de Ofensiva (Streak)
       │
[ RETENÇÃO SOCIAL ]  ──► Sistema de Clãs (Cobrança saudável entre parceiros de tatame)
       │
[ ENGAJAMENTO COMP. ] ─► Liga Mundial + Desafio PvP entre Academias
       │
[ MONETIZAÇÃO ]      ──► Passe de Temporada VIP/PRO + Mentorias com Mestres Internacionais
```

*   **Login Diário Progressivo:** No 1º dia concede 5 JiuTickets. No dia 7, concede uma Caixa de Recompensa Épica com 1 moldura exclusiva.
*   **Notificações Push Reativas:** Se o usuário começa a perder posição no ranking global, o motor de push gera alertas automáticos.
*   **Growth entre Academias:** Alunos estimulam outros alunos a entrar no clã da academia ("Guerra de XP coletivo contra a academia rival"), criando um funil de entrada viral e orgânico sem custo de tráfego pago.

---

## 7. ROADMAP CONCRETO DE IMPLEMENTAÇÃO EM 5 FASES

```
┌────────────────────────────────────────────────────────┐
│ FASE 1: MVP GLOBAL (Sincronização de Temporadas & API) │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ FASE 2: COMUNIDADE (Clãs, Academias e Chat Coletivo)   │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ FASE 3: COMPETIÇÃO (Liga Mundial, Divisões de ELO)      │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ FASE 4: MONETIZAÇÃO (Season Pass VIP/PRO & Mentorias)  │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ FASE 5: EXPANSÃO (Push Inteligente, Reels de Técnicas) │
└────────────────────────────────────────────────────────┘
```

---

## 8. CONFIGURAÇÕES FINAIS E CONEXÕES VALIDADA

O aplicativo foi configurado para comunicação e deploy reais utilizando as rotas oficiais:

*   **Base URL (APIs):** `https://www.jiuspeak.com.br/api/`
*   **Socket.IO Server:** `https://www.jiuspeak.com.br`
*   **Serviço de Push (FCM):** `com.example.data.network.MyFirebaseMessagingService`

O aplicativo está 100% livre de referências locais ou simuladas direcionadas a localhost ou emuladores.

`STATUS DO PROJETO: ✅ PRONTO PARA TESTE NO CELULAR FÍSICO`
