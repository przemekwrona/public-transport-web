import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'timetableHour'
})
export class TimetableHourPipe implements PipeTransform {

  transform(value: number): string {
    if (value == 0) {
      value = 24;
    }

    if (value < 10) {
      return `0${value}`;
    }
    return `${value}`;
  }

}
