import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Data} from "@angular/router";
import {map} from "rxjs";
import {
    BrigadeTimetableVariant,
    RouteStopTimetable,
    RouteStopTimetableService,
    RouteStops,
    TripMode
} from "../../../../generated/public-transport-api";
import {buildStopSequence, StopSequenceItem} from "./stop-sequence";
import {LoginService} from "../../../../auth/login.service";
import {getRouteParam} from "../trip-list.resolver";
import {TimetableBoardComponent} from "../../../brigade/brigade-scheduler/brigade-timetable-modal/timetable-board/timetable-board.component";

@Component({
    selector: 'app-trip-list-timetable',
    templateUrl: './trip-list-timetable.component.html',
    standalone: true,
    imports: [TimetableBoardComponent]
})
export class TripListTimetableComponent implements OnInit {
    public frontStops: StopSequenceItem[] = [];
    public backStops: StopSequenceItem[] = [];
    public selectedStop: StopSequenceItem | null = null;
    public selectedDirection: string | null = null;
    public timetable: RouteStopTimetable | null = null;
    public loading = false;

    public get columns(): { title: string; tripMode: TripMode; stops: StopSequenceItem[] }[] {
        return [
            {title: 'TAM', tripMode: TripMode.Front, stops: this.frontStops},
            {title: 'POWRÓT', tripMode: TripMode.Back, stops: this.backStops}
        ];
    }

    public get timetableVariant(): BrigadeTimetableVariant {
        return {departures: this.timetable?.departures ?? []};
    }

    constructor(
        private route: ActivatedRoute,
        private loginService: LoginService,
        private routeStopTimetableService: RouteStopTimetableService
    ) {
    }

    ngOnInit(): void {
        this.route.data.pipe(map((data: Data) => data['response'] as RouteStops)).subscribe(response => {
            this.frontStops = buildStopSequence(response?.front);
            this.backStops = buildStopSequence(response?.back);
        });
    }

    public isSelected(stop: StopSequenceItem, direction: string): boolean {
        return this.selectedStop?.stopId === stop.stopId && this.selectedDirection === direction;
    }

    public loadStopTimetable(stop: StopSequenceItem, tripMode: TripMode, direction: string): void {
        this.selectedStop = stop;
        this.selectedDirection = direction;
        this.loading = true;
        this.timetable = null;

        this.routeStopTimetableService.getRouteStopTimetable(
            this.loginService.getInstance(),
            getRouteParam(this.route.snapshot, 'routeCode')!,
            String(stop.stopId),
            tripMode
        ).subscribe({
            next: response => {
                this.timetable = response;
                this.loading = false;
            },
            error: () => {
                this.timetable = {departures: []};
                this.loading = false;
            }
        });
    }
}
