# Database migrations

Flyway applies the SQL files in version order when the application connects to
an empty MariaDB database.

- `V1`: users and pets
- `V2`: places
- `V3`: reservations and payments
- `V4`: reviews

Do not edit a migration after it has been applied to a shared database. Add a
new migration with the next version instead.

Local databases that were already created from `petnow_schema.sql` are adopted
at baseline version `4` by the local profile. For an existing production
database, set `FLYWAY_BASELINE_ON_MIGRATE=true` only for its first Flyway-backed
startup, verify the `flyway_schema_history` entry, and then turn the setting off.
