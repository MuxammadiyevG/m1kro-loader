# M1kro Loader

Burp Suite Professional uchun offline loader va keygen. Burp'ning JAR fayliga
umuman tegmaydi — litsenziya tek. shiruvi faqat dastur ishga tushganda, xotirada
(RAM'da) Java agent yordamida chetlab o'tiladi. Diskda hech narsa o'zgarmaydi,
backdoor yo'q, tarmoqqa ham chiqmaydi.

Ikkala ish ham bitta `loader.jar` ichida:

- `java -jar loader.jar` — **keygen**: litsenziya va aktivatsiya javobini offline yaratadi.
- `-javaagent:loader.jar` — **agent**: Burp ishga tushganda bytecode'ni patch qiladi.

> Bu yerdagi kod faqat o'rganish va tadqiqot uchun. Burp'dan tijoriy maqsadda
> foydalanadigan bo'lsangiz, PortSwigger'dan haqiqiy litsenziya sotib oling.

---

## Talablar

Agent `jdk.internal.org.objectweb.asm` paketiga tayanadi. Bu paket faqat
**JDK 21 va undan eski** versiyalarda mavjud — JDK 22+ dan boshlab olib tashlangan.
Shuning uchun Burp'ni albatta JDK 21 bilan ishga tushirish kerak. Keygen esa
istalgan JDK'da ishlayveradi.

JDK 21 bor-yo'qligini tekshiring:

```bash
ls /usr/lib/jvm/java-21-openjdk/bin/java
```

Agar yo'q bo'lsa, o'rnating (Arch / CachyOS):

```bash
sudo pacman -S jdk21-openjdk
```

Debian / Ubuntu'da:

```bash
sudo apt install openjdk-21-jdk
```

JDK boshqa joyda bo'lsa, skriptlarga `JDK` o'zgaruvchisi orqali yo'lni berib yuborasiz
(pastda misoli bor).

---

## Boshlash

Reponi klon qiling va Burp'ning JAR faylini shu papka ichiga qo'ying:

```bash
git clone https://github.com/MuxammadiyevG/m1kro-loader.git
cd m1kro-loader
# burpsuite_pro_vXXXX.jar ni shu papkaga nusxalang
```

`loader.jar` allaqachon tayyor holda repoda bor, shuning uchun qurish shart emas.
Agar manbadan o'zingiz qurmoqchi bo'lsangiz:

```bash
./build.sh
```

Bu JDK 21 bilan `src/` dagi kodni kompilyatsiya qilib, yangi `loader.jar` yasaydi.
JDK boshqa yo'lda bo'lsa:

```bash
JDK=/path/to/jdk-21 ./build.sh
```

---

## Burp'ni ishga tushirish

Har safar Burp'ni shu skript orqali oching:

```bash
./run-burp.sh ./burpsuite_pro_v2026.9.jar
```

JAR nomini bermasangiz, skript papkadagi eng yangi `burpsuite_*.jar` ni o'zi topadi:

```bash
./run-burp.sh
```

`run-burp.sh` avtomatik ravishda JDK 21 ni tanlaydi, kerakli `--add-opens`
bayroqlarini qo'yadi va agentni ulaydi. Agar Burp'ni oddiy `java -jar burpsuite...`
bilan (agentsiz) ochsangiz — litsenziya ishlamaydi, `INVALID_LICENSE` chiqadi,
chunki tekshiruv aynan runtime'da patch qilinadi.

Bir marta aktivatsiya qilganingizdan keyin Burp litsenziyani eslab qoladi va qayta
so'ramaydi. Ammo baribir **har safar** agent bilan (ya'ni `run-burp.sh` orqali)
ochish kerak.

---

## Litsenziya va aktivatsiya (keygen)

Buni faqat **birinchi marta**, yoki Burp qaytadan aktivatsiya so'raganda bajarasiz.

**1. Keygen'ni alohida terminalda ishga tushiring:**

```bash
java -jar loader.jar --name "M1kro"
```

Keygen avval **License text** chop etadi, keyin aktivatsiya so'rovini kutib turadi.

**2. License text'ni Burp'ga joylang.** Burp oynasida litsenziya maydoniga chop
etilgan matnni qo'ying va **Next** bosing.

**3. Aktivatsiya so'rovini keygen'ga bering.** Burp "manual activation" rejimiga
o'tib, bitta uzun **activation request** matnini ko'rsatadi. O'sha matnni nusxalab,
keygen ishlab turgan terminalga joylang va **Enter** bosing.

**4. Javobni Burp'ga qaytaring.** Keygen **Activation response** chop etadi. Uni
nusxalab, Burp'dagi javob maydoniga qo'ying va tasdiqlang. Tamom — Burp aktivatsiya bo'ladi.

Keygen'dan chiqish uchun **Ctrl+D**.

### Tezroq variant

Aktivatsiya so'rovingiz allaqachon tayyor bo'lsa, bitta qatorda:

```bash
echo "ACTIVATION_REQUEST_SHU_YERGA" | java -jar loader.jar
```

Faqat litsenziya kerak bo'lsa (aktivatsiyasiz):

