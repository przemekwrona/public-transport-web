import { ResolveFn } from '@angular/router';
import {inject} from "@angular/core";
import {CalendarService, GetCalendarsResponse} from "../../../generated/public-transport";

export const brigadeGetAllCalendarsResolver: ResolveFn<GetCalendarsResponse> = (route, state) => {
  const calendarService = inject(CalendarService);
  return calendarService.getCalendars();
};
