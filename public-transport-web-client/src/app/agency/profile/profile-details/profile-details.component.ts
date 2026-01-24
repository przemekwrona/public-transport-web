import {Component, OnInit, SecurityContext} from '@angular/core';
import {
    AgencyAddress,
    AgencyDetails,
    AgencyService
} from "../../../generated/public-transport-api";
import {faGlobe, faSpinner, fas, IconDefinition} from '@fortawesome/free-solid-svg-icons';
import {CommonModule} from "@angular/common";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {TranslocoPipe} from "@jsverse/transloco";
import {ActivatedRoute, Router} from "@angular/router";
import {LoginService} from "../../../auth/login.service";
import {GoogleAnalyticsService} from "../../../google-analytics.service";
import {NgxMaskDirective, provideNgxMask} from "ngx-mask";
import {NotificationService} from "../../../shared/notification.service";
import {ImageCroppedEvent, ImageCropperComponent, LoadedImage} from "ngx-image-cropper";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {ImageEvent, CropPhotoModalComponent} from "../../shared/crop-photo-modal/crop-photo-modal.component";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";

@Component({
    selector: 'app-profile',
    templateUrl: './profile-details.component.html',
    styleUrl: './profile-details.component.scss',
    imports: [
        CommonModule,
        FormsModule,
        FontAwesomeModule,
        TranslocoPipe,
        NgxMaskDirective,
        ReactiveFormsModule
    ],
    providers: [
        AgencyService,
        provideNgxMask()
    ]
})
export class ProfileDetailsComponent implements OnInit {

    public faGlobe: IconDefinition = faGlobe;
    public faSpinner: IconDefinition = faSpinner;

    public modelForm: FormGroup;

    public isSubmitted: boolean = false;
    public isAgencyDetailsSaving = false;
    public image: SafeUrl;

    constructor(private agencyService: AgencyService, private router: Router, private route: ActivatedRoute, private authService: LoginService, private agencyStorageService: AgencyStorageService, private googleAnalyticsService: GoogleAnalyticsService, private notificationService: NotificationService, private formBuilder: FormBuilder, private sanitizer: DomSanitizer, private dialog: MatDialog) {
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
                    this.notificationService.showSuccess('Dane zostały zaktualizowane');
                }, error => {
                }, () => {
                    this.isAgencyDetailsSaving = false;
                });
            }
        } else {
            this.notificationService.showError('Formularz zawiera błędy');
        }
    }

    public validControl(controlName: string): boolean {
        return !this.isSubmitted || this.modelForm.controls[controlName].valid;
    }

    fileChangeEvent(event: Event): void {
        let dialogRef: MatDialogRef<CropPhotoModalComponent> = this.dialog.open(CropPhotoModalComponent, {
            data: { event: event } as ImageEvent
        });

        dialogRef.afterClosed().subscribe((imageCroppedEvent: ImageCroppedEvent) => {
            this.agencyService.putAgencyPhoto(this.agencyStorageService.getInstance(), imageCroppedEvent.blob).subscribe(() => {
                this.agencyService.getAgencyPhoto(this.agencyStorageService.getInstance()).subscribe(photo => {
                    this.image = this.sanitizer.bypassSecurityTrustUrl(imageCroppedEvent.objectUrl);
                });
            });
        })
    }

    public hasImage(): boolean {
        return this.image !== null;
    }

}
