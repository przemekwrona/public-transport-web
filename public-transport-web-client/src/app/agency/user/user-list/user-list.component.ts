import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {DatePipe} from "@angular/common";
import {ActivatedRoute, RouterModule} from "@angular/router";
import {AppUsersResponse} from "../../../generated/public-transport-api";

@Component({
    selector: 'app-user-list',
    imports: [
        DatePipe,
        RouterModule
    ],
    templateUrl: './user-list.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './user-list.component.scss'
})
export class UserListComponent implements OnInit {

    public appUsers: AppUsersResponse = {} as AppUsersResponse;

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.data.subscribe(data => {
            this.appUsers = data['users'];
        });
    }

}
