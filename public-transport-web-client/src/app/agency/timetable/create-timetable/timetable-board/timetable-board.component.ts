import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

export interface Departure {
    hour: number;
    minutes: number | null;
    symbol: string;
}

@Component({
    selector: 'app-timetable-board',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule
    ],
    templateUrl: './timetable-board.component.html',
    styleUrl: './timetable-board.component.scss'
})
export class TimetableBoardComponent implements OnInit {

    public hours: number[] = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23]

    public departures: Map<number, Departure[]> = new Map<number, Departure[]>();

    ngOnInit(): void {
        for (const hour of this.hours) {
            const emptyDeparture: Departure = {hour: hour, minutes: null, symbol: ""} as Departure;

            const emptyList: Departure[] = [];
            emptyList.push(emptyDeparture);

            this.departures.set(hour, emptyList);
        }
    }

    public addEmptyDepartureInHour(hour: number) {
        const hasEmptyDeparture: boolean = this.departures?.get(hour)
            .map(departure => departure.minutes)
            .filter(minute => minute == null).length > 0;

        if (!hasEmptyDeparture) {
            const emptyDeparture: Departure = {} as Departure;
            emptyDeparture.hour = hour;
            emptyDeparture.symbol = "";
            this.departures.get(hour).push(emptyDeparture);
        }

        const arr: Departure[] = this.departures.get(hour) ?? [];
        const sortedArr = [...arr].sort((a, b) => a.minutes - b.minutes);

        this.departures.set(hour, sortedArr);
    }

}
