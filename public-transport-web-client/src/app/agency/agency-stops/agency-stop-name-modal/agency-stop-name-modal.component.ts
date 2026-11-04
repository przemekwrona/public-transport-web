import {AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, Inject, OnDestroy, ViewChild} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import * as L from 'leaflet';
import {Map} from 'leaflet';
import {AgencyStopNameRequest, AgencyStopsService, ErrorResponse} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';
import {NotificationService} from '../../../shared/notification.service';

export interface AgencyStopNameDialogData {
    stopId: number;
    name: string;
    lat?: number;
    lon?: number;
}

export interface AgencyStopNameDialogResult {
    stopId: number;
    name: string;
}

function agencyStopName(control: AbstractControl): ValidationErrors | null {
    const value = String(control.value ?? '').trim();
    if (!value) {
        return {required: true};
    }
    if (value.length > 150) {
        return {maxlength: true};
    }
    return null;
}

@Component({
    selector: 'app-agency-stop-name-modal',
    imports: [MatDialogModule, ReactiveFormsModule, TranslocoPipe],
    templateUrl: './agency-stop-name-modal.component.html',
    styleUrl: './agency-stop-name-modal.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager
})
export class AgencyStopNameModalComponent implements AfterViewInit, OnDestroy {

    @ViewChild('mapHost') mapHost?: ElementRef<HTMLDivElement>;

    public saving = false;
    public readonly form;

    private map?: Map;
    private readonly stopIcon = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    });

    constructor(
        private dialogRef: MatDialogRef<AgencyStopNameModalComponent, AgencyStopNameDialogResult>,
        @Inject(MAT_DIALOG_DATA) public data: AgencyStopNameDialogData,
        formBuilder: FormBuilder,
        private agencyStopsService: AgencyStopsService,
        private agencyStorageService: AgencyStorageService,
        private notificationService: NotificationService,
        private transloco: TranslocoService
    ) {
        this.form = formBuilder.group({
            name: [data.name ?? '', agencyStopName]
        });
    }

    ngAfterViewInit(): void {
        const host = this.mapHost?.nativeElement;
        if (!host) {
            return;
        }
        this.map = this.createMap(host);
        const refresh = () => this.map?.invalidateSize();
        setTimeout(refresh);
        this.dialogRef.afterOpened().subscribe(() => setTimeout(refresh));
    }

    ngOnDestroy(): void {
        this.map?.remove();
    }

    public close(): void {
        this.dialogRef.close();
    }

    public save(): void {
        this.form.markAllAsTouched();
        if (this.form.invalid || this.saving) {
            return;
        }
        const name = String(this.form.controls.name.value ?? '').trim();
        const request = {name} as AgencyStopNameRequest;
        this.saving = true;
        this.agencyStopsService.updateAgencyStopName(this.agencyStorageService.getInstance(), this.data.stopId, request).subscribe({
            next: () => {
                this.notificationService.showSuccess(this.transloco.translate('agency.stops.used.edit.saved'));
                this.dialogRef.close({stopId: this.data.stopId, name});
            },
            error: (response: HttpErrorResponse) => {
                this.saving = false;
                const payload = response.error as ErrorResponse | undefined;
                this.notificationService.showError(payload?.message || this.transloco.translate('agency.stops.used.edit.failed'));
            }
        });
    }

    private createMap(host: HTMLDivElement): Map {
        const lat = this.data.lat;
        const lon = this.data.lon;
        const hasPoint = lat != null && lon != null;
        const map = L.map(host, {
            center: hasPoint ? [lat, lon] : [52.2297, 21.0122],
            zoom: hasPoint ? 16 : 6,
            zoomControl: false
        });

        L.control.zoom({position: 'bottomright'}).addTo(map);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            minZoom: 3,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        if (hasPoint) {
            L.marker([lat, lon], {icon: this.stopIcon, title: this.data.name}).addTo(map);
        }
        return map;
    }
}
