# CLAUDE.md — project-v1

## 0. Bu fayl nima uchun kerak

Claude har yangi sessiyada shu faylni avtomatik o'qiydi. Foydalanuvchi har safar
"menga katta loyihalardagidek o'rgatib ber" deb qaytadan tushuntirmasligi uchun
ish uslubi shu yerda yozib qo'yilgan. **Bu bo'limlar buyruq, taklif emas.**

---

## 1. ISH USLUBI (majburiy — har bir sessiyada amal qil)

Bu loyiha **o'rganish loyihasi**. Egasi backend'ni chuqur o'rganmoqchi va
**kodni o'zi yozadi**.

### 1.1 Kod yozish qoidasi

- ❌ Foydalanuvchi aniq "sen yoz", "o'zing qil", "implement qil" demaguncha
  `src/` ichidagi `.java`, `.yml`, `.sql` fayllarni **yaratma va tahrirlama**.
- ✅ Sening vazifang: **spetsifikatsiya berish** — qaysi fayl, qaysi paketda,
  qanday nom bilan, ichida qanday metod/annotatsiya bo'lishi va **nega**.
- ✅ Chatda yoki `docs/` ichidagi `.md` fayllarda skelet/snippet ko'rsatish mumkin
  va kerak. Lekin to'liq ishlaydigan implementatsiyani ko'chirib berish emas —
  metod imzosi, annotatsiyalar, mantiqning qadamlari yetarli.
- ✅ Foydalanuvchi kodni yozib bo'lgach "tekshir" desa — **code review** qil:
  xavfsizlik, tranzaksiya chegarasi, N+1, null-safety, naming, layer buzilishi.

### 1.2 O'rgatish darajasi: ENTERPRISE

Har bir tushuntirish **katta, production loyihalar kesimida** bo'lsin.
"Ishlasa bo'ldi" darajasi **yetarli emas**. Har bir qadamda quyidagilar bo'lsin:

1. **Nima qilinadi** — aniq fayl va paket nomi bilan.
2. **Nega shunday** — qaysi muammoni hal qiladi.
3. **Pattern nomi** — sanoatda qanday ataladi (masalan: "Layered architecture",
   "DTO pattern", "Token rotation", "RBAC vs ABAC", "Aggregate root").
4. **Alternativa va trade-off** — boshqa yo'l ham bor, nega uni tanlamadik.
   Kichik loyihada X yetarli, lekin 50 ta endpoint va 5 ta jamoa bo'lsa Y kerak.
5. **Anti-pattern ogohlantirishi** — yangi backendchilar shu yerda nimani buzadi.
6. **Tekshirish** — qadam tugagach qanday `curl` / `psql` / test bilan tasdiqlanadi.

### 1.3 Tushuntirish tili va shakli

- Tushuntirish **o'zbek tilida**. Texnik atamalar inglizcha qoladi
  (entity, repository, claim, filter chain, bean) — tarjima qilma, izohla.
- Bir vaqtning o'zida **bitta faza**. 10 ta fazani birdan tashlama.
- Har fazadan keyin to'xta va foydalanuvchi kod yozib bo'lishini kut.

### 1.4 Roadmap bilan ishlash

To'liq yo'l xaritasi: **`docs/auth-rbac-roadmap.md`**.

- Sessiya boshida shu faylni o'qi — qaysi faza tugagani `[x]` bilan belgilangan.
- Faza tugagach, foydalanuvchi kodini tekshirib, checkbox'ni yangila.
- Yo'lda arxitektura qarori qabul qilinsa — roadmap'ning "Qarorlar jurnali"
  bo'limiga bir qator qo'sh.

---

## 2. LOYIHA MA'LUMOTLARI

| | |
|---|---|
| Stack | Spring Boot **4.1.1**, Java **26**, Gradle (Groovy DSL) |
| DB | PostgreSQL (`project_v1`), Hibernate/JPA |
| Base package | `uz.app.projectv1` |
| Port | 8081 |
| API prefix | `/api/v1` — `WebConfig` har bir `@RestController`ga avtomatik qo'shadi |
| Auth | JWT, `spring-boot-starter-oauth2-resource-server` orqali (jjwt EMAS) |

