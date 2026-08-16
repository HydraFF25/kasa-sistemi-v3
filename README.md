# CrateSystem

Minecraft **1.21.4** (Paper/Spigot) için, CrazyCrates / ExcellentCrates tarzında,
sıfırdan yazılmış, tamamen config edilebilir bir **kasa (crate) eklentisi**.

## Özellikler

- **3 animasyon tipi**
  - `CHEST` — klasik sandık: ortada hızlı ödül yanıp sönmesi, sonunda kazanılan ödülde durur.
  - `CSGO` — CS:GO tarzı yatay kayan şerit, yavaşlayarak kazanılan ödüle kilitlenir.
  - `ROULETTE` — Rulet temalı, farklı ses/görsel ile aynı mantık.
- **Fiziksel kasalar**: `/crate setlocation <kasa>` ile baktığın bloğu kasa yap, oyuncular
  doğru anahtarla sağ tıklayarak açsın.
- **Sanal açma**: `/crate open <kasa>` komutuyla, envanterindeki anahtar tüketilerek de açılabilir.
- **Anahtarlar** gerçek itemlerdir (PersistentDataContainer ile hangi kasaya ait oldukları işaretlenir),
  `/crate give <oyuncu> <kasa> <miktar>` ile dağıtılır.
- **Ağırlıklı şans sistemi**: her ödülün `chance` değeri toplam üzerinden orantılı seçilir.
- **Ödüller**: item verme, komut çalıştırma (ekonomi eklentisi vs. ile entegre edilebilir),
  `display-only: true` ile sadece komutla ödül (item vermeden).
- **Nadir ödül duyurusu**: `broadcast-rare` + `rare-chance-threshold` ile efsanevi ödülleri sunucuya duyur.
- Tüm mesajlar `messages.yml` üzerinden özelleştirilebilir, hex renk (`&#RRGGBB`) desteklenir.
- Her kasa kendi `.yml` dosyasında tanımlanır — `plugins/CrateSystem/crates/` klasörüne
  istediğin kadar dosya ekleyebilirsin.
- **Tüm anahtarlar aynı görsel**: Her kasanın anahtarı Tuzak Kancası (Tripwire Hook) şeklinde,
  sadece isim/lore farklı — CrazyCrates'teki gibi standart bir "anahtar" hissi verir.
  Birlikte gelen texture pack ile her kasanın anahtarı ayrıca kendi renginde görünür.
- **Anahtarlar yere/bloğa konulamaz**: Anahtar itemleri bloğa yerleştirilemez, sadece kasa
  açmak için kullanılabilir.
- **Otomatik hologram**: Fiziksel kasa oluşturduğunda (`/crate setlocation`), kasanın üstünde
  otomatik olarak kasa ismini gösteren bir hologram belirir. Harici bir hologram eklentisi
  gerekmez, kendi başına çalışır.
- **Sade animasyon başlığı**: Kasa açılırken üstte "CS-KASA" ya da "RULET" gibi teknik
  yazılar görünmez, sadece kasanın kendi ismi yazar.
- **Spawner ödülleri artık komuta bağımlı değil**: Spawner kasasındaki ödüller doğrudan
  içine mob gömülü gerçek bir item olarak verilir (bkz. Spawner Kasası bölümü).
- **Animasyonu erken kapatsan bile ödülün kaybolmaz**: Kasa açılırken ESC/E ile envanteri
  kapatırsan, kazandığın ödül anında verilir — animasyonun sonunu beklemek zorunda değilsin.
