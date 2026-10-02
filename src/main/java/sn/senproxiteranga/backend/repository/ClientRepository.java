package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.senproxiteranga.backend.domain.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}