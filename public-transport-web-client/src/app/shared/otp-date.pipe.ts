import {Pipe, PipeTransform} from '@angular/core';
import moment from "moment";

@Pipe({
  name: 'otpDate'
})
export class OtpDatePipe implements PipeTransform {

  transform(value: number | string | undefined, format: string = 'HH:mm'): string {
    const numberValue = Number(value);
    return moment(numberValue).format(format);
  }

}
