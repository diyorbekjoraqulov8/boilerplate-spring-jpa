# Auth + RBAC yo'l xaritasi — project-v1

> O'qish tartibi: bu faylni yuqoridan pastga qarab bosqichma-bosqich bajar.
> Bir fazani tugatmasdan keyingisiga o'tma. Har faza oxirida "Tekshirish"
> bo'limi bor — u o'tmasa keyingi fazaga o'tish mumkin emas.
>
> Ish uslubi va o'rgatish qoidalari: `../CLAUDE.md`.

## Progress

- [x] **Faza 0** — Fundament: BaseEntity, auditing, exception handling, DTO qatlami ✅ 2026-09-10
- [ ] **Faza 1** — Domen: Role + Permission entity'lar, RBAC modeli
- [ ] **Faza 2** — Migration: Flyway + seed data
- [ ] **Faza 3** — Security infra: PasswordEncoder, UserDetails, UserDetailsService
- [ ] **Faza 4** — JWT: RSA kalitlar, JwtEncoder/JwtDecoder, claim → authority
- [ ] **Faza 5** — Auth endpoint'lar: register / login / refresh / logout
- [ ] **Faza 6** — Authorization: `@PreAuthorize`, permission-based tekshiruv
- [ ] **Faza 7** — Production hardening: profillar, rate limit, CORS, testlar

---

# Avval: RBAC nima va nega shunday quriladi

## Model

```
users ──< user_roles >── roles ──< role_permissions >── permissions
```

- **Permission** — eng mayda huquq. `user:read`, `user:create`, `role:assign`.
  Bitta aniq amalni bildiradi. Kodda **shu** tekshiriladi.
- **Role** — permission'lar to'plamiga qo'yilgan nom. `ADMIN`, `MANAGER`.
  Odamga **rol** beriladi, permission emas.
- **User** — bir nechta rolga ega bo'lishi mumkin (ManyToMany).

Foydalanuvchining yakuniy huquqlari (`authorities`) =
`ROLE_<role nomi>` lar + shu rollarning **barcha** permission'lari birlashmasi.

## Eng muhim qoida (katta loyihalarning asosiy darsi)

**Endpoint rolni emas, permission'ni tekshiradi.**

```java
@PreAuthorize("hasRole('ADMIN')")             // ❌ yomon
@PreAuthorize("hasAuthority('user:delete')")  // ✅ to'g'ri
```

Nega? Chunki `hasRole('ADMIN')` yozsang — ertaga "MANAGER ham user o'chira olsin"
degan talab kelganda **kodni o'zgartirib, qayta deploy qilishing** kerak.
Permission bilan tekshirsang — admin panelidan MANAGER roliga `user:delete`
permission'ini qo'shasan, deploy yo'q. Bu RBAC'ning butun ma'nosi.

Rol darajasidagi tekshiruv faqat juda yirik darvozalar uchun qoladi:
`/api/v1/admin/**` → `hasRole('ADMIN')`.

## `ROLE_` prefiksi tuzog'i

Spring Security'da `hasRole('ADMIN')` ichkarida `hasAuthority('ROLE_ADMIN')` ga
aylanadi. Ya'ni DB'da rol nomini `ADMIN` deb saqla, lekin `GrantedAuthority`
yasaganda `"ROLE_" + name` qil. Permission'ga prefiks **qo'shilmaydi**.
Bu ikkalasini aralashtirib yuborish — eng ko'p uchraydigan xato.

## Permission nomlash konvensiyasi

`<resurs>:<amal>` — hammasi kichik harf: `user:read`, `user:create`,
`user:update`, `user:delete`, `role:read`, `role:assign`, `permission:read`.

Alternativa `USER_READ` — bir xil ishlaydi, lekin `:` bilan resurs bo'yicha
prefiks qidiruv (`user:*`) qulayroq. Bittasini tanla va **hech qachon o'zgartirma**.

## Qaysi qismi DB'da, qaysi qismi kodda?

| | Kodda (enum/konstanta) | DB'da |
|---|---|---|
| Permission | ✅ konstanta sifatida (kod shularni tekshiradi) | ✅ jadval sifatida (FK butunligi uchun) |
| Role | ⚠️ faqat tizim rollari nomi | ✅ to'liq — admin yangi rol yarata oladi |
| Rol↔permission bog'lanishi | ❌ | ✅ — runtime'da o'zgaradi |

Permission'lar kodda ham bo'lishi shart, chunki `@PreAuthorize("hasAuthority('user:read')")`
dagi satr kompilyatsiyada tekshirilmaydi — typo qilsang endpoint **jimgina**
hech kimga ochilmay qoladi. Shuning uchun konstanta klass yasaladi va
`@PreAuthorize(Permissions.CAN_READ_USER)` ko'rinishida ishlatiladi.

---

# FAZA 0 — Fundament

**Maqsad:** auth yozishdan oldin loyihada yetishmayotgan asosiy qatlamlarni qo'yish.
Buni auth'dan **keyin** qilsang — hamma joyni qayta yozishga to'g'ri keladi.

## 0.1 `common/entity/BaseEntity.java`