- **Dönen 3D kasa görseli**: Fiziksel kasaların üstünde artık sadece isim değil, yavaşça
  dönen bir 3D ürün görseli (o kasanın `preview-item`'ı) de beliriyor, ayrıca kasa çevresinde
  hafif bir parçacık efekti akıyor — kasa çok daha "canlı" görünüyor.
- **`/crate preview <kasa>`**: Kasayı hiç açmadan, içindeki tüm ödülleri ve gerçek kazanma
  yüzdelerini gösteren bir bilgi ekranı.

## Texture Pack (Özel Kasa Görselleri)

`CrateSystemTexturePack.zip` içinde, her kasanın anahtarına özel bir görsel (doku) tanımlayan
bir resource pack bulunuyor. Minecraft 1.21.4'ün yeni **item model** sistemini kullanıyor
(eski `CustomModelData` yerine), yani her anahtar kendi rengiyle/şekliyle görünür:

- Her kasanın anahtarı aynı "eski usul anahtar" siluetine sahip, sadece rengi farklı
  (yeşil = Vote, mor = Şans/Profesyonel/Ametist, gri = Spawner, kırmızı-altın = Bworld, altın = Para).
- **Bworld** ve **Ametist** anahtarları hem büyülü parıltı (glint) hem de etraflarında
  parıldayan parçacık efektiyle diğerlerinden ayrılıyor — "efsanevi" hissi güçlendiriyor.
- Resource pack yüklü değilse hiçbir sorun olmaz, anahtarlar sade bir Tuzak Kancası olarak görünür.

### Nasıl kurulur?

1. `CrateSystemTexturePack.zip` dosyasını olduğu gibi bir yere yükle — GitHub Releases,
   kendi web sunucun, ya da [MC-Packs](https://mc-packs.net) gibi ücretsiz bir hosting servisi
   (zip dosyasını sürükle-bırak yükleyip direkt indirme linki alabilirsin).
2. `server.properties` dosyasına şunları ekle:
   ```properties
   resource-pack=https://.../CrateSystemTexturePack.zip
   resource-pack-sha1=<zip dosyasinin sha1 hash'i>
   require-resource-pack=false
   ```
   SHA1 hash'i almak için (Linux/Mac): `sha1sum CrateSystemTexturePack.zip`
   Windows PowerShell: `Get-FileHash CrateSystemTexturePack.zip -Algorithm SHA1`
3. Sunucuyu yeniden başlat. Oyuncular sunucuya girince Minecraft otomatik olarak
   "bu sunucunun bir resource pack'i var, yüklemek ister misin?" diye soracak.
4. `require-resource-pack=true` yaparsan oyuncular paketi kabul etmeden sunucuya giremez
   (daha tutarlı görünüm için önerilir, ama oyuncuları zorlamak istemezsen `false` bırak).

> Not: Resource pack'i geliştirmek istersen (daha detaylı pixel art, 3D model vb.),
> `CrateSystemTexturePack/assets/cratesystem/textures/item/` altındaki PNG dosyalarını
> herhangi bir pixel-art programıyla (Aseprite, Blockbench, hatta MS Paint) düzenleyebilirsin —
> dosya isimlerini ve boyutlarını (128x128) değiştirmeden üzerine kaydetmen yeterli.

## Hologramlar

`/crate setlocation <kasa>` ile bir bloğu fiziksel kasa yaptığında, bloğun üstünde iki gorsel
unsur otomatik belirir:

- Kasanın ismini gösteren bir yazı (hologram)
- Kasanın `preview-item`'ını gösteren, yavaşça dönen bir 3D obje + hafif parçacık efekti

Ekstra bir ayar yapmana gerek yok:

- `/crate removelocation` dediğinde ikisi de otomatik kaybolur.
- `/crate reload` dediğinde tümü güncellenir (kasa ismini/görselini değiştirdiysen yansır).
- Sunucu yeniden başladığında otomatik olarak tekrar oluşturulur, kaybolmaz.
- Bir önceki oturumdan (örneğin sunucu çökmesi sonrası) kalan gorseller de otomatik temizlenir,
  yani üst üste binme/çoğalma yaşanmaz.
- Performans için tüm kasaların dönme animasyonu **tek bir** zamanlanmış görev üzerinden
  yönetilir — kasa sayısı artsa da sunucuya ek yük bindirmez.

## Oy Siteleri Entegrasyonu (NuVotifier Kurulumu)

Bilindik oy sitelerinden (minecraft-server-list, topg, planetminecraft vb.) oy verildiğinde
otomatik kasa anahtarı vermek için **NuVotifier** kullanıyoruz — bu, Minecraft dünyasının
standart oy bildirim sistemi. CrateSystem, NuVotifier'ı sunucunda algılayınca kendiliğinden
devreye giriyor, ekstra bir eklenti yazmana gerek yok.

### 1. Adım — NuVotifier'ı kur

1. [NuVotifier'ı SpigotMC'den indir](https://www.spigotmc.org/resources/nuvotifier.13449/)
   (evrensel jar, tüm Paper/Spigot sürümleriyle uyumlu).
2. İndirdiğin `NuVotifier.jar` dosyasını sunucunun `plugins/` klasörüne koy.
3. Sunucuyu bir kere başlat — NuVotifier otomatik olarak `plugins/NuVotifier/config.yml`
   ve bir RSA anahtar çifti (`plugins/NuVotifier/rsa/`) oluşturacak, sonra sunucuyu durdur.

### 2. Adım — Portu aç

NuVotifier varsayılan olarak **8192** portunu dinler. Sunucu barındırdığın yerde
(VPS/dedicated ise firewall, hosting panelin varsa "port ayarları") bu portu **TCP** olarak
dışarıya aç. Paylaşımlı hosting kullanıyorsan panelinde genelde hazır bir "Votifier Port"
alanı olur, onu kullan.

### 3. Adım — Genel anahtarı (public key) al

`plugins/NuVotifier/rsa/public.key` dosyasını aç, içindeki metni kopyala. Bu, oy sitesinin
sana ait sunucuyu doğrulaması için kullanacağı şifreleme anahtarı.

### 4. Adım — Sunucunu oy sitesine ekle

Kullandığın oy sitesinde (örnek: minecraft-server-list.com, planetminecraft.com,
topminecraftservers.org) "sunucu ekle" formunda genelde şu bilgiler istenir:

- **Sunucu IP + Votifier Portu** (8192)
- **Public Key** (3. adımda kopyaladığın metin)

Bu bilgileri gir, site seni doğruladıktan sonra `plugins/NuVotifier/config.yml` içindeki
`websites` bölümüne otomatik bir token eklemen istenebilir — sitenin verdiği talimatları takip et,
her site biraz farklı token/anahtar ister, NuVotifier'ın kendi
[Setup Guide](https://github.com/NuVotifier/NuVotifier/wiki/Setup-Guide) sayfası bu adımı
detaylıca anlatıyor.

### 5. Adım — CrateSystem tarafını ayarla

`plugins/CrateSystem/config.yml` içinde:

```yaml
vote-crate:
  enabled: true
  crate: vote              # crates/ klasorundeki hangi kasa verilecek
  amount-per-vote: 1        # her oyda kac anahtar (2 yaparsan 2x anahtar verilir)
  broadcast: true           # oy geldiginde sunucuya duyuru yapsin mi
```

`crate: vote` zaten bizim `vote.yml` kasamızla eşleşiyor. Farklı bir kasa istiyorsan
`crates/` klasöründeki başka bir dosyanın `id` değerini yazman yeterli.

### 6. Adım — Sunucuyu yeniden başlat ve test et

Sunucuyu yeniden başlat. Konsolda şunu görmelisin:

```
[CrateSystem] NuVotifier bulundu, oy sistemi entegrasyonu aktif edildi.
```

Test etmek için gerçek bir siteden oy vermeyi bekleyebilir ya da NuVotifier'ın kendi
test komutunu kullanabilirsin (konsolda `nuvotifier generatetoken` / `votifier test`
gibi komutlar sürüme göre değişir, `/nuvotifier` yazıp tab'layarak görebilirsin).

Oy geldiğinde:
- Oyuncu **online** ise → anında `vote` kasasından anahtar alır, mesaj görür.
- Oyuncu **offline** ise → anahtar `pending_votes.yml` dosyasında bekler, oyuncu
  bir sonraki girişinde otomatik teslim alır (kaybolmaz).

> NuVotifier kurulu değilse CrateSystem hiçbir hata vermeden normal çalışmaya devam eder,
> sadece oy entegrasyonu pasif kalır — bu yüzden bu adımları atlaman diğer özellikleri bozmaz.

## Kurulum / Derleme (GitHub Actions ile)

1. Bu klasörü bir GitHub reposuna yükle (push et).
2. `.github/workflows/build.yml` otomatik olarak her push'ta Maven ile derler.
3. Actions sekmesinden **Artifacts** kısmındaki `CrateSystem` dosyasını indir,
   içindeki `CrateSystem.jar` dosyasını sunucunun `plugins/` klasörüne koy.

Yerelde derlemek istersen (JDK 21 + Maven gerekli):

```bash
mvn clean package
```

Çıktı: `target/CrateSystem.jar`

## Komutlar

| Komut | Açıklama | Yetki |
|---|---|---|
| `/crate list` | Kasaları listeler | - |
| `/crate open <kasa>` | Envanterdeki anahtarla kasa açar | - |
| `/crate preview <kasa>` | Kasayı açmadan ödülleri ve şansları gösterir | - |
| `/crate give <oyuncu> <kasa> <miktar>` | Anahtar verir | `cratesystem.admin` |
| `/crate setlocation <kasa>` | Baktığın bloğu fiziksel kasa yapar | `cratesystem.admin` |
| `/crate removelocation` | Baktığın bloktaki kasayı kaldırır | `cratesystem.admin` |
| `/crate forceopen <oyuncu> <kasa>` | Anahtar harcamadan zorla açtırır | `cratesystem.admin` |
| `/crate reload` | Tüm configleri yeniden yükler | `cratesystem.admin` |

## Yeni kasa ekleme

`plugins/CrateSystem/crates/` klasörüne yeni bir `.yml` dosyası ekle (örneğin `vip.yml`).
İçeriği için `crates/example.yml` dosyasını örnek al. En önemli alanlar:

```yaml
id: vip
name: "&6&lVIP Kasa"
animation: CSGO          # CHEST | CSGO | ROULETTE
key-item: TRIPWIRE_HOOK
key-name: "&6VIP Anahtari"
broadcast-rare: true
rare-chance-threshold: 5.0

items:
  - material: DIAMOND
    amount: 3
    name: "&b3 Elmas"
    chance: 40.0
    commands: []

  - material: NETHERITE_INGOT
    amount: 1
    name: "&5Netherite"
    chance: 2.0
    glow: true
    commands:
      - "give %player% netherite_ingot 1"
```

`/crate reload` çalıştırdığında yeni kasa otomatik yüklenir.

## Proje yapısı

```
src/main/java/com/cratesystem/
  CratePlugin.java          -> ana sınıf
  crate/                    -> Crate, CrateReward, CrateManager, CrateAnimationType
  key/                      -> KeyManager (anahtar itemleri)
  location/                 -> LocationManager (fiziksel kasa konumları)
  hologram/                 -> HologramManager (kasa üstü isim hologramları)
  vote/                     -> VoteManager (offline oy ödülleri bekletme)
  animation/                -> ChestAnimation, CSGOAnimation, RouletteAnimation
  listener/                 -> sağ tık, envanter/anahtar koruması, oy, giriş
  command/                  -> /crate komutu
  util/                     -> ItemBuilder, ColorUtils
```

## Notlar

- Java 21 ve Paper API 1.21.4 kullanılarak yazılmıştır, sadece `provided` scope'ta
  paper-api'ye bağımlıdır — harici kütüphane / shading gerektirmez.
- Ekonomi entegrasyonu istersen ödüllerin `commands` listesine kendi ekonomi
  eklentinin komutunu (`eco give %player% <miktar>` gibi) ekleyebilirsin.
