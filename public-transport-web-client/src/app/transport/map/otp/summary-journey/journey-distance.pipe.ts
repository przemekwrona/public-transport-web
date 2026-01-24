import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'journeyDistance',
    standalone: false
})
export class JourneyDistancePipe implements PipeTransform {

    transform(value: number | undefined): string {
        if (value == undefined) {
            return '-';
        }
        if (value > 1500) {
            return (value / 1000).toFixed(2) + 'km'
        }
        return value + 'm';
    }

}
