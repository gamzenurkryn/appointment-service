# Callcenter AgenticAI Backend

Callcenter AgenticAI; WhatsApp, randevu yönetimi, otomatik teyit çağrısı, Google Calendar, gerçek zamanlı bildirim ve CRM teklif akışlarını bir araya getiren Java 21 / Spring Boot tabanlı bir backend projesidir.

## Servisler

| Servis | Port | Sorumluluk |
|---|---:|---|
| API Gateway | 8080 | Tek giriş noktası, yönlendirme, correlation ID ve rate limit |
| Appointment Service | 8081 | Randevu, mağaza, uygunluk, WhatsApp ve Google Calendar |
| Call Service | 8082 | LiveKit odaları ve çağrı yaşam döngüsü |
| Notification Service | 8083 | Merkezi log, WebSocket olayları ve SMS bildirimleri |
| CRM Service | 8084 | Randevu olaylarından otomatik upsell teklifleri |
| PostgreSQL | 5432 | Kalıcı veri |
| Redis | 6379 | Gateway rate limiting |
| RabbitMQ | 5672 / 15672 | Servisler arası olay iletişimi / yönetim ekranı |

## Temel Akış

1. Müşteri için appointment-service üzerinden randevu oluşturulur.
2. `appointment.created` olayı RabbitMQ'ya yayınlanır.
3. notification-service olayı merkezi loga ve WebSocket'e aktarır.
4. crm-service randevu hizmetine uygun ek hizmet teklifi üretir.
5. call-service LiveKit teyit çağrısını yönetir ve çağrı olaylarını yayınlar.
6. Çağrı tamamlandığında `call.completed` olayı appointment-service tarafından işlenir.
7. Çağrı sonucu randevu durumuna eşlenir: `CONFIRMED → CONFIRMED`, `DECLINED → CANCELLED`, `RESCHEDULE_REQUESTED → RESCHEDULED`.
8. Dashboard, `/api/v1/stream` WebSocket bağlantısından anlık güncellemeleri alır.

## Gereksinimler

- Java 21
- Docker Desktop
- İnternet bağlantısı (ilk bağımlılık ve Docker imajı indirmeleri için)

## Docker ile Çalıştırma

Önce çalıştırılabilir Spring Boot JAR dosyalarını oluşturun:

```powershell
.\gradlew.bat :bootJar :api-gateway:bootJar :call-service:bootJar :notification-service:bootJar :crm-service:bootJar
```

Ardından imajları oluşturup sistemi başlatın:

```powershell
docker compose up --build -d
```

Container durumlarını görüntüleyin:

```powershell
docker compose ps
```

Sistemi durdurun:

```powershell
docker compose down
```

> `docker compose down -v` veritabanı verilerini silebileceği için geliştirme verisi korunacaksa kullanılmamalıdır.

## Swagger Adresleri

- Appointment: http://localhost:8081/swagger-ui/index.html
- Call: http://localhost:8082/swagger-ui/index.html
- Notification: http://localhost:8083/swagger-ui/index.html
- CRM: http://localhost:8084/swagger-ui/index.html

## Önemli Endpoint'ler

| Metot | Yol | Açıklama |
|---|---|---|
| POST | `/api/v1/appointments` | Randevu oluşturur |
| GET | `/api/v1/appointments` | Randevuları filtreleyerek listeler |
| GET | `/api/v1/availability` | Boş randevu saatlerini getirir |
| GET | `/api/v1/stores` | Şubeleri listeler |
| POST | `/api/v1/calls` | LiveKit teyit çağrısını başlatır |
| PATCH | `/api/v1/calls/{id}` | Çağrı durumunu ve sonucunu günceller |
| GET | `/api/v1/logs` | Merkezi log kayıtlarını listeler |
| POST | `/api/v1/notifications/sms` | SMS gönderim isteğini kabul eder |
| GET | `/api/v1/offers` | CRM upsell tekliflerini listeler |
| GET | `/api/v1/system/status` | Temel bileşenlerin sağlık durumunu verir |

WebSocket test sayfası: http://localhost:8083/websocket-test.html

## Testler

Tüm testleri çalıştırmak için:

```powershell
.\gradlew.bat test
```

Tek bir servisin testini çalıştırmak için:

```powershell
.\gradlew.bat :call-service:test
```

## Ortam Değişkenleri ve Güvenlik

API anahtarları ve şifreler kaynak koda yazılmaz. Aşağıdaki hassas değerler IntelliJ Run Configuration, işletim sistemi ortam değişkenleri veya güvenli deployment secret'ları üzerinden verilmelidir:

- `GOOGLE_CALENDAR_CREDENTIALS`
- `WHATSAPP_ACCESS_TOKEN`
- `WHATSAPP_APP_SECRET`
- `WHATSAPP_PHONE_NUMBER_ID`
- `WHATSAPP_VERIFY_TOKEN`
- `LIVEKIT_API_KEY`
- `LIVEKIT_API_SECRET`
- `JWT_SECRET` (JWT etkinse en az 32 karakter)

Gateway JWT doğrulamasını etkinleştirmek için `JWT_ENABLED=true` ve güvenli bir `JWT_SECRET` değeri verilmelidir. Sağlık endpoint'i ile dış servis webhook'ları açık kalır; diğer `/api/v1/**` yolları geçerli Bearer token ister.

`secrets/` klasörü Docker build context'ine ve Git'e dahil edilmemelidir. Telefon numarası gibi kişisel veriler uygulama loglarına açık biçimde yazılmamalıdır.

## Geliştirme Notları

- Lombok kullanılmamaktadır; constructor, getter ve setter'lar açık şekilde yazılır.
- Controller katmanı yalnızca HTTP isteğini alır ve service katmanını çağırır.
- Entity nesneleri doğrudan API yanıtı olarak kullanılmaz; DTO ile dışarı aktarılır.
- Servisler arası asenkron iletişim RabbitMQ üzerinden yapılır.
- Her akış `X-Correlation-Id` ile uçtan uca takip edilebilir.
