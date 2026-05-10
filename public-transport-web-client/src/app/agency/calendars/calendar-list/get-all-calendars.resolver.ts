import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {Observable} from "rxjs";
import {CalendarService, GetCalendarItemResponse, GetCalendarsResponse} from "../../../generated/public-transport-api";
import {LoginService} from "../../../auth/login.service";

export const getAllCalendarsResolver: ResolveFn<GetCalendarItemResponse> = (route, state): Observable<GetCalendarItemResponse> => {
    const calendarService = inject(CalendarService);
    const loginService = inject(LoginService);

    return calendarService.getCalendarItems(loginService.getInstance());
};
