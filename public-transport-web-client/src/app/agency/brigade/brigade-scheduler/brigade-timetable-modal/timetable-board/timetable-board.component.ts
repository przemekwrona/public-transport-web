import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
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
export class TimetableBoardComponent implements OnChanges {

    @Input() title: string = '';
    @Input() variant: BrigadeTimetableVariant = {};
    @Input() excludeHours: number[] = [];

    readonly hours: number[] = Array.from({length: 24}, (_, hour) => hour);

    get visibleHours(): number[] {
        return this.hours.filter(hour => !this.excludeHours.includes(hour));
    }

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

    // Departures reload in place (the board itself is never destroyed), so re-trigger
    // a short flash animation on the minute values whenever fresh data comes in,
    // instead of the previous "delete and rebuild the whole table" flow.
    public isUpdating = false;

    ngOnChanges(changes: SimpleChanges): void {
        if (!changes['variant'] || changes['variant'].firstChange) {
            return;
        }
        this.isUpdating = false;
        requestAnimationFrame(() => {
            this.isUpdating = true;
        });
    }

}
