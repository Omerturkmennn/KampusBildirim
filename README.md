Proje Özeti
Kampüs Bildirim Sistemi, üniversite kampüsü içerisindeki teknik arıza, şikayet, istek ve acil durumların hızlı bir şekilde yetkililere iletilmesini sağlayan mobil tabanlı bir iletişim platformudur.
Geleneksel yöntemlerin aksine, bu proje ile kullanıcılar (öğrenciler ve personel); karşılaştıkları sorunları fotoğraf ve GPS konumu ile destekleyerek anlık olarak raporlayabilmektedir. Uygulama, Google Maps entegrasyonu sayesinde olayların harita üzerinde görselleştirilmesini sağlar.
Sistem iki temel rol üzerine kuruludur:
1.	Kullanıcılar: Bildirim oluşturabilir, durumunu takip edebilir, önemli gördüğü diğer bildirimleri favorilerine ekleyerek güncellemelerden haberdar olabilir.
2.	Yöneticiler (Admin): Gelen bildirimleri filtreleyebilir, harita üzerinde inceleyebilir, durumlarını (Açık/Çözüldü) güncelleyebilir ve tüm kampüse anlık Acil Durum Duyurusu yayınlayabilir.
Proje, kampüs güvenliğini artırmayı, sorunların çözüm süresini kısaltmayı ve yönetimi dijitalleştirerek şeffaf bir takip mekanizması sunmayı hedeflemektedir.
Ekran Listesi ve Ekran Görüntüleri
2.1. Giriş ve Kayıt Ekranları
Giriş Ekranı: Kullanıcıların e-posta/şifre ile giriş yaptığı ve Şifremi Unuttum fonksiyonunun bulunduğu ekran.
<img width="962" height="557" alt="image" src="https://github.com/user-attachments/assets/d81fb55c-742e-4442-a9c0-4a1d1bd23521" />
Kayıt Ekranı: Ad-soyad,e-posta/şifre ve birim bilgileriyle sisteme üye olunan ekran.
<img width="1028" height="556" alt="image" src="https://github.com/user-attachments/assets/3218e2cf-da1d-4724-96ac-ebe2147db1a2" />
2.2. Ana Sayfa ve Listeleme
•	Ana Sayfa (Home) ve Acil Durum Kartı(en üstteki kırmızı kutu):Tüm raporların listelendiği, arama çubuğu ve kategori filtrelerinin (Arıza, İstek vb.) bulunduğu ana akış
<img width="948" height="528" alt="image" src="https://github.com/user-attachments/assets/0ced5b83-0ed9-4d44-a7d5-2a0bb2a351cc" />
2.3. Harita Modülü
•	Harita Ekranı: Kampüsteki bildirimlerin türlerine göre renkli pinlerle (Örn: Arıza=Kırmızı, İstek=Mavi) gösterildiği Google Maps arayüzü.
<img width="990" height="581" alt="image" src="https://github.com/user-attachments/assets/81654759-6f50-475f-b3fc-fe9e3236f060" />
<img width="992" height="555" alt="image" src="https://github.com/user-attachments/assets/00a5d87b-85c7-4d67-8978-c9fb29311e04" />
2.4. Raporlama Süreci
•	Rapor Ekleme Ekranı: Fotoğraf çekme/yükleme, başlık, açıklama girme ve otomatik konum alma işlemlerinin yapıldığı form.
<img width="872" height="558" alt="image" src="https://github.com/user-attachments/assets/f784c6ed-b5fe-4680-8008-34a0c502e0aa" />
Rapor Detay Ekranı: Bildirimin büyük fotoğrafının, harita konumunun ve durumunun görüntülendiği sayfa. Ayrıca "Takip Et" (Yıldız) butonu burada bulunur.
<img width="945" height="626" alt="image" src="https://github.com/user-attachments/assets/abb4f866-58b5-47b3-85e9-8a68bb1737a0" />
2.5. Kullanıcı Profili
•	Profil Ekranı: Kullanıcı bilgilerinin, "Raporlarım" ve "Takip Ettiklerim" sekmelerinin yer aldığı kişisel alan.
<img width="945" height="672" alt="image" src="https://github.com/user-attachments/assets/4a982106-035d-48b4-9c1d-691f4d02aac2" />
2.6. Yönetici (Admin) Paneli
•	Admin Listesi: Yöneticinin tüm raporları yönettiği, silme ve onaylama butonlarının bulunduğu özel panel.
•	Duyuru Penceresi: Tüm okula acil bildirim göndermek için açılan diyalog penceresi.
<img width="945" height="672" alt="image" src="https://github.com/user-attachments/assets/562b308b-e5a4-4fa5-87fc-4717d44069ce" />

Kullanılan Teknolojiler ve Araçlar
1. Yazılım Dili ve Ortam
•	Kotlin: Android geliştirme için kullanılan modern programlama dili.
•	Android Studio: Entegre Geliştirme Ortamı (IDE).
•	XML: Kullanıcı arayüzü (UI) tasarımları için kullanıldı.
2. Backend ve Veritabanı (Firebase)
•	Firebase Authentication: Kullanıcıların güvenli bir şekilde kayıt olması, giriş yapması ve şifre sıfırlama işlemleri için kullanıldı.
•	Firebase Firestore: NoSQL tabanlı bulut veritabanı. Raporlar, kullanıcı bilgileri ve duyurular burada JSON formatında tutuldu.
•	Firebase Storage: Kullanıcıların yüklediği olay yeri fotoğraflarının bulutta depolanması için kullanıldı.
3. Harita ve Konum Servisleri
•	Google Maps : Haritaların görüntülenmesi, yakınlaştırma işlemleri ve pin (marker) yönetimi için entegre edildi.
•	Google Play Services Location (FusedLocationProvider): Cihazın anlık GPS konumunu yüksek doğrulukla almak için kullanıldı.
4. Kütüphaneler ve Mimari
•	Glide: İnternet üzerindeki (Firebase Storage'daki) resimlerin hızlı yüklenmesi ve önbelleğe alınması (caching) için kullanıldı.
•	ViewBinding: Kod ile XML tasarımları arasındaki bağlantıyı güvenli ve hızlı kurmak, findViewById karmaşasını önlemek için kullanıldı.
•	Navigation Component: Tek Activity üzerinde (Single Activity Architecture) fragmentlar arası geçişleri ve veri transferini yönetmek için kullanıldı.
•	Git & GitHub: Versiyon kontrolü ve kaynak kod yönetimi için kullanıldı.






