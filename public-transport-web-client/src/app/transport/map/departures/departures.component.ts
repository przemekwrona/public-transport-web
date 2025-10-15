import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {interval, Subscription} from "rxjs";
import moment from 'moment';
import {Departure} from "../../../http/timetable.service";
import {Pattern, Route, Stop, StopTime, Time} from "../../../generated/public-transport-planner-api";

export class TimeTuple {
    time: Time;
    pattern: Pattern | undefined;

    constructor(time: Time, pattern: Pattern | undefined) {
        this.time = time;
        this.pattern = pattern;
    }
}

@Component({
    selector: 'app-departures',
    templateUrl: './departures.component.html',
    styleUrl: './departures.component.scss'
})
export class DeparturesComponent implements OnInit, OnDestroy {

    private intervalTimer = interval(1000);
    private intervalSubscription: Subscription;

    private _line: string | null;

    @Input() set line(line: string | null) {
        this._line = line;
        if (this.line != null) {
            this.numberOfDepartures = 3;
        }
        this.numberOfDepartures = this.countNumberOfLines();
    }

    get line(): string | null {
        return this._line;
    }

    @Input() stop: Stop | null;

    @Output() onClick = new EventEmitter<string>();

    private _stopTimes: StopTime[];

    @Input() set stopTimes(stopTimes: StopTime[]) {
        this._stopTimes = stopTimes;
        if (this.line != null) {
            this.numberOfDepartures = 3;
        }
        this.departures = this.getDepartures(stopTimes);
        this.numberOfDepartures = this.countNumberOfLines();
    }

    get stopTimes(): StopTime[] {
        return this._stopTimes;
    }

    @Input() routes: Route[] = [];

    public departures: TimeTuple[] = [];
    public currentDate: moment.Moment = moment();
    public numberOfDepartures: number = 0;
    public isOpen: boolean = true;

    constructor() {
    }

    ngOnInit(): void {
        this.intervalSubscription = this.intervalTimer.subscribe(number => {
            this.currentDate = moment();
            const currentDeparture = this.departures[0];


            if (currentDeparture != null) {
                const departure = moment().startOf('day').add(currentDeparture.time.scheduledDeparture, 'second');

                if (departure.isBefore(moment())) {
                    this.departures.shift();
                }
            }
        });
    }

    ngOnDestroy(): void {
        this.intervalSubscription.unsubscribe();
    }

    getDepartures(stopTimes: StopTime[]): TimeTuple[] {
        const now = moment().add(1, 'hour');
        const secondOfTheDay = now.hour() * 60 * 60 + now.minute() * 60 + now.second();

        return stopTimes
            .map(stopTime => (stopTime.times || []).map(time => new TimeTuple(time, stopTime?.pattern)))
            .reduce((prev: TimeTuple[], current: TimeTuple[]) => prev.concat(current), [])
            .filter((tuple: TimeTuple) => (tuple.time.scheduledDeparture || 0) >= secondOfTheDay)
            .sort((prev: TimeTuple, curr: TimeTuple) => (prev.time?.scheduledDeparture || 0) - (curr?.time.scheduledDeparture || 0));
    }

    countNumberOfLines(): number {
        if (this.hasLine()) {
            return 2;
        }

        const now = moment();

        const countDeparturesUnder1H = this.departures.slice(0, 10)
            .map(departure => moment(departure.time?.scheduledDeparture))
            .map(time => time.diff(now, 'minutes'))
            .filter(time => time <= 60).length;

        if (countDeparturesUnder1H <= 2) {
            return 2;
        } else if (countDeparturesUnder1H >= 6) {
            return 6;
        } else {
            if (countDeparturesUnder1H % 2 == 0) {
                return countDeparturesUnder1H;
            } else {
                return countDeparturesUnder1H + 1;
            }
        }
    }

    hasLine(): boolean {
        return this.line != null;
    }

    hasDepartures(): boolean {
        return this.departures != null && this.departures.length > 0;
    }

    lessThan1Min(departure: Time): boolean {
        const departureDate = moment().startOf('day').add(departure?.scheduledDeparture, 'second');
        const duration = moment.duration(departureDate.diff(moment()));
        return duration.asSeconds() < 60;
    }

    showShape(shapeId: string) {
        this.onClick.emit(shapeId);
    }

    public getRouteByRouteId(routeId: string): Route {
        return this.routes.filter(route => route.id === routeId)[0];
    }

}
