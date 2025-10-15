import {Component, EventEmitter, Input, Output} from '@angular/core';
import moment from "moment";
import {Pattern, StopTime, Time} from "../../../generated/public-transport-planner-api";

export class DepartureMetadata {
    tripId: string;
    shapeId: string;
}

export class PatternTime {
    pattern?: Pattern;
    time?: Time;

    constructor(pattern?: Pattern, time?: Time) {
        this.pattern = pattern;
        this.time = time;
    }
}

@Component({
    selector: 'app-timetable-content',
    templateUrl: './timetable-content.component.html',
    styleUrl: './timetable-content.component.scss'
})
export class TimetableContentComponent {

    private _timetables: StopTime[] = [];

    @Input() set timetables(timetables: StopTime[]) {
        this._timetables = timetables;
        this.groupedTimetables = this.getTimetable();
        this.closestDeparture = this.findFirstDeparture(this.groupedTimetables);
    }

    get timetables(): StopTime[] {
        return this._timetables || [];
    }

    @Input() timetableDate: moment.Moment;

    @Input() line: string = '';

    @Output() onClickDeparture = new EventEmitter<DepartureMetadata>();

    public groupedTimetables: { [hour: number]: PatternTime[] };
    public closestDeparture: PatternTime | null;

    public hasTimetables(): boolean {
        return this.timetables.map(timetables => timetables.times).flat().length > 0;
    }

    public hasHour(hour: number): boolean {
        return this.groupedTimetables[hour] != null && this.groupedTimetables[hour].length > 0;
    }

    public isClose(patternTime: PatternTime): boolean {
        return this.closestDeparture != null
            && patternTime.time?.scheduledDeparture === (this.closestDeparture?.time?.scheduledDeparture || 0)
        //   && moment(this.closestDeparture.departure).diff(moment(), 'hours') < 4;
    }

    showTimetable(timetable: PatternTime) {
        // const metadata = new DepartureMetadata();
        // metadata.tripId = timetable.tripId;
        // metadata.shapeId = timetable.shapeId;
        // this.onClickDeparture.emit(metadata);
    }

    private getTimetable(): { [hour: number]: PatternTime[] } {
        return this.timetables
            .map(timetables => timetables.times?.map(time => new PatternTime(timetables?.pattern, time)))
            .flat()
            .sort((prev, curr) => (prev?.time?.scheduledDeparture || 0) - (curr?.time?.scheduledDeparture || 0))
            .reduce((group: { [hour: number]: PatternTime[] }, product: PatternTime | undefined) => {
                const category = moment().startOf('day').add(product?.time?.scheduledDeparture, 'second').hour();
                group[category] = group[category] ?? [];

                if (product != undefined) {
                    group[category].push(product);
                }
                return group;
            }, {});
    }

    private findFirstDeparture(timetable: { [hour: number]: PatternTime[] }): PatternTime | null {
        const now: moment.Moment = moment();
        const currentHours: StopTime[] = (timetable[now.hour()] || []).filter(timetable => moment().startOf('day').add(timetable?.time?.scheduledDeparture, 'second').isAfter(now));

        if (currentHours.length > 0) {
            return currentHours[0];
        }

        const nextHours: PatternTime[] = timetable[now.hour() + 1] || [];

        if (nextHours.length > 0) {
            return nextHours[0];
        }

        return null;
    }

    public hasClose(hour: number): boolean {
        const departure = moment().startOf('day').add(this.closestDeparture?.time?.scheduledDeparture, 'second');
        return this.closestDeparture != null && departure.hour() === hour && departure.diff(moment(), 'hours') < 2;
    }

    public getClosesDepartureInMinutes(): number {
        return moment().startOf('day').add(this.closestDeparture?.time?.scheduledDeparture, 'second').diff(moment(), 'minutes') + 1;
    }

    public getHours(): number[] {
        if (this.line.startsWith('N')) {
            if (this.hasHour(22) && this.hasHour(23) && this.hasHour(5)) {
                return [22, 23, 0, 1, 2, 3, 4, 5];
            }

            if (this.hasHour(23) && this.hasHour(5)) {
                return [23, 0, 1, 2, 3, 4, 5];
            }

            if (this.hasHour(5)) {
                return [0, 1, 2, 3, 4, 5];
            }

            return [22, 23, 0, 1, 2, 3, 4, 5];
        }

        if (this.hasHour(4) && this.hasHour(5) && this.hasHour(6)) {
            if (this.hasHour(22) && this.hasHour(23) && this.hasHour(0)) {
                return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];
            }
            if (this.hasHour(22) && this.hasHour(23)) {
                return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
            }
            if (this.hasHour(22)) {
                return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22];
            }
            return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21];
        }

        if (this.hasHour(5) && this.hasHour(6)) {
            if (this.hasHour(22) && this.hasHour(23) && this.hasHour(0)) {
                return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];
            }
            if (this.hasHour(22) && this.hasHour(23)) {
                return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
            }
            if (this.hasHour(22)) {
                return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22];
            }
            return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21];
        }

        if (this.hasHour(6)) {
            if (this.hasHour(22) && this.hasHour(23) && this.hasHour(0)) {
                return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];
            }
            if (this.hasHour(22) && this.hasHour(23)) {
                return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
            }
            if (this.hasHour(22)) {
                return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22];
            }
            return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21];
        }

        return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];
    }


}
