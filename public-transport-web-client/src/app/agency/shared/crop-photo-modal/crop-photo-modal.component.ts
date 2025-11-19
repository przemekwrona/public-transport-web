import {Component, inject, OnInit, Sanitizer} from '@angular/core';
import {CommonModule} from "@angular/common";
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from "@angular/material/dialog";
import {ImageCroppedEvent, ImageCropperComponent, LoadedImage} from "ngx-image-cropper";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";

export interface ImageEvent {
    event: Event;
}

@Component({
    selector: 'app-crop-photo-modal',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        ImageCropperComponent
    ],
    templateUrl: './crop-photo-modal.component.html',
    styleUrl: './crop-photo-modal.component.scss'
})
export class CropPhotoModalComponent implements OnInit {

    private readonly _1MB = 1024 * 1024;
    private readonly _2MB = 2 * 1024 * 1024;

    readonly dialogRef = inject(MatDialogRef<CropPhotoModalComponent>);
    readonly data: ImageEvent = inject<ImageEvent>(MAT_DIALOG_DATA);

    public imageChangedEvent: ImageCroppedEvent;
    public croppedImage: SafeUrl;

    constructor(private sanitizer: DomSanitizer) {
    }

    ngOnInit(): void {
    }

    forceCloseModal() {
        this.dialogRef.close(null);
    }

    closeModal(): void {
        if (this.getFileSize() <= this._2MB) {
            this.dialogRef.close(this.imageChangedEvent);
        }
    }

    imageCropped(event: ImageCroppedEvent) {
        this.imageChangedEvent = event;
        this.croppedImage = this.sanitizer.bypassSecurityTrustUrl(event.objectUrl);
    }

    imageLoaded(image: LoadedImage) {
        // show cropper
    }

    cropperReady() {
        // cropper ready
    }

    loadImageFailed() {
        // show message
    }

    public getFileSize(): number {
        return (this.imageChangedEvent?.blob?.size || 0) / this._1MB;
    }

    public hasSizeGtOrEqThan2MB(): boolean {
        return (this.imageChangedEvent?.blob?.size || 0) >= this._2MB;
    }

}
