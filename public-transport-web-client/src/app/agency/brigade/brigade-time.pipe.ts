import {Pipe, PipeTransform} from '@angular/core';
import moment from 'moment';
import {Moment} from "moment";

@Pipe({
    name: 'brigadeEditorTime'
})
export class BrigadeTimePipe implements PipeTransform {

    private ONE_MINUTE: number = 60;
    private ONE_HOUR: number = 60 * 60;

    transform(value: number): string {
        if (value < this.ONE_MINUTE) {
            return `${value} s`
        }

        const hour: string = Number(value / 60 / 60).toFixed(0);
        const momentTime: Moment = moment(0).add(value, 'seconds');

        if (value < this.ONE_HOUR) {
            return `${momentTime.format('mm')}min`
        } else {
            console.log(value / 60);
            return `${hour}h ${momentTime.format('mm')}min`
        }
    }

}
