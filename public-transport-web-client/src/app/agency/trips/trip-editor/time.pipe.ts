import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'time'
})
export class TimePipe implements PipeTransform {

  transform(timeInSeconds: number | undefined): number {
    const value: number = timeInSeconds || 0.0;
    return value / 60;
  }

}
