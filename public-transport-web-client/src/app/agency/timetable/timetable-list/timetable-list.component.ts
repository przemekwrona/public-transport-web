import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterModule} from "@angular/router";
import {
    TimetableGeneratorFindAllItem,
    TimetableGeneratorFindAllResponse
} from "../../../generated/public-transport-api";
import {CommonModule} from "@angular/common";
import {size} from "lodash";

@Component({
    selector: 'app-timetable-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule
    ],
    templateUrl: './timetable-list.component.html',
    styleUrl: './timetable-list.component.scss'
})
export class TimetableListComponent implements OnInit {

    public response: TimetableGeneratorFindAllResponse;

    constructor(private route: ActivatedRoute) {
    }

    public hasTimetables(): boolean {
        return size(this.response.items) > 0;
    }

    ngOnInit(): void {
        this.response = this.route.snapshot.data["timetables"]
    }

    public openTimetableGenerator(timetableGenerator: TimetableGeneratorFindAllItem): void {

    }

}
