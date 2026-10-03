package sn.senproxiteranga.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sn.senproxiteranga.backend.domain.Role;
import sn.senproxiteranga.backend.domain.enums.NomRole;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNom(NomRole nom);
}
