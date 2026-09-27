package com.bookingengine.repository;

import com.bookingengine.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
}
