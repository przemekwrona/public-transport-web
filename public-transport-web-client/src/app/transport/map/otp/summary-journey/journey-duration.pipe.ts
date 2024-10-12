import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'journeyDuration'
})
export class JourneyDurationPipe implements PipeTransform {

    transform(value: number | undefined, brackets: boolean = false): string {
        if (value == undefined) {
            return '';
        }

        const min: number = value / 60;

        if (brackets) {
            return "(" + Math.round(min) + "min)";
        } else {
            return Math.round(min) + "min"
        }

    }

}
