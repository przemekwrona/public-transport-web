import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {GetCalendarsResponse} from "../../../generated/public-transport";
import {includes} from "lodash";

@Component({
    selector: 'app-calendars',
    templateUrl: './calendar-list.component.html',
    styleUrl: './calendar-list.component.scss'
})
export class CalendarListComponent implements OnInit {

    public calendarsResponse: GetCalendarsResponse;

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.calendarsResponse = this.route.snapshot.data['calendars'];
    }

    protected readonly includes = includes;
}
