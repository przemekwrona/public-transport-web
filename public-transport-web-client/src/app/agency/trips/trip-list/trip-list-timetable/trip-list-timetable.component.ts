import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Data} from "@angular/router";
import {map} from "rxjs";
import {MatTabsModule} from "@angular/material/tabs";
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

export interface TimetableDirectionTab {
    label: string;
    tripMode: TripMode;
}

@Component({
    selector: 'app-trip-list-timetable',
    templateUrl: './trip-list-timetable.component.html',
    standalone: true,
    imports: [TimetableBoardComponent, MatTabsModule]
})
export class TripListTimetableComponent implements OnInit {
    public readonly tabs: TimetableDirectionTab[] = [
        {label: 'FRONT', tripMode: TripMode.Front},
        {label: 'BACK', tripMode: TripMode.Back}
    ];
    public activeTab = this.tabs[0];
    public frontStops: StopSequenceItem[] = [];
    public backStops: StopSequenceItem[] = [];
    public selectedStop: StopSequenceItem | null = null;
    public selectedDirection: string | null = null;
    public timetable: RouteStopTimetable | null = null;
    public loading = false;

    public get activeStops(): StopSequenceItem[] {
        return this.activeTab.tripMode === TripMode.Front ? this.frontStops : this.backStops;
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

    public selectTab(tab: TimetableDirectionTab): void {
        if (this.activeTab.tripMode === tab.tripMode) {
            return;
        }
        this.activeTab = tab;
        this.selectedStop = null;
        this.selectedDirection = null;
        this.timetable = null;
        this.loading = false;
    }

    public isSelected(stop: StopSequenceItem): boolean {
        return this.selectedStop?.stopId === stop.stopId && this.selectedDirection === this.activeTab.label;
    }

    public loadStopTimetable(stop: StopSequenceItem): void {
        this.selectedStop = stop;
        this.selectedDirection = this.activeTab.label;
        this.loading = true;
        this.timetable = null;

        this.routeStopTimetableService.getRouteStopTimetable(
            this.loginService.getInstance(),
            getRouteParam(this.route.snapshot, 'routeCode')!,
            String(stop.stopId),
            this.activeTab.tripMode
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
