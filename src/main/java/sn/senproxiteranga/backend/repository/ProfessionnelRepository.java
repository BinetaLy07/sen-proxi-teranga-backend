package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Professionnel;

public interface ProfessionnelRepository extends JpaRepository<Professionnel, Long> {
}