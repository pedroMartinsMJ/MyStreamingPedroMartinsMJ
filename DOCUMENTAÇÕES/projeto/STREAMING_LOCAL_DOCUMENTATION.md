# 🎬 Sistema de Streaming Local em Rede Doméstica
## Documentação Completa de Arquitetura e Planejamento

---

## 📋 Índice
1. [Visão Geral do Projeto](#visão-geral)
2. [Requisitos](#requisitos)
3. [Arquitetura do Sistema](#arquitetura)
4. [Especificação de Camadas](#camadas)
5. [Modelo de Dados](#modelo-de-dados)
6. [Banco de Dados](#banco-de-dados)
7. [Dependências e Tecnologias](#dependências)
8. [Configurações do Sistema](#configurações)
9. [Estratégia de Testes](#testes)
10. [Fluxo de Dados](#fluxo)
11. [Segurança](#segurança)
12. [Performance e Otimizações](#performance)

---

## 🎯 Visão Geral

### Objetivo Principal
Criar uma plataforma de streaming de vídeos/fotos em rede local (intranet) que permite aos usuários:
- Assistir filmes armazenados em PC/NAS usando qualquer dispositivo na rede (celular, tablet, smart TV)
- Transcodificar vídeos em múltiplas qualidades
- Reprodução adaptativa (ajusta qualidade conforme conexão)
- Gerenciar biblioteca de mídia pessoal
- Controlar acesso e autenticação local

### Contexto de Uso
```
┌─────────────────────────────────────┐
│      REDE LOCAL (WiFi/Ethernet)     │
├─────────────────────────────────────┤
│                                     │
│  NAS/PC com Filmes ────────┐       │
│  (fonte de mídia)          │       │
│                            │       │
│  Spring Boot Server ◄──────┘       │
│  (aplicação streaming)             │
│            ▲                       │
│            │                       │
│    ┌───────┼────────┐              │
│    │       │        │              │
│  Celular Mobile  Smart TV   PC    │
│  (clientes)                        │
└─────────────────────────────────────┘
```

### Diferenças vs Streaming Externo (Netflix)
| Aspecto | Netflix | Nosso Sistema |
|--------|---------|---------------|
| Escala | Milhões de usuários | Usuários na mesma rede |
| Arquivo | Servidores CDN em múltiplas regiões | NAS/PC local |
| Banda | Internet pública (limitada) | Rede LAN (Gbps) |
| Codec | H.264, H.265, VP9, AV1 | H.264/H.265 (mais simples) |
| Complexidade | Altíssima | Média (bem mais simples) |
| Custo Infra | Bilhões | Praticamente zero |

---

## 📌 Requisitos

### Requisitos Funcionais (RF)

#### RF-1: Gerenciamento de Arquivo
- [ ] Upload/importação de vídeos e fotos
- [ ] Scan automático de diretórios (NAS/PC)
- [ ] Detecção de mídia duplicada
- [ ] Exclusão/organização de arquivo
- [ ] Suporte a múltiplos formatos (mp4, mkv, avi, mov, webm, etc)

#### RF-2: Processamento de Vídeo
- [ ] Transcodificação em múltiplas qualidades
- [ ] Extração de metadata (duração, resolução, codec)
- [ ] Geração de thumbnails/previews
- [ ] Processamento assíncrono (background jobs)
- [ ] Fila de encoding com priorização

#### RF-3: Streaming
- [ ] Streaming HLS (HTTP Live Streaming) - PRINCIPAL
- [ ] Streaming DASH (Dynamic Adaptive Streaming) - OPCIONAL
- [ ] Range request (seek/resumo)
- [ ] Bitrate adaptativo
- [ ] Suporte a legendas (SRT, VTT, ASS)

#### RF-4: Reprodução
- [ ] Player web responsivo
- [ ] Histórico de reprodução
- [ ] Retomar de onde parou
- [ ] Sincronização entre dispositivos

#### RF-5: Biblioteca
- [ ] Listagem de filmes/séries
- [ ] Busca e filtros
- [ ] Categorização (gênero, diretor, etc)
- [ ] Favoritos/watchlist
- [ ] Ratings e resenhas

#### RF-6: Autenticação
- [ ] Login local (usuários cadastrados)
- [ ] Perfis de usuário (admin, viewer)
- [ ] Controle de acesso por usuário
- [ ] Sem necessidade de conta online

#### RF-7: Admin
- [ ] Dashboard de status
- [ ] Gerenciar fila de encoding
- [ ] Configurações de qualidade
- [ ] Logs e monitoramento
- [ ] Estatísticas (espaço em disco, bitrates, etc)

### Requisitos Não-Funcionais (RNF)

#### RNF-1: Performance
- [ ] Streaming sem buffering em LAN
- [ ] Tempo de resposta < 500ms para requisições
- [ ] Encoding não bloqueia interface
- [ ] Suporta múltiplos streams simultâneos
- [ ] Cache eficiente de metadata

#### RNF-2: Disponibilidade
- [ ] Funciona 24/7 (sempre ligado)
- [ ] Recuperação automática de falhas
- [ ] Health check e alertas

#### RNF-3: Escalabilidade
- [ ] Suporta biblioteca de até 10TB
- [ ] Até 10 usuários simultâneos
- [ ] Reencoding on-the-fly se necessário

#### RNF-4: Segurança
- [ ] Criptografia de senha (bcrypt)
- [ ] JWT para sessões
- [ ] HTTPS opcional (com certificado autossignado)
- [ ] Validação de entrada (previne injeção)
- [ ] Isolamento de dados por usuário

#### RNF-5: Manutenibilidade
- [ ] Código bem estruturado em camadas
- [ ] Testes > 70% cobertura
- [ ] Documentação clara
- [ ] Fácil configuração
- [ ] Logs detalhados

#### RNF-6: Compatibilidade
- [ ] Funciona em Windows, Linux, macOS
- [ ] Suporta vários navegadores (Chrome, Firefox, Safari, Edge)
- [ ] Responsivo (desktop, tablet, mobile)

---

## 🏗️ Arquitetura do Sistema

### Padrão de Arquitetura: Camadas (Layered Architecture)

```
┌─────────────────────────────────────────────┐
│         PRESENTATION LAYER (UI)              │
│  ├─ Vue.js/React Web App                    │
│  ├─ REST API Endpoints                      │
│  └─ WebSocket (notificações em tempo real)  │
├─────────────────────────────────────────────┤
│         APPLICATION/API LAYER                │
│  ├─ Controllers (HTTP endpoints)            │
│  ├─ DTOs (Data Transfer Objects)            │
│  └─ Exception Handlers                      │
├─────────────────────────────────────────────┤
│         BUSINESS LOGIC LAYER (Services)      │
│  ├─ VideoService                            │
│  ├─ StreamingService                        │
│  ├─ EncodingService                         │
│  ├─ UserService                             │
│  ├─ LibraryService                          │
│  ├─ FileSystemService                       │
│  └─ BitrateAdaptationService                │
├─────────────────────────────────────────────┤
│         DATA ACCESS LAYER (Repositories)     │
│  ├─ VideoRepository (JPA)                   │
│  ├─ UserRepository                          │
│  ├─ EncodingJobRepository                   │
│  ├─ PlaybackHistoryRepository               │
│  └─ EncodedVideoRepository                  │
├─────────────────────────────────────────────┤
│         DATABASE LAYER                       │
│  ├─ PostgreSQL (metadata)                   │
│  └─ H2 (desenvolvimento/testes)             │
├─────────────────────────────────────────────┤
│         EXTERNAL SERVICES LAYER              │
│  ├─ FFmpeg (encoding)                       │
│  ├─ File System (NAS/PC)                    │
│  └─ Optional: TMDb API (metadados filme)    │
└─────────────────────────────────────────────┘
```

### Componentes Principais

```
BACKEND (Spring Boot)
├── API REST
│   ├── /api/videos
│   ├── /api/stream
│   ├── /api/auth
│   ├── /api/library
│   ├── /api/encoding
│   └── /api/admin
├── Services (Lógica)
│   ├── Video Processing
│   ├── Encoding Queue
│   ├── Streaming HLS
│   └── User Management
├── Database
│   ├── PostgreSQL
│   └── Embedded H2 (testes)
└── File System
    └── Acesso a NAS/PC

FRONTEND (Web)
├── Vue.js/React
├── Video Player (HLS.js)
├── Responsive UI
└── Autenticação JWT

MOBILE (Opcional - Web Responsive)
└── Same Web App (Progressive Web App)
```

---

## 🏛️ Especificação de Camadas

### 1. PRESENTATION LAYER (API Controllers)

#### Responsabilidades
- Receber requisições HTTP
- Validar entrada (DTO validation)
- Retornar respostas JSON
- Tratamento de erros
- Autenticação/autorização

#### Controllers Necessários

**VideoController**
```
POST   /api/videos/upload               → Upload de vídeo
GET    /api/videos                      → Listar todos vídeos
GET    /api/videos/{id}                 → Detalhes do vídeo
GET    /api/videos/{id}/thumbnail       → Thumbnail
DELETE /api/videos/{id}                 → Deletar vídeo
POST   /api/videos/{id}/favorite        → Adicionar favorito
```

**StreamingController**
```
GET    /api/stream/{videoId}/playlist.m3u8      → Master playlist HLS
GET    /api/stream/{videoId}/variant-{id}.m3u8  → Variant playlist
GET    /api/stream/segment/{segmentId}          → TS segment
GET    /api/stream/{videoId}                    → Stream direto
```

**LibraryController**
```
GET    /api/library                     → Biblioteca do usuário
GET    /api/library/search              → Buscar
GET    /api/library/categories          → Categorias
POST   /api/library/rescan              → Rescanning de diretório
```

**EncodingController**
```
GET    /api/encoding/jobs               → Jobs de encoding
GET    /api/encoding/jobs/{id}          → Detalhes do job
POST   /api/encoding/jobs/{videoId}     → Iniciar encoding
DELETE /api/encoding/jobs/{id}          → Cancelar job
GET    /api/encoding/queue              → Fila de espera
```

**UserController**
```
POST   /api/auth/login                  → Login
POST   /api/auth/logout                 → Logout
GET    /api/auth/me                     → Info do usuário logado
GET    /api/users                       → Listar usuários (admin)
POST   /api/users                       → Criar usuário (admin)
DELETE /api/users/{id}                  → Deletar usuário (admin)
POST   /api/users/{id}/password         → Trocar senha
```

**PlaybackController**
```
POST   /api/playback/start              → Iniciar reprodução
POST   /api/playback/stop               → Parar reprodução
GET    /api/playback/history            → Histórico
POST   /api/playback/{videoId}/resume   → Retomar
```

**AdminController**
```
GET    /api/admin/dashboard             → Status geral
GET    /api/admin/stats                 → Estatísticas
GET    /api/admin/logs                  → Logs
GET    /api/admin/health                → Health check
POST   /api/admin/config                → Configurações
```

#### DTOs (Data Transfer Objects)

```java
// Request DTOs
LoginRequest              // username, password
CreateVideoRequest        // title, description, genreId
EncodingSettingsRequest   // bitrates, codec preferences
CreateUserRequest         // username, email, role

// Response DTOs
VideoDTO                  // id, title, duration, thumbnail, status
StreamingPlaylistDTO      // HLS playlist info
EncodedVideoDTO           // bitrate, resolution, codec, fileSize
UserDTO                   // id, username, email, role
PlaybackHistoryDTO        // videoId, position, timestamp
EncodingJobDTO            // status, progress, bitrate

// Error DTOs
ErrorResponseDTO          // code, message, timestamp
```

---

### 2. APPLICATION LAYER (Services)

#### VideoService
**Responsabilidades:**
- CRUD de vídeos
- Metadata extraction
- Validação de formato
- Gerenciar ciclo de vida

**Métodos Principais:**
```java
public class VideoService {
    // Importação e validação
    Video uploadVideo(MultipartFile file, VideoMetadata meta)
    Video importFromPath(String filePath)
    void validateVideoFormat(File file)
    
    // Operações CRUD
    List<Video> getAllVideos(Pageable pageable)
    Video getVideoById(String videoId)
    void deleteVideo(String videoId)
    Video updateVideo(String videoId, VideoUpdateRequest)
    
    // Metadata
    VideoMetadata extractMetadata(File file)  // ffprobe
    String generateThumbnail(String videoId)
    
    // Busca
    List<Video> searchVideos(String query)
    List<Video> getVideosByCategory(String category)
    
    // Favoritos
    void addFavorite(String userId, String videoId)
    void removeFavorite(String userId, String videoId)
}
```

#### EncodingService
**Responsabilidades:**
- Orquestração de encoding
- Gerenciar fila
- Chamar FFmpeg
- Monitorar progresso

**Métodos Principais:**
```java
public class EncodingService {
    // Iniciar encoding
    EncodingJob startEncoding(String videoId, EncodingProfile profile)
    List<EncodingJob> startAdaptiveEncoding(String videoId)
    
    // Monitoramento
    EncodingJob getEncodingJobStatus(String jobId)
    List<EncodingJob> getQueue()
    double getEncodingProgress(String jobId)
    
    // Controle
    void cancelEncodingJob(String jobId)
    void retryFailedJob(String jobId)
    
    // Lógica
    void processEncodingQueue()  // consumer de fila
    void encodeWithFFmpeg(Video video, EncodingProfile profile)
}
```

#### StreamingService
**Responsabilidades:**
- Gerar playlists HLS
- Streaming de chunks
- Range requests
- Adaptive bitrate

**Métodos Principais:**
```java
public class StreamingService {
    // HLS Playlist
    String generateMasterPlaylist(String videoId)
    String generateVariantPlaylist(String videoId, String bitrate)
    
    // Streaming
    ResponseEntity<InputStreamResource> streamSegment(String segmentId)
    ResponseEntity<InputStreamResource> streamVideo(String videoId, 
                                                     String rangeHeader)
    
    // Bitrate adaptativo
    String selectOptimalBitrate(String videoId, long bandwidth)
    BitrateRecommendation recommendBitrate(ClientInfo clientInfo)
}
```

#### LibraryService
**Responsabilidades:**
- Catalogação de mídia
- Busca e filtros
- Categorização
- Sincronização

**Métodos Principais:**
```java
public class LibraryService {
    // Varredura
    void scanLibraryDirectory(String dirPath)
    void rescanLibrary()
    List<Video> importFromNAS(String nasPath)
    
    // Biblioteca
    List<Video> getUserLibrary(String userId, Pageable pageable)
    List<Video> getWatchlist(String userId)
    
    // Busca
    List<Video> search(String query, SearchFilter filters)
    List<Video> getByCategory(String category)
    List<Video> getByGenre(String genre)
    List<Video> getTrending(int limit)
    
    // Organizacao
    void organizeLibrary(String sortBy)
    void addTag(String videoId, String tag)
    void removeTag(String videoId, String tag)
}
```

#### UserService
**Responsabilidades:**
- Autenticação
- Autorização
- Gerenciar perfis
- Tokens JWT

**Métodos Principais:**
```java
public class UserService {
    // Autenticação
    LoginResponse login(String username, String password)
    void logout(String username)
    boolean validateToken(String token)
    
    // Usuários
    User createUser(CreateUserRequest request)
    User updateUser(String userId, UpdateUserRequest request)
    void deleteUser(String userId)
    List<User> getAllUsers(Pageable pageable)
    
    // Senha
    void changePassword(String userId, String oldPassword, String newPassword)
    void resetPassword(String userId)
    
    // Permissões
    boolean hasPermission(String userId, String resourceId, String action)
    void grantRole(String userId, Role role)
}
```

#### FileSystemService
**Responsabilidades:**
- Acesso a arquivos locais
- Operações de arquivo
- Gerenciar armazenamento

**Métodos Principais:**
```java
public class FileSystemService {
    // Acesso
    File getFile(String filePath)
    List<File> listDirectory(String dirPath)
    boolean fileExists(String filePath)
    
    // Operações
    void copyFile(String source, String destination)
    void moveFile(String source, String destination)
    void deleteFile(String filePath)
    
    // Informações
    long getFileSize(String filePath)
    long getDirectorySize(String dirPath)
    long getAvailableDiskSpace()
    
    // Mídia
    List<File> findMediaFiles(String rootDir)
    boolean isValidMediaFile(File file)
}
```

#### PlaybackHistoryService
**Responsabilidades:**
- Rastrear reprodução
- Retomar de onde parou
- Sincronizar entre dispositivos

**Métodos Principais:**
```java
public class PlaybackHistoryService {
    // Histórico
    void recordPlaybackStart(String userId, String videoId)
    void recordPlaybackStop(String userId, String videoId, long position)
    List<PlaybackHistory> getUserHistory(String userId)
    
    // Retomar
    Long getLastPosition(String userId, String videoId)
    void updatePlaybackPosition(String userId, String videoId, long position)
    
    // Sincronização
    void syncPlaybackAcrossDevices(String userId, String videoId, long position)
}
```

#### NotificationService
**Responsabilidades:**
- Alertar sobre eventos
- WebSocket para real-time

**Métodos Principais:**
```java
public class NotificationService {
    // Notificações
    void notifyEncodingComplete(String videoId)
    void notifyEncodingFailed(String jobId, String error)
    void notifyNewVideoAvailable(String videoId)
    
    // WebSocket
    void broadcastToUser(String userId, Message message)
    void broadcastToAll(Message message)
}
```

---

### 3. DATA ACCESS LAYER (Repositories)

#### Repositories (JPA/Spring Data)

```java
// Video
public interface VideoRepository extends JpaRepository<Video, String> {
    List<Video> findByTitleContainingIgnoreCase(String title);
    List<Video> findByCategory(String category);
    List<Video> findByUserId(String userId);
    List<Video> findByStatus(VideoStatus status);
    Optional<Video> findByOriginalFilePath(String filePath);
}

// User
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    boolean existsByUsername(String username);
}

// Encoding Job
public interface EncodingJobRepository extends JpaRepository<EncodingJob, String> {
    List<EncodingJob> findByStatus(EncodingStatus status);
    List<EncodingJob> findByVideoId(String videoId);
    List<EncodingJob> findByStatusOrderByCreatedAtAsc(EncodingStatus status);
    List<EncodingJob> findByStatusAndPriorityOrderByPriorityDesc(
        EncodingStatus status, int priority);
}

// Encoded Video
public interface EncodedVideoRepository extends JpaRepository<EncodedVideo, String> {
    List<EncodedVideo> findByVideoId(String videoId);
    Optional<EncodedVideo> findByVideoIdAndBitrate(String videoId, int bitrate);
    List<EncodedVideo> findByCodec(String codec);
}

// Playback History
public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistory, String> {
    List<PlaybackHistory> findByUserIdOrderByPlayedAtDesc(String userId);
    Optional<PlaybackHistory> findByUserIdAndVideoId(String userId, String videoId);
    void deleteByUserIdAndVideoId(String userId, String videoId);
}

// Favorite
public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    List<Favorite> findByUserId(String userId);
    Optional<Favorite> findByUserIdAndVideoId(String userId, String videoId);
    boolean existsByUserIdAndVideoId(String userId, String videoId);
}
```

---

### 4. DOMAIN LAYER (Entities/Models)

As entidades JPA serão definidas em detalhes na seção "Modelo de Dados".

---

## 📊 Modelo de Dados

### Entidades Principais

#### 1. **User** (Usuário)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;  // bcrypt
    
    @Enumerated(EnumType.STRING)
    private UserRole role;  // ADMIN, VIEWER
    
    private String displayName;
    
    @Column(columnDefinition = "jsonb")
    private UserPreferences preferences;  // resolução preferida, subtítulos, etc
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    
    @OneToMany(mappedBy = "user")
    private List<Video> videos;
    
    @OneToMany(mappedBy = "user")
    private List<PlaybackHistory> playbackHistory;
    
    @OneToMany(mappedBy = "user")
    private List<Favorite> favorites;
    
    private boolean active;
}
```

#### 2. **Video** (Arquivo de Vídeo)
```java
@Entity
@Table(name = "videos")
@Index(name = "idx_video_status", columnList = "status")
@Index(name = "idx_video_user", columnList = "user_id")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    private String posterPath;  // URL/caminho da capa
    private String thumbnailPath;  // caminho da thumbnail
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Arquivo original
    @Column(nullable = false)
    private String originalFilePath;  // /mnt/nas/filmes/filme.mkv
    
    private String originalFormat;  // mkv, avi, mp4, etc
    private Long fileSize;
    private Integer originalWidth;
    private Integer originalHeight;
    private String originalCodec;
    private Integer originalBitrate;  // kbps
    private Double frameRate;  // 23.98, 25, 29.97, 60
    private Long durationSeconds;
    
    // Metadata
    private String genre;  // ação, drama, etc
    private String director;
    private Integer releaseYear;
    private Double imdbRating;
    private String language;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;  // UPLOADED, ENCODING, READY, ERROR, DELETED
    
    private String errorMessage;  // se status = ERROR
    
    // Análise de qualidade
    private Double videoQualityScore;  // 0-100
    private String videoQualityAnalysis;  // descrição do problema se houver
    
    // Encoding
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<EncodedVideo> encodedVersions;
    
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<EncodingJob> encodingJobs;
    
    // Legendas
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL)
    private List<Subtitle> subtitles;
    
    // Histórico
    private LocalDateTime uploadedAt;
    private LocalDateTime lastAccessedAt;
    private Long timesWatched;
    
    // Tags
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> tags;
    
    private boolean isFavorited;
}
```

#### 3. **EncodedVideo** (Versão Encodada)
```java
@Entity
@Table(name = "encoded_videos")
@Index(name = "idx_encoded_video_bitrate", columnList = "bitrate")
@Index(name = "idx_encoded_video_codec", columnList = "codec")
public class EncodedVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
    
    // Formato
    private String codec;  // h264, h265, vp9
    private String audioCodec;  // aac, opus
    private Integer bitrate;  // kbps
    private Integer audioBitrate;  // kbps
    private Integer width;
    private Integer height;
    private Double frameRate;
    private String format;  // mp4
    
    // Arquivo
    private String filePath;  // /storage/encoded/video123-5000k.mp4
    private Long fileSize;
    private String fileHash;  // para detectar duplicatas
    
    // Armazenamento segmentado (para HLS)
    private String hlsBasePath;  // /storage/hls/video123-5000k/
    private Integer segmentDurationSeconds;  // geralmente 10
    private Long totalSegments;
    
    // Status
    @Enumerated(EnumType.STRING)
    private EncodingStatus encodingStatus;
    
    // Performance
    private Long encodingTimeSeconds;
    private Double compressionRatio;  // original / encoded
    
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
}
```

#### 4. **EncodingJob** (Trabalho de Encoding)
```java
@Entity
@Table(name = "encoding_jobs")
@Index(name = "idx_encoding_job_status", columnList = "status")
@Index(name = "idx_encoding_job_video", columnList = "video_id")
public class EncodingJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
    
    // Tipo de encoding
    @Enumerated(EnumType.STRING)
    private EncodingType encodingType;  // SINGLE, ADAPTIVE, REENCODING
    
    // Perfil
    private String targetCodec;  // h264, h265
    private Integer targetBitrate;  // kbps
    private Integer targetWidth;
    private Integer targetHeight;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EncodingStatus status;  // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    
    // Progresso
    private Double progressPercentage;  // 0-100
    private Long currentFrame;
    private Long totalFrames;
    private String eta;  // tempo estimado restante
    
    // Erro
    private String errorMessage;
    private Integer retryCount;
    private Integer maxRetries;
    
    // Performance
    private Double encodingSpeed;  // frames por segundo
    private Integer priority;  // 1-10, maior = mais prioritário
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    // Acompanhamento
    private String logContent;  // últimas linhas do log de encoding
}
```

#### 5. **PlaybackHistory** (Histórico de Reprodução)
```java
@Entity
@Table(name = "playback_history")
@Index(name = "idx_playback_user", columnList = "user_id")
@Index(name = "idx_playback_video", columnList = "video_id")
public class PlaybackHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
    
    private Long currentPositionSeconds;  // onde parou
    private Long durationSeconds;  // duração total
    
    private String deviceInfo;  // Chrome/Firefox/Safari
    private String resolution;  // qual bitrate foi assistido
    
    private LocalDateTime playedAt;
    private LocalDateTime pausedAt;
    
    private Long watchTimeSeconds;  // quanto tempo realmente assistiu
    
    private boolean completed;  // se assistiu até o final
    
    private Double avgBitrate;  // média de bitrate durante reprodução
}
```

#### 6. **Favorite** (Favoritos)
```java
@Entity
@Table(name = "favorites", uniqueConstraints = 
    @UniqueConstraint(columnNames = {"user_id", "video_id"}))
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
    
    private LocalDateTime addedAt;
    
    private String userRating;  // 1-5 stars
    private String userReview;
}
```

#### 7. **Subtitle** (Legendas)
```java
@Entity
@Table(name = "subtitles")
public class Subtitle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
    
    private String language;  // pt, en, es, etc
    private String format;  // srt, vtt, ass
    private String filePath;  // /storage/subtitles/video123-pt.srt
    
    private boolean isDefault;
    private boolean isEmbedded;  // se vem no arquivo original
    
    private LocalDateTime uploadedAt;
}
```

#### 8. **SystemConfig** (Configurações)
```java
@Entity
@Table(name = "system_config")
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Encoding
    private String ffmpegPath;  // /usr/bin/ffmpeg
    private String ffprobePath;  // /usr/bin/ffprobe
    private Integer maxConcurrentEncodingJobs;  // 2-4
    private String defaultEncodingCodec;  // h264, h265
    
    // Storage
    private String videoStoragePath;  // /storage/videos
    private String encodedStoragePath;  // /storage/encoded
    private String tempWorkingPath;  // /tmp/encoding
    private Long maxStorageGB;  // limite de armazenamento
    
    // Streaming
    private String streamingProtocol;  // HLS, DASH
    private Integer hlsSegmentDuration;  // segundos
    private String availableBitrates;  // "8000,5000,3000,1500,800"
    
    // Segurança
    private String jwtSecret;
    private Integer jwtExpirationHours;
    
    // Performance
    private Integer thumbnailWidth;
    private Integer thumbnailHeight;
    private Integer posterWidth;
    private Integer posterHeight;
    
    private LocalDateTime updatedAt;
}
```

### Diagrama ER

```
┌─────────────┐
│    User     │
├─────────────┤
│ id (PK)     │
│ username    │
│ email       │
│ password    │
│ role        │
└──────┬──────┘
       │ 1
       │
       │ *
   ┌───┴──────────────┬──────────────────┬─────────────┐
   │                  │                  │             │
   │ *                │ *                │ *           │
  ┌▼────────┐    ┌──────────┐      ┌──────────┐  ┌────────┐
  │  Video  │    │Playback  │      │Favorite  │  │ Token  │
  │         │    │History   │      │          │  │(Cache) │
  └────┬────┘    └──────────┘      └──────────┘  └────────┘
       │ 1
       │
       │ *
   ┌───┴─────────────┬──────────────┬──────────────┐
   │                 │              │              │
   │ 1               │ 1            │ 1            │ 1
  ┌▼──────────┐ ┌──────────┐  ┌──────────┐  ┌────────┐
  │ Encoded   │ │ Encoding │  │Subtitle  │  │ Tag    │
  │ Video     │ │ Job      │  │          │  │        │
  └───────────┘ └──────────┘  └──────────┘  └────────┘

  ┌──────────────┐
  │SystemConfig  │
  │(singleton)   │
  └──────────────┘
```

---

## 💾 Banco de Dados

### Escolha do SGBD

#### **PRODUÇÃO: PostgreSQL**

**Por quê PostgreSQL?**
```
✅ Open source (gratuito)
✅ Suporta JSONB (configurações, metadados)
✅ Full-text search (buscar filmes)
✅ Índices avançados (performance)
✅ ACID compliance (dados confiáveis)
✅ Fácil replicação/backup
✅ Community grande
✅ Roda em qualquer SO (Windows/Linux/macOS)

❌ Um pouco mais lento que MySQL (negligenciável para este projeto)
```

**Alternativas consideradas e descartadas:**

| SGBD | Por quê descartar |
|------|------------------|
| MySQL | Não precisa de JSONB, menos recursos avançados |
| SQLite | Bom para desktop, ruim para múltiplos users simultâneos |
| MongoDB | Overkill, não precisa de documento denormalizado |
| Redis | Apenas cache, não persistência principal |

#### **DESENVOLVIMENTO/TESTES: H2 Embedded**

**H2 é perfeito para testes porque:**
```
✅ Roda em memória (super rápido)
✅ Sem configuração externa
✅ Compatível com SQL padrão
✅ Console web para debug
✅ Funciona com Spring Data JPA
✅ Simula bem o PostgreSQL (não 100%, mas perto)
```

### Configuração PostgreSQL

**Installation (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib

# Iniciar serviço
sudo systemctl start postgresql
sudo systemctl enable postgresql  # auto-start

# Conectar
sudo -u postgres psql
```

**Criar database:**
```sql
CREATE DATABASE streaming_db;

CREATE USER streaming_user WITH PASSWORD 'secure_password_here';

ALTER ROLE streaming_user SET client_encoding TO 'utf8';
ALTER ROLE streaming_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE streaming_user SET default_transaction_deferrable TO on;
ALTER ROLE streaming_user SET timezone TO 'UTC';

GRANT ALL PRIVILEGES ON DATABASE streaming_db TO streaming_user;
```

**Otimizações para este projeto:**

```sql
-- Conexões
ALTER SYSTEM SET max_connections = 200;

-- Memory
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';

-- WAL
ALTER SYSTEM SET wal_level = replica;
ALTER SYSTEM SET max_wal_senders = 3;

-- Reload
SELECT pg_reload_conf();
```

### Schema Inicial (JPA)

```yaml
# application.properties (produção)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/streaming_db
    username: streaming_user
    password: secure_password_here
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Em produção é "validate"
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL13Dialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
    show-sql: false
  
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
```

### Versionamento de Schema (Liquibase)

**Usar Liquibase para migração de schema:**

```xml
<!-- db/changelog/db.changelog-master.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
    
    <include file="db/changelog/v001/001-initial-schema.xml"/>
    <include file="db/changelog/v001/002-add-indexes.xml"/>
    <include file="db/changelog/v001/003-add-audit-columns.xml"/>
    
</databaseChangeLog>
```

### Backup e Restore

```bash
# Backup full
pg_dump -U streaming_user -h localhost streaming_db > backup.sql

# Backup apenas dados
pg_dump -U streaming_user -h localhost -a streaming_db > data_backup.sql

# Restore
psql -U streaming_user -h localhost streaming_db < backup.sql
```

---

## 📦 Dependências e Tecnologias

### Stack Tecnológico

```
BACKEND:  Spring Boot 3.x + Java 21
FRONTEND: Vue.js 3 ou React 18
DATABASE: PostgreSQL 14+
CACHE:    Redis (opcional, para sessões)
MESSAGE:  RabbitMQ (opcional, para async jobs)
ENCODING: FFmpeg 5.0+
```

### Dependências Maven (pom.xml)

#### **Core Spring Boot**
```xml
<!-- Versão base: 3.2.0 (compatível com Java 21) -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>

<!-- Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- JPA/Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- H2 para testes -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Liquibase para migração -->
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

#### **Segurança e Autenticação**
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Bcrypt para hash de senha -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

#### **Video Processing - FFmpeg**
```xml
<!-- Wrapper Java para FFmpeg -->
<dependency>
    <groupId>net.bramp.ffmpeg</groupId>
    <artifactId>ffmpeg</artifactId>
    <version>0.8.0</version>
</dependency>

<!-- Alternativa: JCodec (puro Java, sem dependência externa) -->
<dependency>
    <groupId>org.jcodec</groupId>
    <artifactId>jcodec</artifactId>
    <version>0.2.3</version>
</dependency>

<!-- Metadata extraction -->
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.0</version>
</dependency>
```

#### **Validação e Serialização**
```xml
<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Mappers (DTO) -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

#### **Utilitários**
```xml
<!-- Lombok (reduz boilerplate) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Apache Commons -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.11.0</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>

<!-- Guava (utilities) -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.3-jre</version>
</dependency>
```

#### **Logging**
```xml
<!-- Logback (já incluído no starter-web) -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
</dependency>

<!-- SLF4J -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>
```

#### **Async & Scheduling**
```xml
<!-- Para jobs de encoding assíncrono -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Opcional: RabbitMQ para message queue -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
    <optional>true</optional>
</dependency>

<!-- Opcional: Redis para cache/sessões -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <optional>true</optional>
</dependency>

<!-- Jedis (cliente Redis) -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
```

#### **WebSocket (real-time)**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

#### **Testing**
```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- AssertJ (assertions fluentes) -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- TestContainers (PostgreSQL em Docker para testes) -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>

<!-- REST Assured (testes de API) -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- WireMock (mock HTTP) -->
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>2.35.0</version>
    <scope>test</scope>
</dependency>

<!-- Spring Boot Test WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
    <scope>test</scope>
</dependency>
```

#### **Code Quality**
```xml
<!-- SonarQube (opcional) -->
<dependency>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.9.1.2184</version>
</dependency>

<!-- JaCoCo para cobertura -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Frontend Dependências (package.json)

```json
{
  "dependencies": {
    "vue": "^3.3.4",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.6",
    "axios": "^1.6.1",
    "hls.js": "^1.4.10",
    "bootstrap": "^5.3.2",
    "bootswatch": "^5.3.2",
    "@fortawesome/fontawesome-free": "^6.4.2"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.2",
    "vitest": "^1.0.4",
    "@vue/test-utils": "^2.4.1",
    "@testing-library/vue": "^8.0.1",
    "prettier": "^3.1.0",
    "eslint": "^8.54.0",
    "eslint-plugin-vue": "^9.19.0"
  }
}
```

---

## ⚙️ Configurações do Sistema

### application.yml (Produção)

```yaml
spring:
  application:
    name: streaming-server
  
  # Database
  datasource:
    url: jdbc:postgresql://localhost:5432/streaming_db
    username: streaming_user
    password: ${DB_PASSWORD:your_secure_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL13Dialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
    show-sql: false
    open-in-view: false
  
  # Security
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD:change_me}
  
  # File upload
  servlet:
    multipart:
      max-file-size: 100GB
      max-request-size: 100GB
      enabled: true
  
  # JMX
  jmx:
    enabled: false
  
  # Logging
  logging:
    level:
      root: INFO
      com.streaming: DEBUG
      org.springframework: INFO
      org.springframework.security: DEBUG
    file:
      name: logs/application.log
      max-size: 10MB
      max-history: 30

# Server
server:
  port: 8080
  servlet:
    context-path: /
  compression:
    enabled: true
    min-response-size: 1024
  http2:
    enabled: true

# FFmpeg Configuration
ffmpeg:
  path: /usr/bin/ffmpeg  # Linux/Mac: /usr/bin/ffmpeg, Windows: C:/ffmpeg/bin/ffmpeg.exe
  ffprobe-path: /usr/bin/ffprobe
  timeout-seconds: 3600  # 1 hora max
  loglevel: info

# Storage
storage:
  base-path: /mnt/streaming  # Mudar para local apropriado
  video-path: ${storage.base-path}/videos
  encoded-path: ${storage.base-path}/encoded
  thumbnail-path: ${storage.base-path}/thumbnails
  temp-path: /tmp/streaming-temp
  max-disk-gb: 500  # limite total

# Encoding Profiles
encoding:
  profiles:
    - name: "8000k"
      bitrate: 8000
      width: 1920
      height: 1080
      codec: h264
    - name: "5000k"
      bitrate: 5000
      width: 1280
      height: 720
      codec: h264
    - name: "3000k"
      bitrate: 3000
      width: 854
      height: 480
      codec: h264
    - name: "1500k"
      bitrate: 1500
      width: 640
      height: 360
      codec: h264
  max-concurrent-jobs: 2
  quality-preset: medium  # fast, medium, slow (mais lento = melhor compressão)
  audio-bitrate: 128  # kbps

# Streaming
streaming:
  protocol: HLS  # HLS ou DASH
  hls:
    segment-duration: 10  # segundos
    segments-in-playlist: 3
  cache-duration-hours: 24

# JWT
jwt:
  secret: ${JWT_SECRET:your_secret_key_change_in_production}
  expiration-hours: 24
  refresh-expiration-hours: 720  # 30 dias

# CORS
cors:
  allowed-origins: 
    - "http://localhost:3000"
    - "http://localhost:5173"
    - "192.168.1.*"  # Network local
  allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
  allowed-headers: "*"
  allow-credentials: true

# Actuator (monitoramento)
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

# Features
features:
  enable-adaptive-bitrate: true
  enable-subtitle-support: true
  enable-library-scan: true
  enable-admin-dashboard: true
  enable-websocket: true
```

### application-dev.yml (Desenvolvimento)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
  
  logging:
    level:
      root: DEBUG
      com.streaming: DEBUG

server:
  port: 8080

ffmpeg:
  timeout-seconds: 60  # Teste com timeout pequeno

storage:
  base-path: ./storage-dev
  max-disk-gb: 10  # Limitado para testes

encoding:
  max-concurrent-jobs: 1  # Um job por vez em dev
  quality-preset: fast  # Mais rápido para testes

jwt:
  secret: dev_secret_key_not_for_production
  expiration-hours: 8

cors:
  allowed-origins: 
    - "http://localhost:3000"
    - "http://localhost:5173"
    - "http://127.0.0.1:*"
```

### application-test.yml (Testes)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    initialization-mode: always
  
  jpa:
    hibernate:
      ddl-auto: create-drop
  
  test:
    database:
      replace: any

server:
  port: 0  # Random port

ffmpeg:
  timeout-seconds: 30

storage:
  base-path: ${java.io.tmpdir}/streaming-test
  max-disk-gb: 5

encoding:
  max-concurrent-jobs: 1
```

### application.properties (Fallback)

```properties
# Caso prefira properties ao invés de YAML

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/streaming_db
spring.datasource.username=streaming_user
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL13Dialect

# Server
server.port=8080

# FFmpeg
ffmpeg.path=/usr/bin/ffmpeg
```

---

## 🧪 Estratégia de Testes

### Pirâmide de Testes

```
        🔺
       /  \
      / 🧠 \      E2E / Integration Tests
     /______\    (poucos, mas críticos)
    /       \
   / 🔍🔍   \    Integration Tests
  /__________\  (média quantidade)
 /           \
/ 🧪🧪🧪    \ Unit Tests
/__________\ (muitos, rápidos)
```

**Meta de Cobertura: 70%+**

### 1. Unit Tests (Camada mais larga)

**Objetivo:** Testar métodos isoladamente

**O que testar:**
- ✅ Services (lógica de negócio)
- ✅ Utilities
- ✅ Validators
- ❌ Controllers (integração)
- ❌ Repositories (dependem de DB)

**Dependências:**
```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<!-- AssertJ -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

**Exemplo - VideoService Unit Test:**

```java
@DisplayName("VideoService - Unit Tests")
class VideoServiceTest {
    
    @Mock
    VideoRepository videoRepository;
    
    @Mock
    FileSystemService fileSystemService;
    
    @InjectMocks
    VideoService videoService;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @DisplayName("Deve criar novo vídeo com metadados válidos")
    @Test
    void testCreateVideoWithValidMetadata() {
        // Given (Arrange)
        String videoTitle = "Filme Teste";
        String filePath = "/videos/teste.mp4";
        
        Video expectedVideo = Video.builder()
            .title(videoTitle)
            .originalFilePath(filePath)
            .build();
        
        when(videoRepository.save(any(Video.class)))
            .thenReturn(expectedVideo);
        
        // When (Act)
        Video result = videoService.createVideo(videoTitle, filePath);
        
        // Then (Assert)
        assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("title", videoTitle)
            .hasFieldOrPropertyWithValue("originalFilePath", filePath);
        
        verify(videoRepository, times(1)).save(any(Video.class));
    }
    
    @DisplayName("Deve lançar exceção quando vídeo não encontrado")
    @Test
    void testGetVideoNotFound() {
        // Given
        String videoId = "non-existent-id";
        when(videoRepository.findById(videoId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> videoService.getVideoById(videoId))
            .isInstanceOf(VideoNotFoundException.class)
            .hasMessage("Video not found: " + videoId);
    }
}
```

### 2. Integration Tests (Camada média)

**Objetivo:** Testar interação entre componentes

**O que testar:**
- ✅ Service + Repository (com BD real em memória)
- ✅ Controller + Service (via MockMvc)
- ✅ Fluxos completos
- ❌ Serviços externos (FFmpeg, NAS)

**Dependências:**
```xml
<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- TestContainers para PostgreSQL -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>

<!-- REST Assured -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```

**Exemplo - VideoService Integration Test:**

```java
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class VideoServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>()
        .withDatabaseName("streaming_test")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    VideoService videoService;
    
    @Autowired
    VideoRepository videoRepository;
    
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @BeforeEach
    void setup() {
        videoRepository.deleteAll();
    }
    
    @DisplayName("Deve salvar e recuperar vídeo do banco")
    @Test
    void testSaveAndRetrieveVideo() {
        // Given
        Video video = Video.builder()
            .title("Test Movie")
            .originalFilePath("/path/to/movie.mp4")
            .fileSize(1024L)
            .build();
        
        // When
        Video saved = videoService.saveVideo(video);
        Video retrieved = videoService.getVideoById(saved.getId());
        
        // Then
        assertThat(retrieved)
            .isNotNull()
            .hasFieldOrPropertyWithValue("title", "Test Movie");
        
        assertThat(videoRepository.count()).isEqualTo(1);
    }
}
```

**Exemplo - Controller Integration Test (REST Assured):**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VideoControllerIntegrationTest {
    
    @LocalServerPort
    int port;
    
    @Autowired
    VideoRepository videoRepository;
    
    private String baseUrl;
    
    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
        videoRepository.deleteAll();
    }
    
    @DisplayName("Deve listar vídeos via GET /api/videos")
    @Test
    void testGetVideos() {
        // Given - criar alguns vídeos
        createTestVideo("Movie 1");
        createTestVideo("Movie 2");
        
        // When & Then
        given()
            .baseUri(baseUrl)
            .basePath("/api/videos")
        .when()
            .get()
        .then()
            .statusCode(200)
            .body("content.size()", is(2))
            .body("content[0].title", anyOf(
                is("Movie 1"), is("Movie 2")
            ));
    }
    
    private void createTestVideo(String title) {
        Video video = Video.builder()
            .title(title)
            .originalFilePath("/path/" + title)
            .build();
        videoRepository.save(video);
    }
}
```

### 3. End-to-End Tests (Poucos, mas críticos)

**Objetivo:** Testar fluxos completos do usuário

**O que testar:**
- ✅ Fazer login → Upload → Encoding → Stream
- ✅ Listar biblioteca
- ✅ Buscar vídeo

**Exemplo de cenário E2E:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StreamingE2ETest {
    
    @LocalServerPort
    int port;
    
    private String baseUrl;
    private String authToken;
    
    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
    }
    
    @DisplayName("Fluxo completo: Login -> Upload -> Encoding -> Stream")
    @Test
    void testCompleteStreamingWorkflow() throws Exception {
        // 1. Login
        String loginResponse = given()
            .baseUri(baseUrl)
            .body(new LoginRequest("admin", "password"))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("token");
        
        authToken = loginResponse;
        
        // 2. Upload vídeo
        File testVideo = new File("test-video.mp4");
        String videoId = given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer " + authToken)
            .multiPart("file", testVideo)
        .when()
            .post("/api/videos/upload")
        .then()
            .statusCode(202)
            .extract()
            .path("videoId");
        
        // 3. Aguardar encoding
        waitForEncodingComplete(videoId);
        
        // 4. Verificar playlist HLS gerada
        given()
            .baseUri(baseUrl)
        .when()
            .get("/api/stream/" + videoId + "/playlist.m3u8")
        .then()
            .statusCode(200)
            .contentType("application/vnd.apple.mpegurl");
    }
    
    private void waitForEncodingComplete(String videoId) throws InterruptedException {
        for (int i = 0; i < 60; i++) {  // Máximo 60 segundos
            String status = given()
                .baseUri(baseUrl)
            .when()
                .get("/api/videos/" + videoId)
            .then()
                .extract()
                .path("status");
            
            if ("READY".equals(status)) {
                return;  // Pronto!
            }
            
            Thread.sleep(1000);  // Esperar 1 segundo
        }
        
        throw new TimeoutException("Encoding não completou em tempo hábil");
    }
}
```

### 4. Testes de Performance

```java
@SpringBootTest
@ActiveProfiles("test")
class PerformanceTest {
    
    @Autowired
    VideoService videoService;
    
    @DisplayName("Listar 10k vídeos em menos de 2 segundos")
    @Test
    void testListVideosPerformance() {
        // Setup - criar muitos vídeos
        List<Video> videos = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            videos.add(Video.builder()
                .title("Movie " + i)
                .originalFilePath("/path/" + i)
                .build());
        }
        videoRepository.saveAll(videos);
        
        // Teste
        assertThatCode(() -> {
            long start = System.currentTimeMillis();
            videoService.getAllVideos(PageRequest.of(0, 50));
            long duration = System.currentTimeMillis() - start;
            
            assertThat(duration).isLessThan(2000);  // < 2 segundos
        }).doesNotThrowAnyException();
    }
}
```

### Configuração de Testes

**application-test.properties:**
```properties
# Database - Em memória H2
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# Logging mínimo
logging.level.root=WARN
logging.level.com.streaming=DEBUG

# Port aleatória para testes paralelos
server.port=0

# Storage em /tmp
storage.base-path=/tmp/streaming-test

# FFmpeg desabilitado em testes (usar mocks)
ffmpeg.enabled=false

# Security desabilitada em alguns testes
security.jwt.skip-in-tests=true
```

### Executar Testes

```bash
# Todos os testes
mvn clean test

# Apenas unit tests
mvn test -Dtest=*Test

# Apenas integration tests
mvn test -Dtest=*IntegrationTest

# Com cobertura
mvn clean test jacoco:report

# Ver relatório
open target/site/jacoco/index.html

# Testes paralelos (mais rápido)
mvn test -T 4

# Teste específico
mvn test -Dtest=VideoServiceTest#testCreateVideo
```

---

## 🔄 Fluxo de Dados

### Fluxo 1: Upload e Encoding

```
Usuário seleciona arquivo
       ↓
[VideoController.uploadVideo()]
       ↓
Validar formato + salvar arquivo
       ↓
[VideoService.createVideo()]
       ↓
Registrar no BD
       ↓
[EncodingService.startAdaptiveEncoding()]
       ↓
Adicionar à fila de encoding
       ↓
[EncodingQueue.process()]
       ↓
├─ Encoding para 8000k  (paralelo)
├─ Encoding para 5000k  (paralelo)
├─ Encoding para 3000k  (paralelo)
├─ Encoding para 1500k  (paralelo)
└─ Encoding para 800k   (paralelo)
       ↓
[EncodingService.encodeWithFFmpeg()]
       ↓
Gerar HLS segments
       ↓
Atualizar BD (status = READY)
       ↓
Notificar usuário (WebSocket)
       ↓
Pronto para streaming!
```

### Fluxo 2: Streaming (Visualizar)

```
Usuário abre player
       ↓
[StreamingController.getMasterPlaylist()]
       ↓
Retorna HLS master playlist (.m3u8)
       │
       ├─ BANDWIDTH=8000000, RESOLUTION=1920x1080
       ├─ BANDWIDTH=5000000, RESOLUTION=1280x720
       ├─ BANDWIDTH=3000000, RESOLUTION=854x480
       └─ ...
       ↓
Player detecta bandwidth disponível
       ↓
[BitrateAdaptationService.selectOptimalBitrate()]
       ↓
Retorna URL da variant playlist mais apropriada
       ↓
[StreamingController.getVariantPlaylist()]
       ↓
Retorna lista de segments (.ts)
       │
       ├─ segment-0.ts
       ├─ segment-1.ts
       ├─ segment-2.ts
       └─ ...
       ↓
Player faz requisição de cada segment
       ↓
[StreamingController.streamSegment()]
       ↓
Retorna arquivo .ts (video stream)
       ↓
Player decodifica e exibe
       ↓
[PlaybackHistoryService.recordPlayback()]
       ↓
Salvar histórico de visualização
```

### Fluxo 3: Busca de Biblioteca

```
Usuário digita "Inception"
       ↓
[LibraryController.search(query)]
       ↓
[LibraryService.searchVideos("Inception")]
       ↓
SELECT * FROM videos 
WHERE LOWER(title) LIKE '%inception%'
       ↓
Retornar resultados com DTOs
       ↓
Player mostra resultados
```

---

## 🔒 Segurança

### Autenticação

```
POST /api/auth/login
{
  "username": "user",
  "password": "pass123"
}
       ↓
[UserService.login()]
       ↓
Buscar usuário no BD
       ↓
Comparar senha com bcrypt
       ↓
✓ Senha válida
       ↓
Gerar JWT token
       ↓
```

**JWT Structure:**
```
Header:  { "alg": "HS256", "typ": "JWT" }
Payload: { "sub": "userId", "exp": 1234567890, "iat": 1234567800 }
Secret:  "your_secret_key"

Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Autorização

```
GET /api/videos/{videoId}

Header: Authorization: Bearer <token>
       ↓
[JwtAuthenticationFilter]
       ↓
Validar e extrair userId do token
       ↓
[VideoService.getVideoById(videoId)]
       ↓
Verificar se usuário tem acesso ao vídeo
       ↓
✓ OK → Retornar vídeo
✗ DENIED → 403 Forbidden
```

### Validação

```java
@PostMapping("/upload")
public ResponseEntity<?> uploadVideo(
        @RequestParam("file") @NotNull @NotBlank MultipartFile file
) {
    // Validação automática com @Valid, @NotNull, etc
    // + Validação customizada
    
    validateFileSize(file);
    validateFileFormat(file);
    validateStorageSpace();
    // ...
}
```

---

## ⚡ Performance e Otimizações

### Caching

```java
@Service
public class LibraryService {
    
    // Cache playlists por 24h
    @Cacheable(value = "masterPlaylist", 
               key = "#videoId")
    public String getMasterPlaylist(String videoId) {
        // ... gerar playlist
    }
    
    // Invalidar quando vídeo é atualizado
    @CacheEvict(value = "masterPlaylist", 
                key = "#videoId")
    public void updateVideo(String videoId, ...) {
        // ...
    }
}
```

### Índices do Banco

```sql
-- Índices essenciais
CREATE INDEX idx_video_user ON videos(user_id);
CREATE INDEX idx_video_status ON videos(status);
CREATE INDEX idx_encoded_video_bitrate ON encoded_videos(bitrate);
CREATE INDEX idx_playback_user ON playback_history(user_id);
CREATE INDEX idx_encoding_job_status ON encoding_jobs(status);

-- Full-text search
CREATE INDEX idx_video_title_search ON videos USING gin(
    to_tsvector('portuguese', title)
);
```

### Compressão de Resposta

```yaml
server:
  compression:
    enabled: true
    min-response-size: 1024  # Comprimir respostas > 1KB
```

### Paginação

```java
@GetMapping("/videos")
public ResponseEntity<Page<VideoDTO>> getVideos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by("title").ascending());
    
    Page<Video> videos = videoService.getAllVideos(pageable);
    return ResponseEntity.ok(videos.map(VideoDTO::from));
}
```

### Lazy Loading

```java
@Entity
public class Video {
    
    // Não carregar todos os segments de uma vez
    @OneToMany(fetch = FetchType.LAZY)
    private List<EncodedVideo> encodedVersions;
}
```

---

## 📝 Próximas Etapas

1. ✅ **Leitura desta documentação** (você está aqui!)
2. 🔜 **Criar estrutura do projeto**
3. 🔜 **Implementar camada de Data Access (Entities + Repositories)**
4. 🔜 **Implementar Business Logic (Services)**
5. 🔜 **Implementar API Controllers**
6. 🔜 **Implementar Tests**
7. 🔜 **Frontend (Vue.js/React)**
8. 🔜 **Deploy e Documentação**

---

## 📚 Referências

- [Spring Boot Official Docs](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [FFmpeg Documentation](https://ffmpeg.org/documentation.html)
- [HLS Specification](https://tools.ietf.org/html/rfc8216)
- [Spring Security](https://spring.io/projects/spring-security)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---

**Documento Criado:** 2024
**Status:** Documento de Arquitetura Completo
**Versão:** 1.0

