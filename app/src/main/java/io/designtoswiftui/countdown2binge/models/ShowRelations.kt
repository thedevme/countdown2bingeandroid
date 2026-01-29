package io.designtoswiftui.countdown2binge.models

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relationship class combining a [Season] with all its [Episode]s.
 *
 * Use with @Transaction queries in DAOs for efficient nested data loading.
 */
data class SeasonWithEpisodes(
    @Embedded val season: Season,
    @Relation(
        parentColumn = "id",
        entityColumn = "seasonId"
    )
    val episodes: List<Episode>
)

/**
 * Room relationship class combining a [Show] with all its [Season]s.
 *
 * Use with @Transaction queries in DAOs for efficient nested data loading.
 */
data class ShowWithSeasons(
    @Embedded val show: Show,
    @Relation(
        parentColumn = "id",
        entityColumn = "showId"
    )
    val seasons: List<Season>
)

/**
 * Room relationship class combining a [Show] with all related entities:
 * - [Season]s
 * - [Genre]s
 * - [Network]s
 *
 * Use with @Transaction queries in DAOs for loading complete show details.
 */
data class ShowWithDetails(
    @Embedded val show: Show,
    @Relation(
        parentColumn = "id",
        entityColumn = "showId"
    )
    val seasons: List<Season>,
    @Relation(
        parentColumn = "id",
        entityColumn = "showId"
    )
    val genres: List<Genre>,
    @Relation(
        parentColumn = "id",
        entityColumn = "showId"
    )
    val networks: List<Network>
)

/**
 * Room relationship class for complete show hierarchy including episodes.
 *
 * Note: For deep nesting (Show -> Seasons -> Episodes), you may need
 * to query SeasonWithEpisodes separately and combine in the repository.
 */
data class ShowWithSeasonsAndEpisodes(
    @Embedded val show: Show,
    @Relation(
        entity = Season::class,
        parentColumn = "id",
        entityColumn = "showId"
    )
    val seasonsWithEpisodes: List<SeasonWithEpisodes>
)
