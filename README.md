# 🖼️ Media Service (Instagram Clone Backend)

The **Media Service** is the asset upload and storage pipe for the [Instagram Clone Microservices Backend](https://github.com/Instagram-Api-Clone). It handles secure multipart file processing, direct integration with Cloudinary cloud storage, and persists media asset metadata.

---

## 🛠️ Features & Responsibilities
* **Cloud-Native Storage**: Direct integration with **Cloudinary** for scalable, CDN-optimized storage of images and videos.
* **Multipart Processing**: Secure backend endpoint accepting multiform media uploads (profiles, posts, avatars).
* **Metadata Persistence**: Tracks asset upload IDs, formats, sizes, and secure URLs in PostgreSQL to easily relate media to other domain entities (posts, users).
* **Security & Authorization**: Coordinates with the API Gateway to enforce that only authenticated users can upload media.

---

## 🧱 Tech Stack
* **Framework**: Spring Boot 3
* **Language**: Java 21
* **Database**: PostgreSQL (relational database to track upload meta-info)
* **Cloud Storage CDN**: Cloudinary API
* **Discovery & Config**: Spring Cloud Client (Eureka and Config Server)
* **Monitoring**: New Relic APM agent
* **Containerization**: Docker (via Jib container builder)

---

## 📡 Key API Endpoints

All endpoints are protected and must be routed via the API Gateway. Downstream port: `8090`.

| Method | Endpoint | Form Data | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/media/upload` | `file` (Multipart file) | Upload an image/video to Cloudinary and retrieve metadata |

---

## ⚙️ Running Locally

### Prerequisites
1. Spring Cloud **Discovery Server** must be running on port `8761`.
2. Spring Cloud **Configuration Server** must be running on port `8888`.
3. A running **PostgreSQL** instance.
4. Active **Cloudinary** API credentials.

### Launching
1. Set the following environment variables:
   * `DB_USER` & `DB_PASSWORD`: PostgreSQL server credentials.
   * `DB_NAME`: The name of the database (defaults to `instagram_media_service`).
   * `CLOUDINARY_CLOUD_NAME`: Your Cloudinary account name.
   * `CLOUDINARY_API_KEY`: Your Cloudinary API Key.
   * `CLOUDINARY_API_SECRET`: Your Cloudinary API Secret Key.
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```
   * The service starts up locally on port **`8090`**.
