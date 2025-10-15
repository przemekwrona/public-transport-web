import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {ActivatedRoute, Router, RouterModule} from "@angular/router";
import {AgenciesAdminResponse, AgencyAdminDetail} from "../../../generated/public-transport-api";
import {LoginService} from "../../../auth/login.service";

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

    constructor(private route: ActivatedRoute, private authService: LoginService, private router: Router) {
    }

    ngOnInit(): void {
        this.route.data.subscribe(data => {
            this.agenciesResponse = data['agencies'];
        });
    }

    public showAgency(agency: AgencyAdminDetail): void {
        this.authService.setInstance(agency.agencyCode);
        this.router.navigate(['/agency/profile']).then(()=>{});
    }

}
