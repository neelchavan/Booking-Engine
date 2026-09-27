package com.bookingengine.repository;

import com.bookingengine.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    /**
     * P2: all shows on a given date at a given theatre, with show timings.
     * Backed by the (screen_id, show_date, start_time) index on `show` and the
     * FK index on screen.theatre_id.
     */
    @Query(value = """
            SELECT s.show_id AS showId,
                   m.title AS movieTitle,
                   sc.name AS screenName,
                   s.show_date AS showDate,
                   s.start_time AS startTime,
                   s.end_time AS endTime
            FROM `show` s
            JOIN screen sc ON s.screen_id = sc.screen_id
            JOIN movie m ON s.movie_id = m.movie_id
            WHERE sc.theatre_id = :theatreId
              AND s.show_date = :showDate
            ORDER BY s.start_time
            """, nativeQuery = true)
    List<ShowListingProjection> findShowsByTheatreAndDate(
            @Param("theatreId") Long theatreId,
            @Param("showDate") LocalDate showDate);
}
