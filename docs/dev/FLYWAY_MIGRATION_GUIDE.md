# Flyway Migration Guide for Playlist Service

## Overview
This guide documents the migration from Spring's SQL initialization (`spring.sql.init.mode`) to Flyway for database schema and seed data management in the playlist-service.

## Problem Solved
**Before:** Spring SQL Init ran on every startup with `mode: always`, attempting to insert the same hardcoded UUIDs repeatedly, causing duplicate key constraint violations when Docker containers restarted.

**After:** Flyway tracks executed migrations in a `flyway_schema_history` table, ensuring each migration runs exactly once, even across container restarts.

## Changes Made

### 1. Dependencies Added (pom.xml)
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### 2. Migration Files Created

#### Directory Structure
```
src/main/resources/
  └── db/
      └── migration/
          ├── V1__create_schema.sql
          └── V2__seed_songs.sql
```

#### V1__create_schema.sql
- Creates `songs` table with UUID primary key
- Creates `playlists` table with UUID primary key
- Creates `playlist_songs` join table (many-to-many)
- Adds indexes for performance optimization

#### V2__seed_songs.sql
- Inserts 100 songs with fixed UUIDs (same as original test-data.sql)
- Categorized by energy levels (Very Calming to Very Energetic)
- Will only execute once thanks to Flyway's migration tracking

### 3. Configuration Updates

#### application.yml & application-docker.yml
**Changed:**
- `jpa.hibernate.ddl-auto`: `update` → `validate`
  - Hibernate no longer manages schema, only validates against it
  - Flyway is now responsible for all DDL changes
  
- `sql.init.mode`: `always`/`never` → `never`
  - Spring SQL Init completely disabled
  
**Added:**
```yaml
flyway:
  enabled: true
  baseline-on-migrate: true  # Handles existing databases gracefully
  locations: classpath:db/migration
  validate-on-migrate: true
```

## How It Works

### First Startup (Fresh Database)
1. Flyway creates `flyway_schema_history` table
2. Runs V1__create_schema.sql → Creates tables
3. Records V1 as executed in flyway_schema_history
4. Runs V2__seed_songs.sql → Inserts 100 songs
5. Records V2 as executed in flyway_schema_history

### Subsequent Startups
1. Flyway checks `flyway_schema_history`
2. Sees V1 and V2 already executed
3. Skips both migrations ✅
4. No duplicate key errors!

### Adding New Migrations
Create versioned files following the pattern:
- V3__add_genre_column.sql
- V4__seed_additional_songs.sql
- etc.

**Naming Convention:** `V{version}__{description}.sql`
- Version must be numeric and sequential
- Use double underscore `__` before description
- Description uses underscores instead of spaces

## Testing the Migration

### Option 1: Fresh Start (Recommended)
```bash
# Stop and remove containers
docker-compose down

# Remove PostgreSQL volume to start fresh
docker volume rm echocare2_playlist-db-data

# Rebuild and start
docker-compose up --build playlist-service
```

### Option 2: Existing Database
If you already have data in PostgreSQL:
- `baseline-on-migrate: true` tells Flyway to baseline at V1
- Flyway won't try to recreate existing tables
- Only new migrations (V3+) will run

### Verification
Check Flyway migration history:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

Expected output:
```
installed_rank | version | description    | type | script                    | checksum    | installed_on        | success
---------------+---------+----------------+------+---------------------------+-------------+---------------------+---------
1              | 1       | create schema  | SQL  | V1__create_schema.sql     | -1234567890 | 2026-02-25 10:00:00 | true
2              | 2       | seed songs     | SQL  | V2__seed_songs.sql        | 987654321   | 2026-02-25 10:00:01 | true
```

## Best Practices

### ✅ DO
- Always use versioned migrations (V1, V2, V3...)
- Never modify already-executed migrations
- Test migrations locally before deploying
- Use meaningful migration descriptions
- Keep migrations small and focused

### ❌ DON'T
- Don't change `ddl-auto` back to `update` or `create`
- Don't manually modify tables when using Flyway
- Don't delete migration files after they've been executed
- Don't skip version numbers (V1, V2, V4 ❌)

## Rollback Strategy

Flyway doesn't support automatic rollback for free version. For rollback:

1. **Create compensating migration:**
   ```sql
   -- V3__remove_invalid_songs.sql
   DELETE FROM songs WHERE id IN ('uuid1', 'uuid2');
   ```

2. **Manual rollback (development only):**
   ```bash
   # Drop database and restart
   docker volume rm echocare2_playlist-db-data
   docker-compose up playlist-service
   ```

## Troubleshooting

### Problem: "Validate failed: Migration checksum mismatch"
**Solution:** You modified a migration file after it was executed. Either:
- Restore the original file
- Or repair the checksum: `flyway.repair=true` (not recommended for production)

### Problem: "Schema-validation: missing table [songs]"
**Solution:** Flyway hasn't run yet. Check:
1. Flyway is enabled: `spring.flyway.enabled=true`
2. Migration files are in correct location
3. Check application logs for Flyway execution

### Problem: Duplicate key constraint violation
**Solution:** Should not happen with Flyway! If it does:
1. Check `flyway_schema_history` - are migrations recorded?
2. Ensure `baseline-on-migrate: true` for existing databases
3. Verify migration files haven't been modified

## Migration from Old System

If you need to clean up old Spring SQL Init setup:
1. ✅ `test-data.sql` can be kept for reference (it's no longer used)
2. ✅ No code changes needed in Java entities
3. ✅ Existing data in database is preserved
4. ✅ Just rebuild and restart!

## Next Steps

1. **Rebuild playlist-service:**
   ```bash
   cd services/playlist-service
   mvn clean package
   ```

2. **Restart Docker containers:**
   ```bash
   docker-compose down
   docker-compose up --build playlist-service
   ```

3. **Verify migrations:**
   - Check logs for "Flyway" messages
   - Query `flyway_schema_history` table
   - Verify 100 songs are present in `songs` table

4. **Test restart behavior:**
   ```bash
   docker-compose restart playlist-service
   # Should start cleanly without duplicate key errors!
   ```

## Summary

✅ **Flyway dependency added to pom.xml**  
✅ **V1__create_schema.sql created with DDL**  
✅ **V2__seed_songs.sql created with 100 song inserts**  
✅ **application.yml updated (both local & docker)**  
✅ **ddl-auto changed to 'validate'**  
✅ **Spring SQL Init disabled**  
✅ **Ready to test!**

---

**Author:** GitHub Copilot  
**Date:** February 25, 2026  
**Service:** playlist-service  
**Migration Type:** Spring SQL Init → Flyway

