import {ResolveFn} from '@angular/router';
import {CalendarBody} from "../../../generated/public-transport-api";
import {Observable, of} from "rxjs";
import moment from "moment";

export const calendarEmptyResolver: ResolveFn<Observable<CalendarBody>> = (route, state) => {
    const calendarBody: CalendarBody = {};
    calendarBody.calendarName = '';
    calendarBody.designation = '';
    calendarBody.description = '';
    calendarBody.description = '';
    calendarBody.startDate = moment().startOf('day').format('yyyy-MM-DD');
    calendarBody.endDate = moment().endOf('year').format('yyyy-MM-DD');
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
