package pl.wrona.osm.stop.deactivate.stop;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class StopService {

    private final StopRepository stopRepository;

    @Transactional
    public List<StopEntity> findNext100NotCheckedStops(int page) {
        return this.stopRepository.findAllByDeactivatedCheckedIsFalseAndOsmIsTrue(PageRequest.of(page, 1000));
    }

    @Transactional
    public boolean hasNotCheckedStops() {
        return this.stopRepository.existsByDeactivatedCheckedIsFalseAndOsmIsTrue();
    }

    @Transactional
    public boolean hasBdot10kStopIn10m(StopEntity stopEntity) {
        return stopRepository.existsByBdot10kStopIn10m(stopEntity.getStopId());
    }

    @Transactional
    public StopEntity save(StopEntity stopEntity) {
        return stopRepository.save(stopEntity);
    }


    @Transactional
    public Optional<StopEntity> findById(Long id) {
        return stopRepository.findById(id);
    }

}