### 2.1 Diqqat: Spring Boot 4 yangi

Boot 4 → Spring Framework 7 + **Spring Security 7**. Internetdagi ko'p
qo'llanmalar Boot 2/3 uchun yozilgan va API'lar farq qiladi
(`spring-boot-starter-web` → `spring-boot-starter-webmvc`, Security'da
deprecated metodlar olib tashlangan). Kod taklif qilishdan oldin API mavjudligiga
ishonchsiz bo'lsang — buni **aytib qo'y**, taxmin qilib "ishonchli" tarzda berma.

### 2.2 Paket tuzilishi konvensiyasi

**Package-by-feature** (package-by-layer emas). Ya'ni `user/` ichida controller,
service, repository birga turadi — `controllers/`, `services/` degan global
paketlar yaratilmaydi.

```
uz.app.projectv1
├── common/          # feature'larga tegishli bo'lmagan umumiy narsalar
│   ├── config/      # WebConfig, JpaAuditingConfig, OpenApiConfig
│   ├── entity/      # BaseEntity
│   ├── exception/   # domain exception'lar + GlobalExceptionHandler
│   └── dto/         # ApiResponse, PageResponse
├── security/        # SecurityConfig, JWT, UserDetails, handler'lar
├── auth/            # register / login / refresh / logout
├── user/            # foydalanuvchi CRUD
└── rbac/            # Role, Permission
```

Har feature ichida: `XController`, `XService`, `XRepository`, `entity/`, `dto/`.

### 2.3 Kod konvensiyalari

