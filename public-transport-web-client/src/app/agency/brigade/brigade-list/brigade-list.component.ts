import {Component} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {BrigadeService, CreateBrigadeBody, CreateBrigadeResponse, GetBrigadeResponse} from "../../../generated/public-transport-api";
import {size} from "lodash";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {MatDialog} from "@angular/material/dialog";
import {BrigadeCreatorModalComponent} from "../brigade-creator-modal/brigade-creator-modal.component";
import {NgxSpinnerService} from "ngx-spinner";
import {NotificationService} from "../../../shared/notification.service";
import {map, switchMap, timer} from "rxjs";

@Component({
    selector: 'app-brigade-list',
    templateUrl: './brigade-list.component.html',
    styleUrl: './brigade-list.component.scss',
    standalone: false
})
export class BrigadeListComponent {

    public brigadesResponse: GetBrigadeResponse;
    public creatingMessage = '';

    private readonly spinnerName = 'brigade-creator';
    private readonly minimumEffectMs = 3000;

    constructor(private route: ActivatedRoute, private router: Router, private agencyStorageService: AgencyStorageService, private brigadeService: BrigadeService, private dialog: MatDialog, private spinner: NgxSpinnerService, private notificationService: NotificationService) {
        this.brigadesResponse = this.route.snapshot.data['brigades'];
    }

    public createBrigade(): void {
        const dialogRef = this.dialog.open(BrigadeCreatorModalComponent);

        dialogRef.afterClosed().subscribe((brigadeBody: CreateBrigadeBody) => {
            if (!brigadeBody?.brigadeName) {
                return;
            }
            this.showCreatingBrigade(brigadeBody);
        });
    }

    private showCreatingBrigade(brigadeBody: CreateBrigadeBody): void {
        const startedAt = Date.now();
        this.showSpinner();

        this.brigadeService.createBrigade(this.agencyStorageService.getInstance(), brigadeBody).pipe(
            switchMap((response: CreateBrigadeResponse) => {
                const remaining = Math.max(0, this.minimumEffectMs - (Date.now() - startedAt));
                return timer(remaining).pipe(map(() => response));
            })
        ).subscribe({
            next: (response: CreateBrigadeResponse) => this.openCreatedBrigade(response.brigadeCode),
            error: () => this.failCreate()
        });
    }

    private openCreatedBrigade(brigadeCode?: string): void {
        if (!brigadeCode) {
            this.failCreate();
            return;
        }

        this.router.navigate(['/agency/brigades', brigadeCode, 'edit']).then(navigated => {
            if (!navigated) {
                this.failCreate();
            }
        }).catch(() => this.failCreate());
    }

    private failCreate(): void {
        this.hideSpinner();
        this.notificationService.showError('Nie udało się utworzyć brygady');
    }

    private showSpinner(): void {
        this.creatingMessage = 'Tworzenie brygady...';
        this.spinner.show(this.spinnerName, {
            type: 'ball-scale-multiple',
            size: 'large',
            bdColor: 'rgba(51,51,51,0.8)',
            color: '#fff',
            fullScreen: true
        });
    }

    private hideSpinner(): void {
        this.spinner.hide(this.spinnerName);
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
