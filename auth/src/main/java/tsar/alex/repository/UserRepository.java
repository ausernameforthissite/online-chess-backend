package tsar.alex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tsar.alex.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
