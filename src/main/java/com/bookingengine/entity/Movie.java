package com.bookingengine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "movie")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String language;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(length = 100)
    private String genre;

    @Column(name = "release_date")
    private LocalDate releaseDate;
}