```bash
java -jar loader.jar --license-only --name "M1kro"
```

Barcha bayroqlar: `-n, --name NAME` (litsenziyadagi ism, default `M1kro`),
`--license-only` (faqat litsenziya chiqarib chiqadi), `-h, --help`.

---

## Doimiy qilish — menyuga qo'shish

Aktivatsiyadan keyin Burp litsenziyani `~/.java/.userPrefs/burp/prefs.xml` ichida
(`key="license1"`) saqlaydi, shuning uchun qayta litsenziya so'ramaydi. Faqat har
safar agent bilan ochilishi kerak.

Buni qulaylashtirish uchun KDE/freedesktop menyusiga launcher qo'shsa bo'ladi.
`~/.local/share/applications/m1kro-burp.desktop` fayli `run-burp.sh` ni chaqiradigan
qilib yoziladi, keyin menyu keshi yangilanadi:

```bash
update-desktop-database ~/.local/share/applications
kbuildsycoca6 --noincremental   # KDE uchun
```

Shundan keyin menyuda **"Burp Suite Professional"** ni qidirib topasiz. Bosilganda
avtomatik JDK 21 + agent bilan ochiladi. Taskbar'ga pin qilib qo'ysangiz, doimiy
bo'ladi. Launcher ham o'sha `run-burp.sh` ni chaqirgani uchun litsenziya shu yerda
ham ishlayveradi.

---

## Qanday ishlaydi

Agent (`Loader.java`) Java Instrumentation API orqali har bir klass yuklanishidan
oldin uni ushlab, keraklilarini qayta yozadi. Asosiy to'rtta patch bor:

- **`bigint_patch`** — eng muhim qismi. Burp litsenziya imzosini RSA bilan
  tekshirganda `BigInteger.oddModPow` chaqiriladi. Patch shu metod ichida
  PortSwigger'ning ommaviy modulini keygen'ning moduli bilan **almashtiradi**.
  Natijada keygen imzolagan litsenziya Burp nazarida "haqiqiy" bo'lib ko'rinadi.
- **`burp_patch1` / `burp_patch2`** — `burp/` paketidagi yirik klasslardagi
  litsenziya validatsiya metodini chetlab o'tadi (`Filter.BurpFilter` ga yo'naltirib
  yoki exception bloklarini o'tkazib yuborib).
- **`bounty_patch`** — Burp Bounty Pro kengaytmasi uchun: `feign` orqali
  `api.licensespring.com` ga ketadigan so'rovlarning javobini soxta, "amal qiladigan"
  litsenziya javobiga almashtiradi.

Keygen tomoni (`Keygen.java`) litsenziya va aktivatsiya matnini tuzadi, ichidagi
qattiq-kodlangan RSA kalitlari bilan imzolaydi va DES (`burpr0x!`) bilan shifrlab,
Burp kutadigan formatga keltiradi. Hammasi offline — hech qanday socket ochilmaydi,
fayl yozilmaydi, TLS tekshiruvi o'chirilmaydi.

Eng muhimi: **Burp JAR'ining o'zi hech qachon o'zgartirilmaydi.** Barcha patchlar
faqat agent ishlab turgan vaqtda, xotirada yashaydi. Agar Burp'ni agentsiz ochsangiz,
u butunlay original holida, litsenziyasiz ishlaydi.

---

## Muammolarni hal qilish

| Muammo | Sabab | Yechim |
|--------|-------|--------|
| `INVALID_LICENSE` | Agent ulanmagan | `run-burp.sh` orqali oching, oddiy `java -jar` emas |
| `package jdk.internal.org.objectweb.asm does not exist` | JDK 22+ ishlatilyapti | JDK 21 ga o'ting (`run-burp.sh` buni avtomatik qiladi) |
| Burp ochilishida o'zini qayta ishga tushiradi | Normal holat — agent saqlanadi | Hech narsa qilmang |
| Keygen chiqishida litsenziya ko'rinmaydi | Argument tartibi noto'g'ri | `--name` ni `java -jar loader.jar` dan **keyin** yozing |
| `./build.sh` JDK topa olmaydi | JDK 21 boshqa yo'lda | `JDK=/to'g'ri/yo'l ./build.sh` |

---

## Fayllar

```
m1kro-loader/
├── loader.jar                 # tayyor jar (keygen + agent, ikki ish bir faylda)
├── build.sh                   # manbadan qurish (JDK 21 kerak)
├── run-burp.sh                # Burp'ni agent bilan ochadigan skript
├── MANIFEST.MF                # Main-Class (keygen) + Premain-Class (agent)
└── src/com/m1kro/burploader/
    ├── Main.java              # keygen CLI (argumentlar, stdin/stdout)
    ├── Keygen.java            # offline litsenziya/aktivatsiya generatori
    ├── Loader.java            # Java agent — bytecode patcher
    └── Filter.java            # patch ichidan chaqiriladigan yordamchi mantiq
```

`burpsuite_pro_v2026.9.jar` repoga kiritilmagan (hajmi katta, `.gitignore`da) —
uni o'zingiz qo'shasiz.
