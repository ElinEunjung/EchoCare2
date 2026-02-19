<p align="center">
  <img src="docs/image/banner.png" alt="EchoCare Banner" width="800"/>
</p>

# EchoCare
# 1. Overview
EchoCare helps caregivers provide personalized music therapy for people living with dementia. By combining user profiling, situation-aware playlist generation, and feedback loops, the app adapts to changing needs and symptoms. This MVP is implemented as a fullstack microservices project using React, TypeScript, Spring Boot, Maven, and Docker.

# 2. Key Features
- Personalized playlists: based on user(patient)'s music history (10s-20s)
- Situation-aware: playlists adapted to purpose (`stress relief`, `activity support`, `calming agitation`, `easing depression` and `reducing anxiety`)
- Feedback loop: caregivers can `like/dislike` to continuosly improve recommendation
- Phase adaptation: music option tailored to different stages of dementia (`mild`,`moderate`,`severe`)

# 3. User Stories
- As a caregiver, I register my mother, set her era (1965-1975), favorite artists, and symptoms
- As a caregiver, I select a situation (`reduce anxiety` - need  and `moderate stage` - dementia stage) and get a suitable playlist that starts with familiar slow songs for the patient.
- As a caregiver, I provide feedback (`like/dislike` or quick 4/5 "calmer" `rating`) on songs so that future playlists are more personalized.
- As a clinician (later), I review outcome trends across sessions and adjust plan (shorter sessions, more familiar tracks)
- As the system, I bias tracks towards the patient's teens/20s and match audio features to the situation.

# 4. Architecture
*add image here from draw.io*

# Project Structure
```
echocare/
├── frontend/         
├── services/
│   ├── profile-service/       ← module
│   ├── playlist-service/      ← module
│   ├── feedback-service/      ← module
│
├── gateway/                  ← module
├── docs/
└── pom.xml                   ← parent POM

```

# 5. Tech Stack
- **Frontend**: React, TypeScript
- **Backend**: Java, Spring Boot, Maven
- **Microservices**: Docker container, REST API, RabbitMQ
- **Database**: PostgreSQL/MySQL

# 6. Setup & Installation
- Prerequisites: Java 17, Node.js, Docker, Maven
- Clone instructions
- Build & run:
```
docker compose up
```
- Access info:
    - localhost ports:
    - default login:

# 7. Usage
- Example caregiver flow: profile -> select situation -> get playlist -> give feedback
- (Include screenshots or gifs later)

# 8. Future work
- Multi-user caregiver dashboard
- Clinician dashboards with progress tracking
- Integrate with Spotify/YouTube API
- More advanced recommendation algorithm
- Multi language support

# 9. Licence
MIT Licence

# 10. Reference
- [Jakob. J., Schmitt. M., Buchholz, M., et al. "A randomized controlled trial of an individualized Music and Dementia app (MDA) for informal caregivers and people living with dementia at home." PubMed, 2024](https://pubmed.ncbi.nlm.nih.gov/38532365/)
- [Prick, A. E. J., de Lange, J., van Hartingsveldt, M., et al. "Effects of a music therapy and music listening intervention on neuropsychiatric symptoms in people with dementia: A cluster randomized controlled trial." Frontiers in Medicine, 2024](https://www.frontiersin.org/journals/medicine/articles/10.3389/fmed.2024.1304349/full)
- [Music For My Mind. "Personalized playlists for people living with dementia: methodology and clinical implementation." Dementia Research UK, 2025](https://demruk.org/music-for-my-mind/)
- [Moreno-Morales, C., Calero, R., Moreno-Morales, P., Pintado, C. “Music Therapy in the Treatment of Dementia: A Systematic Review.” Medical Sciences, 2020](https://pmc.ncbi.nlm.nih.gov/articles/PMC7248378/)
- [Rao, S., Srinivasan, N., Lin, Y.-C., et al. “A Focus on the Reminiscence Bump to Personalize Music Interventions in Healthy Older Adults and People Living With Dementia.” Frontiers in Neuroscience, 2021.](https://pmc.ncbi.nlm.nih.gov/articles/PMC8374316/)
- [Garrido, S., Dunne, L., Chang, E., Perz, J., Stevens, C. J., Haertsch, M. “Music playlists for people with dementia.” BMC Geriatrics, 2021.](https://pmc.ncbi.nlm.nih.gov/articles/PMC10455001/)

# 11. AI usage
- Github Copilot (Auto): readme.md, documentation, boilerplate code, code snippets, 
project structure, architecture design, user stories, feature list, commit message format, etc.

