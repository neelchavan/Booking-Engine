package com.bookingengine.repository;

import java.time.LocalDate;
import java.time.LocalTime;

/** Projection for the P2 "shows by theatre + date" query. */
public interface ShowListingProjection {
    Long getShowId();
    String getMovieTitle();
    String getScreenName();
    LocalDate getShowDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
