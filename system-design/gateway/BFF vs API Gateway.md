## BFF vs API Gateway

Both sit **between the client and your backend services**. Both receive requests and forward
them to internal services. Because of that, people often confuse them.

The difference is simple:

- **API Gateway** = one shared front door for **all** clients. It focuses on *infrastructure* concerns (routing, auth, rate limit).
- **BFF (Backend for Frontend)** = a **separate** backend built for **one specific** client (web, mobile, TV...). It focuses on *shaping data* for that client.

---

## The Problem They Solve

Imagine you have many small backend services (User, Order, Product, Payment...) and many
different clients (a Web app, a Mobile app, a Smart TV app).

**Without anything in the middle**, every client must:
- Know the address of every service.
- Call each service separately and glue the data together.
- Handle auth, retries, versioning for each of them.

That is messy and hard to maintain. Gateway and BFF are two ways to fix this — but they fix
*different parts* of the mess.

---

## API Gateway

A single entry point that all requests go through first. It does **generic** work that every
request needs, regardless of who the client is.

```
                 ┌───────────────┐        ┌──────────────┐
   Web / Mobile  │               │───────▶│ User Service │
   / TV / 3rd    │  API Gateway  │───────▶│ Order Service│
   party  ──────▶│               │───────▶│ Product Svc  │
                 └───────────────┘        └──────────────┘
                (auth, routing, rate limit)
```

**Typical jobs:**

| Job | What it means |
|-----|---------------|
| Routing | Send `/orders` to Order Service, `/users` to User Service |
| Authentication | Check the token once, before hitting any service |
| Rate limiting | Block a client sending too many requests |
| SSL / TLS | Handle HTTPS in one place |
| Logging / metrics | Track all traffic centrally |

**Key idea:** the Gateway does **not care** which client is calling. It treats a web request
and a mobile request the same way. It is *client-agnostic*.

> Examples: Kong, AWS API Gateway, Spring Cloud Gateway, Nginx.

---

## BFF (Backend for Frontend)

A backend that belongs to **one type of client**. Each client gets its own BFF that returns
data in exactly the shape that client needs.

```
   Web App ──────▶  Web BFF   ──┐
                                 ├──▶ User / Order / Product Services
   Mobile App ───▶ Mobile BFF ──┘
```

**Why?** Different clients need different data:

- A **mobile** screen shows a small summary → needs less data, smaller payload (save battery/network).
- A **web** dashboard shows everything → needs more data, more fields.
- A **TV** app has a totally different layout → needs a different structure.

If a single API tries to serve all of them, it becomes bloated and full of `if client == mobile`
conditions. A BFF avoids that: each client team owns its own BFF and shapes data freely.

**Typical jobs:**

| Job | What it means |
|-----|---------------|
| Aggregation | Call 3 services, merge results into 1 response |
| Data shaping | Remove fields mobile doesn't need, rename fields |
| Client-specific logic | Format dates/prices the way this client wants |

**Example — one mobile screen needs user + latest order:**

```
Mobile App  ──▶  Mobile BFF
                     │  calls User Service   → { name, avatar }
                     │  calls Order Service  → { lastOrderId, status }
                     ▼
             returns ONE tidy response:
             { "name": "...", "avatar": "...", "lastOrder": { ... } }
```

The mobile app makes **one** call instead of two, and gets exactly the fields it needs.

---

## Side-by-Side Comparison

| | API Gateway | BFF |
|---|---|---|
| **Purpose** | One shared front door for everyone | A dedicated backend per client |
| **Cares about client type?** | No (client-agnostic) | Yes (built for one client) |
| **Main focus** | Infrastructure: routing, auth, rate limit | Data: aggregate & shape responses |
| **How many?** | Usually **one** for the whole system | **One per client** (web, mobile, TV) |
| **Contains business logic?** | No, just passes through | Yes, light logic to shape data |
| **Who owns it?** | Platform / infra team | The frontend team of that client |

---

## They Are Not Rivals — They Work Together

This is the most important point: **you often use both**. The Gateway handles the shared
"door" concerns, then forwards to the right BFF, which shapes the data.

```
                 ┌───────────────┐     ┌────────────┐
   Web App  ────▶│               │────▶│  Web BFF   │──┐
                 │  API Gateway  │     └────────────┘  │   ┌──────────────┐
   Mobile   ────▶│  (auth, rate  │     ┌────────────┐  ├──▶│  Services    │
                 │   limit, TLS) │────▶│ Mobile BFF │──┘   │ User/Order/..│
                 └───────────────┘     └────────────┘      └──────────────┘
```

- **Gateway** = security guard at the entrance (checks everyone the same way).
- **BFF** = a personal assistant for each client (prepares data the way that client likes).

---

## Simple Analogy

Think of a **restaurant**:

- **API Gateway** = the front door + the host. Everyone enters here, gets checked (reservation/ID),
  and is directed to the right area. Same process for every guest.
- **BFF** = a waiter assigned to your table. The kids' table gets a kids' menu, the VIP table gets
  a special menu. The waiter tailors the experience to *that* table.

You need the door for order and security. You need the waiter to serve each table well. Different
jobs, and a good restaurant has both.

---

## When Do You Actually Need Them?

| Situation | Use |
|-----------|-----|
| Many services, want one entry + central auth/rate limit | **API Gateway** |
| One client type only, simple data | Maybe **neither** — call services directly is fine |
| Multiple clients needing very different data shapes | **BFF** per client |
| Big system, multiple clients, need security *and* tailored data | **Both together** |

> Don't add them "just because". They add extra moving parts. Add a Gateway when shared concerns
> pile up; add a BFF when one client's data needs diverge from the others.

---

## Summary

```
API Gateway  →  ONE door for ALL clients   →  infrastructure (route, auth, limit)
BFF          →  ONE backend PER client      →  shape & aggregate data for that client

They solve different problems and are commonly used together:
Client → API Gateway (shared concerns) → BFF (client-specific data) → Services
```