Hozir `UserEntity` ichida `createdDate`, `updatedDate`, `isActive`, `isDeleted`
bor va ularni **hech kim to'ldirmaydi** — DB'ga `null` yozilyapti. Har bir yangi
entity'da bu maydonlarni qayta yozish — duplikatsiya.

Yasa: `@MappedSuperclass` abstrakt klass.

```java
@MappedSuperclass
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate  @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private boolean deleted = false;
}
```

- `@MappedSuperclass` — bu klass uchun **alohida jadval yaratilmaydi**, ustunlar
  merosxo'r jadvallarga qo'shiladi. `@Entity` + `@Inheritance` bilan adashtirma:
  u haqiqiy meros va jadval yaratadi — bu yerda kerak emas.
- `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate` — sanani
  Hibernate emas, **Spring Data** to'ldiradi. Buning uchun 0.2 kerak.
- `updatable = false` — `createdDate` keyin hech qachon o'zgarmaydi.

**Nega `id` ham shu yerda?** Har entity'da bir xil `Long id` — takrorlash bekor.

**Trade-off:** BaseEntity'ga juda ko'p narsa tiqish — anti-pattern. Faqat
**barcha** entity'lar uchun rostdan kerak bo'lgani kirsin. `isActive` masalan
faqat `User`ga tegishli — u `UserEntity`da qolsin.

## 0.2 `common/config/JpaAuditingConfig.java`

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
    @Bean
    AuditorAware<Long> auditorAware() { /* SecurityContext'dan user id */ }
}
```

`@EnableJpaAuditing` bo'lmasa `@CreatedDate` **jim turadi** — xato bermaydi,
shunchaki `null` qoladi. Eng ko'p vaqt yeydigan tuzoqlardan biri.

`AuditorAware` — "kim yaratdi/o'zgartirdi" ni yozish uchun. Hozircha bo'sh
`Optional.empty()` qaytar, Faza 4 dan keyin SecurityContext'dan `userId` olasan
va `BaseEntity`ga `@CreatedBy Long createdBy` qo'shasan.

## 0.3 `UserEntity` ni BaseEntity'ga o'tkaz

`extends BaseEntity` qil, ichidan `id`, `createdDate`, `updatedDate`, `isDeleted`
ni **o'chir**. `email`, `password`, `isActive`, `role` qoladi.

⚠️ `boolean isActive` + Lombok = getter `isActive()`. Jackson uni JSON'ga
`active` deb chiqaradi. Bu odatiy chalkashlik — maydon nomini `active` deb
qo'ysang muammo yo'qoladi.

## 0.4 Soft delete

`deleted` maydoni bor, lekin hech qayerda ishlatilmayapti — har `findAll()`
o'chirilganlarni ham qaytaradi. Hibernate 6 usuli:

```java
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
```

- `@SQLDelete` — `repository.delete()` chaqirilganda DELETE o'rniga UPDATE.
- `@SQLRestriction` — har bir SELECT'ga avtomatik `AND deleted = false`.
  (Boot 3+ da `@Where` deprecated → `@SQLRestriction`.)

**Ogohlantirish:** `@SQLRestriction` global — o'chirilganni **hech qachon**
ko'ra olmaysan, hatto admin panelda ham. Katta loyihalarda ko'pincha buning
o'rniga repository'da aniq `findAllByDeletedFalse()` yoziladi, chunki oshkora.
Ikkisidan birini tanla, aralashtirma.

## 0.5 Exception qatlami — `common/exception/`

Hozir xato bo'lsa Spring o'zining standart javobini qaytaradi va u har xil
shaklda bo'ladi — frontend ishonchli parse qila olmaydi. Katta loyihada
**bitta standart xato formati** bo'ladi.

Fayllar:

| Fayl | Vazifa |
|---|---|
| `AppException.java` | `RuntimeException`, ichida `HttpStatus` + xabar. Barcha domen xatolarining otasi |
| `NotFoundException.java` | `extends AppException`, 404 |
| `ConflictException.java` | `extends AppException`, 409 — masalan email band |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` |

`GlobalExceptionHandler` da kamida:

- `@ExceptionHandler(AppException.class)` → o'z statusi bilan
- `@ExceptionHandler(MethodArgumentNotValidException.class)` → 400 + qaysi maydon
  qanday xato qilgani (`Map<String, String>`)
- `@ExceptionHandler(AccessDeniedException.class)` → 403
- `@ExceptionHandler(Exception.class)` → 500, **stacktrace'ni javobga qo'shma**,
  faqat log'ga yoz. Stacktrace'ni tashqariga chiqarish — xavfsizlik teshigi.

**Format:** Spring 6+ da standart — RFC 7807 `ProblemDetail`. Uni ishlatsang
tayyor sinf keladi (`type`, `title`, `status`, `detail`, `instance`). O'z
`ApiResponse<T>` wrapper'ingni yasashing ham mumkin — lekin ikkalasini birdan
qilma. Tavsiya: `ProblemDetail`, chunki bu standart va Spring uni o'zi ham
ishlatadi.

