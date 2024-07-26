import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TimetableDeparture} from "../../../http/timetable.service";
import {groupBy} from "lodash";
import moment from 'moment';
import {DepartureMetadata} from "../timetable-content/timetable-content.component";
import {Stop, StopTime} from "../../../generated";


@Component({
    selector: 'app-timetable',
    templateUrl: './timetable.component.html',
    styleUrl: './timetable.component.scss'
})
export class TimetableComponent {

    private _line: string = '';

    @Input() set line(line: string) {
        this._line = line;
    }

    get line(): string {
        return this._line;
    }

    private _stopTimes: StopTime[];

    @Input() set stopTimes(stopTimes: StopTime[]) {
        this._stopTimes = stopTimes;

        this.timetable = this.stopTimes.reduce((acc: { [routeId: string]: StopTime[] }, curr: StopTime) => {
            let key: string = curr.pattern?.routeId || '';
            if (!acc[key]) {
                acc[key] = [];
            }
            acc[key].push(curr);
            return acc;
        }, {});

        this.headsign = this.getMainHeadsing();

        if (this.line != null) {
            // this.numberOfDepartures = 3;
        }
        // this.departures = this.getDepartures(stopTimes);
        // this.numberOfDepartures = this.countNumberOfLines();
    }

    get stopTimes(): StopTime[] {
        return this._stopTimes;
    }

    @Input() stop: Stop | null;
    @Input() direction: string;
    @Output() onClickDeparture = new EventEmitter<DepartureMetadata>();

    public timetable: { [routeId: string]: StopTime[] };
    public headsign: string = '';

    public timetableDate: moment.Moment = moment();

    public getTimetable(date: moment.Moment): StopTime[] {
        return this.timetable[this.getRouteId()];
    }

    public getMainHeadsing(): string {
        const headsigns: string[] = this.stopTimes
            .map(stopTime => (stopTime?.times || []).map(time => time?.headsign || ''))
            .flat();

        return headsigns[0];
    }

    clickDeparture(timetableDeparture: DepartureMetadata) {
        this.onClickDeparture.emit(timetableDeparture);
    }

    getTimetableDates(): moment.Moment[] {
        return [
            moment(),
            moment().add(1, 'days'),
            moment().add(2, 'days'),
            moment().add(3, 'days'),
            moment().add(4, 'days'),
            moment().add(5, 'days'),
            moment().add(6, 'days')
        ];
    }

    public getRouteId(): string {
        return `1:${this.line}`;
    }

}
