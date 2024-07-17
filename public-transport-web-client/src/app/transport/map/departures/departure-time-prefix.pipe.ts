import {Pipe, PipeTransform} from '@angular/core';
import {map, Observable, timer} from "rxjs";
import moment from "moment";

@Pipe({
    name: 'departureTimePrefix'
})
export class DepartureTimePrefixPipe implements PipeTransform {

    transform(value: number | undefined): Observable<string> {
        return timer(0, 1000)
            .pipe(map(() => {
                const now = moment().startOf('day').add(value, 'second');
                const differenceInMinutes = now.diff(moment(), 'minutes');

                if (differenceInMinutes <= 60) {
                    return 'za'
                }
                return '';
            }));
    }

}
