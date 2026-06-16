Backend-managed image storage for local demos and uploads.

Public URLs use `/storage/...` and are served by the Spring Boot resource
handler. Runtime uploads and seeded demo assets share this directory tree:

- `avatar/`
- `restaurant/`
- `payment/`
- `product/food/`
- `product/drink/`
- `product/vegetable/`
- `product/digital/`

Commit images that should travel with classroom demos across devices.
