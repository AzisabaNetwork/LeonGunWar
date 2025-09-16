package net.azisaba.lgw.core.db;

import net.azisaba.lgw.core.api.syogo.EmblemData;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;
import java.util.UUID;

@RegisterConstructorMapper(EmblemData.class)
public interface SyogoDataRepository {
    @SqlQuery("""
            INSERT INTO syogos(UUID, name, syogo)
            VALUES (:uuid, :name, :syogo) ON DUPLICATE KEY UPDATE name = VALUES(name) syogo = VALUES(name)
            """)
    void upsert(@BindFields EmblemData data);

    @SqlQuery("SELECT * FROM syogos WHERE uuid = :uuid")
    Optional<EmblemData> select(@Bind("uuid") UUID uuid);

    @SqlQuery("SELECT * FROM syogos WHERE name = :name")
    Optional<EmblemData> selectByName(@Bind("name") String name);

    @SqlQuery("DELETE FROM syogos WHERE uuid = :uuid")
    void delete(@Bind("uuid") UUID uuid);
}
