import {Component} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {BrigadeService, GetBrigadeResponse} from "../../../generated/public-transport-api";
import {size} from "lodash";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {MatDialog} from "@angular/material/dialog";
import {BrigadeCreatorModalComponent} from "../brigade-creator-modal/brigade-creator-modal.component";

@Component({
    selector: 'app-brigade-list',
    templateUrl: './brigade-list.component.html',
    styleUrl: './brigade-list.component.scss',
    standalone: false
})
export class BrigadeListComponent {

    public brigadesResponse: GetBrigadeResponse;

    constructor(private route: ActivatedRoute, private router: Router, private agencyStorageService: AgencyStorageService, private brigadeService: BrigadeService, private dialog: MatDialog) {
        this.brigadesResponse = this.route.snapshot.data['brigades'];
    }

    public createBrigade(): void {
        const dialogRef = this.dialog.open(BrigadeCreatorModalComponent);

        dialogRef.afterClosed().subscribe((results) => {
            this.router.navigate(['/agency/brigades/edit'], { queryParams: { name: '200' } }).then(() => {});
        });
    }

    public deleteBrigadeByBrigadeCode(brigadeCode: string): void {
        const agency = this.agencyStorageService.getInstance();

        this.brigadeService.deleteBrigadeByBrigadeCode(agency, brigadeCode).subscribe(() => {
            this.brigadeService.getBrigades(agency).subscribe(response => this.brigadesResponse = response);
        });
    }

    public hasBrigades(): boolean {
        return size(this.brigadesResponse.items) > 0;
    }

}