⚠️ **`@RestControllerAdvice` Security filtrlarigacha yetib bormaydi.**
JWT noto'g'ri bo'lsa xato filter zanjirida chiqadi va handler'ga tushmaydi —
u yerda alohida `AuthenticationEntryPoint` kerak (Faza 3.4). Bu ham klassik tuzoq.

## 0.6 DTO qatlami

Qoida: **Entity hech qachon controller'dan chiqmaydi va kirmaydi.**

Nega:
1. `UserEntity` da `password` bor — bitta e'tiborsizlik va u JSON'ga chiqadi.
2. Frontend'ga kerakli shakl DB shaklidan farq qiladi.
3. Kirishda entity qabul qilsang — foydalanuvchi `"role": "ADMIN"` yuborib
   o'ziga rol berib oladi (**mass assignment** zaifligi).

`user/dto/` ichida `record` lar:

```java
public record UserResponse(Long id, String email, Set<String> roles, boolean active) {}
```

`record` — o'zgarmas (immutable), `equals/hashCode/toString` tayyor.
DTO uchun ideal.

**Mapping:** hozircha qo'lda static factory (`UserResponse.from(entity)`) yetadi.
MapStruct 30+ DTO paydo bo'lganda kiritiladi, hozir emas.

## 0.7 `UserService` ni bean qil

Hozir u oddiy bo'sh klass — Spring uni umuman ko'rmayapti.
`@Service` + `@RequiredArgsConstructor` + `private final UserRepository`.

Service qatlami qoidalari:
- `@Transactional` **service**da bo'ladi, controller yoki repository'da emas.
  Tranzaksiya chegarasi = biznes amal chegarasi.
- Faqat o'qiydigan metodlarga `@Transactional(readOnly = true)` — Hibernate
  dirty checking qilmaydi, tezroq.
