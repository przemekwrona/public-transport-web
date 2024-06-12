import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'otpDuration'
})
export class OtpDurationPipe implements PipeTransform {

  transform(value: number | undefined): number {
    if(value == undefined) {
      return -1;
    }
    return value / 60;
  }

}
