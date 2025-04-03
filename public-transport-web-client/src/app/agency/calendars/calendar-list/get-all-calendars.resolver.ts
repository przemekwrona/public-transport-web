import {ResolveFn} from '@angular/router';
import {CalendarsService} from "../calendars.service";
import {inject} from "@angular/core";
import {Observable} from "rxjs";
import {GetCalendarsResponse} from "../../../generated/public-transport";

export const getAllCalendarsResolver: ResolveFn<Observable<GetCalendarsResponse>> = (route, state): Observable<GetCalendarsResponse> => {
    const brigadeService = inject(CalendarsService);

    return brigadeService.getAllCalendars();
};