- ⚠️ Bir service ichida `this.boshqaMetod()` chaqirsang `@Transactional`
  **ishlamaydi** (self-invocation — proxy chetlab o'tiladi). Bu Spring'dagi
  eng ko'p yiqitadigan tuzoqlardan biri.

## Faza 0 — Tekshirish

```bash
./gradlew build
```

- Ilova ko'tariladi.
- `psql` da: `\d users` → `created_date` ustuni bor.
- Yangi user qo'shilganda `created_date` **null emas**.
- Mavjud bo'lmagan resursga so'rov → sening standart JSON xato formating.

---

# FAZA 1 — Domen: Role va Permission

## 1.1 Paket

Yangi feature paketi: `uz.app.projectv1.rbac`.

```
rbac/
├── entity/Role.java
├── entity/Permission.java
├── RoleRepository.java
├── PermissionRepository.java
├── RoleService.java
├── RoleController.java
├── Permissions.java
└── dto/RoleResponse.java, CreateRoleRequest.java
```

`role/` va `permission/` deb ikkiga bo'lish ham mumkin, lekin ular bir-birisiz
ma'noga ega emas — bitta **bounded context**. Shuning uchun `rbac/`.

## 1.2 `Permission.java`

```java
@Entity @Table(name = "permissions")
public class Permission extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String name;          // "user:read"

    private String description;   // "Foydalanuvchilar ro'yxatini ko'rish"
}
```

`unique = true` — muhim. Permission nomi tizimning kaliti.

## 1.3 `Role.java`

```java
@Entity @Table(name = "roles")
public class Role extends BaseEntity {
    @Column(nullable = false, unique = true, length = 64)
    private String name;          // "ADMIN"  — ROLE_ prefiksisiz!

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();
}
```

- `Set`, `List` emas — takrorlanmaslik kafolati. ManyToMany'da `List`
  ishlatilsa Hibernate bitta element o'chirilganda butun bog'lanishlar
  to'plamini o'chirib qayta yozadi.
- `= new HashSet<>()` — ishga tushirish. `null` collection = `NullPointerException`.
- `fetch = LAZY` — **doim**. `EAGER` qo'ysang har user o'qilganda barcha
  permission'lar tortiladi.

## 1.4 `UserEntity` ni yangila

`UserRole role` enum'ini olib tashla, o'rniga:

```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
private Set<Role> roles = new HashSet<>();
```

**Nega enum yetarli emas?** Enum bilan yangi rol qo'shish = kod o'zgarishi +
deploy. Bir user bir vaqtda MANAGER ham, AUDITOR ham bo'la olmaydi.
`user/enums/UserRole.java` ni o'chirib tashla.

**Trade-off:** agar loyihada 3 ta rol bo'lib, ular hech qachon o'zgarmasa —
enum soddaroq va tezroq. Lekin sen "role va permission based" tizim
qurmoqchisan, demak DB varianti.

## 1.5 `Permissions.java` — konstantalar

```java
public final class Permissions {
    public static final String USER_READ   = "user:read";
    public static final String USER_CREATE = "user:create";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String ROLE_READ   = "role:read";
    public static final String ROLE_ASSIGN = "role:assign";

    // @PreAuthorize uchun tayyor SpEL satrlari
    public static final String CAN_READ_USER = "hasAuthority('" + USER_READ + "')";

    private Permissions() {}
}
```

`@PreAuthorize` ichidagi satr faqat runtime'da tekshiriladi. Konstanta
ishlatsang typo kompilyatsiyada tutiladi.

## 1.6 N+1 va LazyInitializationException — eng muhim tuzoq

Login paytida userni roli va permission'lari bilan **bir marta** olish kerak.
Oddiy `findByEmail` LAZY collection'larni tortmaydi va service'dan
chiqqandan keyin ularga tegsang `LazyInitializationException` chiqadi.

`UserRepository` da:

```java
@EntityGraph(attributePaths = {"roles", "roles.permissions"})
Optional<UserEntity> findByEmail(String email);
```

`@EntityGraph` — Hibernate'ga "bu so'rovda bu bog'lanishlarni ham JOIN qilib
ol" deydi. Ikkita ManyToMany'ni birdan JOIN qilish **kartezian ko'paytma**
beradi (3 rol × 10 permission = 30 qator) — Hibernate `Set` tufayli
dublikatlarni yig'ib tashlaydi, lekin katta hajmda ikkita alohida so'rov
(`@BatchSize` yoki subselect) tezroq. Hozircha `@EntityGraph` yetarli —
optimallashtirishni o'lchamasdan qilma.

## 1.7 `RoleService` / `RoleController`

Admin uchun: rol yaratish, rolga permission qo'shish/olib tashlash,
userga rol biriktirish. Har biri o'z permission'i bilan himoyalanadi
(`role:create`, `role:assign`).

⚠️ Tizim rollarini (`ADMIN`) o'chirishga ruxsat berma — `Role`ga
`boolean systemRole` maydoni qo'shib, o'chirishdan oldin tekshir. Aks holda
kimdir ADMIN rolini o'chirib tizimga hech kim kira olmay qoladi.

## Faza 1 — Tekshirish

Ilovani ko'tar, `psql`:

```sql
\dt   -- roles, permissions, role_permissions, user_roles jadvallari bor
```

`ddl-auto: update` ularni yaratadi. Ammo bu **vaqtinchalik** — Faza 2 da
Flyway'ga o'tasan.

---

# FAZA 2 — Flyway migration

## Nega `ddl-auto: update` production'da ishlamaydi

- Ustun **o'chirmaydi**, tip **o'zgartirmaydi**, ba'zi constraint'larni qo'shmaydi.
- Nima qilishini oldindan bilib bo'lmaydi — jamoada har kimning DB'si boshqacha.
- Rollback yo'q. Migration tarixi yo'q.
- CI/CD'da bir xil natija kafolati yo'q.

Katta loyihada **doim**: `ddl-auto: validate` + Flyway (yoki Liquibase).
`validate` — Hibernate DB sxemasi entity'larga mos kelishini tekshiradi,
mos kelmasa ilova **ko'tarilmaydi**. Bu juda foydali xavfsizlik to'ri.

## 2.1 Dependency

`build.gradle`:

```groovy
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
```

## 2.2 Fayllar

`src/main/resources/db/migration/` — nom formati qat'iy:
`V<versiya>__<tavsif>.sql`, **ikkita** pastki chiziq.

```
V1__create_users_table.sql
V2__create_rbac_tables.sql
V3__seed_permissions_and_roles.sql
```

## 2.3 Qoidalar (buzilsa — production'da avariya)

1. **Migrate qilingan faylni hech qachon o'zgartirma.** Flyway checksum
   saqlaydi, o'zgartirsang keyingi ishga tushishda xato beradi. Tuzatish uchun
   **yangi** migration yoz.
2. Seed data (permission ro'yxati) ham migration — `INSERT ... ON CONFLICT DO NOTHING`.
3. Katta jadvalga index qo'shishda PostgreSQL'da `CREATE INDEX CONCURRENTLY` —
   aks holda jadval bloklanadi.
4. Index kerak bo'ladigan joylar: `users.email`, `refresh_tokens.token_hash`,
   ManyToMany jadvallardagi FK ustunlar.

## 2.3.1 ⚠️ `email` uchun QISMAN unique index

Soft delete bilan oddiy `UNIQUE(email)` mina qo'yadi:
o'chirilgan `ali@mail.uz` qatori DB'da qoladi → `existsByEmail` uni ko'rmaydi
(`@SQLRestriction` yashiradi) → kod INSERT qiladi → `duplicate key` → 500.

Yechim (PostgreSQL):

```sql
CREATE UNIQUE INDEX uk_users_email_active ON users (email) WHERE deleted = false;
```

Hibernate shartli index generatsiya qila olmaydi, shuning uchun:
`UserEntity` dan `unique = true` va `@Index(unique = true)` **olib tashlanadi**,
index faqat migration'da yashaydi.

## 2.4 Boshlang'ich holat

Hozirgi DB'da `users` jadvali `ddl-auto: update` bilan yaratilgan. Toza
boshlash uchun: DB'ni tashlab, qaytadan yarat (`DROP DATABASE project_v1;`),
keyin `ddl-auto: validate` qo'yib Flyway'ga hamma narsani yaratdir.

## 2.5 Seed qilinadigan ma'lumot

- Barcha permission'lar (`Permissions.java` dagi konstantalar bilan **aynan** bir xil).
- Tizim rollari: `ADMIN` (hamma permission), `USER` (minimal), `MANAGER`, `OPERATOR`.
- Birinchi admin user — paroli **bcrypt hash** ko'rinishida. Ochiq parol yozma.

⚠️ Seed'ni `CommandLineRunner` da kod bilan qilish ham mumkin va tez, lekin
migration'da qilish to'g'riroq: versiyalanadi, har muhitda bir xil, va
ilova kodi DB holatini "tuzatib" yurmaydi.

## Faza 2 — Tekshirish

```sql
SELECT * FROM flyway_schema_history;   -- barcha migration'lar "success"
SELECT r.name, p.name FROM roles r
  JOIN role_permissions rp ON rp.role_id = r.id
  JOIN permissions p ON p.id = rp.permission_id;
```

`ddl-auto: validate` bilan ilova xatosiz ko'tarilishi kerak.

---

# FAZA 3 — Security infratuzilmasi

## 3.1 `PasswordEncoder` bean — `security/SecurityBeansConfig.java`

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

**Nega `new BCryptPasswordEncoder()` emas?**
`DelegatingPasswordEncoder` hash'ni `{bcrypt}$2a$10$...` ko'rinishida saqlaydi.
Ertaga Argon2'ga o'tsang — eski `{bcrypt}` parollar ishlashda davom etadi va
yangi parollar `{argon2}` bo'ladi. Prefikssiz saqlasang — migratsiya qilib
bo'lmaydi, hamma parolni reset qilishga to'g'ri keladi.

⚠️ Seed migration'dagi hash ham `{bcrypt}` prefiksi bilan bo'lsin.

## 3.2 `security/CustomUserDetails.java`

Spring Security'ga `UserDetails` interfeysi kerak. Ikki yo'l:

- **A)** `UserEntity implements UserDetails` — tez, lekin domen entity'ni
  Security'ga bog'lab qo'yadi (layer buzilishi) va LAZY collection'lar bilan
  muammo chiqaradi.
- **B)** Alohida adapter klass — `UserEntity` ni o'rab oladi. ✅ **Buni tanla.**

