import {Pipe, PipeTransform} from '@angular/core';
import moment from 'moment';
import {interval, map, Observable, timer} from "rxjs";

@Pipe({
  name: 'departureTime'
})
export class DepartureTimePipe implements PipeTransform {

  transform(value: Date, format: string): Observable<string> {
    return timer(0, 1000)
      .pipe(map(() => {
        const now = moment(value);
        const differenceInMinutes = now.diff(moment(), 'minutes') + 1;

        if (differenceInMinutes <= 1) {
          const differenceInSeconds = now.diff(moment(), 'seconds');

          if (differenceInSeconds <= 15) {
            return '<<< 1 min'
          } else if (differenceInSeconds > 15 && differenceInSeconds <= 30) {
            return '<< 1 min'
          } else if (differenceInSeconds > 30 && differenceInSeconds <= 45) {
            return '< 1 min'
          } else {
            return '1 min';
          }
        } else if (differenceInMinutes <= 60) {
          return `${differenceInMinutes} min`;
        }
        return moment(value).format(format);
      }));
  }

}
