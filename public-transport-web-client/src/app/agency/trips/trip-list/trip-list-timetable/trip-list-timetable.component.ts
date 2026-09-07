import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Data} from "@angular/router";
import {map} from "rxjs";
import {RouteStops} from "../../../../generated/public-transport-api";
import {buildStopSequence, StopSequenceItem} from "./stop-sequence";

@Component({
    selector: 'app-trip-list-timetable',
    templateUrl: './trip-list-timetable.component.html',
    standalone: true
})
export class TripListTimetableComponent implements OnInit {
    public frontStops: StopSequenceItem[] = [];
    public backStops: StopSequenceItem[] = [];
    public selectedStop: StopSequenceItem | null = null;
    public selectedDirection: string | null = null;

    public get columns(): { title: string; stops: StopSequenceItem[] }[] {
        return [
            {title: 'TAM', stops: this.frontStops},
            {title: 'POWRÓT', stops: this.backStops}
        ];
    }

    constructor(private route: ActivatedRoute) {
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

    public loadStopTimetable(stop: StopSequenceItem, direction: string): void {
        this.selectedStop = stop;
        this.selectedDirection = direction;
    }
}
