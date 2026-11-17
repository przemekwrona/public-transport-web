import {Component, DestroyRef, OnInit, ChangeDetectionStrategy, inject} from '@angular/core';
import {CommonModule} from "@angular/common";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {ActivatedRoute, Router, RouterModule} from "@angular/router";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {catchError, forkJoin, map, of} from "rxjs";
import {AgenciesAdminResponse, AgencyAdminDetail, AgencyService} from "../../../generated/public-transport-api";
import {LoginService} from "../../../auth/login.service";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

@Component({
    selector: 'app-profile-list',
    imports: [
        CommonModule,
        RouterModule
    ],
    templateUrl: './profile-list.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './profile-list.component.scss'
})
export class ProfileListComponent implements OnInit {

    private readonly defaultAgencyPhoto = 'assets/media/avatars/blank.png';
    private readonly destroyRef = inject(DestroyRef);
    private readonly agencyPhotos = new Map<string, SafeUrl>();
    private readonly objectUrls: string[] = [];

    public agenciesResponse: AgenciesAdminResponse = {agencies: []};

    constructor(private route: ActivatedRoute, private authService: LoginService, private agencyStorageService: AgencyStorageService, private router: Router, private agencyService: AgencyService, private sanitizer: DomSanitizer) {
        this.destroyRef.onDestroy(() => {
            for (const url of this.objectUrls) {
                URL.revokeObjectURL(url);
            }
        });
    }

    ngOnInit(): void {
        this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(data => {
            const agencies = data['agencies'] as AgenciesAdminResponse | undefined;
            if (!agencies) {
                return;
            }
            this.agenciesResponse = agencies;
            this.loadPhotos(agencies.agencies ?? []);
        });
    }

    public agencyPhoto(agencyCode: string | undefined): SafeUrl | string {
        if (!agencyCode) {
            return this.defaultAgencyPhoto;
        }
        return this.agencyPhotos.get(agencyCode) ?? this.defaultAgencyPhoto;
    }

    private loadPhotos(agencies: AgencyAdminDetail[]): void {
        const codes = agencies
            .map(agency => agency.agencyCode)
            .filter((code): code is string => !!code);
        if (codes.length === 0) {
            return;
        }

        forkJoin(codes.map(code => this.agencyService.getAgencyPhoto(code, 'response').pipe(
            map(response => ({code, response})),
            catchError(() => of(null))
        ))).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(results => {
            for (const result of results) {
                const body = result?.response.body;
                if (!result || result.response.status !== 200 || !body || body.size === 0) {
                    continue;
                }
                const url = URL.createObjectURL(body);
                this.objectUrls.push(url);
                this.agencyPhotos.set(result.code, this.sanitizer.bypassSecurityTrustUrl(url));
            }
        });
    }

    public showAgency(agency: AgencyAdminDetail): void {
        this.agencyStorageService.setInstance(agency.agencyCode);
        this.router.navigate(['/agency/profile']).then(()=>{});
    }

}
