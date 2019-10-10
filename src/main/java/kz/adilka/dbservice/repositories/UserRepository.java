package kz.adilka.dbservice.repositories;

import kz.adilka.dbservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
