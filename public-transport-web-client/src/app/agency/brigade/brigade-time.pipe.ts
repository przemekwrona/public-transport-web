import {Pipe, PipeTransform} from '@angular/core';
import moment from 'moment';

@Pipe({
    name: 'brigadeEditorTime',
    standalone: false
})
export class BrigadeTimePipe implements PipeTransform {

    private ONE_MINUTE: number = 60;
    private ONE_HOUR: number = 60 * 60;

    transform(value: number): string {
        if (value < this.ONE_HOUR) {
            return moment('00:00', 'HH:mm').add(value, 'seconds').startOf('minute').add(1, 'minute').format('HH:mm');

        } else {
            return moment('00:00', 'HH:mm').add(value, 'seconds').startOf('minute').add(1, 'minute').format('HH:mm');
        }
    }

}
