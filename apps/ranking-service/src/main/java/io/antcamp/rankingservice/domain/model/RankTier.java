package io.antcamp.rankingservice.domain.model;

public enum RankTier {
    RANK_1,
    RANK_2,
    RANK_3,
    TOP_10,
    TOP_20,
    TOP_30,
    TOP_40,
    TOP_50,
    TOP_60,
    TOP_70,
    TOP_80,
    TOP_90,
    TOP_100;

    public static RankTier from(long rank, long totalParticipants) {
        if (rank == 1) return RANK_1;
        if (rank == 2) return RANK_2;
        if (rank == 3) return RANK_3;
        double percentile = (double) rank / totalParticipants * 100;
        if (percentile <= 10) return TOP_10;
        if (percentile <= 20) return TOP_20;
        if (percentile <= 30) return TOP_30;
        if (percentile <= 40) return TOP_40;
        if (percentile <= 50) return TOP_50;
        if (percentile <= 60) return TOP_60;
        if (percentile <= 70) return TOP_70;
        if (percentile <= 80) return TOP_80;
        if (percentile <= 90) return TOP_90;
        return TOP_100;
    }
}
