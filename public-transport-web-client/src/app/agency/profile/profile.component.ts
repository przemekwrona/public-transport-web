import {Component, OnInit} from '@angular/core';
import {ProfileService} from "./profile.service";
import {AgencyDetails} from "../../generated/public-transport";

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {

    public agencyDetails: AgencyDetails = {};

    constructor(private profileService: ProfileService) {
    }

    ngOnInit(): void {
        this.profileService.getAgencyDetails().subscribe((response: AgencyDetails) => {
            this.agencyDetails = response;
        })
    }

    public saveAgencyDetails() {
        this.profileService.saveAgencyDetails(this.agencyDetails).subscribe(() => {});
    }

}
