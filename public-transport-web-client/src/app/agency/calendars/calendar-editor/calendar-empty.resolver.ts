import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from '@angular/router';
import {CalendarBody, CalendarService, CalendarSymbolBody} from "../../../generated/public-transport-api";
import {Observable, of} from "rxjs";
import moment from "moment";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const calendarEmptyResolver: ResolveFn<Observable<CalendarSymbolBody>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const calendarName: string = route.paramMap.get('calendarName');
    const [startDate, endDate] = calendarName
        .split('--')
        .map(dateStr => moment(dateStr));

    const calendarBody: CalendarSymbolBody = {};
    calendarBody.calendarName = calendarName;
    calendarBody.designation = '';
    calendarBody.description = '';
    calendarBody.description = '';
    calendarBody.startDate = startDate.format('YYYY-MM-DD');
    calendarBody.endDate = endDate.format('YYYY-MM-DD');
    calendarBody.monday = false;
    calendarBody.tuesday = false;
    calendarBody.wednesday = false;
    calendarBody.thursday = false;
    calendarBody.friday = false;
    calendarBody.saturday = false;
    calendarBody.sunday = false;
    calendarBody.included = [];
    calendarBody.excluded = [];

    return of(calendarBody);
};
