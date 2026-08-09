import {ResolveFn} from '@angular/router';
import {inject} from "@angular/core";
import {
    CalendarService,
    CalendarSymbolBody
} from "../../../generated/public-transport-api";
import {Observable} from "rxjs";
import {LoginService} from "../../../auth/login.service";

export const calendarResolver: ResolveFn<Observable<CalendarSymbolBody>> = (route, state) => {
    const calendarsService: CalendarService = inject(CalendarService);
    const loginService: LoginService = inject(LoginService);

    const instance = loginService.getInstance();
    const calendarCode: string = route.paramMap.get('calendarCode');
    const calendarSymbol: string = route.paramMap.get('calendarSymbol');

    return calendarsService.getCalendarSymbol(instance, calendarCode, calendarSymbol);
};
