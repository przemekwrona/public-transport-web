import {Component} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {BrigadeDeleteBody, BrigadeService, GetBrigadeResponse} from "../../../generated/public-transport-api";
import {size} from "lodash";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

@Component({
    selector: 'app-brigade-list',
    templateUrl: './brigade-list.component.html',
    styleUrl: './brigade-list.component.scss',
    standalone: false
})
export class BrigadeListComponent {

    public brigadesResponse: GetBrigadeResponse;

    constructor(private route: ActivatedRoute, private agencyStorageService: AgencyStorageService, private brigadeService: BrigadeService) {
        this.brigadesResponse = this.route.snapshot.data['brigades'];
    }

    public deleteBrigadeByName(brigadeName: string): void {
        const agency = this.agencyStorageService.getInstance();
        const brigadeDeleteBody = {} as BrigadeDeleteBody;
        brigadeDeleteBody.brigadeName = brigadeName;

        this.brigadeService.deleteBrigade(agency, brigadeDeleteBody).subscribe(() => {
            this.brigadeService.getBrigades(agency).subscribe(response => this.brigadesResponse = response);
        });
    }

    public hasBrigades(): boolean {
        return size(this.brigadesResponse.brigades) > 0;
    }

}
