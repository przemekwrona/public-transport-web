import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {GetCalendarsResponse} from "../../../generated/public-transport";
import {includes} from "lodash";

@Component({
    selector: 'app-calendars',
    templateUrl: './calendars.component.html',
    styleUrl: './calendars.component.scss'
})
export class CalendarsComponent implements OnInit {

    public calendarsResponse: GetCalendarsResponse;

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.calendarsResponse = this.route.snapshot.data['calendars'];
    }

    protected readonly includes = includes;
}
