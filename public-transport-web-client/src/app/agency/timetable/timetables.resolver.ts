import {ResolveFn} from '@angular/router';
import {
    TimetableGeneratorFindAllResponse,
    TimetableGeneratorService
} from "../../generated/public-transport-api";
import {inject} from "@angular/core";
import {Observable} from "rxjs";
import {AgencyStorageService} from "../../auth/agency-storage.service";

export const generatedTimetablesResolver: ResolveFn<TimetableGeneratorFindAllResponse> = (route, state): Observable<TimetableGeneratorFindAllResponse> => {
    const timetableGeneratorService: TimetableGeneratorService = inject(TimetableGeneratorService);
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    return timetableGeneratorService.findAll(agencyStorageService.getInstance());
};
