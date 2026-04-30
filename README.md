# Library App with Supabase

## Proje Özeti
Bu proje, Supabase altyapısını kullanan bir kütüphane yönetim uygulamasıdır. Kullanıcıların kayıt olabileceği, giriş yapabileceği ve kütüphanedeki kitapları yönetebileceği modern bir Android uygulamasıdır.

## Kullanılan Teknolojiler
*   **Kotlin** & **Jetpack Compose**
*   **Material 3**
*   **Supabase** (Auth & Postgrest)
*   **MVVM Mimari Yapısı**
*   **Kotlin Coroutines & Flow**
*   **Ktor**
*   **Serialization JSON**

## Özellikler
*   **Supabase Auth:** Email/Şifre ile güvenli kayıt ve giriş işlemleri.
*   **Profil Yönetimi:** `profiles` tablosuna kullanıcı bilgilerinin (ad-soyad, rol vb.) otomatik kaydı.
*   **Kitap Listeleme:** Kütüphanedeki tüm kitapların anlık olarak listelenmesi.
*   **Kitap Arama:** Başlık, yazar veya kategoriye göre gelişmiş filtreleme.
*   **Kitap Düzenleme:** Mevcut kitap bilgilerinin (stok, isim vb.) güncellenmesi.
*   **Kitap Silme:** Kitapların veritabanından güvenli bir şekilde kaldırılması.
*   **Modern UI:** Material 3 standartlarında, kullanıcı dostu arayüz.

## Yapılan Ödevler

### Ödev 1: Kayıt Ol Ekranı Başarı Yapısı
*   Kayıt işlemi başarılı olduğunda kullanıcıya net bir geri bildirim (Toast/State) verilir.
*   Başarılı kayıt sonrası kullanıcı otomatik olarak Giriş (Login) ekranına yönlendirilir.

### Ödev 2: Repository ve CRUD Mantığı
*   `BookRepository` içine `updateBook`, `deleteBook` ve `searchBooks` fonksiyonları eklendi.
*   `BookViewModel` üzerinden liste yenileme, silme ve güncelleme işlemleri yönetilmektedir.

### Ödev 3: Kitap Kartı ve UI Bileşenleri
*   `BookCard` adında bağımsız bir Composable bileşen tasarlandı.
*   Kitaplar düz liste yerine gölgelendirmeli ve düzenli kart yapısı ile gösterilir.
*   Arama alanı ve boş durum (empty state) mesajları eklendi.

## Türkçe Karakter Destekli Arama
Uygulama, Türkçe karakterlere (ç, ğ, ı, ö, ş, ü) duyarlı bir normalizasyon algoritmasına sahiptir.
*   Kullanıcı "yasar" yazdığında "Yaşar Kemal" sonucuna ulaşabilir.
*   Kullanıcı "ince" yazdığında "İnce Memed" sonucuna ulaşabilir.
*   Arama işlemi büyük/küçük harf duyarsızdır.

## Ekran Görüntüleri
![Register Screen](screenshots/register.png)
![Login Screen](screenshots/login.png)
![Home Screen](screenshots/home.png)
![Search Screen](screenshots/search.png)
![Edit Book Screen](screenshots/edit.png)

## Proje Yapısı
```
com.turkcell.libraryapp
├── data
│   ├── model           # Veri modelleri (Book, Profile)
│   ├── repository      # Veri kaynakları (AuthRepository, BookRepository)
│   └── supabase        # Supabase istemci yapılandırması
├── ui
│   ├── navigation      # Navigasyon (NavGraph, Screen)
│   ├── screen          # Ana ekranlar (Login, Register, Home)
│   │   └── components  # UI Bileşenleri (BookCard)
│   └── viewmodel       # ViewModel sınıfları (AuthViewModel, BookViewModel)
```

## Supabase Yapısı
**local.properties** dosyası güvenlik nedeniyle GitHub’a eklenmemiştir.
Projeyi çalıştırmak için proje ana dizinindeki `local.properties` dosyasına şu değerler eklenmelidir:

```properties
SUPABASE_URL=YOUR_SUPABASE_URL
SUPABASE_ANON_KEY=YOUR_SUPABASE_ANON_KEY
```

## Geliştirici
Emirhan Erdoğan
