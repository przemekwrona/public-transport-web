import { ResolveFn } from '@angular/router';
import {inject} from "@angular/core";
import {CalendarService, GetCalendarSymbolsResponse} from "../../../generated/public-transport-api";
import {LoginService} from "../../../auth/login.service";

export const brigadeGetAllCalendarsResolver: ResolveFn<GetCalendarSymbolsResponse> = (route, state) => {
  const calendarService = inject(CalendarService);
  const loginService = inject(LoginService);
  return calendarService.getCalendars(loginService.getInstance());
};
