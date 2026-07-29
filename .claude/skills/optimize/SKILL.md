---
name: optimize
description: Improve performance across backend and frontend. Database queries, API response times, bundle size, rendering.
user-invokable: true
args:
  - name: target
    description: The area to optimize
    required: false
---

# Performance Optimization

## Backend
- Query optimization (avoid N+1, use eager fetching where appropriate)
- Pagination for all list endpoints
- Database indexes on frequently queried columns
- Response compression (gzip)
- Connection pooling tuning
- Async processing for non-blocking operations (email, notifications)
- Caching for static/slow-changing data

## Frontend
- Code splitting with lazy loading
- Memoization where measured improvement exists
- Virtualized lists for large datasets
- Image optimization (SVG for icons, modern formats for images)
- CSS purge for minimal bundle
- API response caching with stale-while-revalidate pattern

## Database
- Proper indexes on foreign keys and query predicates
- Query analysis with EXPLAIN for slow queries
- Batch operations for bulk processing
- Connection pool sizing for expected concurrency
