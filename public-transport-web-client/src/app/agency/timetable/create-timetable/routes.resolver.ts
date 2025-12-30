import {ResolveFn} from "@angular/router";
import {
    TimetableGeneratorFilterByRoutesResponse,
    TimetableGeneratorService
} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const routesInGeneratedTimetableResolver: ResolveFn<TimetableGeneratorFilterByRoutesResponse> = (route, state) => {
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);
    const timetableGeneratorService: TimetableGeneratorService = inject(TimetableGeneratorService);

    return timetableGeneratorService.findRoutes(agencyStorageService.getInstance());
};