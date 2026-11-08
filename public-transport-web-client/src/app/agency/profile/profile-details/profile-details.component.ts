import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {
    AgencyAddress,
    AgencyDetails,
    AgencyService
} from "../../../generated/public-transport-api";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {TranslocoPipe, TranslocoService} from "@jsverse/transloco";
import {ActivatedRoute, Router} from "@angular/router";
import {LoginService} from "../../../auth/login.service";
import {GoogleAnalyticsService} from "../../../google-analytics.service";
import {NgxMaskDirective, provideNgxMask} from "ngx-mask";
import {NotificationService} from "../../../shared/notification.service";
import {ImageCroppedEvent} from "ngx-image-cropper";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {ImageEvent, CropPhotoModalComponent} from "../../shared/crop-photo-modal/crop-photo-modal.component";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Camera} from "@primeicons/angular/camera";
import {ExternalLink} from "@primeicons/angular/external-link";
import {ButtonDirective} from "primeng/button";
import {Fluid} from "primeng/fluid";
import {InputGroup} from "primeng/inputgroup";
import {InputGroupAddon} from "primeng/inputgroupaddon";
import {InputText} from "primeng/inputtext";
import {Message} from "primeng/message";

@Component({
    selector: 'app-profile',
    templateUrl: './profile-details.component.html',
    styleUrl: './profile-details.component.scss',
    imports: [
        ReactiveFormsModule,
        TranslocoPipe,
        NgxMaskDirective,
        ButtonDirective,
        Fluid,
        InputGroup,
        InputGroupAddon,
        InputText,
        Message,
        Camera,
        ExternalLink
    ],
    changeDetection: ChangeDetectionStrategy.Eager,
    providers: [
        AgencyService,
        provideNgxMask()
    ]
})
export class ProfileDetailsComponent implements OnInit {

    public modelForm: FormGroup;

    public isSubmitted: boolean = false;
    public isAgencyDetailsSaving = false;
    public image: SafeUrl;

    constructor(private agencyService: AgencyService, private router: Router, private route: ActivatedRoute, private authService: LoginService, private agencyStorageService: AgencyStorageService, private googleAnalyticsService: GoogleAnalyticsService, private notificationService: NotificationService, private formBuilder: FormBuilder, private sanitizer: DomSanitizer, private dialog: MatDialog, private transloco: TranslocoService) {
        this.modelForm = this.formBuilder.group({
            agencyName: ['', [Validators.required, Validators.minLength(3)]],
            street: ['', Validators.required],
            houseNumber: ['', Validators.required],
            flatNumber: ['', Validators.maxLength(5)],
            postalCode: ['', Validators.required],
            postalCity: ['', Validators.required],
            agencyUrl: [''],
            agencyTimetableUrl: ['']
        });
    }

    ngOnInit(): void {
        this.route.data.subscribe(data => {
            const agencyDetails: AgencyDetails = data['agencyDetails'];
            this.modelForm.controls['agencyName'].setValue(agencyDetails.agencyName);
            this.modelForm.controls['street'].setValue(agencyDetails.agencyAddress.street);
            this.modelForm.controls['houseNumber'].setValue(agencyDetails.agencyAddress.houseNumber);
            this.modelForm.controls['flatNumber'].setValue(agencyDetails.agencyAddress.flatNumber);
            this.modelForm.controls['postalCode'].setValue(agencyDetails.agencyAddress.postalCode);
            this.modelForm.controls['postalCity'].setValue(agencyDetails.agencyAddress.postalCity);
            this.modelForm.controls['agencyUrl'].setValue(agencyDetails.agencyUrl);
            this.modelForm.controls['agencyTimetableUrl'].setValue(agencyDetails.agencyTimetableUrl);
        });

        this.agencyService.getAgencyPhoto(this.agencyStorageService.getInstance()).subscribe((photoResponse: Blob) => {
            if (photoResponse != null) {
                let objectURL: string = URL.createObjectURL(photoResponse);
                this.image = this.sanitizer.bypassSecurityTrustUrl(objectURL);
            }
        });
    }

    public saveAgencyDetails(modelForm: FormGroup) {
        this.isSubmitted = true;
        if (modelForm.valid) {
            if (this.isAgencyDetailsSaving === false) {
                this.isAgencyDetailsSaving = true;
                const agencyDetails: AgencyDetails = {} as AgencyDetails;
                agencyDetails.agencyName = this.modelForm.controls['agencyName'].value;

                agencyDetails.agencyAddress = {} as AgencyAddress;
                agencyDetails.agencyAddress.street = this.modelForm.controls['street'].value;
                agencyDetails.agencyAddress.houseNumber = this.modelForm.controls['houseNumber'].value;
                agencyDetails.agencyAddress.flatNumber = this.modelForm.controls['flatNumber'].value;
                agencyDetails.agencyAddress.postalCode = this.modelForm.controls['postalCode'].value;
                agencyDetails.agencyAddress.postalCity = this.modelForm.controls['postalCity'].value;

                agencyDetails.agencyUrl = this.modelForm.controls['agencyUrl'].value;
                agencyDetails.agencyTimetableUrl = this.modelForm.controls['agencyTimetableUrl'].value;

                this.agencyService.updateAgency(this.authService.getInstance(), agencyDetails).subscribe(() => {
                    this.notificationService.showSuccess(this.transloco.translate('agency.profile.saved'));
                }, error => {
                }, () => {
                    this.isAgencyDetailsSaving = false;
                });
            }
        } else {
            this.notificationService.showError(this.transloco.translate('agency.profile.invalid'));
        }
    }

    public invalid(controlName: string): boolean {
        return this.isSubmitted && this.modelForm.controls[controlName].invalid;
    }

    public hasError(controlName: string, errorName: string): boolean {
        return this.invalid(controlName) && !!this.modelForm.controls[controlName].errors?.[errorName];
    }

    public websiteHref(): string | null {
        const value = this.modelForm.controls['agencyUrl'].value;
        return value ? `https://www.${value}` : null;
    }

    public timetablePrefix(): string {
        const site = this.modelForm.controls['agencyUrl'].value;
        return site ? `https://${site}/` : 'https://';
    }

    public timetableHref(): string | null {
        const site = this.modelForm.controls['agencyUrl'].value;
        const path = this.modelForm.controls['agencyTimetableUrl'].value;
        if (!site || !path) {
            return null;
        }
        return `https://www.${site}/${path}`;
    }

    public openWebsite(): void {
        this.openExternal(this.websiteHref());
    }

    public openTimetable(): void {
        this.openExternal(this.timetableHref());
    }

    private openExternal(href: string | null): void {
        if (!href) {
            return;
        }
        window.open(href, '_blank', 'noopener,noreferrer');
    }

    fileChangeEvent(event: Event): void {
        const input = event.target as HTMLInputElement;
        let dialogRef: MatDialogRef<CropPhotoModalComponent> = this.dialog.open(CropPhotoModalComponent, {
            data: { event: event } as ImageEvent
        });

        dialogRef.afterClosed().subscribe((imageCroppedEvent: ImageCroppedEvent | undefined) => {
            input.value = '';
            if (!imageCroppedEvent?.blob) {
                return;
            }
            this.agencyService.putAgencyPhoto(this.agencyStorageService.getInstance(), imageCroppedEvent.blob).subscribe(() => {
                this.agencyService.getAgencyPhoto(this.agencyStorageService.getInstance()).subscribe(() => {
                    this.image = this.sanitizer.bypassSecurityTrustUrl(imageCroppedEvent.objectUrl);
                });
            });
        })
    }

}
