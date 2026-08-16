import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
    BrigadeTimetableDeparture,
    BrigadeTimetableVariant
} from '../../../../../generated/public-transport-api';

@Component({
    selector: 'app-timetable-board',
    imports: [
        CommonModule
    ],
    templateUrl: './timetable-board.component.html',
    styleUrl: './timetable-board.component.scss'
})
export class TimetableBoardComponent {

    @Input() title: string = '';
    @Input() variant: BrigadeTimetableVariant = {};

    readonly hours: number[] = Array.from({length: 24}, (_, hour) => hour);

    departuresForHour(hour: number): BrigadeTimetableDeparture[] {
        return (this.variant?.departures ?? []).filter(departure => this.hourOf(departure) === hour);
    }

    private hourOf(departure: BrigadeTimetableDeparture): number | null {
        if (departure.h != null) {
            return departure.h;
        }
        if (departure.time) {
            const [hours] = departure.time.split(':').map(Number);
            return Number.isFinite(hours) ? hours : null;
        }
        return null;
    }

}
