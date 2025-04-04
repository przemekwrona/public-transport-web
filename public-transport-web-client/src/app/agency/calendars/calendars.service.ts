import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {
    CalendarBody,
    CalendarPayload,
    CalendarQuery,
    GetCalendarsResponse,
    Status
} from "../../generated/public-transport";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class CalendarsService {

    constructor(private httpClient: HttpClient) {
    }

    public createCalendar(payload: CalendarPayload): Observable<Status> {
        return this.httpClient.post<Status>(`/api/v1/calendar`, payload);
    }

    public getCalendarByCalendarName(calendarQuery: CalendarQuery): Observable<CalendarBody> {
        return this.httpClient.post<CalendarBody>(`/api/v1/calendar/details`, calendarQuery);
    }

    public getAllCalendars(): Observable<GetCalendarsResponse> {
        return this.httpClient.get<GetCalendarsResponse>(`/api/v1/calendar`);
    }

    public deleteCalendarByCalendarName(calendarQuery: CalendarQuery): Observable<Status> {
        return this.httpClient.delete<Status>(`/api/v1/calendar`, { body: calendarQuery });
    }
}
