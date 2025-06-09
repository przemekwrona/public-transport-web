package pl.wrona.webserver.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AppRoleRepository extends JpaRepository<AppRole, String> {

    Set<AppRole> findAppRolesByAppUsers(Set<AppUser> appUsers);
}
