import {Component, OnInit} from '@angular/core';
import {DatePipe} from "@angular/common";
import {ActivatedRoute} from "@angular/router";
import {AppUsersResponse} from "../../../generated/public-transport";

@Component({
    selector: 'app-user-list',
    standalone: true,
    imports: [
        DatePipe
    ],
    templateUrl: './user-list.component.html',
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
