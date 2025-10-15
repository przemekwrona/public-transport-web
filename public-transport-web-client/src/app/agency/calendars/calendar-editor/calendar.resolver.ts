import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {CalendarBody, CalendarQuery, CalendarService} from "../../../generated/public-transport-api";
import {Observable} from "rxjs";

export const calendarResolver: ResolveFn<Observable<CalendarBody>> = (route, state) => {
    const calendarsService: CalendarService = inject(CalendarService);

    const query: CalendarQuery = {};
    query.calendarName = route.queryParams['name'];

    return calendarsService.getCalendarByCalendarName(query);
};
