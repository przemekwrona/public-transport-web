import {ChangeDetectionStrategy, Component, EventEmitter, Input, Output} from '@angular/core';
import moment from 'moment';
import {DepartureMetadata} from "../timetable-content/timetable-content.component";
import {Route, Stop, StopTime} from "../../../generated";


@Component({
    selector: 'app-timetable',
    templateUrl: './timetable.component.html',
    styleUrl: './timetable.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TimetableComponent {

    @Input() line: string;
    @Input() stopTimes: StopTime[] = [];
    @Input() stop: Stop | null;
    @Input() routes: Route[];
    @Input() direction: string;
    @Output() onClickDeparture = new EventEmitter<DepartureMetadata>();

    public timetable: { [routeId: string]: StopTime[] };
    public headsign: string = '';

    public timetableDate: moment.Moment = moment();

    public getTimetable(line: string, date: moment.Moment): StopTime[] {
        const routesId: string[] = [...new Set<string>(this.routes
            .filter(route => route.shortName === line)
            .map(route => route.id || ''))];

        return this.stopTimes.filter(stopTime => routesId.includes(stopTime.pattern?.routeId || ''));
    }

    public getMainHeadsing(line: string): string {
        const routesId: string[] = [...new Set<string>(this.routes
            .filter(route => route.shortName === line)
            .map(route => route.id || ''))];

        const stopTime = this.stopTimes
            .filter(stopTime => routesId.includes(stopTime.pattern?.routeId || ''))
            .sort((prev: StopTime, curr: StopTime) => (curr.times?.length || 0) - (prev.times?.length || 0));

        return (stopTime[0]?.times || [])[0]?.headsign || '';
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

}
