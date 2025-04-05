import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {Observable} from "rxjs";
import {CalendarService, GetCalendarsResponse} from "../../../generated/public-transport";

export const getAllCalendarsResolver: ResolveFn<Observable<GetCalendarsResponse>> = (route, state): Observable<GetCalendarsResponse> => {
    const brigadeService = inject(CalendarService);

    return brigadeService.getCalendars();
};
