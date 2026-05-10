import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {
    CalendarSymbolQuery,
    CalendarService,
    CalendarSymbolBody
} from "../../../generated/public-transport-api";
import {Observable} from "rxjs";
import {LoginService} from "../../../auth/login.service";

export const calendarResolver: ResolveFn<Observable<CalendarSymbolBody>> = (route, state) => {
    const calendarsService: CalendarService = inject(CalendarService);
    const loginService: LoginService = inject(LoginService);

    const query: CalendarSymbolQuery = {};
    query.designation = route.queryParams['designation'];
    query.startDate = route.queryParams['startDate'];
    query.endDate = route.queryParams['endDate'];

    return calendarsService.getCalendarByCalendarName(loginService.getInstance(), query);
};
