# Learning PgBouncer — From Basics to Production

A curated path for learning PgBouncer, PostgreSQL's lightweight connection pooler, from zero to production-ready.

---

## Why PgBouncer exists (read this first)

PostgreSQL forks a new OS process for every client connection. Each one costs roughly
10 MB of RAM plus CPU overhead for the fork itself. A server with `max_connections = 500`
runs out of headroom fast, even if most of those connections sit idle. PgBouncer sits
between your app and Postgres, multiplexing many cheap client connections onto a small
set of real backend connections — at ~2 KB overhead per client instead of ~10 MB.

---

## Learning path

### 1. Core concepts (30 min)
Start here to understand *why* pooling matters before touching config files.

- **Cybertec — "Types of PostgreSQL Connection Pooling"**
  https://www.cybertec-postgresql.com/en/pgbouncer-types-of-postgresql-connection-pooling/
  Best plain-language explanation of the fork() cost problem and why pooling helps.
  Includes a real benchmark showing a 98% throughput drop without pooling.

- **JP Camara — "PgBouncer is useful, important, and fraught with peril"**
  https://jpcamara.com/2023/04/12/pgbouncer-is-useful.html
  The best "here's what actually bites you in production" post. Read this before you
  deploy transaction mode anywhere near real traffic.

### 2. Official documentation (reference, not tutorial — but authoritative)
- **Usage / quick start**: https://www.pgbouncer.org/usage.html
- **Full config reference**: https://www.pgbouncer.org/config.html
- **Feature/compatibility matrix**: https://www.pgbouncer.org/features.html
  (Shows exactly which Postgres features break in which pooling mode — bookmark this.)

### 3. Pooling modes deep dive (the concept that trips everyone up)
- **Heroku Dev Center — "PgBouncer Configuration and Best Practices"**
  https://devcenter.heroku.com/articles/best-practices-pgbouncer-configuration
  The clearest structured breakdown of session vs. transaction vs. statement mode,
  with concrete guidance on `SET LOCAL` vs `SET`, temp tables, and prepared statements.

- **Podostack — "PgBouncer modes: why your pool either leaks or deadlocks"**
  https://podostack.com/p/postgres-connection-pooling-pgbouncer-modes
  Excellent on failure modes: `LISTEN/NOTIFY`, advisory locks, `WITH HOLD` cursors,
  and the `max_prepared_statements` fix added in PgBouncer 1.21.

### 4. Hands-on installation & config walkthrough
- **OneUptime — PostgreSQL + PgBouncer connection pooling guide**
  https://oneuptime.com/blog/post/2026-01-21-postgresql-pgbouncer-connection-pooling/view
  Up-to-date, copy-pasteable `pgbouncer.ini`, install steps for apt/dnf/source builds,
  and a full production-style config block.

- **Scaleway — Installing PgBouncer on Ubuntu/Debian**
  https://www.scaleway.com/en/docs/tutorials/install-pgbouncer/
  Good on admin console commands (`PAUSE`, `RESUME`, `RECONNECT`, `SHOW POOLS`) and
  zero-downtime restarts.

- **YouTube — PgBouncer Tutorial**
  https://www.youtube.com/watch?v=ddKm7a7xOpk
  Video walkthrough if you prefer watching setup happen over reading it.

### 5. Pool sizing (the part most guides skip)
- **Swyftstack — "PostgreSQL Connection Pool Sizing: Formula + PgBouncer Tuning"**
  https://swyftstack.com/blog/postgresql-connection-pooling-explained-pgbouncer
  Gives the `(cores × 2) + 1` sizing formula and explains why oversized pools make
  Postgres *slower*, not faster — a genuinely counterintuitive but important point.

### 6. Real-world case study
- **Mastodon PgBouncer guide** (felx/mastodon-documentation)
  https://github.com/felx/mastodon-documentation/blob/master/Running-Mastodon/PgBouncer-guide.md
  Shows the gotchas of retrofitting pooling onto an existing app (migrations needing
  a direct connection, empty-password auth failures, etc.) — useful even if you don't
  run Mastodon.

### 7. Advanced / edge cases
- **Severalnines — "A Guide to Using pgBouncer for PostgreSQL"**
  https://severalnines.com/blog/guide-using-pgbouncer/
  Covers `server_reset_query`, `application_name_add_host`, and prepared-statement
  sharing quirks — good once you're past the basics.

---

## Cheat sheet: the three pooling modes

| Mode | Connection held for | Multiplexing | Breaks |
|---|---|---|---|
| **session** (default) | Entire client session | None — 1:1 like a raw connection | Nothing (safest, least benefit) |
| **transaction** (most common) | One transaction | High — this is the point of PgBouncer | `SET` (use `SET LOCAL`), `LISTEN/NOTIFY`, session advisory locks, `WITH HOLD` cursors, named prepared statements (unless `max_prepared_statements > 0`, PgBouncer ≥ 1.21) |
| **statement** (rare) | One statement | Highest | Multi-statement transactions entirely; forces autocommit |

**Rule of thumb:** use `transaction` mode for your app's normal traffic, keep a direct
(non-pooled) connection for migrations and admin scripts, and size `default_pool_size`
off your database's CPU cores — not off how many app clients you have.

## Minimal working config

```ini
[databases]
mydb = host=127.0.0.1 port=5432 dbname=mydb

[pgbouncer]
listen_addr = 0.0.0.0
listen_port = 6432
auth_type = scram-sha-256
auth_file = /etc/pgbouncer/userlist.txt
pool_mode = transaction
max_client_conn = 1000
default_pool_size = 20
admin_users = pgbouncer_admin
```

## Essential admin console commands

Connect with `psql -p 6432 -U pgbouncer_admin pgbouncer`, then:

```sql
SHOW POOLS;     -- current pool state per db/user
SHOW STATS;     -- aggregate query/connection stats
SHOW CLIENTS;   -- connected clients
SHOW SERVERS;   -- backend connections to Postgres
RELOAD;         -- re-read config without dropping connections
PAUSE [db];     -- drain gracefully before restart/failover
RESUME [db];
```

---

## Suggested order if you only have an hour

1. Cybertec concepts post (10 min)
2. Heroku pooling-modes doc (15 min)
3. OneUptime install/config walkthrough — actually set it up locally (25 min)
4. JP Camara's pitfalls post (10 min) — read *before* you touch prod
