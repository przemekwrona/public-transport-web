package pl.wrona.webserver.bussiness.route.deletion;

import org.checkerframework.common.util.count.report.qual.ReportCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.wrona.webserver.core.agency.RouteEntity;

@ReportCreation
public interface RouteDeletionRepository extends JpaRepository<RouteEntity, Long> {
}