```java
public record CustomUserDetails(Long id, String email, String password,
                                boolean active, Collection<GrantedAuthority> authorities)
        implements UserDetails { ... }
```

Bu yerda `getAuthorities()` qaytaradigan to'plam:

```
ROLE_ADMIN, ROLE_MANAGER, user:read, user:create, ...
```

Ya'ni rollar `ROLE_` prefiksi bilan, permission'lar prefikssiz. Ikkalasi
bitta `Collection<GrantedAuthority>` ichida yashaydi — Spring Security ularni
farqlamaydi, `hasRole` shunchaki prefiks qo'shib qidiradi.

Bu konvertatsiyani **bitta joyda** qil (`CustomUserDetails.from(user)`),
aks holda uch joyda uch xil bo'lib ketadi.

`isEnabled()` → `active && !deleted`. `isAccountNonLocked()` → keyinroq
brute-force himoyasi uchun kerak bo'ladi.

## 3.3 `security/CustomUserDetailsService.java`

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) { ... }
}
```

- 1.6 dagi `@EntityGraph` li `findByEmail` ni ishlat.
- Topilmasa `UsernameNotFoundException`.
- ⚠️ Login xatosida javobda **hech qachon** "bunday email yo'q" dema — faqat
  "Email yoki parol noto'g'ri". Aks holda hujumchi qaysi emaillar ro'yxatdan
  o'tganini aniqlaydi (**user enumeration**).

## 3.4 `SecurityConfig` ni to'g'rila

Hozirgi konfiguratsiyada yetishmayapti:

```java
http
  .csrf(AbstractHttpConfigurer::disable)          // stateless API uchun kerak emas
  .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
  .authorizeHttpRequests(...)                     // mavjud
  .exceptionHandling(e -> e
      .authenticationEntryPoint(...)              // 401 JSON
      .accessDeniedHandler(...))                  // 403 JSON
  .oauth2ResourceServer(o -> o.jwt(...));         // Faza 4
