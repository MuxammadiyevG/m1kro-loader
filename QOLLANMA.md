# M1kro Loader — lokal foydalanish qo'llanmasi

> Shaxsiy/lokal foydalanish uchun. Burp JAR'ning o'zi o'zgartirilmaydi — crack faqat
> ishga tushganda (RAM'da), Java agent orqali ishlaydi.

---

## 0. Talablar (bir marta tekshiriladi)

Agent `jdk.internal.org.objectweb.asm` ga tayanadi — bu faqat **JDK 21 va undan eski**
versiyalarda bor. JDK 27 da YO'Q, shuning uchun Burp'ni JDK 21 bilan ishga tushirish shart.
Keygen esa istalgan JDK bilan ishlaydi.

```bash
# JDK 21 bor-yo'qligini tekshirish:
ls /usr/lib/jvm/java-21-openjdk/bin/java

# Agar yo'q bo'lsa (Arch/CachyOS):
sudo pacman -S jdk21-openjdk
```

Fayllar shu papkada bo'lishi kerak:
```bash
cd /home/mikro/burp_pro/m1kro-loader
ls -1
# build.sh  loader.jar  MANIFEST.MF  run-burp.sh  src/  burpsuite_pro_v2026.9.jar
```

---

## 1. Loader'ni manbadan qurish (ixtiyoriy — loader.jar allaqachon tayyor)

Agar manbadan qayta qurmoqchi bo'lsangiz:

```bash
cd /home/mikro/burp_pro/m1kro-loader
./build.sh
```

Natija: shu papkada yangi `loader.jar` paydo bo'ladi.

---

## 2. Burp'ni ishga tushirish (HAR SAFAR shunday)

```bash
cd /home/mikro/burp_pro/m1kro-loader
DISPLAY=:0 ./run-burp.sh ./burpsuite_pro_v2026.9.jar
```

> `run-burp.sh` avtomatik JDK 21 ni ishlatadi va agentni ulaydi.
> **Agent'siz** oddiy `java -jar burpsuite...` qilsangiz — litsenziya ishlamaydi
> (INVALID_LICENSE), chunki tekshiruv runtime'da patch qilinadi.

Bir marta aktivatsiya qilgandan keyin Burp litsenziyani eslab qoladi — lekin baribir
har safar agent bilan (`run-burp.sh` orqali) ochish kerak.

---

## 3. Yangi litsenziya + aktivatsiya yaratish (keygen)

Bu faqat **birinchi marta** yoki Burp qayta aktivatsiya so'raganda kerak.

### 3.1. Keygen'ni ishga tushiring (alohida terminalda)

```bash
cd /home/mikro/burp_pro/m1kro-loader
java -jar loader.jar --name "M1kro"
```

Keygen quyidagilarni qiladi:
1. **License text** chop etadi — uni nusxalang.
2. Keyin kutib turadi (activation request so'raydi).

### 3.2. License'ni Burp'ga kiriting

Burp oynasida litsenziya maydoniga nusxalangan **License text**'ni joylang → **Next**.

### 3.3. Activation request'ni keygen'ga bering

Burp "manual activation" rejimida bir **activation request** matnini ko'rsatadi.
Uni nusxalab, keygen ishlab turgan terminalga **joylang va Enter** bosing.

Keygen **Activation response** chop etadi.

### 3.4. Response'ni Burp'ga qaytaring

Chop etilgan **Activation response**'ni nusxalab, Burp'dagi javob maydoniga joylang →
tasdiqlang. Burp aktivatsiya bo'ladi.

Keygen'dan chiqish: **Ctrl+D**.

---

## 4. Bitta komandada keygen (tez variant)

Agar activation request'ingiz tayyor bo'lsa, bitta qatorda javob olish:

```bash
cd /home/mikro/burp_pro/m1kro-loader
echo "BU_YERGA_ACTIVATION_REQUEST" | java -jar loader.jar
```

Faqat license kerak bo'lsa (activation'siz):

```bash
java -jar loader.jar --license-only --name "M1kro"
```

---

## 4.5. Doimiy qilish — Applications menyusiga qo'shish

Bir marta aktivatsiya qilgandan keyin Burp litsenziyani
`~/.java/.userPrefs/burp/prefs.xml` (`key="license1"`) ichida **saqlaydi** — qayta
license so'ramaydi. Faqat **har safar agent bilan** ochish kerak.

Shuni osonlashtirish uchun KDE applications menyusiga launcher qo'shilgan:

```bash
# Launcher fayli:
cat ~/.local/share/applications/m1kro-burp.desktop

# Menyu keshini yangilash (kerak bo'lsa):
update-desktop-database ~/.local/share/applications
kbuildsycoca6 --noincremental
```

Endi KDE menyusida **"Burp Suite Professional"** ni qidiring (Development / Security
bo'limida) yoki Application Launcher'da yozib qidiring. Bosilganda avtomatik JDK 21 +
agent bilan ochiladi. Taskbar'ga pin qilsangiz — doimiy bo'ladi.

> Launcher `run-burp.sh` ni chaqiradi, u esa agentni ulaydi. Shuning uchun menyudan
> ochilganda ham litsenziya ishlaydi. Agar Burp'ni boshqa yo'l bilan (agentsiz) ochsangiz,
> `INVALID_LICENSE` chiqadi.

---

## 5. Muammolarni hal qilish

| Muammo | Sabab | Yechim |
|--------|-------|--------|
| `INVALID_LICENSE` | Agent ulanmagan | `run-burp.sh` orqali oching (oddiy `java -jar` emas) |
| `package jdk.internal.org.objectweb.asm does not exist` | JDK 27 ishlatilyapti | JDK 21 ishlating (`run-burp.sh` buni avtomatik qiladi) |
| Burp o'zini qayta ishga tushiradi | Normal xatti-harakat — agent saqlanadi | Hech narsa qilmang |
| Keygen chiqishida license ko'rinmaydi | Argument tartibi xato | `java -jar loader.jar` dan KEYIN `--name` yozing |

---

## 6. Qanday ishlaydi (qisqacha)

- **loader.jar** ikki vazifa bajaradi:
  - `java -jar loader.jar` → **keygen** (license/activation yaratadi, offline)
  - `-javaagent:loader.jar` → **agent** (Burp'ni runtime'da patch qiladi)
- Asosiy mexanizm `bigint_patch`: Burp RSA imzosini tekshirganda, `BigInteger.oddModPow`
  ichida PortSwigger'ning ommaviy modulini keygen moduliga **almashtiradi**. Shuning uchun
  keygen imzolagan litsenziya "haqiqiy" bo'lib ko'rinadi.
- **Burp JAR o'zgartirilmagan** — backdoor yo'q. Crack faqat agent ishlaganda faol.

---

## 7. Fayllar

```
m1kro-loader/
├── loader.jar               # tayyor jar (keygen + agent)
├── build.sh                 # manbadan qurish (JDK 21)
├── run-burp.sh              # Burp'ni agent bilan ochish
├── MANIFEST.MF              # Main-Class + Premain-Class
├── src/com/m1kro/burploader/
│   ├── Loader.java          # agent (bytecode patcher)
│   ├── Filter.java          # patch payloadlari
│   ├── Keygen.java          # offline keygen
│   └── Main.java            # keygen CLI
└── burpsuite_pro_v2026.9.jar  # rasmiy Burp (SHA256 tasdiqlangan, toza)
```
