import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {Observable} from "rxjs";
import {CalendarService, GetCalendarsResponse} from "../../../generated/public-transport-api";
import {LoginService} from "../../../auth/login.service";

export const getAllCalendarsResolver: ResolveFn<GetCalendarsResponse> = (route, state): Observable<GetCalendarsResponse> => {
    const calendarService = inject(CalendarService);
    const loginService = inject(LoginService);

    return calendarService.getCalendars(loginService.getInstance());
};
