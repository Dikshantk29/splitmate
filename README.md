# SplitMate – Roommate Bill Fairness Tracker

SplitMate is a full-stack app for keeping shared household expenses visible and settling balances fairly. It uses Java 21, Spring Boot, PostgreSQL, Flyway, React, TypeScript, Vite, Tailwind CSS, and Docker Compose.

## Features

- Email/password registration and login with BCrypt password hashes and expiring JWTs.
- Households, owner managed roommate membership, and household scoped access checks.
- Shared expense entry with equal or custom cent-accurate allocation.
- Paginated expense API, category filtering, individual net balances, and minimum transfer suggestions.
- Payment records with pending/paid status and balance updates after payment.
- Monthly and all-time spend summaries, responsive pages, validation and API error messages.
- OpenAPI documentation at `/swagger-ui.html`.

## Run with Docker Compose

1. Copy `.env.example` to `.env` and replace `JWT_SECRET` with a unique random value of at least 32 characters.
2. Run `docker compose up --build` from this directory.
3. Open [http://localhost:3000](http://localhost:3000). The API listens at [http://localhost:8080](http://localhost:8080), and Swagger is at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

The database runs Flyway migrations on startup. Docker Compose persists PostgreSQL data in the `splitmate-db` volume.

## Deploy to Render and Vercel

See [DEPLOYMENT.md](DEPLOYMENT.md) for the GitHub, Render Blueprint, Vercel frontend, environment variable, and CORS steps.

## Run locally

Requirements: Java 21, Maven 3.9+, Node.js 20+, npm, and PostgreSQL 16. Create a PostgreSQL database and user, then set the following environment variables for the backend:

```text
DB_URL=jdbc:postgresql://localhost:5432/splitmate
DB_USERNAME=splitmate
DB_PASSWORD=your-local-password
JWT_SECRET=your-unique-random-secret-at-least-32-characters-long
```

In one terminal, from `backend/`, run `mvn spring-boot:run`. In another terminal, from `frontend/`, run `npm install`, then `npm run dev`. The development UI uses `http://localhost:8080/api` by default. To point it elsewhere, set `VITE_API_URL` before starting Vite.

## Tests and production build

- Backend tests: `cd backend && mvn test`
- Frontend tests: `cd frontend && npm install && npm test`
- Frontend production build: `cd frontend && npm run build`
- Backend package: `cd backend && mvn package`

## API overview

All endpoints except registration, login, and the OpenAPI docs require `Authorization: Bearer <token>`.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Register and receive a token |
| POST | `/api/auth/login` | Login and receive a token |
| GET/POST | `/api/households` | List or create households |
| GET | `/api/households/{id}` | Household members |
| POST | `/api/households/{id}/members` | Owner adds an existing SplitMate user by email |
| GET/POST | `/api/households/{id}/expenses` | List expenses (`page`, `size`) or record one |
| GET | `/api/households/{id}/summary` | Spend, balances, and suggested transfers |
| GET/POST | `/api/households/{id}/settlements` | List or record a payment |
| PATCH | `/api/households/{id}/settlements/{id}/paid` | Payer marks a pending payment paid |

## Expense request example

```json
{
  "householdId": 1,
  "description": "Internet bill",
  "category": "WIFI",
  "amount": 60.00,
  "expenseDate": "2026-10-27",
  "splitType": "EQUAL",
  "shares": []
}
```

For `CUSTOM`, provide `shares` as `{ "userId": 1, "amount": 30.00 }` objects. Their amounts must sum exactly to the expense amount. Monetary calculations use `BigDecimal` and allocate spare cents deterministically.

## Security and access

Passwords are hashed with BCrypt. JWT signing keys are supplied through environment configuration; the backend refuses to start with a missing or short key. Users can only access a household after joining it. Only owners can add members, and only the payer of a pending settlement can mark it paid. Invitations currently add registered users by email; invite links/email delivery are intentionally not implemented.
