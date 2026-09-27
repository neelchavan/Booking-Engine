package com.bookingengine.controller;

import com.bookingengine.dto.ShowListingResponse;
import com.bookingengine.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
public class ShowController {

    private final ShowRepository showRepository;

    /** P2: list all shows on a given date at a given theatre, with show timings. */
    @GetMapping("/{theatreId}/shows")
    public List<ShowListingResponse> listShows(
            @PathVariable Long theatreId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showRepository.findShowsByTheatreAndDate(theatreId, date).stream()
                .map(p -> new ShowListingResponse(
                        p.getShowId(), p.getMovieTitle(), p.getScreenName(),
                        p.getShowDate(), p.getStartTime(), p.getEndTime()))
                .toList();
    }
}
