package com.alvayonara.finguardriskservice.risk.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class RiskStateWriter {
    @Autowired
    private DatabaseClient databaseClient;

    public Mono<Void> insertInitial(Long userId, String level) {
        return databaseClient.sql("""
                        INSERT IGNORE INTO risk_state (user_id, last_level, updated_at)
                        VALUES (:userId, :level, NOW())
                        """)
                .bind("userId", userId)
                .bind("level", level)
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<Boolean> updateLevel(Long userId, String newLevel) {
        return databaseClient.sql("""
                        UPDATE risk_state
                        SET last_level = :level,
                            updated_at = NOW()
                        WHERE user_id = :userId
                          AND last_level <> :level
                        """)
                .bind("userId", userId)
                .bind("level", newLevel)
                .fetch()
                .rowsUpdated()
                .map(rows -> rows > 0);
    }
}
