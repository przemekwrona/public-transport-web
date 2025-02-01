import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'time'
})
export class TimePipe implements PipeTransform {

  transform(timeInMinutes: number | undefined): number {
    const value: number = timeInMinutes || 0.0;
    return value / 60;
  }

}