- Entity nomi: `UserEntity` (mavjud konvensiya — buzma, izchil bo'lsin).
- DTO: `record` ishlat (Java 26) — `RegisterRequest`, `AuthResponse`.
- Controller **hech qachon** Entity qaytarmaydi — faqat DTO.
- Service — interfeys **shart emas**, ikkinchi implementatsiya paydo bo'lgunga
  qadar oddiy `@Service` klass yetarli.
- Lombok bor: `@Getter/@Setter/@Builder/@RequiredArgsConstructor`.
  `@Data` va `@AllArgsConstructor` entity'da ishlatilmaydi (equals/hashCode tuzoq).
- Dependency injection — **faqat constructor** orqali (`@RequiredArgsConstructor`),
  `@Autowired` field'ga qo'yilmaydi.

---

## 3. HOZIRGI HOLAT (2026-09-10)

**✅ Faza 0 tugadi** — kod yozildi, review qilindi, ishlab turgan ilovada tekshirildi.

Tayyor:
- `common/entity/BaseEntity` — `@MappedSuperclass`, auditing, proxy-safe equals/hashCode
- `common/config/JpaAuditingConfig` (bo'sh, ataylab) + `AuditorConfig`
- `common/exception/` — `AppException` / `NotFoundException` / `ConflictException` /
  `GlobalExceptionHandler extends ResponseEntityExceptionHandler` (`ProblemDetail`, RFC 7807)
- `user/dto/` — `UserRequest`, `UserResponse` (record, toza)
- `UserEntity extends BaseEntity` + `@SQLDelete` / `@SQLRestriction` soft delete
- `UserService` — `@Service`, klass darajasida `readOnly = true`
- `UserMapper` — MapStruct 1.6.3, `unmappedTargetPolicy = ERROR` (Java 26 da tekshirilgan)

Tekshirilgan javoblar: `/users` 200, `/users/999` 404, `/users/abc` **400**,
noma'lum yo'l 403 (Security filtri, Faza 3 da tuzatiladi).

Qolgan mayda ish (bloklamaydi):
1. `GlobalExceptionHandler:26` — annotatsiyasiz `handleValidation` **o'lik kod**,
   `handleMethodArgumentNotValid` override uni almashtirgan → o'chirilsin
2. `handleExceptionInternal` 404/405 ni ham `code: "BAD_REQUEST"` deb belgilaydi
3. `ProjectV1Application` — izohga olingan `CommandLineRunner` + ishlatilmagan importlar

**✅ Faza 1 tugadi** (2026-09-13) — `rbac/` paketi: `Role`, `Permission` entity'lar
(ManyToMany, cascade yo'q, LAZY, `Set`), `RoleRepository`, `PermissionRepository`,
`Permissions` + `RoleNames` konstantalari. `UserEntity.role` enum → `Set<Role> roles`.
`user/enums/` paketi o'chirildi.

O'lchangan so'rovlar soni (5 user, 3 rol): `GET /users` → 1, `GET /users/{id}` → 1,
`find-by-email` → 1. N+1 yo'q.

**DTO qarori:** `UserResponse` — tekis (`Set<String> roles`). `RoleResponse` /
`PermissionResponse` (ichma-ich) yozilgan, lekin hozircha ishlatilmaydi — ular
Faza 6 dagi rol admin endpoint'lari uchun. Permission'lar Faza 5 da `MeResponse`
orqali tekis `Set<String>` sifatida qaytariladi.

⚠️ Qoida: **DTO ichiga collection qo'shsang, `@EntityGraph` ni ham yangila.**
Bir marta buzilgan: `UserResponse` ichma-ich qilinganda `GET /users` 1 → 8 so'rovga
chiqib ketgan edi (`open-in-view` yoqiq bo'lgani uchun xato bermay, jimgina).

**✅ Faza 2 tugadi** (2026-09-17) — Flyway. `ddl-auto: validate`,
`db/migration/` da V1–V4 (users → rbac jadvallar → permission/rol seed → 4 ta
dev user). Constraint'lar o'z nomi bilan (`pk_users`, `fk_user_roles_role`, ...).
**Qisman (partial) unique index'lar** — `users.email`, `roles.name`, `permissions.name`
uchun `... WHERE deleted = FALSE`. Sabab: uchala entity'da ham `@SQLRestriction`
soft delete bor, oddiy `UNIQUE` esa o'chirilgan qiymatni abadiy band qilardi
(`existsByX` uni ko'rmaydi → `INSERT` → duplicate key → 500).
Shu sabab **`UserEntity`, `Role`, `Permission` da `unique = true` YO'Q** (ataylab).

⚠️ Qisman index'ning oqibati: `ON CONFLICT (name)` **ishlamaydi** —
`there is no unique or exclusion constraint matching the ON CONFLICT specification`.
Shart index'dagi shart bilan aynan mos bo'lishi kerak:
`ON CONFLICT (name) WHERE deleted = FALSE DO NOTHING`.

⚠️ Boot 4 da Flyway uchun `flyway-core` **yetarli emas** — autoconfiguration
alohida modulda. Kerak: `spring-boot-starter-flyway` + `flyway-database-postgresql`.
`flyway-core` bilan Flyway **jimgina ishga tushmaydi**.

⚠️ `ddl-auto: validate` **tekshirmaydi**: ustun uzunligi (`VARCHAR(255)` vs
`length = 64` — sinab ko'rilgan, xato bermaydi), index, unique, FK, check.
Faqat jadval / ustun nomi / tipni tekshiradi.

Dev userlar: `admin@app.co`, `user@app.co`, `manager@app.co`, `operator@app.co` —
paroli `Parol12345`, `{bcrypt}` prefiksli hash bilan seed qilingan.

Migration'larni ilovani ko'tarmasdan tekshirish:
```bash
dropdb --if-exists mig_test; createdb mig_test
for f in src/main/resources/db/migration/V*.sql; do psql -q -d mig_test -f "$f"; done
dropdb mig_test
```

**✅ Faza 2.5 tugadi** (2026-09-19) — sirlar env'da:
```yaml
url:      ${DB_URL:jdbc:postgresql://localhost:5432/project_v1}
username: ${DB_USERNAME:postgres}
password: ${DB_PASSWORD}           # default YO'Q
```
Ishga tushirish uchun `DB_USERNAME=macbookpro` va `DB_PASSWORD=...` kerak
(IntelliJ Run Config yoki `export`). `DB_USERNAME` defaulti `postgres` — bu
mashinada mavjud emas, shuning uchun o'rnatish majburiy.

⚠️ `spring.datasource.*` da yechilmagan `${VAR}` **exception tashlamaydi** —
literal satr sifatida o'tib ketadi (tekshirilgan). Lokalda `trust` auth bilan
ilova baribir ulanadi. Prodda parol xato bo'lgani uchun yiqiladi, lekin xabar
`password authentication failed` bo'ladi, "placeholder" haqida emas.

**✅ Faza 3 tugadi** — `SecurityBeansConfig` (`PasswordEncoder` = `DelegatingPasswordEncoder`),
`CustomUserDetails` (adapter record), `CustomUserDetailsService`,
`SecurityConfig` (stateless, CSRF cookie'da, `permitAll` olib tashlangan).
Dev userlar paroli: **`Parol12345`** (V5 da tuzatilgan — V4 dagi hash buzuq edi).

**🟡 Faza 4 asosiy qismi ishlaydi** (2026-09-20) — `JwtConfig` (RSA encoder/decoder +
`BearerTokenResolver` cookie'dan + `JwtAuthenticationConverter`), `JwtService`,
vaqtinchalik `POST /api/v1/auth/token`. O'lchangan: JWT bilan `GET /users/1` → **1 so'rov**,
Basic auth bilan → **2 so'rov** (autentifikatsiya uchun qo'shimcha DB o'qish).

**✅ Sessiyalar ishlaydi** (2026-09-20) — `sessions` jadvali (V6), `sid` claim,
`SessionValidator` (`OAuth2TokenValidator`) decoder'ga ulangan. Sinab tasdiqlangan:
sessiya bekor qilinsa **o'sha token darhol 401**, boshqa qurilma ishlashda davom
etadi. Narx: +1 so'rov/request (1→2).

⚠️ `setJwtValidator` standart tekshiruvlarni **almashtiradi** — timestamp va issuer
validator'larini qo'lda qo'shish shart, aks holda `exp` yoki `iss` tekshirilmay qoladi.
`JwtValidators.createDefaultWithIssuer` **60 soniyalik clock skew** bilan keladi
(o'lchangan: muddati o'tgan token 60s ishlayveradi) — shuning uchun
`new JwtTimestampValidator(Duration.ofSeconds(5))` qo'lda yoziladi.

**✅ Faza 4.5 tugadi** (2026-09-23) — **huquqlar token'dan DB'ga ko'chirildi**.
Zhrms loyihasi tahlilidan keyin qabul qilingan arxitektura qarori.

```
Token:  sub, email, sid, iss, iat, exp, jti      ← authorities YO'Q (600 belgi)
Har so'rovda:  sid → sessiya tirikmi | email → rol/permission DB'dan
```

`security/DbAuthenticationConverter` (`Converter<Jwt, AbstractAuthenticationToken>`)
`jwtAuthenticationConverter` sifatida ulangan; principal — `security/AuthUser` record
(id, email, sessionId, roles, permissions). `@AuthenticationPrincipal AuthUser` ishlaydi.

**Sinab tasdiqlangan:** `DELETE FROM user_roles` → **o'sha token bilan**
`/auth/me` darhol `roles: []`, `/admin/**` 403. Rol qaytarilganda yana 200.
Token'ga umuman tegilmaydi.

So'rovlar: `/auth/me` → 2, `/users/1` → 3 (auth uchun doim +2).

**Shu qaror tufayli KERAK EMAS:** `token_version`, `ver` claim, `bumpTokenVersion*`,
`SessionValidator`, `authorities` claim, `SCOPE_` prefiksi muammosi, `AuthService.me(Jwt)`.

**✅ Faza 5B tugadi** (2026-09-23) — refresh token rotatsiya bilan.
`V7`: `sessions` ga `refresh_token_hash` + `previous_refresh_token_hash` (qisman
unique index). Token — **JWT emas**, `SecureRandom` 256 bit, DB'da **SHA-256 hash**
(bcrypt emas: 256 bit tasodifiy qiymatga brute force yo'q). Cookie:
`REFRESH-TOKEN`, `Path=/api/v1/auth/refresh`, TTL `jwt.refresh-token-ttl: 3d`.

Har `/auth/refresh` da **ikkala token ham yangilanadi**, `sid` o'zgarmaydi.
Eski hash `previous_refresh_token_hash` ga ko'chadi va **tuzoq** bo'lib qoladi.

⚠️ **Ikkita filter chain:** `@Order(1) publicChain` — `securityMatcher` bilan
`/auth/login|register|refresh`, `oauth2ResourceServer` YO'Q. Sababi: muddati o'tgan
`AUTH-TOKEN` cookie bu endpoint'larni **401 bilan to'sib qo'yardi** (`permitAll`
autentifikatsiyani o'tkazib yubormaydi — credential bor, lekin yaroqsiz → 401).

⚠️ **Tranzaksiya tuzog'i:** o'g'irlik aniqlanganda `revokeAllForUser` chaqirilib
keyin exception tashlansa — `@Transactional` **rollback qiladi va bekor qilish
bekor bo'ladi** (o'lchangan: UPDATE yuborilgan, DB o'zgarmagan). Yechim:
`@Transactional(propagation = REQUIRES_NEW)` — xavfsizlik amali mustaqil commit qilsin.

⚠️ **`yml` da vaqt birligi majburiy:** `3` = **3 millisekund** (o'lchangan:
`exp - iat = 0`). `3d`, `15m`, `30s` deb yozing. Spring Boot `Duration` ga o'zi
o'giradi — qo'lda parse qilish kerak emas.

🟡 Cheklov: `previous_refresh_token_hash` faqat **bitta** oldingi token'ni eslaydi.
Ikki rotatsiyadan keyin qayta ishlatilgan token oddiy "yaroqsiz" 401 oladi,
o'g'irlik sifatida aniqlanmaydi. Hozircha maqbul.

Qolgan: cookie sozlamalarini profildan olish (`CookieProperties`), refresh token
(Faza 5 da `sessions` ga `refresh_token_hash` ustuni qo'shiladi).

⚠️ **Spring Security nomlash tuzog'i:** sozlayotgan Spring sinfi bilan **bir xil nomli**
o'z klassingni yaratma (`JwtAuthenticationConverter`, `BearerTokenResolver`) — u
Spring'nikini soya qiladi va `new X()` o'zini o'zi yasaydi. Bean — konfiguratsiya
klassi **ichidagi metod**, alohida klass emas.

⚠️ **`@Value` importi:** `org.springframework.beans.factory.annotation.Value`.
`lombok.Value` — klass annotatsiyasi, `Cannot find @interface method 'value()'` beradi.

Hali yo'q: refresh token, `@PreAuthorize`, register/login/logout.

⚠️ Ikki vaqtinchalik narsa Faza 3 da tuzatiladi:
- `SecurityConfig` da `/api/v1/users/**` → `permitAll`
- CSRF yoqiq → hozir har qanday POST/PUT/DELETE **403** qaytaradi

⚠️ Windows va macOS DB'larida `users` jadvali **har xil** chiqdi (`role` ustuni
biryerda nullable, biryerda not null) — `ddl-auto: update` mavjud ustunni
o'zgartirmaydi. Bu Faza 2 (Flyway) ning tirik dalili.

Keyingi qadam: `docs/auth-rbac-roadmap.md` → Faza 1 (Role + Permission).