```

**CSRF nega o'chiriladi?** CSRF hujumi brauzer cookie'ni avtomatik yuborishiga
tayanadi. JWT'ni `Authorization` header'da yuborsang — avtomatik yuborilmaydi,
demak CSRF vektori yo'q. ⚠️ Lekin token'ni cookie'da saqlasang — CSRF **qaytadan
kerak bo'ladi**. "JWT = CSRF kerak emas" degan qoida faqat header uchun to'g'ri.

**`STATELESS` nega?** Server sessiya saqlamaydi → gorizontal skeyl oson,
`JSESSIONID` yaratilmaydi.

**`requestMatchers` tartibi muhim** — birinchi mos kelgani ishlaydi. Eng aniqi
yuqorida, `anyRequest()` doim oxirida.

⚠️ Statik/hujjat yo'llarini (`/actuator/health`, OpenAPI) ochishni unutma,
aks holda ular ham 401 qaytaradi.

## Faza 3 — Tekshirish

Vaqtincha `CommandLineRunner` da
`userDetailsService.loadUserByUsername("admin@app.uz")` chaqirib, authority'lar
ro'yxatini log'ga chiqar. `ROLE_ADMIN` + permission'lar ko'rinishi kerak.
Shuningdek `passwordEncoder.matches(...)` seed'dagi hash bilan `true` bersin.

---

# FAZA 4 — JWT

## 4.1 Muhim qaror: qaysi kutubxona

Loyihada allaqachon `spring-boot-starter-oauth2-resource-server` bor. Demak
**jjwt yoki java-jwt kerak emas** — Spring Security'ning o'zi Nimbus orqali
JWT'ni imzolaydi va tekshiradi.

| | Spring `JwtEncoder`/`JwtDecoder` | jjwt + `OncePerRequestFilter` |
|---|---|---|
| Kod hajmi | kam — filter yozilmaydi | ko'p — qo'lda filter |
| Xato boshqaruvi | Security zanjirida standart | o'zing yozasan |
| JWK / kalit rotatsiyasi | tayyor | qo'lda |
| Internetdagi tutorial'lar | kam | ko'p |

Katta loyihada **birinchisi**. YouTube'dagi ko'p qo'llanmalar ikkinchisini
o'rgatadi, chunki eski. Sen birinchisini qil.

## 4.2 Kalitlar: HMAC yoki RSA?

- **HMAC (HS256)** — bitta maxfiy kalit, imzolaydi ham, tekshiradi ham.
  Monolit uchun yetarli va sodda.
- **RSA (RS256)** — private key imzolaydi, public key tekshiradi. Boshqa
  servislar sening token'ingni **maxfiy kalitni bilmasdan** tekshira oladi.
  Mikroservis kelajagi bo'lsa — shu.

Tavsiya: **RS256**. Kalitlarni `application.yml` ga emas, environment
o'zgaruvchisiga qo'y. `.gitignore` ga `*.pem` qo'sh.

## 4.3 `security/JwtConfig.java`

```java
@Bean JwtDecoder jwtDecoder()  { return NimbusJwtDecoder.withPublicKey(pub).build(); }
@Bean JwtEncoder jwtEncoder()  { /* ImmutableJWKSet(new RSAKey...) */ }
```

## 4.4 `security/JwtService.java` — token yasash

Access token claim'lari:

| Claim | Qiymat | Izoh |
|---|---|---|
| `iss` | app nomi | kim chiqargan |
| `sub` | user id (email emas) | email o'zgarishi mumkin, id — yo'q |
| `iat` / `exp` | vaqt | **15 daqiqa** |
| `authorities` | `"ROLE_ADMIN user:read user:create"` | probel bilan ajratilgan |
| `jti` | UUID | bekor qilish/audit uchun |

⚠️ **Token'ga hech qachon parol, hash, karta raqami qo'yma.** JWT
**shifrlanmaydi**, faqat imzolanadi — istagan odam `base64` ni ochib o'qiy oladi.

## 4.5 Permission'lar token ichida bo'lsinmi?

| | Token ichida | Har so'rovda DB'dan |
|---|---|---|
| Tezlik | tez, DB'ga tegmaydi | har so'rovda so'rov (yoki cache) |
| Yangilanish | rolni olib qo'ysang ham token `exp` gacha ishlaydi | darhol kuchga kiradi |
| Hajm | 50 permission = katta header | kichik token |

Standart yechim: **token ichida + qisqa `exp` (15 daq)**. Huquq o'zgarishi
maksimum 15 daqiqada kuchga kiradi, bu ko'pchilik tizim uchun maqbul.
Bankdek tizimda esa har so'rovda DB/Redis'dan tekshiriladi.

## 4.6 `JwtAuthenticationConverter` — buni tushirib qoldirma

Standart Spring `scope` claim'ini o'qib `SCOPE_` prefiksini qo'shadi —
sen `SCOPE_user:read` emas, `user:read` xohlaysan. Shuning uchun:

```java
@Bean JwtAuthenticationConverter jwtAuthenticationConverter() {
    var g = new JwtGrantedAuthoritiesConverter();
    g.setAuthorityPrefix("");            // prefikssiz
    g.setAuthoritiesClaimName("authorities");
    var c = new JwtAuthenticationConverter();
    c.setJwtGrantedAuthoritiesConverter(g);
    return c;
}
```

Bu qadam tushib qolsa — token to'g'ri, lekin **hamma joyda 403**. Va sababi
ko'rinmaydi. Debug qilganda birinchi shu yerga qara.

## 4.7 Refresh token

Access token qisqa umrli → foydalanuvchini har 15 daqiqada login qildirmaslik
uchun refresh token kerak.

`auth/entity/RefreshToken.java`:

| Maydon | Izoh |
|---|---|
| `tokenHash` | ⚠️ token'ning **SHA-256 hash'i**, o'zi emas. DB o'g'irlansa token ishlamasin |
| `userId` | egasi |
| `expiresAt` | 7–30 kun |
| `revokedAt` | bekor qilingan vaqti |
| `replacedBy` | rotatsiya zanjiri |

**Token rotation:** har `/refresh` da eski token bekor qilinadi va yangisi
beriladi. Agar allaqachon bekor qilingan token bilan so'rov kelsa — demak uni
kimdir o'g'irlagan → **shu foydalanuvchining barcha token'larini bekor qil**.
Bu OAuth 2.0 Security BCP'dagi standart himoya.

Refresh token — **JWT bo'lishi shart emas**, oddiy `SecureRandom` 256-bit
tasodifiy satr yetadi (u baribir DB'dan tekshiriladi).

## Faza 4 — Tekshirish

Token'ni qo'lda yasab, `header.payload` ni base64'dan ochib ko'r: `sub`, `exp`,
`authorities` joyida. Keyin himoyalangan endpoint'ga
`Authorization: Bearer <token>` bilan urin — 200; tokensiz — 401;
buzilgan token — 401; yetarli huquqsiz — 403.

---

# FAZA 5 — Auth endpoint'lar

## 5.1 DTO — `auth/dto/`

| DTO | Maydonlar | Validatsiya |
|---|---|---|
| `RegisterRequest` | email, password | `@Email`, `@NotBlank`, `@Size(min=8)` |
| `LoginRequest` | email, password | `@NotBlank` |
| `AuthResponse` | accessToken, refreshToken, expiresIn, tokenType="Bearer" | — |
| `RefreshRequest` | refreshToken | `@NotBlank` |

⚠️ `RegisterRequest` da **`role` maydoni bo'lmasin**. Ro'yxatdan o'tayotgan
odam o'ziga rol tanlay olmasligi kerak — server `USER` rolini majburan beradi.

Controller'da `@Valid` qo'yishni unutma — annotatsiyalarsiz validatsiya
**jimgina ishlamaydi**.

## 5.2 `auth/AuthService.java`

`register(RegisterRequest)`:
1. Email band emasligini tekshir → band bo'lsa `ConflictException`.
   ⚠️ Bu tekshiruv **race condition**'ga ochiq — DB'da ham `unique` constraint
   bo'lsin va `DataIntegrityViolationException` ni ushla.
2. `passwordEncoder.encode(...)`.
3. Default `USER` rolini biriktir.
4. `@Transactional` — user va rol bog'lanishi bitta tranzaksiyada.

`login(LoginRequest)`:
1. `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(...))`
   — **o'zing parol solishtirma**, `AuthenticationManager` qilsin. U timing
   attack'dan himoya va lock/disabled tekshiruvlarini o'z ichiga oladi.
2. `BadCredentialsException` → 401, xabar umumiy: "Email yoki parol noto'g'ri".
3. Access + refresh token yasa, refresh'ni hash qilib DB'ga yoz.

`refresh(...)`: hash bo'yicha topish → muddat/bekor tekshiruvi → rotatsiya.

`logout(...)`: refresh token'ni `revokedAt` bilan belgila. Access token
**bekor qilinmaydi** — u `exp` gacha yashaydi. Bu JWT'ning tabiati.
Darhol bekor qilish kerak bo'lsa — `jti` blacklist (Redis) qo'shiladi, lekin
bu stateless'lik afzalligini yo'qotadi. Talab bo'lmasa qilma.

## 5.3 `AuthController`

`/ping` ni o'chir. `@RequestMapping("/auth")` qoladi (WebConfig `/api/v1`
qo'shadi → `/api/v1/auth/...`).

| Metod | Yo'l | Status |
|---|---|---|
| POST | `/register` | 201 Created |
| POST | `/login` | 200 |
| POST | `/refresh` | 200 |
| POST | `/logout` | 204 No Content |

Controller **yupqa** bo'lsin: `@Valid` DTO qabul qiladi → service chaqiradi →
`ResponseEntity` qaytaradi. Biznes mantiq controller'da bo'lmaydi.

## Faza 5 — Tekshirish

```bash
curl -X POST localhost:8081/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@b.uz","password":"Parol12345"}'

