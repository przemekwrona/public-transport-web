import {ActivatedRouteSnapshot, ResolveFn, RouterStateSnapshot} from '@angular/router';
import {CalendarService, CalendarSymbolBody, GetCalendarItemResponse} from "../../../generated/public-transport-api";
import {map, Observable} from "rxjs";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const calendarEmptyResolver: ResolveFn<Observable<CalendarSymbolBody>> = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);
    const calendarService: CalendarService = inject(CalendarService);

    const instance: string = agencyStorageService.getInstance();
    const calendarCode: string = route.paramMap.get('calendarCode');

    return calendarService.getCalendarByCalendarCode(instance, calendarCode).pipe(
        map((response: GetCalendarItemResponse) => {
            const calendarItem = response.items?.[0];

            const calendarBody: CalendarSymbolBody = {
                calendarName: calendarCode,
                designation: '',
                description: '',
                startDate: calendarItem?.startDate,
                endDate: calendarItem?.endDate,
                monday: false,
                tuesday: false,
                wednesday: false,
                thursday: false,
                friday: false,
                saturday: false,
                sunday: false,
                included: [],
                excluded: []
            };

            return calendarBody;
        })
    );
};
