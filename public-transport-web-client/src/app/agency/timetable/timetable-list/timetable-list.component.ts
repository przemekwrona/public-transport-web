import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {TimetableGeneratorFindAllResponse} from "../../../generated/public-transport-api";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-timetable-list',
    standalone: true,
    imports: [
        CommonModule
    ],
    templateUrl: './timetable-list.component.html',
    styleUrl: './timetable-list.component.scss'
})
export class TimetableListComponent implements OnInit {

    public response: TimetableGeneratorFindAllResponse;

    constructor(private route: ActivatedRoute) {
    }

    public hasTimetables(): boolean {
        return false;
    }

    ngOnInit(): void {
        this.response = this.route.snapshot.data["timetables"]
    }

}
