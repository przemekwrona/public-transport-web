package pl.wrona.webserver.summary;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.Difference;
import org.igeolab.iot.pt.server.api.model.ModeDifference;
import org.igeolab.iot.pt.server.api.model.ModeSummary;

@UtilityClass
public class TripDifferenceStatisticUtils {

    ModeDifference modeDifference(ModeSummary core, ModeSummary transit, ModeSummary walk, ModeSummary bike, ModeSummary car) {

        return new ModeDifference()
                .transit(new Difference()
                        .time(diff(core, transit))
                        .percentage(percentage(core, transit))
                        .ratio(ratio(core, transit)))
                .walk(new Difference()
                        .time(diff(core, walk))
                        .percentage(percentage(core, walk))
                        .ratio(ratio(core, walk)))
                .bike(new Difference()
                        .time(diff(core, bike))
                        .percentage(percentage(core, bike))
                        .ratio(ratio(core, bike)))
                .car(new Difference()
                        .time(diff(core, car))
                        .percentage(percentage(core, car))
                        .ratio(ratio(core, car)));
    }

    private Integer diff(ModeSummary core, ModeSummary mode) {
        if (core == null || mode == null) {
            return null;
        }
        return mode.getTotalDuration() - core.getTotalDuration();
    }

    private Integer percentage(ModeSummary core, ModeSummary mode) {
        if (core == null || mode == null) {
            return null;
        }
        return (int) (((double) 100 * diff(core, mode)) / ((double) core.getTotalDuration()));
    }

    private Float ratio(ModeSummary core, ModeSummary mode) {
        if (core == null || mode == null) {
            return null;
        }
        return ((float) mode.getTotalDuration()) / ((float) core.getTotalDuration());
    }
}
