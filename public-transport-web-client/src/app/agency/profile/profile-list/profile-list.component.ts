import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {ActivatedRoute, RouterModule} from "@angular/router";
import {AgenciesAdminResponse} from "../../../generated/public-transport";

@Component({
    selector: 'app-profile-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule
    ],
    templateUrl: './profile-list.component.html',
    styleUrl: './profile-list.component.scss'
})
export class ProfileListComponent implements OnInit {

    public agenciesResponse: AgenciesAdminResponse = {} as AgenciesAdminResponse;

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.route.data.subscribe(data => {
            this.agenciesResponse = data['agencies'];
        });
    }

}
