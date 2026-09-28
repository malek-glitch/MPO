# PC E-com

E-commerce app for small and medium Tunisian stores selling new and used PCs.
One installation per store: same API and admin for everyone, custom storefront per store.

```
pc-ecom/
├── api/                  Ktor API (Kotlin)          ← step 1 (this commit)
├── admin/                SvelteKit admin            ← next
├── packages/shared/      API client, types, components for storefronts
└── storefronts/          one custom SvelteKit storefront per store
```

## Run the API locally

Requirements: JDK 21, Docker.

```bash
docker compose -f docker-compose.dev.yml up -d
cd api
gradle wrapper            # once, or open the folder in IntelliJ
ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=change-me-please ./gradlew run
```

- API: http://localhost:8080/api/health
- MinIO console: http://localhost:9001 (minioadmin / minioadmin)
- Flyway creates the tables and default categories on first start.
- The first admin account is created from `ADMIN_EMAIL` / `ADMIN_PASSWORD` if none exists.

All settings are in `api/src/main/resources/application.conf` and can be overridden by the
environment variables listed in `.env.example`.

## API

### Public (storefront)
| Method | Path | Notes |
|---|---|---|
| GET | `/api/health` | |
| GET | `/api/store` | Store name, logo, WhatsApp number, contact info, message template |
| GET | `/api/categories` | Visible categories as a tree |
| GET | `/api/brands` | Brands of available products |
| GET | `/api/products` | Filters below, paginated |
| GET | `/api/products/{slug}` | Available or sold products (hidden = 404) |
| POST | `/api/contact-clicks` | `{ "productId": 12 }` or `{}` for the general button. Rate-limited |

`/api/products` query parameters: `category` (slug, includes subcategories), `used` (true/false),
`condition` (like_new, very_good, good, fair), `brand`, `ram` (GB), `storageMin` (GB),
`priceMin`, `priceMax` (TND), `screenMin`, `screenMax` (inches), `q` (search),
`sort` (newest | price_asc | price_desc), `includeSold`, `page`, `pageSize` (max 100).
List parameters accept `brand=HP,Dell` or `brand=HP&brand=Dell`.

### Admin (`Authorization: Bearer <token>`)
| Method | Path | Notes |
|---|---|---|
| POST | `/api/admin/login` | `{ email, password }` → `{ token }`. Rate-limited |
| GET | `/api/admin/me` | |
| PUT | `/api/admin/password` | `{ currentPassword, newPassword }` |
| GET | `/api/admin/products` | Same filters + `status` (available/sold/hidden) |
| POST | `/api/admin/products` | Create |
| GET / PUT / DELETE | `/api/admin/products/{id}` | |
| PATCH | `/api/admin/products/{id}/status` | `{ status }` — quick "mark as sold" |
| POST | `/api/admin/products/{id}/photos` | Multipart, one or more image files (max 12 per product) |
| PUT | `/api/admin/products/{id}/photos/order` | `{ photoIds: [...] }`, first = main photo |
| DELETE | `/api/admin/products/{id}/photos/{photoId}` | |
| GET / POST | `/api/admin/categories` | |
| PUT / DELETE | `/api/admin/categories/{id}` | Delete fails (409) if still used |
| GET / PUT | `/api/admin/settings` | WhatsApp number is normalised to 216XXXXXXXX |
| POST | `/api/admin/settings/logo` | Multipart, one image |
| GET | `/api/admin/stats` | Available products, sold this month, clicks (30 days), top 5 |

### Example product
```json
{
  "categoryId": 5,
  "isUsed": true,
  "brand": "Dell",
  "model": "Latitude 7420",
  "processor": "Intel Core i7-1185G7",
  "ramGb": 16,
  "storageGb": 512,
  "storageType": "SSD NVMe",
  "screenInches": 14,
  "os": "Windows 11 Pro",
  "keyboard": "AZERTY",
  "priceTnd": 1650,
  "warrantyMonths": 3,
  "used": { "condition": "very_good", "batteryHealth": 87, "defects": "Petite rayure sur le capot", "accessories": "Chargeur d'origine" }
}
```

## Notes
- Prices are whole dinars (`priceTnd`).
- Used products always have quantity 1.
- Photos are converted to WebP in 3 sizes (320, 800, 1600 px) and served from the public bucket.
- **MinIO:** the upstream community project is archived and no longer publishes images.
  The code only uses the S3 API, so any S3-compatible server works (a maintained MinIO fork,
  Garage, SeaweedFS…). Only `MINIO_*` settings change.
