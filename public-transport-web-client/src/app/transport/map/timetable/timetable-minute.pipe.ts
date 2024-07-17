import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'timetableMinute'
})
export class TimetableMinutePipe implements PipeTransform {

    transform(value: number): string {
        if (value < 10) {
            return `0${value}`;
        }
        return `${value}`;
    }

}