curl -X POST localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@b.uz","password":"Parol12345"}'
```

- Bir xil email ikki marta → 409.
- `"password":"123"` → 400 va qaysi maydon xato ekani ko'rinadi.
- DB'da `password` ustuni `{bcrypt}$2a$...` — ochiq parol emas.
- Javobda `password` **yo'q**.

---

# FAZA 6 — Authorization

## 6.1 `@EnableMethodSecurity`

`SecurityConfig` ustiga qo'y. Busiz `@PreAuthorize` **butunlay e'tiborsiz
qoldiriladi** — endpoint hammaga ochiq bo'lib turaveradi va hech qanday
ogohlantirish chiqmaydi. Eng xavfli "jim" xato.

## 6.2 Ikki qatlamli himoya

1. **URL darajasi** (`SecurityConfig`) — yirik darvozalar:
   `/api/v1/auth/**` → permitAll, `/api/v1/admin/**` → `hasRole('ADMIN')`.
2. **Metod darajasi** (`@PreAuthorize`) — aniq huquq:
   `@PreAuthorize(Permissions.CAN_DELETE_USER)`.

Ikkalasi ham kerak. URL qoidasi unutilishi mumkin, metod annotatsiyasi kod
bilan birga ko'chib yuradi.

`@PreAuthorize` ni **service**ga qo'ymi yoki **controller**ga?
Katta loyihalarda ko'pincha service'ga — chunki service'ni boshqa controller,
scheduler yoki message consumer ham chaqirishi mumkin va himoya bitta joyda
qoladi. Bittasini tanlab, izchil bo'l.

## 6.3 Ownership tekshiruvi

"Foydalanuvchi **o'z** profilini tahrirlay oladi" — buni permission hal
qilmaydi (bu ABAC hududi).

Oddiy yo'l:
```java
@PreAuthorize("hasAuthority('user:update') or #id == authentication.principal.id")
```

Murakkablashsa — `@Component("userSecurity")` yasab
`@PreAuthorize("@userSecurity.canEdit(#id, authentication)")` deb chaqir.
SpEL ichida uzun mantiq yozish — o'qib bo'lmaydigan kod. Uch shartdan
oshsa — komponentga ko'chir.

## 6.4 Joriy foydalanuvchini olish

`SecurityContextHolder.getContext().getAuthentication()` ni har joyda yozma.
`@AuthenticationPrincipal Jwt jwt` parametri yoki kichik `CurrentUser` helper
yasa. Bir joyda bo'lsin.

## Faza 6 — Tekshirish

- `USER` roli bilan `DELETE /api/v1/users/1` → **403**.
- `ADMIN` bilan → 200.
- Admin panelidan MANAGER roliga `user:delete` qo'shib, MANAGER token'ini
  **yangilagach** → 200. Kod o'zgarmadi. RBAC ishlayapti.

---

# FAZA 7 — Production hardening

Ushbular tugagach qilinadi, oldin emas:

- [ ] **Profillar** — `application-dev.yml`, `application-prod.yml`.
      Parol/kalitlar `${DB_PASSWORD}` orqali env'dan. `application.yml` da
      hech qanday sir bo'lmasin (hozir `password: 1234` git'da turibdi).
- [ ] **CORS** — `SecurityConfig` da, `@CrossOrigin` bilan har controller'da emas.
- [ ] **Brute-force himoyasi** — 5 marta xato login → hisobni 15 daqiqa
      bloklash (`failedAttempts`, `lockedUntil` ustunlari) yoki IP bo'yicha
      rate limit (Bucket4j).
- [ ] **Xavfsizlik header'lari** — HSTS, `X-Content-Type-Options`, CSP.
- [ ] **Audit log** — kim kirdi, kim rol o'zgartirdi. Alohida jadval.
- [ ] **Testlar** — `@SpringBootTest` + `MockMvc` + `@WithMockUser`.
      Har rol uchun 403/200 testi. DB uchun **Testcontainers** (H2 emas —
      H2 PostgreSQL'dek tutmaydi va yolg'on ishonch beradi).
- [ ] **OpenAPI** — springdoc, `Authorize` tugmasi bilan.
- [ ] **Logging** — `MDC` ga `requestId` va `userId`. Parol/token'ni **hech
      qachon** log'ga yozma.
- [ ] **Actuator** — `/actuator/health` ochiq, qolgani `ADMIN`ga.

---

# Qarorlar jurnali (ADR)

Arxitektura qarorlari shu yerga yoziladi — "nega bunday qilganmiz" savoli
olti oydan keyin ham javobsiz qolmasin.

| # | Sana | Qaror | Sabab |
|---|---|---|---|
| 1 | 2026-09-10 | JWT uchun `oauth2-resource-server`, jjwt emas | Kam kod, standart xato boshqaruvi, JWK tayyor |
| 2 | 2026-09-10 | Rol va permission DB'da, enum emas | Yangi rol qo'shish uchun deploy kerak bo'lmasin |
| 3 | 2026-09-10 | Endpoint'lar permission tekshiradi, rol emas | Huquqlarni kodsiz o'zgartirish |
| 4 | 2026-09-10 | `ddl-auto: validate` + Flyway | Takrorlanuvchi, versiyalangan sxema |
| 5 | 2026-09-10 | MapStruct (1.6.3) Faza 0 dan boshlab | `unmappedTargetPolicy=ERROR` — unutilgan maydon build'ni yiqitadi. Java 26 da ishlashi tekshirildi |
| 6 | 2026-09-10 | Soft delete: `@SQLRestriction` + kerak bo'lganda native query | Default xavfsiz (faqat tiriklar), admin/audit uchun eshik ochiq. `@FilterDef` — teskari default, AOP kerak, hozir ortiqcha |
| 7 | 2026-09-10 | `email` uchun **qisman** unique index (Faza 2) | Soft delete bilan oddiy unique constraint o'chirilgan email'ni abadiy band qiladi |
| 8 | 2026-09-10 | `GlobalExceptionHandler extends ResponseEntityExceptionHandler` | Spring MVC'ning ~20 ta standart xatosi (tip mos kelmasligi, buzuq JSON, noto'g'ri HTTP metod, topilmagan yo'l) `Exception.class` ga tushib 500 bo'lib ketmasin |
| 9 | 2026-09-10 | `@Table(indexes = @Index(unique = true))` **qoldiriladi** | Tekshirildi: Hibernate `@Column(unique=true)` bilan birlashtiradi, DB'da bitta obyekt chiqadi. `@Index` unga o'qiladigan nom beradi (`UK_6dotkott...` emas, `idx_users_email`) |
