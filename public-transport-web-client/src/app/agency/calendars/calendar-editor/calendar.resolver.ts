import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {CalendarsService} from "../calendars.service";
import {CalendarBody, CalendarQuery} from "../../../generated/public-transport";
import {Observable} from "rxjs";

export const calendarResolver: ResolveFn<Observable<CalendarBody>> = (route, state) => {
    const calendarsService: CalendarsService = inject(CalendarsService);

    const query: CalendarQuery = {};
    query.calendarName = route.queryParams['name'];

    return calendarsService.getCalendarByCalendarName(query);
};
